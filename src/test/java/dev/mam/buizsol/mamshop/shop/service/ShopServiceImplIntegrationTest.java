package dev.mam.buizsol.mamshop.shop.service;

import dev.mam.buizsol.mamshop.billing.model.Invoice;
import dev.mam.buizsol.mamshop.contract.exception.BrandMismatchException;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import dev.mam.buizsol.mamshop.contract.model.ContractStatus;
import dev.mam.buizsol.mamshop.customer.exception.CustomerNotActiveException;
import dev.mam.buizsol.mamshop.customer.exception.CustomerNotFoundException;
import dev.mam.buizsol.mamshop.customer.exception.CustomerValidationException;
import dev.mam.buizsol.mamshop.customer.model.Address;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.customer.model.CommunicationDetails;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.customer.model.CustomerStatus;
import dev.mam.buizsol.mamshop.product.exception.ProductNotFoundException;
import dev.mam.buizsol.mamshop.product.model.PremiumMailProduct;
import dev.mam.buizsol.mamshop.product.model.Product;
import dev.mam.buizsol.mamshop.product.model.StandardMailProduct;
import dev.mam.buizsol.mamshop.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@DisplayName("ShopServiceImpl Integration Test")
class ShopServiceImplIntegrationTest {

    @Autowired
    private ShopService shopService;

