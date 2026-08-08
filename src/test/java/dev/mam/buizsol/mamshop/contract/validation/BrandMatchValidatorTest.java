package dev.mam.buizsol.mamshop.contract.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.product.model.Product;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("BrandMatchValidator Tests")
class BrandMatchValidatorTest {

    private BrandMatchValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new BrandMatchValidator();
    }

    @Test
    @DisplayName("Should return true when values are null or insufficient to allow other validators to" + " handle it")
    void shouldReturnTrueWhenValuesAreInvalid() {
        assertTrue(validator.isValid(null, context));
        assertTrue(validator.isValid(new Object[] {}, context));
        assertTrue(validator.isValid(new Object[] {new Object()}, context));
    }

    @Test
    @DisplayName("Should return true when argument types do not match Customer and Product")
    void shouldReturnTrueWhenTypesMismatch() {
        assertTrue(validator.isValid(new Object[] {"NotACustomer", "NotAProduct"}, context));
    }

    @Test
    @DisplayName("Should return true when brands belonging to customer and product are identical")
    void shouldReturnTrueWhenBrandsMatch() {
        Customer customer = mock(Customer.class);
        Product product = mock(Product.class);

        when(customer.getBrand()).thenReturn(Brand.GMX);
        when(product.getBrand()).thenReturn(Brand.GMX);

        assertTrue(validator.isValid(new Object[] {customer, product}, context));
    }

    @Test
    @DisplayName("Should return false when brands belonging to customer and product differ")
    void shouldReturnFalseWhenBrandsDoNotMatch() {
        Customer customer = mock(Customer.class);
        Product product = mock(Product.class);

        when(customer.getBrand()).thenReturn(Brand.GMX);
        when(product.getBrand()).thenReturn(Brand.WEB_DE);

        assertFalse(validator.isValid(new Object[] {customer, product}, context));
    }
}
