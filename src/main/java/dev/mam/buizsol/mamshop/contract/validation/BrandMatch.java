package dev.mam.buizsol.mamshop.contract.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({METHOD, CONSTRUCTOR, TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = BrandMatchValidator.class)
@Documented
public @interface BrandMatch {
    String message() default "Customer brand does not match product brand";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
