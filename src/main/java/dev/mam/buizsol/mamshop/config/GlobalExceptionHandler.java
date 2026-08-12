package dev.mam.buizsol.mamshop.config;

import dev.mam.buizsol.mamshop.billing.exception.InvalidInvoiceDiscountException;
import dev.mam.buizsol.mamshop.billing.exception.InvoiceValidationException;
import dev.mam.buizsol.mamshop.contract.exception.BrandMismatchException;
import dev.mam.buizsol.mamshop.contract.exception.ContractNotFoundException;
import dev.mam.buizsol.mamshop.contract.exception.ContractValidationException;
import dev.mam.buizsol.mamshop.customer.exception.CustomerNotActiveException;
import dev.mam.buizsol.mamshop.customer.exception.CustomerNotFoundException;
import dev.mam.buizsol.mamshop.customer.exception.CustomerValidationException;
import dev.mam.buizsol.mamshop.product.exception.ProductNotFoundException;
import dev.mam.buizsol.mamshop.product.exception.ProductValidationException;
import io.sentry.Sentry;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private String getCorrelationId() {
        return Optional.ofNullable(MDC.get("correlationId")).orElse("no-id");
    }

    private void captureSentry(Throwable ex) {
        log.info("captureSentry called for exception: {}, Sentry.isEnabled: {}", ex.getMessage(), Sentry.isEnabled());
        Sentry.withScope(scope -> {
            scope.setTag("correlationId", getCorrelationId());
            Sentry.captureException(ex);
        });
        Sentry.flush(2000);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFoundException(CustomerNotFoundException ex) {
        log.error("Customer not found for correlationId: {}", getCorrelationId(), ex);
        captureSentry(ex);
        return ResponseEntity.status(404)
                .body(new ErrorResponse(
                        getCorrelationId(), "CUSTOMER_NOT_FOUND", "Customer not found", LocalDateTime.now()));
    }

    @ExceptionHandler(CustomerNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotActiveException(CustomerNotActiveException ex) {
        log.error("Customer is not active for correlationId: {}", getCorrelationId(), ex);
        captureSentry(ex);
        return ResponseEntity.status(409)
                .body(new ErrorResponse(
                        getCorrelationId(), "CUSTOMER_NOT_ACTIVE", "Customer is not active", LocalDateTime.now()));
    }

    @ExceptionHandler(CustomerValidationException.class)
    public ResponseEntity<ErrorResponse> handleCustomerValidationException(CustomerValidationException ex) {
        log.error("Customer validation failed for correlationId: {}: {}", getCorrelationId(), ex.getMessage(), ex);
        captureSentry(ex);
        return ResponseEntity.status(400)
                .body(new ErrorResponse(
                        getCorrelationId(),
                        "CUSTOMER_VALIDATION_ERROR",
                        "Customer validation failed: " + ex.getMessage(),
                        LocalDateTime.now()));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(ProductNotFoundException ex) {
        log.error("Product not found for correlationId: {}", getCorrelationId(), ex);
        captureSentry(ex);
        return ResponseEntity.status(404)
                .body(new ErrorResponse(
                        getCorrelationId(), "PRODUCT_NOT_FOUND", "Product not found", LocalDateTime.now()));
    }

    @ExceptionHandler(ProductValidationException.class)
    public ResponseEntity<ErrorResponse> handleProductValidationException(ProductValidationException ex) {
        log.error("Product validation failed for correlationId: {}: {}", getCorrelationId(), ex.getMessage(), ex);
        captureSentry(ex);
        return ResponseEntity.status(400)
                .body(new ErrorResponse(
                        getCorrelationId(),
                        "PRODUCT_VALIDATION_ERROR",
                        "Product validation failed: " + ex.getMessage(),
                        LocalDateTime.now()));
    }

    @ExceptionHandler(ContractNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleContractNotFoundException(ContractNotFoundException ex) {
        log.error("Contract not found for correlationId: {}", getCorrelationId(), ex);
        captureSentry(ex);
        return ResponseEntity.status(404)
                .body(new ErrorResponse(
                        getCorrelationId(), "CONTRACT_NOT_FOUND", "Contract not found", LocalDateTime.now()));
    }

    @ExceptionHandler(ContractValidationException.class)
    public ResponseEntity<ErrorResponse> handleContractValidationException(ContractValidationException ex) {
        log.error("Contract validation failed for correlationId: {}: {}", getCorrelationId(), ex.getMessage(), ex);
        captureSentry(ex);
        return ResponseEntity.status(400)
                .body(new ErrorResponse(
                        getCorrelationId(),
                        "CONTRACT_VALIDATION_ERROR",
                        "Contract validation failed: " + ex.getMessage(),
                        LocalDateTime.now()));
    }

    @ExceptionHandler(BrandMismatchException.class)
    public ResponseEntity<ErrorResponse> handleBrandMismatchException(BrandMismatchException ex) {
        log.error("Brand mismatch for correlationId: {}", getCorrelationId(), ex);
        captureSentry(ex);
        return ResponseEntity.status(422)
                .body(new ErrorResponse(getCorrelationId(), "BRAND_MISMATCH", "Brand mismatch", LocalDateTime.now()));
    }

    @ExceptionHandler(InvalidInvoiceDiscountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInvoiceDiscountException(InvalidInvoiceDiscountException ex) {
        log.error("Invalid invoice discount for correlationId: {}", getCorrelationId(), ex);
        captureSentry(ex);
        return ResponseEntity.status(400)
                .body(new ErrorResponse(
                        getCorrelationId(), "INVALID_DISCOUNT", "Invalid invoice discount", LocalDateTime.now()));
    }

    @ExceptionHandler(InvoiceValidationException.class)
    public ResponseEntity<ErrorResponse> handleInvoiceValidationException(InvoiceValidationException ex) {
        log.error("Invoice validation failed for correlationId: {}: {}", getCorrelationId(), ex.getMessage(), ex);
        captureSentry(ex);
        return ResponseEntity.status(400)
                .body(new ErrorResponse(
                        getCorrelationId(),
                        "INVOICE_VALIDATION_ERROR",
                        "Invoice validation failed: " + ex.getMessage(),
                        LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("Request validation failed for correlationId: {}", getCorrelationId(), ex);
        captureSentry(ex);
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining(", "));
        return ResponseEntity.status(400)
                .body(new ErrorResponse(
                        getCorrelationId(),
                        "REQUEST_VALIDATION_ERROR",
                        "Request validation failed: " + details,
                        LocalDateTime.now()));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        log.error("Request validation failed for correlationId: {}", getCorrelationId(), ex);
        captureSentry(ex);
        String details = ex.getValueResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(error -> error.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining(", "));
        return ResponseEntity.status(400)
                .body(new ErrorResponse(
                        getCorrelationId(),
                        "REQUEST_VALIDATION_ERROR",
                        "Request validation failed: " + details,
                        LocalDateTime.now()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("Constraint violation for correlationId: {}", getCorrelationId(), ex);
        captureSentry(ex);
        String details = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(java.util.stream.Collectors.joining(", "));
        return ResponseEntity.status(400)
                .body(new ErrorResponse(
                        getCorrelationId(),
                        "CONSTRAINT_VIOLATION",
                        "Constraint violation: " + details,
                        LocalDateTime.now()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex) {
        log.error("Missing servlet request parameter for correlationId: {}", getCorrelationId(), ex);
        captureSentry(ex);
        return ResponseEntity.status(400)
                .body(new ErrorResponse(
                        getCorrelationId(),
                        "MISSING_PARAMETER",
                        "Missing servlet request parameter",
                        LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        log.error("Type mismatch for correlationId: {}", getCorrelationId(), ex);
        captureSentry(ex);
        return ResponseEntity.status(400)
                .body(new ErrorResponse(getCorrelationId(), "TYPE_MISMATCH", "Type mismatch", LocalDateTime.now()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex) {
        log.error("Resource not found for correlationId: {}", getCorrelationId(), ex);
        captureSentry(ex);
        return ResponseEntity.status(404)
                .body(new ErrorResponse(
                        getCorrelationId(), "RESOURCE_NOT_FOUND", "Resource not found", LocalDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Unhandled exception occurred for correlationId: {}", getCorrelationId(), ex);
        captureSentry(ex);
        return ResponseEntity.status(500)
                .body(new ErrorResponse(
                        getCorrelationId(), "INTERNAL_ERROR", "An unexpected error occurred", LocalDateTime.now()));
    }
}
