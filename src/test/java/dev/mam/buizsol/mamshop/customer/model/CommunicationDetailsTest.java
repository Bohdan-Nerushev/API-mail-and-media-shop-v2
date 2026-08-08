package dev.mam.buizsol.mamshop.customer.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.mam.buizsol.mamshop.customer.exception.CustomerValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("CommunicationDetails Tests")
class CommunicationDetailsTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    private CommunicationDetails createDefaultCommunicationDetails(String email, String telephone) {
        CommunicationDetails details = new CommunicationDetails(email, telephone);
        Set<ConstraintViolation<CommunicationDetails>> violations = validator.validate(details);
        if (!violations.isEmpty()) {
            throw new CustomerValidationException("Validation failed");
        }
        return details;
    }

    @Test
    @DisplayName("Positive: Successful creation with valid data")
    void shouldCreateCommunicationDetailsWhenDataIsValid() {
        CommunicationDetails details = createDefaultCommunicationDetails("test@example.com", "+123456789");
        assertEquals("test@example.com", details.getEmail());
        assertEquals("+123456789", details.getTelephone());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Negative: Validation of 'email' field")
    void shouldThrowExceptionWhenEmailIsInvalid(String invalidEmail) {
        assertThrows(
                CustomerValidationException.class, () -> createDefaultCommunicationDetails(invalidEmail, "+123456789"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Negative: Validation of 'telephone' field")
    void shouldThrowExceptionWhenTelephoneIsInvalid(String invalidPhone) {
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultCommunicationDetails("test@example.com", invalidPhone));
    }

    @Test
    @DisplayName("Boundary: Extremely long email string")
    void shouldHandleExtremelyLongEmail() {
        String longEmail = "a".repeat(60) + "@" + "a".repeat(60) + ".com";
        CommunicationDetails details = createDefaultCommunicationDetails(longEmail, "12345678901234567890");
        assertEquals(longEmail, details.getEmail());
    }

    @Test
    @DisplayName("Boundary: Shortest valid phone number")
    void shouldHandleShortTelephone() {
        CommunicationDetails details = createDefaultCommunicationDetails("av@b.c", "01234567890123456789");
        assertEquals("01234567890123456789", details.getTelephone());
    }

    @ParameterizedTest
    @ValueSource(strings = {"user@domain.com", "user.name@sub.domain.org", "123@456.789"})
    @DisplayName("Positive: Support for various email formats")
    void shouldHandleVariousEmailFormats(String email) {
        CommunicationDetails details = createDefaultCommunicationDetails(email, "12345");
        assertEquals(email, details.getEmail());
    }

    @ParameterizedTest
    @ValueSource(strings = {"+4912345678", "004912345678", "12377", "0151-1234567"})
    @DisplayName("Positive: Support for various telephone formats")
    void shouldHandleVariousTelephoneFormats(String phone) {
        CommunicationDetails details = createDefaultCommunicationDetails("test@test.com", phone);
        assertEquals(phone, details.getTelephone());
    }
}
