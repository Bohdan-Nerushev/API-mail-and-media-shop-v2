package dev.mam.buizsol.mamshop.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

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
import jakarta.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        MDC.clear();
    }

    @Test
    @DisplayName("Should use correlationId from MDC when present")
    void shouldReturnCorrelationIdFromMdc() {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);

        ResponseEntity<ErrorResponse> response = handler.handleException(new Exception("Error"));
        assertEquals(correlationId, response.getBody().correlationId());
    }

    @Test
    @DisplayName("Should use 'no-id' when correlationId is missing from MDC")
    void shouldReturnNoIdWhenMdcIsEmpty() {
        ResponseEntity<ErrorResponse> response = handler.handleException(new Exception("Error"));
        assertEquals("no-id", response.getBody().correlationId());
    }

    @Test
    @DisplayName("Should handle CustomerNotFoundException (404)")
    void shouldHandleCustomerNotFoundException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleCustomerNotFoundException(new CustomerNotFoundException("Not found"));
        assertEquals(404, response.getStatusCode().value());
        assertEquals("CUSTOMER_NOT_FOUND", response.getBody().errorCode());
    }

    @Test
    @DisplayName("Should handle CustomerNotActiveException (409)")
    void shouldHandleCustomerNotActiveException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleCustomerNotActiveException(new CustomerNotActiveException("Inactive"));
        assertEquals(409, response.getStatusCode().value());
        assertEquals("CUSTOMER_NOT_ACTIVE", response.getBody().errorCode());
    }

    @Test
    @DisplayName("Should handle CustomerValidationException (400)")
    void shouldHandleCustomerValidationException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleCustomerValidationException(new CustomerValidationException("Invalid"));
        assertEquals(400, response.getStatusCode().value());
        assertEquals("CUSTOMER_VALIDATION_ERROR", response.getBody().errorCode());
    }

    @Test
    @DisplayName("Should handle ProductNotFoundException (404)")
    void shouldHandleProductNotFoundException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleProductNotFoundException(new ProductNotFoundException("Not found"));
        assertEquals(404, response.getStatusCode().value());
        assertEquals("PRODUCT_NOT_FOUND", response.getBody().errorCode());
    }

    @Test
    @DisplayName("Should handle ProductValidationException (400)")
    void shouldHandleProductValidationException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleProductValidationException(new ProductValidationException("Invalid"));
        assertEquals(400, response.getStatusCode().value());
        assertEquals("PRODUCT_VALIDATION_ERROR", response.getBody().errorCode());
    }

    @Test
    @DisplayName("Should handle ContractNotFoundException (404)")
    void shouldHandleContractNotFoundException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleContractNotFoundException(new ContractNotFoundException("Not found"));
        assertEquals(404, response.getStatusCode().value());
        assertEquals("CONTRACT_NOT_FOUND", response.getBody().errorCode());
    }

    @Test
    @DisplayName("Should handle ContractValidationException (400)")
    void shouldHandleContractValidationException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleContractValidationException(new ContractValidationException("Invalid"));
        assertEquals(400, response.getStatusCode().value());
        assertEquals("CONTRACT_VALIDATION_ERROR", response.getBody().errorCode());
    }

    @Test
    @DisplayName("Should handle BrandMismatchException (422)")
    void shouldHandleBrandMismatchException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBrandMismatchException(new BrandMismatchException("Mismatch"));
        assertEquals(422, response.getStatusCode().value());
        assertEquals("BRAND_MISMATCH", response.getBody().errorCode());
    }

    @Test
    @DisplayName("Should handle InvalidInvoiceDiscountException (400)")
    void shouldHandleInvalidInvoiceDiscountException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidInvoiceDiscountException(new InvalidInvoiceDiscountException("Invalid"));
        assertEquals(400, response.getStatusCode().value());
        assertEquals("INVALID_DISCOUNT", response.getBody().errorCode());
    }

    @Test
    @DisplayName("Should handle InvoiceValidationException (400)")
    void shouldHandleInvoiceValidationException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleInvoiceValidationException(new InvoiceValidationException("Invalid"));
        assertEquals(400, response.getStatusCode().value());
        assertEquals("INVOICE_VALIDATION_ERROR", response.getBody().errorCode());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException (400)")
    void shouldHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValidException(ex);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("REQUEST_VALIDATION_ERROR", response.getBody().errorCode());
    }

    @Test
    @DisplayName("Should handle MissingServletRequestParameterException (400)")
    void shouldHandleMissingServletRequestParameterException() {
        MissingServletRequestParameterException ex = mock(MissingServletRequestParameterException.class);
        ResponseEntity<ErrorResponse> response = handler.handleMissingServletRequestParameterException(ex);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("MISSING_PARAMETER", response.getBody().errorCode());
    }

    @Test
    @DisplayName("Should handle MethodArgumentTypeMismatchException (400)")
    void shouldHandleMethodArgumentTypeMismatchException() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentTypeMismatchException(ex);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("TYPE_MISMATCH", response.getBody().errorCode());
    }

    @Test
    @DisplayName("Should handle NoResourceFoundException (404)")
    void shouldHandleNoResourceFoundException() {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/api/resource", "Not found");
        ResponseEntity<ErrorResponse> response = handler.handleNoResource(ex);
        assertEquals(404, response.getStatusCode().value());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().errorCode());
    }

    @Test
    @DisplayName("Should handle ConstraintViolationException (400)")
    void shouldHandleConstraintViolationException() {
        ConstraintViolationException ex = new ConstraintViolationException(Collections.emptySet());
        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolationException(ex);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("CONSTRAINT_VIOLATION", response.getBody().errorCode());
    }

    @Test
    @DisplayName("Should handle generic Exception (500)")
    void shouldHandleGenericException() {
        ResponseEntity<ErrorResponse> response = handler.handleException(new Exception("Error"));
        assertEquals(500, response.getStatusCode().value());
        assertEquals("INTERNAL_ERROR", response.getBody().errorCode());
    }
}
