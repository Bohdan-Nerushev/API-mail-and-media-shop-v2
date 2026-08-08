package dev.mam.buizsol.mamshop.shop.service;

import dev.mam.buizsol.mamshop.billing.service.BillingService;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import dev.mam.buizsol.mamshop.contract.model.ContractStatus;
import dev.mam.buizsol.mamshop.contract.service.ContractService;
import dev.mam.buizsol.mamshop.customer.exception.CustomerValidationException;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.customer.model.CustomerStatus;
import dev.mam.buizsol.mamshop.customer.service.CustomerService;
import dev.mam.buizsol.mamshop.product.model.Product;
import dev.mam.buizsol.mamshop.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShopService Unit Tests")
class ShopServiceTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private ProductService productService;

    @Mock
    private ContractService contractService;

    @Mock
    private BillingService billingService;

    private ShopService shopService;

    @BeforeEach
    void setUp() {
        shopService = new ShopServiceImpl(customerService, productService, contractService, billingService);
    }

    @Test
    @DisplayName("Remove Customer: Should throw exception when customer is active and has active contracts")
    void shouldThrowExceptionWhenRemovingActiveCustomerWithActiveContracts() {
        final UUID customerId = UUID.randomUUID();
        final Customer customer = mock(Customer.class);
        final Contract activeContract = mock(Contract.class);

        when(customerService.findCustomerById(customerId)).thenReturn(Optional.of(customer));
        when(customer.getStatus()).thenReturn(CustomerStatus.ACTIVE);
        when(contractService.findContractsByCustomerId(customerId)).thenReturn(List.of(activeContract));
        when(activeContract.getStatus()).thenReturn(ContractStatus.ACTIVE);

        assertThrows(CustomerValidationException.class, () -> shopService.removeCustomer(customerId));
        verify(customerService, never()).deleteCustomer(customerId);
    }

    @Test
    @DisplayName("Remove Customer: Should succeed when customer is active but has no active contracts")
    void shouldSucceedWhenRemovingActiveCustomerWithoutActiveContracts() throws Exception {
        final UUID customerId = UUID.randomUUID();
        final Customer customer = mock(Customer.class);
        final Contract inactiveContract = mock(Contract.class);

        when(customerService.findCustomerById(customerId)).thenReturn(Optional.of(customer));
        when(customer.getStatus()).thenReturn(CustomerStatus.ACTIVE);
        when(contractService.findContractsByCustomerId(customerId)).thenReturn(List.of(inactiveContract));
        when(inactiveContract.getStatus()).thenReturn(ContractStatus.INACTIVE);

        shopService.removeCustomer(customerId);

        verify(customerService).deleteCustomer(customerId);
    }

    @Test
    @DisplayName("Activate Customer: Should skip activation if already active")
    void shouldSkipActivationIfCustomerIsAlreadyActive() throws Exception {
        final UUID customerId = UUID.randomUUID();
        final Customer customer = mock(Customer.class);

        when(customerService.findCustomerById(customerId)).thenReturn(Optional.of(customer));
        when(customer.getStatus()).thenReturn(CustomerStatus.ACTIVE);

        shopService.activateCustomer(customerId);

        verify(customerService, never()).activateCustomer(customerId);
    }

    @Test
    @DisplayName("Activate Contract: Should throw exception if contract belongs to another customer")
    void shouldThrowExceptionIfContractBelongsToAnotherCustomer() {
        final UUID customerId = UUID.randomUUID();
        final UUID otherCustomerId = UUID.randomUUID();
        final UUID contractId = UUID.randomUUID();
        final Contract contract = mock(Contract.class);
        final Customer otherCustomer = mock(Customer.class);

        when(contractService.findContractById(contractId)).thenReturn(Optional.of(contract));
        when(contract.getCustomer()).thenReturn(otherCustomer);
        when(otherCustomer.getId()).thenReturn(otherCustomerId);

        assertThrows(CustomerValidationException.class, () -> shopService.activateContract(customerId, contractId));
        verify(contractService, never()).updateContractStatus(any(), any());
    }

    @Test
    @DisplayName("Activate Contract: Should skip if already active")
    void shouldSkipContractActivationIfAlreadyActive() throws Exception {
        final UUID customerId = UUID.randomUUID();
        final UUID contractId = UUID.randomUUID();
        final Contract contract = mock(Contract.class);
        final Customer customer = mock(Customer.class);

        when(contractService.findContractById(contractId)).thenReturn(Optional.of(contract));
        when(contract.getCustomer()).thenReturn(customer);
        when(customer.getId()).thenReturn(customerId);
        when(contract.getStatus()).thenReturn(ContractStatus.ACTIVE);

        shopService.activateContract(customerId, contractId);

        verify(contractService, never()).updateContractStatus(any(), any());
    }

    @Test
    @DisplayName("Purchase Product: Should return same contract if already exists")
    void shouldReturnExistingContractIfProductAlreadyPurchased() throws Exception {
        final UUID customerId = UUID.randomUUID();
        final UUID productId = UUID.randomUUID();
        final Customer customer = mock(Customer.class);
        final Product product = mock(Product.class);
        final Contract existingContract = mock(Contract.class);

        when(customerService.findCustomerById(customerId)).thenReturn(Optional.of(customer));
        when(customer.getStatus()).thenReturn(CustomerStatus.ACTIVE);
        when(productService.findById(productId)).thenReturn(Optional.of(product));
        when(contractService.findContractsByCustomerId(customerId)).thenReturn(List.of(existingContract));
        when(existingContract.getProductId()).thenReturn(productId);

        final Contract result = shopService.purchaseProduct(customerId, productId);

        assertEquals(existingContract, result);
        verify(contractService, never()).createContract(any(), any());
    }
}
