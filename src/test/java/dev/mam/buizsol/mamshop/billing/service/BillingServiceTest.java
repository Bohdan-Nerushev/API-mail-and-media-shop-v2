package dev.mam.buizsol.mamshop.billing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.mam.buizsol.mamshop.billing.model.Invoice;
import dev.mam.buizsol.mamshop.billing.model.InvoiceItem;
import dev.mam.buizsol.mamshop.billing.validation.InvoiceDiscountValidator;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import dev.mam.buizsol.mamshop.contract.model.ContractStatus;
import dev.mam.buizsol.mamshop.contract.service.ContractService;
import dev.mam.buizsol.mamshop.customer.exception.CustomerNotFoundException;
import dev.mam.buizsol.mamshop.customer.model.Address;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.customer.model.CommunicationDetails;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.customer.service.CustomerService;
import dev.mam.buizsol.mamshop.product.exception.ProductNotFoundException;
import dev.mam.buizsol.mamshop.product.model.Product;
import dev.mam.buizsol.mamshop.product.model.StandardMailProduct;
import dev.mam.buizsol.mamshop.product.service.ProductService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationInterceptor;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BillingService Tests")
class BillingServiceTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private ProductService productService;

    @Mock
    private ContractService contractService;

    private BillingService billingService;

    private Customer testCustomer;
    private Product testProduct;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        final BigDecimal zero = BigDecimal.ZERO;
        final BigDecimal minimalDiscount = new BigDecimal("0.10");

        final BillingServiceImpl target =
                new BillingServiceImpl(customerService, productService, contractService, zero, minimalDiscount);

        final LocalValidatorFactoryBean validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.setConstraintValidatorFactory(new ConstraintValidatorFactory() {
            @Override
            public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
                try {
                    T instance = key.getDeclaredConstructor().newInstance();
                    if (instance instanceof InvoiceDiscountValidator) {
                        ReflectionTestUtils.setField(instance, "zeroAmount", BigDecimal.ZERO);
                        ReflectionTestUtils.setField(instance, "minimalDiscountAmount", new BigDecimal("0.10"));
                    }
                    return instance;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void releaseInstance(ConstraintValidator<?, ?> instance) {
                // No-op: Spring handles validator lifecycle management.
            }
        });
        validatorFactory.afterPropertiesSet();

        final ProxyFactory factory = new ProxyFactory();
        factory.setTarget(target);
        factory.addInterface(BillingService.class);
        factory.addAdvice(new MethodValidationInterceptor(validatorFactory.getValidator()));

        billingService = (BillingService) factory.getProxy();

        final Address address = new Address("Street", "1", "12345", "City", "Country");
        final CommunicationDetails communication = new CommunicationDetails("test@test.com", "123456789");

        testCustomer =
                Customer.create("John", "Doe", LocalDate.of(1990, 1, 1), address, null, communication, Brand.GMX);
        testCustomer.setId(UUID.randomUUID());
        customerId = testCustomer.getId();

        testProduct = new StandardMailProduct("Standard Mail", Brand.GMX, new BigDecimal("5.00"));
    }

    @Test
    @DisplayName("Successful generation of invoice without discount")
    void shouldGenerateInvoiceWithoutDiscountWhenAllRequirementsAreMet() throws Exception {
        when(customerService.findCustomerById(customerId)).thenReturn(Optional.of(testCustomer));

        final Contract contract = mock(Contract.class);
        when(contract.getStatus()).thenReturn(ContractStatus.ACTIVE);
        when(contract.getProductId()).thenReturn(testProduct.getId());
        when(contract.getId()).thenReturn(UUID.randomUUID());
        when(contract.getCreationDate()).thenReturn(LocalDate.now());

        when(contractService.findContractsByCustomerId(customerId)).thenReturn(List.of(contract));
        when(productService.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

        final Invoice invoice = billingService.generateInvoice(customerId);

        assertNotNull(invoice);
        assertEquals(customerId, invoice.getCustomer().getId());
        assertEquals(BigDecimal.ZERO, invoice.getDiscount());
        assertEquals(1, invoice.getItems().size());
    }

    @Test
    @DisplayName("Successful generation of invoice with discount")
    void shouldGenerateInvoiceWithDiscountWhenValidDiscountProvided() throws Exception {
        final BigDecimal discount = new BigDecimal("1.00");
        when(customerService.findCustomerById(customerId)).thenReturn(Optional.of(testCustomer));
        when(contractService.findContractsByCustomerId(customerId)).thenReturn(List.of());

        final Invoice invoice = billingService.generateInvoice(customerId, discount);

        assertNotNull(invoice);
        assertEquals(discount, invoice.getDiscount());
    }

    @Test
    @DisplayName("Verification of correct totals calculation in invoice")
    void shouldCalculateTotalsCorrectlyWhenMultipleContractsExist() throws Exception {
        when(customerService.findCustomerById(customerId)).thenReturn(Optional.of(testCustomer));

        final Product p1 = new StandardMailProduct("P1", Brand.GMX, new BigDecimal("5.00"));
        final Product p2 = new StandardMailProduct("P2", Brand.GMX, new BigDecimal("15.00"));

        final Contract c1 = mock(Contract.class);
        when(c1.getStatus()).thenReturn(ContractStatus.ACTIVE);
        when(c1.getProductId()).thenReturn(p1.getId());
        when(c1.getCreationDate()).thenReturn(LocalDate.now());

        final Contract c2 = mock(Contract.class);
        when(c2.getStatus()).thenReturn(ContractStatus.ACTIVE);
        when(c2.getProductId()).thenReturn(p2.getId());
        when(c2.getCreationDate()).thenReturn(LocalDate.now());

        when(contractService.findContractsByCustomerId(customerId)).thenReturn(List.of(c1, c2));
        when(productService.findById(p1.getId())).thenReturn(Optional.of(p1));
        when(productService.findById(p2.getId())).thenReturn(Optional.of(p2));

        final BigDecimal discount = new BigDecimal("5.00");

        final Invoice invoice = billingService.generateInvoice(customerId, discount);

        assertEquals(new BigDecimal("9.98"), invoice.getTotalSetupFee());
        assertEquals(new BigDecimal("20.00"), invoice.getTotalMonthlyFee());
        assertEquals(new BigDecimal("24.98"), invoice.getTotalAmount());
    }

    @Test
    @DisplayName("Verification of invoice item field structure")
    void shouldPopulateInvoiceItemCorrectlyWhenGeneratingInvoice() throws Exception {
        when(customerService.findCustomerById(customerId)).thenReturn(Optional.of(testCustomer));
        final LocalDate creationDate = LocalDate.now().minusDays(10);
        final Contract contract = mock(Contract.class);
        when(contract.getStatus()).thenReturn(ContractStatus.ACTIVE);
        when(contract.getProductId()).thenReturn(testProduct.getId());
        final UUID cid = UUID.randomUUID();
        when(contract.getId()).thenReturn(cid);
        when(contract.getCreationDate()).thenReturn(creationDate);

        when(contractService.findContractsByCustomerId(customerId)).thenReturn(List.of(contract));
        when(productService.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

        final Invoice invoice = billingService.generateInvoice(customerId);
        final InvoiceItem item = invoice.getItems().get(0);

        assertEquals(testProduct.getId(), item.getProductId());
        assertEquals(testProduct.getName(), item.getProductName());
        assertEquals(cid, item.getContract().getId());
        assertEquals(creationDate, item.getContractCreationDate());
        assertEquals(testProduct.getSetupFee(), item.getSetupFee());
        assertEquals(testProduct.getMonthlyFee(), item.getMonthlyFee());
    }

    @DisplayName("Invalid small positive discount validation checks")
    @ParameterizedTest(name = "Invalid small positive and negative discount validation - value: {0}")
    @ValueSource(strings = {"0.01", "0.05", "0.09", "0.10", "-0.01", "-1.00", "-0.10"})
    void shouldThrowExceptionWhenDiscountIsInvalid(final String invalidDiscountStr) {
        final BigDecimal invalidDiscount = new BigDecimal(invalidDiscountStr);
        assertThrows(
                ConstraintViolationException.class, () -> billingService.generateInvoice(customerId, invalidDiscount));
    }

    @Test
    @DisplayName("Handling scenario when customer is not found")
    void shouldThrowExceptionWhenCustomerDoesNotExist() {
        when(customerService.findCustomerById(customerId)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> billingService.generateInvoice(customerId));
    }

    @Test
    @DisplayName("Successful generation for customer without active contracts")
    void shouldGenerateEmptyInvoiceWhenCustomerHasNoActiveContracts() throws Exception {
        when(customerService.findCustomerById(customerId)).thenReturn(Optional.of(testCustomer));

        final Contract contract = mock(Contract.class);
        when(contract.getStatus()).thenReturn(ContractStatus.INACTIVE);
        when(contractService.findContractsByCustomerId(customerId)).thenReturn(List.of(contract));

        final Invoice invoice = billingService.generateInvoice(customerId);

        assertTrue(invoice.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, invoice.getTotalSetupFee());
        assertEquals(BigDecimal.ZERO, invoice.getTotalMonthlyFee());
        assertEquals(BigDecimal.ZERO, invoice.getTotalAmount());
    }

    @DisplayName("Null arguments validation for generateInvoice")
    @ParameterizedTest(name = "Null arguments validation - customerId={0}, discount={1}")
    @CsvSource(
            value = {"null, 10.00", "550e8400-e29b-41d4-a716-446655440000, null", "null, null"},
            nullValues = {"null"})
    void shouldThrowExceptionWhenArgumentsAreNull(final UUID cid, final BigDecimal disc) {
        assertThrows(ConstraintViolationException.class, () -> billingService.generateInvoice(cid, disc));
    }

    @Test
    @DisplayName("Negative: generateInvoice with null customerId")
    void shouldThrowExceptionWhenGeneratingInvoiceByNullCustomerId() {
        assertThrows(ConstraintViolationException.class, () -> billingService.generateInvoice(null));
    }

    @Test
    @DisplayName("Handling scenario when product is not found for a contract")
    void shouldThrowExceptionWhenProductInContractDoesNotExist() {
        when(customerService.findCustomerById(customerId)).thenReturn(Optional.of(testCustomer));
        final Contract contract = mock(Contract.class);
        when(contract.getStatus()).thenReturn(ContractStatus.ACTIVE);
        when(contract.getProductId()).thenReturn(UUID.randomUUID());
        when(contractService.findContractsByCustomerId(customerId)).thenReturn(List.of(contract));
        when(productService.findById(any())).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> billingService.generateInvoice(customerId));
    }

    @DisplayName("Verification of valid boundary discount values")
    @ParameterizedTest(name = "Boundary discount values - value: {0}")
    @ValueSource(strings = {"0.00", "0.11", "0.10001", "1.00", "5.00", "100.00"})
    void shouldGenerateInvoiceWhenValidBoundaryDiscountsProvided(final String validDiscountStr) throws Exception {
        final BigDecimal validDiscount = new BigDecimal(validDiscountStr);
        when(customerService.findCustomerById(customerId)).thenReturn(Optional.of(testCustomer));
        when(contractService.findContractsByCustomerId(customerId)).thenReturn(List.of());

        final Invoice invoice = billingService.generateInvoice(customerId, validDiscount);

        assertNotNull(invoice);
        assertEquals(validDiscount, invoice.getDiscount());
    }

    @Test
    @DisplayName("Generation with mixed contract states (active and inactive)")
    void shouldOnlyIncludeActiveContractsInInvoice() throws Exception {
        when(customerService.findCustomerById(customerId)).thenReturn(Optional.of(testCustomer));

        final Contract activeContract = mock(Contract.class);
        when(activeContract.getStatus()).thenReturn(ContractStatus.ACTIVE);
        when(activeContract.getProductId()).thenReturn(testProduct.getId());
        when(activeContract.getCreationDate()).thenReturn(LocalDate.now());

        final Contract inactiveContract = mock(Contract.class);
        when(inactiveContract.getStatus()).thenReturn(ContractStatus.INACTIVE);

        when(contractService.findContractsByCustomerId(customerId))
                .thenReturn(List.of(activeContract, inactiveContract));
        when(productService.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

        final Invoice invoice = billingService.generateInvoice(customerId);

        assertEquals(1, invoice.getItems().size());
        assertEquals(testProduct.getId(), invoice.getItems().get(0).getProductId());
    }

    @Test
    @DisplayName("Generation with multiple contracts for the same product")
    void shouldIncludeMultipleInvoiceItemsWhenContractsAreForSameProduct() throws Exception {
        when(customerService.findCustomerById(customerId)).thenReturn(Optional.of(testCustomer));

        final UUID c1Id = UUID.randomUUID();
        final UUID c2Id = UUID.randomUUID();

        final Contract c1 = mock(Contract.class);
        when(c1.getStatus()).thenReturn(ContractStatus.ACTIVE);
        when(c1.getProductId()).thenReturn(testProduct.getId());
        when(c1.getId()).thenReturn(c1Id);
        when(c1.getCreationDate()).thenReturn(LocalDate.now());

        final Contract c2 = mock(Contract.class);
        when(c2.getStatus()).thenReturn(ContractStatus.ACTIVE);
        when(c2.getProductId()).thenReturn(testProduct.getId());
        when(c2.getId()).thenReturn(c2Id);
        when(c2.getCreationDate()).thenReturn(LocalDate.now());

        when(contractService.findContractsByCustomerId(customerId)).thenReturn(List.of(c1, c2));
        when(productService.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

        final Invoice invoice = billingService.generateInvoice(customerId);

        assertEquals(2, invoice.getItems().size());
        assertEquals(c1Id, invoice.getItems().get(0).getContract().getId());
        assertEquals(c2Id, invoice.getItems().get(1).getContract().getId());
    }

    @Test
    @DisplayName("Stress test for generation with 100 active contracts")
    void shouldHandleLargeNumberOfContractsWhenGeneratingInvoice() throws Exception {
        when(customerService.findCustomerById(customerId)).thenReturn(Optional.of(testCustomer));

        final int count = 100;
        final List<Contract> contracts = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            final Contract c = mock(Contract.class);
            when(c.getStatus()).thenReturn(ContractStatus.ACTIVE);
            when(c.getProductId()).thenReturn(testProduct.getId());
            when(c.getCreationDate()).thenReturn(LocalDate.now());
            contracts.add(c);
        }

        when(contractService.findContractsByCustomerId(customerId)).thenReturn(contracts);
        when(productService.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

        final Invoice invoice = billingService.generateInvoice(customerId);

        assertEquals(count, invoice.getItems().size());
        final BigDecimal expectedTotal =
                testProduct.getSetupFee().add(testProduct.getMonthlyFee()).multiply(new BigDecimal(count));
        assertEquals(expectedTotal, invoice.getTotalAmount());
    }
}
