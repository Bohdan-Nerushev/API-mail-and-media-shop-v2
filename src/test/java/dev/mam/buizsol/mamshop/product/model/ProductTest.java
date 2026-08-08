package dev.mam.buizsol.mamshop.product.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.mam.buizsol.mamshop.customer.model.Brand;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Product Base Class Tests")
class ProductTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Success: Default implementation of getStorageSize should return Optional.empty()")
    void shouldReturnEmptyStorageSizeByDefault() {
        final Product product = createValidProductStub().build();

        final Optional<Long> storageSize = product.getStorageSize();

        assertTrue(storageSize.isEmpty());
    }

    @Test
    @DisplayName("Success: SuperBuilder should correctly initialize all shared fields")
    void shouldInitializeFieldsCorrectlyViaSuperBuilder() {
        final UUID id = UUID.randomUUID();
        final String name = "Test Product";
        final Brand brand = Brand.GMX;
        final BigDecimal setupFee = new BigDecimal("9.99");
        final BigDecimal monthlyFee = new BigDecimal("4.99");

        final Product product = ProductTestStub.builder()
                .id(id)
                .name(name)
                .brand(brand)
                .setupFee(setupFee)
                .monthlyFee(monthlyFee)
                .build();

        assertEquals(id, product.getId());
        assertEquals(name, product.getName());
        assertEquals(brand, product.getBrand());
        assertEquals(setupFee, product.getSetupFee());
        assertEquals(monthlyFee, product.getMonthlyFee());
    }

    @Test
    @DisplayName("Success: toBuilder should create an identical copy of the product")
    void shouldCloneFieldsCorrectlyViaToBuilder() {
        final ProductTestStub original = createValidProductStub().build();
        final ProductTestStub clone = original.toBuilder().build();

        assertEquals(original.getId(), clone.getId());
        assertEquals(original.getName(), clone.getName());
        assertEquals(original.getBrand(), clone.getBrand());
        assertEquals(original.getSetupFee(), clone.getSetupFee());
        assertEquals(original.getMonthlyFee(), clone.getMonthlyFee());
    }

    @Test
    @DisplayName("Negative: Validation failure when name is blank")
    void shouldFailValidationWhenNameIsBlank() {
        final Product product = createValidProductStub().name(" ").build();

        final Set<ConstraintViolation<Product>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("Negative: Validation failure when name exceeds maximum length")
    void shouldFailValidationWhenNameExceedsMaxLength() {
        final String longName = "A".repeat(101);
        final Product product = createValidProductStub().name(longName).build();

        final Set<ConstraintViolation<Product>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("Negative: Validation failure when brand is null")
    void shouldFailValidationWhenBrandIsNull() {
        final Product product = createValidProductStub().brand(null).build();

        final Set<ConstraintViolation<Product>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("brand")));
    }

    @Test
    @DisplayName("Negative: Validation failure when setupFee is null")
    void shouldFailValidationWhenSetupFeeIsNull() {
        final Product product = createValidProductStub().setupFee(null).build();

        final Set<ConstraintViolation<Product>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("setupFee")));
    }

    @Test
    @DisplayName("Negative: Validation failure when setupFee is negative")
    void shouldFailValidationWhenSetupFeeIsNegative() {
        final Product product =
                createValidProductStub().setupFee(new BigDecimal("-0.01")).build();

        final Set<ConstraintViolation<Product>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("setupFee")));
    }

    @Test
    @DisplayName("Negative: Validation failure when monthlyFee is below minimum threshold")
    void shouldFailValidationWhenMonthlyFeeIsBelowMinimum() {
        final Product product =
                createValidProductStub().monthlyFee(new BigDecimal("0.10")).build();

        final Set<ConstraintViolation<Product>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("monthlyFee")));
    }

    @Test
    @DisplayName("Success: Verify getter/setter for ID")
    void shouldHandleProductIdCorrectly() {
        final UUID id = UUID.randomUUID();
        final Product product = createValidProductStub().id(id).build();

        assertNotNull(product.getId());
        assertEquals(id, product.getId());
    }

    private ProductTestStub.ProductTestStubBuilder<?, ?> createValidProductStub() {
        return ProductTestStub.builder()
                .id(UUID.randomUUID())
                .name("Valid Product")
                .brand(Brand.GMX)
                .setupFee(BigDecimal.ZERO)
                .monthlyFee(new BigDecimal("0.11"));
    }

    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ProductTestStub extends Product {

        @Override
        public Product withMonthlyFee(final BigDecimal monthlyFee) {
            return this.toBuilder().monthlyFee(monthlyFee).build();
        }
    }
}
