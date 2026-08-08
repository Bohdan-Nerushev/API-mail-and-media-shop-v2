package dev.mam.buizsol.mamshop.billing.validation;

import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;

public class InvoiceDiscountValidator implements ConstraintValidator<InvoiceDiscount, BigDecimal> {

    @Value("${billing.minimal-discount-amount}")
    private BigDecimal minimalDiscountAmount;

    @Value("${billing.zero-amount}")
    private BigDecimal zeroAmount;

    @Override
    public boolean isValid(@Nullable final BigDecimal value, final ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value.compareTo(zeroAmount) < 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Discount cannot be negative")
                    .addConstraintViolation();
            return false;
        }
        if (value.compareTo(zeroAmount) > 0 && value.compareTo(minimalDiscountAmount) <= 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Discount must be greater than " + minimalDiscountAmount + " €")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
