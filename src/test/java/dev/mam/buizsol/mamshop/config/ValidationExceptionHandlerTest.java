package dev.mam.buizsol.mamshop.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.mam.buizsol.mamshop.billing.exception.InvalidInvoiceDiscountException;
import dev.mam.buizsol.mamshop.billing.exception.InvoiceValidationException;
import dev.mam.buizsol.mamshop.billing.model.Invoice;
import dev.mam.buizsol.mamshop.billing.service.BillingService;
import dev.mam.buizsol.mamshop.billing.validation.InvoiceDiscount;
import dev.mam.buizsol.mamshop.contract.exception.BrandMismatchException;
import dev.mam.buizsol.mamshop.contract.exception.ContractValidationException;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import dev.mam.buizsol.mamshop.contract.service.ContractService;
import dev.mam.buizsol.mamshop.contract.validation.BrandMatch;
import dev.mam.buizsol.mamshop.customer.exception.CustomerNotActiveException;
import dev.mam.buizsol.mamshop.customer.exception.CustomerValidationException;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.customer.service.CustomerService;
import dev.mam.buizsol.mamshop.product.exception.ProductValidationException;
import dev.mam.buizsol.mamshop.product.model.Product;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.metadata.ConstraintDescriptor;
import java.lang.annotation.Annotation;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

@DisplayName("ValidationExceptionHandler Aspect Tests")
class ValidationExceptionHandlerTest {

    private ValidationExceptionHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new ValidationExceptionHandler();
    }

    private @interface ActiveCustomer {}

    @Test
    @DisplayName("Should map ActiveCustomer violation to CustomerNotActiveException")
    void shouldHandleActiveCustomerViolation() {
        ConstraintViolationException ex = createException(ActiveCustomer.class, "Message", Customer.class);
        assertThrows(CustomerNotActiveException.class, () -> handler.handleValidationException(ex));
    }

    @Test
    @DisplayName("Should map InvoiceDiscount violation to InvalidInvoiceDiscountException")
    void shouldHandleInvoiceDiscountViolation() {
        ConstraintViolationException ex = createException(InvoiceDiscount.class, "Message", Invoice.class);
        assertThrows(InvalidInvoiceDiscountException.class, () -> handler.handleValidationException(ex));
    }

    @Test
    @DisplayName("Should map BrandMatch violation to BrandMismatchException")
    void shouldHandleBrandMatchViolation() {
        ConstraintViolationException ex = createException(BrandMatch.class, "Message", ContractService.class);
        assertThrows(BrandMismatchException.class, () -> handler.handleValidationException(ex));
    }

    @Test
    @DisplayName("Should map general billing package violation to InvoiceValidationException")
    void shouldHandleGeneralBillingViolation() {
        ConstraintViolationException ex = createException(NotNull.class, "Message", BillingService.class);
        assertThrows(InvoiceValidationException.class, () -> handler.handleValidationException(ex));
    }

    @Test
    @DisplayName("Should map general product package violation to ProductValidationException")
    void shouldHandleGeneralProductViolation() {
        ConstraintViolationException ex = createException(NotNull.class, "Message", Product.class);
        assertThrows(ProductValidationException.class, () -> handler.handleValidationException(ex));
    }

    @Test
    @DisplayName("Should map general contract package violation to ContractValidationException")
    void shouldHandleGeneralContractViolation() {
        ConstraintViolationException ex = createException(NotNull.class, "Message", Contract.class);
        assertThrows(ContractValidationException.class, () -> handler.handleValidationException(ex));
    }

    @Test
    @DisplayName("Should map general customer package violation to CustomerValidationException")
    void shouldHandleGeneralCustomerViolation() {
        ConstraintViolationException ex = createException(NotNull.class, "Message", CustomerService.class);
        assertThrows(CustomerValidationException.class, () -> handler.handleValidationException(ex));
    }

    private ConstraintViolationException createException(
            Class<? extends Annotation> annotationType, String message, Class<?> rootBeanClass) {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
        Annotation annotation = mock(Annotation.class);

        when(violation.getMessage()).thenReturn(message);
        when(violation.getConstraintDescriptor()).thenReturn(descriptor);
        when(descriptor.getAnnotation()).thenReturn(annotation);
        when(annotation.annotationType()).thenReturn((Class) annotationType);
        when(violation.getRootBeanClass()).thenReturn((Class) rootBeanClass);

        return new ConstraintViolationException(Set.of(violation));
    }

    @Test
    @DisplayName("Should rethrow original exception if no package/annotation mapping matches (fallback branch)")
    void shouldRethrowOriginalExceptionWhenNoMappingMatches() {
        ConstraintViolationException ex = createException(NotNull.class, "Fallback", String.class);

        assertThrows(ConstraintViolationException.class, () -> handler.handleValidationException(ex));
    }

    @Test
    @DisplayName("Should rethrow original exception if violations are null or empty")
    void shouldRethrowExIfViolationsAreEmpty() {
        ConstraintViolationException ex = new ConstraintViolationException(null);
        assertThrows(ConstraintViolationException.class, () -> handler.handleValidationException(ex));

        ConstraintViolationException ex2 = new ConstraintViolationException(java.util.Collections.emptySet());
        assertThrows(ConstraintViolationException.class, () -> handler.handleValidationException(ex2));
    }
}
