package dev.mam.buizsol.mamshop.product.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("ValidStorageSizeValidator Tests")
class ValidStorageSizeValidatorTest {

    private ValidStorageSizeValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new ValidStorageSizeValidator();
    }

    @Test
    @DisplayName("Should return true when value is null (delegated to @NotNull if needed)")
    void shouldReturnTrueWhenValueIsNull() {
        assertTrue(validator.isValid(null, context));
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 100L, 1024L})
    @DisplayName("Should return true when storage size is >= 1GB")
    void shouldReturnTrueWhenValueIsAtLeastOne(Long value) {
        assertTrue(validator.isValid(value, context));
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -100L})
    @DisplayName("Should return false when storage size is < 1GB")
    void shouldReturnFalseWhenValueIsLessThanOne(Long value) {
        assertFalse(validator.isValid(value, context));
    }
}
