package dev.mam.buizsol.mamshop.shop.service.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.mam.buizsol.mamshop.contract.dto.ContractResponseDTO;
import dev.mam.buizsol.mamshop.contract.mapper.ContractMapper;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import dev.mam.buizsol.mamshop.contract.model.ContractStatus;
import dev.mam.buizsol.mamshop.customer.controller.CustomerTestFactory;
import dev.mam.buizsol.mamshop.customer.dto.AddressRequestDTO;
import dev.mam.buizsol.mamshop.customer.dto.AddressResponseDTO;
import dev.mam.buizsol.mamshop.customer.dto.CommunicationDetailsRequestDTO;
import dev.mam.buizsol.mamshop.customer.dto.CommunicationDetailsResponseDTO;
import dev.mam.buizsol.mamshop.customer.dto.CustomerRequestDTO;
import dev.mam.buizsol.mamshop.customer.dto.CustomerResponseDTO;
import dev.mam.buizsol.mamshop.customer.dto.PurchaseRequestDTO;
import dev.mam.buizsol.mamshop.customer.exception.CustomerNotFoundException;
import dev.mam.buizsol.mamshop.customer.mapper.CustomerMapper;
import dev.mam.buizsol.mamshop.customer.model.Address;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.customer.model.CommunicationDetails;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.customer.model.CustomerStatus;
import dev.mam.buizsol.mamshop.product.dto.ProductResponseDTO;
import dev.mam.buizsol.mamshop.product.exception.ProductNotFoundException;
import dev.mam.buizsol.mamshop.product.mapper.ProductMapper;
import dev.mam.buizsol.mamshop.product.model.Product;
import dev.mam.buizsol.mamshop.shop.service.ShopController;
import dev.mam.buizsol.mamshop.shop.service.ShopService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName(value = "Shop Tests")
@WebMvcTest(
        controllers = ShopController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
class ShopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShopService shopService;

    @MockitoBean
    private ProductMapper productMapper;

    @MockitoBean
    private CustomerMapper customerMapper;

    @MockitoBean
    private ContractMapper contractMapper;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @DisplayName(
            value = "Positve: Given a valid brand, when the client requests products, then all"
                    + " products for that brand are returned successfully")
    @Test
    void testLoadAllProductsForBrand_Success() throws Exception {
        final Brand brand = Brand.MAIL_COM;

        final Product firstProduct =
                ShopTestFactory.createPremiumProduct("first", Brand.MAIL_COM, new BigDecimal("100.0"));

        final Product secondProduct =
                ShopTestFactory.createStandardProduct("second", Brand.MAIL_COM, new BigDecimal("120.0"));

        final List<Product> products = ShopTestFactory.createProductList(firstProduct, secondProduct);

        final ProductResponseDTO firstDto = ShopTestFactory.createDtoFromProduct(
                firstProduct.getId(),
                firstProduct.getName(),
                firstProduct.getBrand(),
                firstProduct.getSetupFee(),
                firstProduct.getMonthlyFee(),
                firstProduct.getStorageSize().orElse(0L));

        final ProductResponseDTO secondDto = ShopTestFactory.createDtoFromProduct(
                secondProduct.getId(),
                secondProduct.getName(),
                secondProduct.getBrand(),
                secondProduct.getSetupFee(),
                secondProduct.getMonthlyFee(),
                secondProduct.getStorageSize().orElse(0L));

        when(shopService.loadAllProductsForBrand(brand)).thenReturn(products);
        when(productMapper.toProductResponseDTO(firstProduct)).thenReturn(firstDto);
        when(productMapper.toProductResponseDTO(secondProduct)).thenReturn(secondDto);

        mockMvc.perform(get("/api/v1/shop/products").param("brand", brand.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("first"))
                .andExpect(jsonPath("$[1].name").value("second"));

        verify(shopService, times(1)).loadAllProductsForBrand(brand);
        verify(productMapper, times(1)).toProductResponseDTO(firstProduct);
        verify(productMapper, times(1)).toProductResponseDTO(secondProduct);
    }

    @Test
    @DisplayName(
            value = "Negative: Given a non-existing brand, when the client requests products, then"
                    + " a 404 error is returned")
    void testLoadAllProductsForBrand_BrandNotFound() throws Exception {
        final Brand brand = Brand.MAIL_COM;
        final String errorMessage = "Brand not found";

        when(shopService.loadAllProductsForBrand(brand)).thenThrow(new ProductNotFoundException(errorMessage));

        mockMvc.perform(get("/api/v1/shop/products").param("brand", brand.name()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found"));

        verify(shopService, times(1)).loadAllProductsForBrand(brand);
        verifyNoInteractions(productMapper);
    }

    @Test
    @DisplayName(
            value = "Negative: Given a null brand, when the client requests products, then a 400"
                    + " Bad Request error is returned")
    void testLoadAllProductsForBrand_NullBrand() throws Exception {
        mockMvc.perform(get("/api/v1/shop/products"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MISSING_PARAMETER"));

        verifyNoInteractions(shopService);
        verifyNoInteractions(productMapper);
    }

    @Test
    @DisplayName(value = "Positive: Should register customer successfully with valid data")
    void shouldRegisterCustomerSuccessfully() throws Exception {
        AddressRequestDTO addressDto =
                CustomerTestFactory.createAddressRequestDTO("Main St", "10", "12345", "Berlin", "Germany");
        CommunicationDetailsRequestDTO commDto =
                CustomerTestFactory.createCommunicationDetailsRequestDTO("john.doe@example.com", "+49123456789");
        CustomerRequestDTO requestDto = CustomerTestFactory.createCustomerRequestDTO(
                "John", "Doe", LocalDate.of(1990, 1, 1), addressDto, null, commDto, Brand.GMX);

        Address address = CustomerTestFactory.createAddress("Main St", "10", "12345", "Berlin", "Germany");
        CommunicationDetails comm =
                CustomerTestFactory.createCommunicationDetails("john.doe@example.com", "+49123456789");
        UUID customerId = UUID.randomUUID();
        Customer customer = CustomerTestFactory.createCustomer(
                customerId,
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                address,
                address,
                comm,
                Brand.GMX,
                CustomerStatus.INACTIVE);

        AddressResponseDTO addressResponseDto =
                CustomerTestFactory.createAddressResponseDTO("Main St", "10", "12345", "Berlin", "Germany");
        CommunicationDetailsResponseDTO commResponseDto =
                CustomerTestFactory.createCommunicationDetailsResponseDTO("john.doe@example.com", "+49123456789");

        CustomerResponseDTO responseDto = CustomerTestFactory.createCustomerResponseDTO(
                customerId,
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                addressResponseDto,
                addressResponseDto,
                commResponseDto,
                Brand.GMX,
                CustomerStatus.INACTIVE);

        when(customerMapper.toCustomer(any(CustomerRequestDTO.class))).thenReturn(customer);
        when(shopService.registerCustomer(any(Customer.class))).thenReturn(customer);
        when(customerMapper.toResponseDTO(any(Customer.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/shop/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(customerId.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.brand").value("GMX"));

        verify(customerMapper).toCustomer(any(CustomerRequestDTO.class));
        verify(shopService).registerCustomer(any(Customer.class));
        verify(customerMapper).toResponseDTO(any(Customer.class));
    }

    @Test
    @DisplayName(value = "Negative: Should return 400 when registering customer with invalid data")
    void shouldReturn400WhenRegisteringWithInvalidData() throws Exception {
        AddressRequestDTO addressDto =
                CustomerTestFactory.createAddressRequestDTO("", "10", "12345", "Berlin", "Germany");
        CommunicationDetailsRequestDTO commDto =
                CustomerTestFactory.createCommunicationDetailsRequestDTO("invalid-email", "+49123456789");
        CustomerRequestDTO requestDto = CustomerTestFactory.createCustomerRequestDTO(
                "John", "Doe", LocalDate.of(1990, 1, 1), addressDto, null, commDto, Brand.GMX);

        mockMvc.perform(post("/api/v1/shop/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("REQUEST_VALIDATION_ERROR"));
    }

    @Test
    @DisplayName(
            value = "Positive: Given an existing customer ID, when the client requests the"
                    + " customer, then the customer data is returned successfully")
    void shouldReturnCustomerDataSuccessfully() throws Exception {
        UUID customerId = UUID.randomUUID();

        Address address = CustomerTestFactory.createAddress("Main St", "10", "12345", "Berlin", "Germany");
        CommunicationDetails comm =
                CustomerTestFactory.createCommunicationDetails("john.doe@example.com", "+49123456789");
        Customer customer = CustomerTestFactory.createCustomer(
                customerId,
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                address,
                address,
                comm,
                Brand.GMX,
                CustomerStatus.INACTIVE);

        AddressResponseDTO addressResponseDto =
                CustomerTestFactory.createAddressResponseDTO("Main St", "10", "12345", "Berlin", "Germany");
        CommunicationDetailsResponseDTO commResponseDto =
                CustomerTestFactory.createCommunicationDetailsResponseDTO("john.doe@example.com", "+49123456789");
        CustomerResponseDTO customerResponseDTO = CustomerTestFactory.createCustomerResponseDTO(
                customerId,
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                addressResponseDto,
                null,
                commResponseDto,
                Brand.GMX,
                CustomerStatus.INACTIVE);

        when(shopService.loadCustomer(any(UUID.class))).thenReturn(customer);
        when(customerMapper.toResponseDTO(any(Customer.class))).thenReturn(customerResponseDTO);

        mockMvc.perform(get("/api/v1/shop/customers/{customerId}", customerId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.birthDate").value("1990-01-01"))
                .andExpect(jsonPath("$.brand").value("GMX"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        verify(shopService).loadCustomer(any(UUID.class));
        verify(customerMapper).toResponseDTO(any(Customer.class));
    }

    @Test
    @DisplayName(
            value = "Negative: Given a non-existing customer ID, when the client requests the"
                    + " customer, then a 404 error is returned")
    void shouldReturn404WhenCustomerNotFound() throws Exception {
        UUID unknownCustomerId = UUID.randomUUID();

        when(shopService.loadCustomer(any(UUID.class)))
                .thenThrow(new CustomerNotFoundException("Customer not found with ID: " + unknownCustomerId));

        mockMvc.perform(get("/api/v1/shop/customers/{customerId}", unknownCustomerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Customer not found"));

        verify(shopService).loadCustomer(any(UUID.class));
        verifyNoInteractions(customerMapper);
    }

    @Test
    @DisplayName(
            value = "Positive: Given an existing customer ID, when the client requests to remove"
                    + " the customer, then the customer status is changed to REMOVED"
                    + " successfully")
    void shouldRemoveCustomerSuccessfully() throws Exception {
        UUID customerId = UUID.randomUUID();

        doNothing().when(shopService).removeCustomer(any(UUID.class));

        mockMvc.perform(delete("/api/v1/shop/customers/{customerId}", customerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(shopService).removeCustomer(customerId);
        verifyNoInteractions(customerMapper);
    }

    @Test
    @DisplayName(
            value = "Negative: Given a non-existing customer ID, when the client requests to remove"
                    + " the customer, then a 404 error is returned")
    void shouldReturn404WhenRemovingNonExistingCustomer() throws Exception {
        UUID unknownId = UUID.randomUUID();

        doThrow(new CustomerNotFoundException("Customer not found"))
                .when(shopService)
                .removeCustomer(unknownId);

        mockMvc.perform(delete("/api/v1/shop/customers/{customerId}", unknownId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Customer not found"));

        verify(shopService).removeCustomer(unknownId);
        verifyNoInteractions(customerMapper);
    }

    @Test
    @DisplayName(
            value = "Positive: Given an existing customer ID, when the client requests to activate"
                    + " the customer, then the customer status is changed to ACTIVE"
                    + " successfully")
    void shouldActivateCustomerSuccessfully() throws Exception {
        UUID customerId = UUID.randomUUID();

        doNothing().when(shopService).activateCustomer(any(UUID.class));

        mockMvc.perform(put("/api/v1/shop/customers/{customerId}/activate", customerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(shopService).activateCustomer(customerId);
        verifyNoInteractions(customerMapper);
    }

    @Test
    @DisplayName(
            value = "Negative: Given a non-existing customer ID, when the client requests to"
                    + " activate the customer, then a 404 error is returned")
    void shouldReturn404WhenActivatingNonExistingCustomer() throws Exception {
        UUID unknownId = UUID.randomUUID();

        doThrow(new CustomerNotFoundException("Customer not found"))
                .when(shopService)
                .activateCustomer(unknownId);

        mockMvc.perform(put("/api/v1/shop/customers/{customerId}/activate", unknownId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Customer not found"));

        verify(shopService).activateCustomer(unknownId);
        verifyNoInteractions(customerMapper);
    }

    @Test
    @DisplayName(
            value = "Positive: Given an existing customer and product, when the client requests to"
                    + " purchase the product, then a new contract is created successfully")
    void shouldPurchaseProductSuccessfully() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        PurchaseRequestDTO purchaseRequestDTO = CustomerTestFactory.createPurchaseRequestDTO(productId);
        Customer customer = Mockito.mock(Customer.class);
        Mockito.when(customer.getId()).thenReturn(customerId);
        Contract contract = CustomerTestFactory.createContract(
                UUID.randomUUID(), customer, "TestProduct", productId, LocalDate.now(), ContractStatus.INACTIVE);
        ContractResponseDTO contractResponseDTO = CustomerTestFactory.createContractResponseDTO(
                contract.getId(), customerId, productId, contract.getCreationDate(), contract.getStatus());

        when(shopService.purchaseProduct(customerId, productId)).thenReturn(contract);
        when(contractMapper.toContractResponseDTO(contract)).thenReturn(contractResponseDTO);

        mockMvc.perform(post("/api/v1/shop/customers/{customerId}/purchases", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(contract.getId().toString()))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.productId").value(productId.toString()));

        verify(shopService).purchaseProduct(customerId, productId);
        verify(contractMapper).toContractResponseDTO(contract);
    }

    @Test
    @DisplayName(
            value = "Negative: Given a non-existing customer, when the client requests to purchase"
                    + " a product, then a 404 error is returned")
    void shouldReturn404WhenPurchasingProductForNonExistingCustomer() throws Exception {
        UUID unknownCustomerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        PurchaseRequestDTO purchaseRequestDTO = CustomerTestFactory.createPurchaseRequestDTO(productId);

        when(shopService.purchaseProduct(unknownCustomerId, productId))
                .thenThrow(new CustomerNotFoundException("Customer not found with ID: " + unknownCustomerId));

        mockMvc.perform(post("/api/v1/shop/customers/{customerId}/purchases", unknownCustomerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Customer not found"));

        verify(shopService).purchaseProduct(unknownCustomerId, productId);
        verifyNoInteractions(contractMapper);
    }

    @Test
    @DisplayName(
            value = "Negative: Given a valid customer and non-existing product, when the client"
                    + " requests to purchase the product, then a 404 error is returned")
    void shouldReturn404WhenPurchasingNonExistingProduct() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID unknownProductId = UUID.randomUUID();
        PurchaseRequestDTO purchaseRequestDTO = CustomerTestFactory.createPurchaseRequestDTO(unknownProductId);

        when(shopService.purchaseProduct(customerId, unknownProductId))
                .thenThrow(new CustomerNotFoundException("Product not found with ID: " + unknownProductId));

        mockMvc.perform(post("/api/v1/shop/customers/{customerId}/purchases", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Customer not found"));

        verify(shopService).purchaseProduct(customerId, unknownProductId);
        verifyNoInteractions(contractMapper);
    }

    @Test
    @DisplayName(
            value = "Negative: Given an existing customer and invalid purchase data, when the"
                    + " client requests to purchase the product, then a 400 error is returned")
    void shouldReturn400WhenPurchasingWithInvalidData() throws Exception {
        UUID customerId = UUID.randomUUID();
        PurchaseRequestDTO invalidRequest = CustomerTestFactory.createPurchaseRequestDTO(null);

        mockMvc.perform(post("/api/v1/shop/customers/{customerId}/purchases", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(shopService);
        verifyNoInteractions(contractMapper);
    }

    @Test
    @DisplayName(value = "Positive: should return list of contracts when customer exists")
    void shouldLoadAllContractsByCustomerId() throws Exception {
        final UUID customerId = UUID.randomUUID();

        final UUID contractId = UUID.randomUUID();
        final UUID productId = UUID.randomUUID();

        final Contract mockContract = Mockito.mock(Contract.class);
        Mockito.when(mockContract.getId()).thenReturn(contractId);
        Mockito.when(mockContract.getStatus()).thenReturn(ContractStatus.INACTIVE);

        final ContractResponseDTO responseDto =
                new ContractResponseDTO(contractId, customerId, productId, LocalDate.now(), ContractStatus.INACTIVE);

        when(shopService.loadAllContracts(any(UUID.class))).thenReturn(List.of(mockContract, mockContract));

        when(contractMapper.toContractResponseDTO(any(Contract.class))).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/shop/contracts/{customerId}", customerId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(contractId.toString())))
                .andExpect(jsonPath("$[0].status", is(ContractStatus.INACTIVE.name())));

        verify(shopService).loadAllContracts(customerId);
    }

    @Test
    @DisplayName(value = "Negative: should return 404 when customer not found for contracts")
    void shouldReturn404WhenCustomerNotFoundForContracts() throws Exception {
        final UUID customerId = UUID.randomUUID();

        when(shopService.loadAllContracts(any(UUID.class)))
                .thenThrow(new CustomerNotFoundException("Customer not found"));

        mockMvc.perform(get("/api/v1/shop/contracts/{customerId}", customerId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Customer not found")))
                .andExpect(jsonPath("$.errorCode", is("CUSTOMER_NOT_FOUND")));

        verify(shopService).loadAllContracts(customerId);
        verifyNoInteractions(contractMapper);
    }
}
