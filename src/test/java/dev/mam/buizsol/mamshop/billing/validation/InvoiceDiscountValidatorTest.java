package dev.mam.buizsol.mamshop.billing.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("InvoiceDiscountValidator Tests")
class InvoiceDiscountValidatorTest {

    private InvoiceDiscountValidator validator;

    private static final BigDecimal DISCOUNT_LIMIT = new BigDecimal("0.10");

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new InvoiceDiscountValidator();
        ReflectionTestUtils.setField(validator, "minimalDiscountAmount", DISCOUNT_LIMIT);
        ReflectionTestUtils.setField(validator, "zeroAmount", BigDecimal.ZERO);

        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    @DisplayName("Should return true when discount is null")
    void shouldReturnTrueWhenValueIsNull() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    @DisplayName("Should return true when discount is zero")
    void shouldReturnTrueWhenValueIsZero() {
        assertTrue(validator.isValid(BigDecimal.ZERO, context));
    }

    @Test
    @DisplayName("Should return true when discount is greater than limit")
    void shouldReturnTrueWhenValueIsGreaterThanThreshold() {
        assertTrue(validator.isValid(DISCOUNT_LIMIT.add(new BigDecimal("0.01")), context));
        assertTrue(validator.isValid(DISCOUNT_LIMIT.add(new BigDecimal("5.00")), context));
    }

    static Stream<BigDecimal> invalidDiscounts() {
        return Stream.of(new BigDecimal("0.01"), DISCOUNT_LIMIT.divide(new BigDecimal("2")), DISCOUNT_LIMIT);
    }

    @ParameterizedTest
    @MethodSource("invalidDiscounts")
    @DisplayName("Should return false when discount is between 0 (exclusive) and limit (inclusive)")
    void shouldReturnFalseWhenValueIsBelowThreshold(BigDecimal value) {
        assertFalse(validator.isValid(value, context));
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("Discount must be greater than " + DISCOUNT_LIMIT + " €");
    }

    @Test
    @DisplayName("Should return false when discount is negative")
    void shouldReturnFalseWhenValueIsNegative() {
        assertFalse(validator.isValid(new BigDecimal("-1.00"), context));
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("Discount cannot be negative");
    }
}
