package dev.mam.buizsol.mamshop.product.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD, PARAMETER, METHOD, RECORD_COMPONENT})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidStorageSizeValidator.class)
@Documented
public @interface ValidStorageSize {
    String message() default "Storage size must be at least 1GB";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
