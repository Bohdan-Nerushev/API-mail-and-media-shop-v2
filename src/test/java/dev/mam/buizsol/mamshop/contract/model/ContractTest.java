package dev.mam.buizsol.mamshop.contract.model;

import dev.mam.buizsol.mamshop.contract.exception.BrandMismatchException;
import dev.mam.buizsol.mamshop.contract.exception.ContractValidationException;
import dev.mam.buizsol.mamshop.customer.exception.CustomerNotActiveException;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.customer.model.CustomerStatus;
import dev.mam.buizsol.mamshop.product.model.Product;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Contract Tests")
class ContractTest {

    @Test
    @DisplayName("Creating a contract for an active customer with the corresponding product")
    void shouldCreateContractWhenCustomerIsActiveAndBrandsMatch() throws BrandMismatchException {
        Customer customer = mock(Customer.class);
        Product product = mock(Product.class);
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        when(customer.getId()).thenReturn(customerId);
        when(customer.getStatus()).thenReturn(CustomerStatus.ACTIVE);
        when(customer.getBrand()).thenReturn(Brand.GMX);
        when(product.getId()).thenReturn(productId);
        when(product.getBrand()).thenReturn(Brand.GMX);
        when(product.getName()).thenReturn("FreeMail");

        Contract contract = Contract.create(customer, product);

        assertNotNull(contract.getCustomer());
        assertEquals(customerId, contract.getCustomer().getId());
        assertEquals(productId, contract.getProductId());
        assertEquals(ContractStatus.INACTIVE, contract.getStatus());
    }

    @Test
    @DisplayName("Verify that only IDs are saved, not objects")
    void shouldStoreOnlyIdsAndNotFullObjects() throws BrandMismatchException {
        Customer customer = mock(Customer.class);
        Product product = mock(Product.class);
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        when(customer.getId()).thenReturn(customerId);
        when(customer.getStatus()).thenReturn(CustomerStatus.ACTIVE);
        when(customer.getBrand()).thenReturn(Brand.GMX);
        when(product.getId()).thenReturn(productId);
        when(product.getBrand()).thenReturn(Brand.GMX);
        when(product.getName()).thenReturn("FreeMail");

        Contract contract = Contract.create(customer, product);

        assertNotNull(contract.getCustomer());
        assertEquals(customerId, contract.getCustomer().getId());
        assertNotNull(contract.getProductId());
        assertEquals(productId, contract.getProductId());
    }

    @Test
    @DisplayName("Checking the creation date")
    void shouldSetCreationDateToCurrentDate() throws BrandMismatchException {
        Customer customer = mock(Customer.class);
        Product product = mock(Product.class);

        when(customer.getId()).thenReturn(UUID.randomUUID());
        when(customer.getStatus()).thenReturn(CustomerStatus.ACTIVE);
        when(customer.getBrand()).thenReturn(Brand.GMX);
        when(product.getId()).thenReturn(UUID.randomUUID());
        when(product.getBrand()).thenReturn(Brand.GMX);
        when(product.getName()).thenReturn("FreeMail");

        Contract contract = Contract.create(customer, product);

        assertEquals(LocalDate.now(), contract.getCreationDate());
    }

    @Test
    @DisplayName("Attempting to create with different brands → BrandMismatchException")
    void shouldThrowExceptionWhenBrandsDoNotMatch() {
        Customer customer = mock(Customer.class);
        Product product = mock(Product.class);

        when(customer.getBrand()).thenReturn(Brand.GMX);
        when(product.getBrand()).thenReturn(Brand.WEB_DE);

        assertThrows(BrandMismatchException.class, () -> Contract.create(customer, product));
    }

    @Test
    @DisplayName("Attempting to create for an inactive customer → CustomerNotActiveException")
    void shouldThrowExceptionWhenCustomerIsNotActive() {
        Customer customer = mock(Customer.class);
        Product product = mock(Product.class);

        when(customer.getStatus()).thenReturn(CustomerStatus.INACTIVE);
        when(customer.getBrand()).thenReturn(Brand.GMX);
        when(product.getBrand()).thenReturn(Brand.GMX);

        assertThrows(CustomerNotActiveException.class, () -> Contract.create(customer, product));
    }