    @Autowired
    private ProductService productService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        initializeProductIfNotExists("GMX Basic Mail", Brand.GMX, new BigDecimal("0.50"));
        initializeProductIfNotExists("MAIL_COM Basic Mail", Brand.MAIL_COM, new BigDecimal("0.50"));
        initializeProductIfNotExists("WEB_DE Basic Mail", Brand.WEB_DE, new BigDecimal("0.50"));
    }

    private void initializeProductIfNotExists(final String name, final Brand brand, final BigDecimal price) {
        if (shopService.loadAllProductsForBrand(brand).stream()
                .noneMatch(p -> p.getName().equals(name))) {
            productService.createProduct(createDefaultStandardMailProduct(name, brand, price));
        }
    }

    private Product createDefaultStandardMailProduct(final String name, final Brand brand, final BigDecimal price) {
        return StandardMailProduct.builder()
                .id(null)
                .name(name)
                .brand(brand)
                .setupFee(new BigDecimal("4.99"))
                .monthlyFee(price)
                .storageSize(4L)
                .build();
    }

    private Product createDefaultPremiumMailProduct(final String name, final Brand brand, final BigDecimal price) {
        return PremiumMailProduct.builder()
                .id(null)
                .name(name)
                .brand(brand)
                .setupFee(new BigDecimal("9.99"))
                .monthlyFee(price)
                .storageSize(8L)
                .build();
    }

    private Address createDefaultAddress(
            final String street, final String houseNumber, final String zip, final String city, final String country) {
        return new Address(street, houseNumber, zip, city, country);
    }

    private CommunicationDetails createDefaultCommunicationDetails(final String email, final String phoneNumber) {
        return new CommunicationDetails(email, phoneNumber);
    }

    private Customer createDefaultTestCustomer(
            final String firstName,
            final String lastName,
            final LocalDate birthDate,
            final Brand brand,
            final Address address,
            final Address invoiceAddress,
            final CommunicationDetails communicationDetails) {
        return Customer.create(firstName, lastName, birthDate, address, invoiceAddress, communicationDetails, brand);
    }

    @ParameterizedTest
    @EnumSource(Brand.class)
    @DisplayName("Register Customer: Successfully creates and stores a customer (Parameterized)")
    void shouldRegisterCustomerWhenValidDetailsProvided(Brand brand) {
        Customer customer = createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                brand,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789"));
        Customer registered = shopService.registerCustomer(customer);

        assertNotNull(registered.getId());
        assertEquals(customer.getFirstName(), registered.getFirstName());
        assertEquals(brand, registered.getBrand());
    }

    @Test
    @DisplayName("Register Customer: Fails when input is null (Negative)")
    void shouldThrowExceptionWhenRegisteringNullCustomer() {
        assertThrows(CustomerValidationException.class, () -> shopService.registerCustomer(null));
    }

    @ParameterizedTest
    @EnumSource(Brand.class)
    @DisplayName("Load Customer: Successfully retrieves existing customer by ID (Parameterized)")
    void shouldLoadCustomerWhenIdExists(Brand brand) throws CustomerNotFoundException {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                brand,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));
        Customer loaded = shopService.loadCustomer(customer.getId());

        assertEquals(customer.getId(), loaded.getId());
        assertEquals(brand, loaded.getBrand());
    }

    @Test
    @DisplayName("Load Customer: Throws exception for non-existent ID (Negative)")
    void shouldThrowExceptionWhenLoadingNonExistentCustomer() {
        UUID nonExistentId = UUID.randomUUID();
        assertThrows(CustomerNotFoundException.class, () -> shopService.loadCustomer(nonExistentId));
    }

    @ParameterizedTest
    @EnumSource(Brand.class)
    @DisplayName("Activate Customer: Changes status to ACTIVE (Parameterized)")
    void shouldActivateCustomerWhenIdExists(Brand brand) throws CustomerNotFoundException {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                brand,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));
        shopService.activateCustomer(customer.getId());

        Customer activated = shopService.loadCustomer(customer.getId());
        assertEquals(CustomerStatus.ACTIVE, activated.getStatus());
    }

    @ParameterizedTest
    @EnumSource(Brand.class)
    @DisplayName("Deactivate Customer: Changes status to INACTIVE (Parameterized)")
    void shouldDeactivateCustomerWhenIdExists(Brand brand) throws CustomerNotFoundException {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                brand,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));
        shopService.activateCustomer(customer.getId());
        shopService.deactivateCustomer(customer.getId());
        UUID customerId = customer.getId();
        Customer deactivated = shopService.loadCustomer(customerId);
        assertEquals(CustomerStatus.INACTIVE, deactivated.getStatus());
    }

    @ParameterizedTest
    @EnumSource(Brand.class)
    @DisplayName("Deactivate Customer: Succeeds for INACTIVE customer (Idempotency)")
    void shouldBeIdempotentWhenDeactivatingCustomer(Brand brand) throws CustomerNotFoundException {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                brand,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));

        UUID customerId = customer.getId();

        assertDoesNotThrow(() -> shopService.deactivateCustomer(customerId));
        Customer deactivated = shopService.loadCustomer(customerId);
        assertEquals(CustomerStatus.INACTIVE, deactivated.getStatus());
    }

    @ParameterizedTest
    @EnumSource(Brand.class)
    @DisplayName("Remove Customer: Successfully deletes customer from system (Parameterized)")
    void shouldRemoveCustomerWhenIdExists(Brand brand) throws CustomerNotFoundException {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                brand,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));
        UUID customerId = customer.getId();
        shopService.removeCustomer(customerId);

        assertThrows(CustomerNotFoundException.class, () -> shopService.loadCustomer(customerId));
    }

    @Test
    @DisplayName("Remove Customer: Fails when customer has active contracts (Scenario 4)")
    void shouldThrowExceptionWhenRemovingCustomerWithActiveContracts() throws Exception {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                Brand.GMX,
                createDefaultAddress("Street", "1", "12345", "City", "Country"),
                null,
                createDefaultCommunicationDetails("test@gmx.de", "+49123456")));
        shopService.activateCustomer(customer.getId());

        Product product = createDefaultStandardMailProduct("TestProd", Brand.GMX, BigDecimal.TEN);
        productService.createProduct(product);
        Contract contract = shopService.purchaseProduct(customer.getId(), product.getId());
        shopService.activateContract(customer.getId(), contract.getId());

        UUID customerId = customer.getId();
        assertThrows(CustomerValidationException.class, () -> shopService.removeCustomer(customerId));
    }

    @ParameterizedTest
    @EnumSource(Brand.class)
    @DisplayName("Update Address: Successfully updates address for ACTIVE customer (Parameterized)")
    void shouldUpdateAddressWhenCustomerIsActive(Brand brand)
            throws CustomerNotFoundException, CustomerNotActiveException {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                brand,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));
        shopService.activateCustomer(customer.getId());

        Address newAddress = createDefaultAddress("Street_", "2", "12345", "City_", "Country_");
        Customer updated = shopService.updateAddress(customer.getId(), newAddress);

        assertEquals(newAddress.getStreet(), updated.getAddress().getStreet());
        assertEquals(newAddress.getCity(), updated.getAddress().getCity());
    }

    @Test
    @DisplayName("Update Address: Fails for INACTIVE customer (Boundary/State)")
    void shouldThrowExceptionWhenUpdatingAddressOfInactiveCustomer() {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                Brand.WEB_DE,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));

        Address newAddress = createDefaultAddress("Street_", "2", "12345", "City_", "Country_");

        UUID customerId = customer.getId();

        assertThrows(CustomerNotActiveException.class, () -> shopService.updateAddress(customerId, newAddress));
    }

    @ParameterizedTest
    @EnumSource(Brand.class)
    @DisplayName("Load All Products: Returns products for specified brand (Parameterized)")
    void shouldLoadAllProductsWhenBrandIsSpecified(Brand brand) {
        List<Product> products = shopService.loadAllProductsForBrand(brand);
        assertNotNull(products);
        assertTrue(products.stream().allMatch(p -> p.getBrand() == brand));
    }

    @ParameterizedTest
    @EnumSource(Brand.class)
    @DisplayName("Purchase Product: Successfully creates contract for matching brand and active customer"
            + " (Parameterized)")
    void shouldPurchaseProductWhenCustomerAndProductMatchBrand(Brand brand) {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                brand,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));
        shopService.activateCustomer(customer.getId());

        Product product = createDefaultStandardMailProduct("Standard Mail", brand, new BigDecimal("2.50"));
        productService.createProduct(product);

        Contract contract = shopService.purchaseProduct(customer.getId(), product.getId());
        assertNotNull(contract);
        assertEquals(customer.getId(), contract.getCustomer().getId());
        assertEquals(product.getId(), contract.getProductId());
    }

    @Test
    @DisplayName("Purchase Product: Prevents duplicate contracts for same product (Scenario 6)")
    void shouldNotCreateDuplicateContractForSameProduct() throws Exception {
        Brand brand = Brand.GMX;
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                brand,
                createDefaultAddress("Street", "1", "12345", "City", "Country"),
                null,
                createDefaultCommunicationDetails("test@gmx.de", "+49123456")));
        shopService.activateCustomer(customer.getId());

        Product product = createDefaultStandardMailProduct("Standard Mail", brand, new BigDecimal("2.50"));
        productService.createProduct(product);

        Contract contract1 = shopService.purchaseProduct(customer.getId(), product.getId());
        Contract contract2 = shopService.purchaseProduct(customer.getId(), product.getId());

        assertEquals(contract1.getId(), contract2.getId(), "Should return same contract for duplicate purchase");
        assertEquals(1, shopService.loadAllContracts(customer.getId()).size(), "Should have only one contract");
    }

    @ParameterizedTest
    @EnumSource(Brand.class)
    @DisplayName("Purchase Product Premium: Successfully creates contract (Parameterized)")
    void shouldPurchasePremiumProductWhenCustomerAndProductMatchBrand(Brand brand) throws Exception {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                brand,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));
        shopService.activateCustomer(customer.getId());

        Product product = createDefaultPremiumMailProduct("Premium Mail", brand, new BigDecimal("2.50"));
        productService.createProduct(product);

        Contract contract = shopService.purchaseProduct(customer.getId(), product.getId());
        assertNotNull(contract);
        assertEquals(customer.getId(), contract.getCustomer().getId());
        assertEquals(product.getId(), contract.getProductId());
    }

    @ParameterizedTest
    @CsvSource({"GMX, WEB_DE", "WEB_DE, MAIL_COM", "MAIL_COM, GMX"})
    @DisplayName("Purchase Product: Throws exception when brands do not match (Negative)")
    void shouldThrowExceptionWhenPurchaseBrandsDoNotMatch(Brand customerBrand, Brand productBrand) throws Exception {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                customerBrand,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));
        shopService.activateCustomer(customer.getId());

        Product product = createDefaultStandardMailProduct("Mismatched Mail", productBrand, new BigDecimal("2.50"));
        productService.createProduct(product);

        UUID customerId = customer.getId();
        UUID productId = product.getId();

        assertThrows(BrandMismatchException.class, () -> shopService.purchaseProduct(customerId, productId));
    }

    @ParameterizedTest
    @EnumSource(Brand.class)
    @DisplayName("Purchase Product: Fails for non-existent product ID (Negative)")
    void shouldThrowExceptionWhenPurchasingNonExistentProduct(Brand brand) throws Exception {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                brand,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));
        shopService.activateCustomer(customer.getId());

        UUID randomUUIDid = UUID.randomUUID();
        UUID customerId = customer.getId();

        assertThrows(ProductNotFoundException.class, () -> shopService.purchaseProduct(customerId, randomUUIDid));
    }

    @ParameterizedTest
    @EnumSource(Brand.class)
    @DisplayName("Purchase Standard Mail: Fails when customer is not active (Boundary)")
    void shouldThrowExceptionWhenPurchasingProductForInactiveCustomer(Brand brand) {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                brand,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));
        Product product = createDefaultStandardMailProduct("Standard Mail", brand, new BigDecimal("2.50"));
        productService.createProduct(product);

        UUID customerId = customer.getId();
        UUID productId = product.getId();
        assertThrows(CustomerNotActiveException.class, () -> shopService.purchaseProduct(customerId, productId));
    }

    @ParameterizedTest
    @EnumSource(Brand.class)
    @DisplayName("Purchase Premium Mail: Fails when customer is not active (Boundary)")
    void shouldThrowExceptionWhenPurchasingPremiumProductForInactiveCustomer(Brand brand) {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                brand,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));
        Product product = createDefaultPremiumMailProduct("Premium Mail", brand, new BigDecimal("2.50"));
        productService.createProduct(product);

        UUID customerId = customer.getId();
        UUID productId = product.getId();

        assertThrows(CustomerNotActiveException.class, () -> shopService.purchaseProduct(customerId, productId));
    }

    @ParameterizedTest
    @EnumSource(Brand.class)
    @DisplayName("Generate Invoice: Returns valid invoice for active customer (Parameterized)")
    void shouldGenerateInvoiceWhenCustomerIsActiveAndHasContracts(Brand brand) {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                brand,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));
        shopService.activateCustomer(customer.getId());

        Product product = createDefaultStandardMailProduct("Mail", brand, new BigDecimal("2.50"));
        productService.createProduct(product);
        Contract contract = shopService.purchaseProduct(customer.getId(), product.getId());
        shopService.activateContract(customer.getId(), contract.getId());

        Invoice invoice = shopService.generateInvoice(customer.getId());
        assertNotNull(invoice);
        assertEquals(customer.getId(), invoice.getCustomer().getId());
        assertFalse(invoice.getItems().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(Brand.class)
    @DisplayName("Load All Contracts: Retrieves list for active customer (Parameterized)")
    void shouldLoadAllContractsWhenCustomerIsActive(Brand brand) throws Exception {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                brand,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));
        shopService.activateCustomer(customer.getId());

        Product product = createDefaultStandardMailProduct("Mail", brand, new BigDecimal("2.50"));
        productService.createProduct(product);
        shopService.purchaseProduct(customer.getId(), product.getId());
        List<Contract> contracts = shopService.loadAllContracts(customer.getId());
        assertFalse(contracts.isEmpty());
        assertEquals(customer.getId(), contracts.get(0).getCustomer().getId());
    }

    @Test
    @DisplayName("Activate Contract: Updates contract status successfully")
    void shouldActivateContractWhenIdExists() throws Exception {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                Brand.GMX,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));
        shopService.activateCustomer(customer.getId());

        Product product = createDefaultPremiumMailProduct("Premium Mail", Brand.GMX, new BigDecimal("2.50"));
        productService.createProduct(product);
        Contract contract = shopService.purchaseProduct(customer.getId(), product.getId());
        shopService.activateContract(customer.getId(), contract.getId());

        List<Contract> contracts = shopService.loadAllContracts(customer.getId());
        Contract updatedContract = contracts.stream()
                .filter(c -> c.getId().equals(contract.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(ContractStatus.ACTIVE, updatedContract.getStatus());
    }

    @Test
    @DisplayName("Multi-Update: Customer can update multiple fields sequentially if active")
    void shouldAllowSequentialUpdatesWhenCustomerIsActive() throws Exception {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                Brand.GMX,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));
        shopService.activateCustomer(customer.getId());

        Address newInv = createDefaultAddress("Street_", "1", "12345", "City_", "Country_");
        Customer updated = shopService.updateInvoiceAddress(customer.getId(), newInv);

        assertEquals(newInv.getStreet(), updated.getInvoiceAddress().getStreet());
    }

    @Test
    @DisplayName("Update Communication Details: Successfully updates for ACTIVE customer")
    void shouldUpdateCommunicationDetailsWhenCustomerIsActive() throws Exception {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                Brand.GMX,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));
        shopService.activateCustomer(customer.getId());

        CommunicationDetails newDetails = createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789");
        Customer updated = shopService.updateCommunicationDetails(customer.getId(), newDetails);

        assertEquals(newDetails.getEmail(), updated.getCommunicationDetails().getEmail());
    }

    @Test
    @DisplayName("Negative - Update Address: Fails when ID is null")
    void shouldThrowExceptionWhenUpdatingAddressWithNullId() {
        Address address = createDefaultAddress("Street_", "1", "12345", "City_", "Country_");
        assertThrows(CustomerValidationException.class, () -> shopService.updateAddress(null, address));
    }

    @Test
    @DisplayName("Negative - Load All Contracts: Fails for inactive customer")
    void shouldThrowExceptionWhenLoadingContractsForInactiveCustomer() {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                Brand.GMX,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));

        UUID customerId = customer.getId();

        assertThrows(CustomerNotActiveException.class, () -> shopService.loadAllContracts(customerId));
    }

    @Test
    @DisplayName("Negative - Update Invoice Address: Fails for inactive customer")
    void shouldThrowExceptionWhenUpdatingInvoiceAddressOfInactiveCustomer() {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                Brand.GMX,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));
        Address newInv = createDefaultAddress("Street_", "1", "12345", "City_", "Country_");

        UUID customerId = customer.getId();

        assertThrows(CustomerNotActiveException.class, () -> shopService.updateInvoiceAddress(customerId, newInv));
    }

    @Test
    @DisplayName("Negative - Update Communication Details: Fails for inactive customer")
    void shouldThrowExceptionWhenUpdatingCommunicationDetailsOfInactiveCustomer() {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                Brand.GMX,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));
        CommunicationDetails newComms = createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789");

        UUID customerId = customer.getId();

        assertThrows(
                CustomerNotActiveException.class, () -> shopService.updateCommunicationDetails(customerId, newComms));
    }

    @Test
    @DisplayName("Negative - Generate Invoice: Fails for inactive customer")
    void shouldThrowExceptionWhenGeneratingInvoiceForInactiveCustomer() {
        Customer customer = shopService.registerCustomer(createDefaultTestCustomer(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                Brand.GMX,
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultAddress("Street_", "1", "12345", "City_", "Country_"),
                createDefaultCommunicationDetails("user_@test-domain.com", "+49-123-456789")));

        UUID customerId = customer.getId();

        assertThrows(CustomerNotActiveException.class, () -> shopService.generateInvoice(customerId));
    }

    @Test
    @DisplayName("Negative - Generate Invoice: Fails for non-existent customer")
    void shouldThrowExceptionWhenGeneratingInvoiceForNonExistentCustomer() {
        UUID randomUUIDid = UUID.randomUUID();
        assertThrows(CustomerNotFoundException.class, () -> shopService.generateInvoice(randomUUIDid));
    }

    @Test
    @DisplayName("Negative - Remove Customer: Fails for non-existent ID")
    void shouldThrowExceptionWhenRemovingNonExistentCustomer() {
        UUID randomUUIDid = UUID.randomUUID();
        assertThrows(CustomerNotFoundException.class, () -> shopService.removeCustomer(randomUUIDid));
    }

    @Test
    @DisplayName("Positive - Load All Products for Brand: Returns non-empty list")
    void shouldHaveProductsInCatalogAfterInitialization() {
        List<Product> gmxProducts = shopService.loadAllProductsForBrand(Brand.GMX);
        List<Product> mailComProducts = shopService.loadAllProductsForBrand(Brand.MAIL_COM);
        List<Product> webDeProducts = shopService.loadAllProductsForBrand(Brand.WEB_DE);

        assertFalse(gmxProducts.isEmpty());
        assertFalse(mailComProducts.isEmpty());
        assertFalse(webDeProducts.isEmpty());
    }
}
