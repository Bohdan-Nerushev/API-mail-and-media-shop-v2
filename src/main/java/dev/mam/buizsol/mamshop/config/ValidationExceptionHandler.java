package dev.mam.buizsol.mamshop.config;

import dev.mam.buizsol.mamshop.billing.exception.InvalidInvoiceDiscountException;
import dev.mam.buizsol.mamshop.billing.exception.InvoiceValidationException;
import dev.mam.buizsol.mamshop.billing.validation.InvoiceDiscount;
import dev.mam.buizsol.mamshop.contract.exception.BrandMismatchException;
import dev.mam.buizsol.mamshop.contract.exception.ContractValidationException;
import dev.mam.buizsol.mamshop.contract.validation.BrandMatch;
import dev.mam.buizsol.mamshop.customer.exception.CustomerNotActiveException;
import dev.mam.buizsol.mamshop.customer.exception.CustomerValidationException;
import dev.mam.buizsol.mamshop.customer.validation.ActiveCustomer;
import dev.mam.buizsol.mamshop.product.exception.ProductValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import java.util.stream.Collectors;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ValidationExceptionHandler {

    private static final String BILLING_PKG = "dev.mam.buizsol.mamshop.billing.";
    private static final String PRODUCT_PKG = "dev.mam.buizsol.mamshop.product.";
    private static final String CUSTOMER_PKG = "dev.mam.buizsol.mamshop.customer.";
    private static final String SHOP_PKG = "dev.mam.buizsol.mamshop.shop.";
    private static final String CONTRACT_PKG = "dev.mam.buizsol.mamshop.contract.";

    @AfterThrowing(pointcut = "execution(* dev.mam.buizsol.mamshop..service..*(..))", throwing = "ex")
    public void handleValidationException(final ConstraintViolationException ex) {
        final Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        if (violations == null || violations.isEmpty()) {
            throw ex;
        }

        final String combinedMessage = buildCombinedMessage(violations);
        final ConstraintViolation<?> violation = violations.iterator().next();
        final Class<?> annotationType =
                violation.getConstraintDescriptor().getAnnotation().annotationType();

        throwIfAnnotationMatches(annotationType, combinedMessage);
        throwIfPackageMatches(violation, annotationType, combinedMessage, ex);
    }

    private String buildCombinedMessage(final Set<ConstraintViolation<?>> violations) {
        return violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("; "));
    }

    private void throwIfAnnotationMatches(final Class<?> annotationType, final String combinedMessage) {
        final String annotationName = annotationType.getSimpleName();
        if (ActiveCustomer.class.getSimpleName().equals(annotationName)) {
            throw new CustomerNotActiveException(combinedMessage);
        }
        if (InvoiceDiscount.class.getSimpleName().equals(annotationName)) {
            throw new InvalidInvoiceDiscountException(combinedMessage);
        }
        if (BrandMatch.class.getSimpleName().equals(annotationName)) {
            throw new BrandMismatchException(combinedMessage);
        }
    }

    private void throwIfPackageMatches(
            final ConstraintViolation<?> violation,
            final Class<?> annotationType,
            final String combinedMessage,
            final ConstraintViolationException ex) {
        final String className = getClassName(violation);
        final String annotationPackage = annotationType.getPackage().getName();

        if (isBillingPackage(className, annotationPackage)) {
            throw new InvoiceValidationException(combinedMessage);
        }
        if (isProductPackage(className, annotationPackage)) {
            throw new ProductValidationException(combinedMessage);
        }

        throwIfRemainingPackageMatches(className, annotationPackage, combinedMessage, ex);
    }

    private void throwIfRemainingPackageMatches(
            final String className,
            final String annotationPackage,
            final String combinedMessage,
            final ConstraintViolationException ex) {
        if (isCustomerOrShopPackage(className, annotationPackage)) {
            throw new CustomerValidationException(combinedMessage);
        }
        if (isContractPackage(className, annotationPackage)) {
            throw new ContractValidationException(combinedMessage);
        }
        throw ex;
    }

    private String getClassName(final ConstraintViolation<?> violation) {
        return violation.getRootBeanClass() != null
                ? violation.getRootBeanClass().getName()
                : "";
    }

    private boolean isBillingPackage(final String className, final String annotationPackage) {
        return className.startsWith(BILLING_PKG) || annotationPackage.startsWith(BILLING_PKG);
    }

    private boolean isProductPackage(final String className, final String annotationPackage) {
        return className.startsWith(PRODUCT_PKG) || annotationPackage.startsWith(PRODUCT_PKG);
    }

    private boolean isCustomerOrShopPackage(final String className, final String annotationPackage) {
        return className.startsWith(CUSTOMER_PKG)
                || className.startsWith(SHOP_PKG)
                || annotationPackage.startsWith(CUSTOMER_PKG);
    }

    private boolean isContractPackage(final String className, final String annotationPackage) {
        return className.startsWith(CONTRACT_PKG) || annotationPackage.startsWith(CONTRACT_PKG);
    }
}