    @Test
    @DisplayName("Attempting to create a client with a null parameter → NullPointerException")
    void shouldThrowExceptionWhenCustomerIsNull() {
        Product product = mock(Product.class);
        assertThrows(NullPointerException.class, () -> Contract.create(null, product));
    }

    @Test
    @DisplayName("Attempting to create a product with a null parameter → NullPointerException")
    void shouldThrowExceptionWhenProductIsNull() {
        Customer customer = mock(Customer.class);
        assertThrows(NullPointerException.class, () -> Contract.create(customer, null));
    }

    @Test
    @DisplayName("Verify @NotNull validation configuration via ExecutableValidator and Reflection")
    void shouldValidateAnnotationsWithExecutableValidator() throws NoSuchMethodException {
        Method createMethod = Contract.class.getMethod("create", Customer.class, Product.class);

        Parameter[] parameters = createMethod.getParameters();
        NotNull notNullCustomer = parameters[0].getAnnotation(NotNull.class);
        NotNull notNullProduct = parameters[1].getAnnotation(NotNull.class);

        assertNotNull(notNullCustomer, "@NotNull missing on customer parameter");
        assertNotNull(notNullProduct, "@NotNull missing on product parameter");
    }

    @ParameterizedTest
    @EnumSource(Brand.class)
    @DisplayName("Parameterized test: creating a contract for all brands")
    void shouldCreateContractForAllBrands(Brand brand) throws BrandMismatchException {
        Customer customer = mock(Customer.class);
        Product product = mock(Product.class);

        when(customer.getId()).thenReturn(UUID.randomUUID());
        when(customer.getStatus()).thenReturn(CustomerStatus.ACTIVE);
        when(customer.getBrand()).thenReturn(brand);
        when(product.getId()).thenReturn(UUID.randomUUID());
        when(product.getBrand()).thenReturn(brand);
        when(product.getName()).thenReturn("FreeMail");

        Contract contract = Contract.create(customer, product);

        assertNotNull(contract);
    }

    @Test
    @DisplayName("Contract status update")
    void shouldUpdateContractStatus() throws BrandMismatchException {
        Customer customer = mock(Customer.class);
        Product product = mock(Product.class);

        when(customer.getId()).thenReturn(UUID.randomUUID());
        when(customer.getStatus()).thenReturn(CustomerStatus.ACTIVE);
        when(customer.getBrand()).thenReturn(Brand.GMX);
        when(product.getId()).thenReturn(UUID.randomUUID());
        when(product.getBrand()).thenReturn(Brand.GMX);
        when(product.getName()).thenReturn("FreeMail");

        Contract contract = Contract.create(customer, product);
        assertEquals(ContractStatus.INACTIVE, contract.getStatus());

        contract = contract.withStatus(ContractStatus.INACTIVE);
        assertEquals(ContractStatus.INACTIVE, contract.getStatus());

        contract = contract.withStatus(ContractStatus.ACTIVE);
        assertEquals(ContractStatus.ACTIVE, contract.getStatus());

        contract = contract.withStatus(ContractStatus.INACTIVE);
        assertEquals(ContractStatus.INACTIVE, contract.getStatus());
    }

    @Test
    @DisplayName("Attempting to update status to null → IllegalArgumentException")
    void shouldThrowExceptionWhenUpdatingStatusWithNull() throws BrandMismatchException {
        Customer customer = mock(Customer.class);
        Product product = mock(Product.class);

        when(customer.getId()).thenReturn(UUID.randomUUID());
        when(customer.getStatus()).thenReturn(CustomerStatus.ACTIVE);
        when(customer.getBrand()).thenReturn(Brand.GMX);
        when(product.getId()).thenReturn(UUID.randomUUID());
        when(product.getBrand()).thenReturn(Brand.GMX);
        when(product.getName()).thenReturn("FreeMail");

        Contract contract = Contract.create(customer, product);

        assertThrows(ContractValidationException.class, () -> contract.withStatus(null));
    }

    @Test
    @DisplayName("Negative: Failure when creating Contract with null fields (using create method)")
    void shouldThrowExceptionWhenCreatingContractWithNullParams() {
        assertThrows(NullPointerException.class, () -> Contract.create(null, mock(Product.class)));
        assertThrows(NullPointerException.class, () -> Contract.create(mock(Customer.class), null));
    }
}
