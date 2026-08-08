package dev.mam.buizsol.mamshop.customer.model;

import dev.mam.buizsol.mamshop.customer.exception.CustomerValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Address Tests")
class AddressTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    private Address createDefaultAddress(String street, String number, String postcode, String city, String country) {
        Address address = new Address(street, number, postcode, city, country);
        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        if (!violations.isEmpty()) {
            throw new CustomerValidationException("Validation failed");
        }
        return address;
    }

    @Test
    @DisplayName("Positive: Successful creation with valid data")
    void shouldCreateAddressInstanceWhenDataIsValid() {
        Address address = createDefaultAddress("Main St", "10", "12345", "Berlin", "Germany");
        assertEquals("Main St", address.getStreet());
        assertEquals("10", address.getNumber());
        assertEquals("12345", address.getPostcode());
        assertEquals("Berlin", address.getCity());
        assertEquals("Germany", address.getCountry());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Negative: Validation of 'street' field")
    void shouldThrowExceptionWhenStreetIsInvalid(String invalidStreet) {
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultAddress(invalidStreet, "10", "12345", "Berlin", "Germany"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Negative: Validation of 'number' field")
    void shouldThrowExceptionWhenNumberIsInvalid(String invalidNumber) {
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultAddress("Main St", invalidNumber, "12345", "Berlin", "Germany"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Negative: Validation of 'postcode' field")
    void shouldThrowExceptionWhenPostcodeIsInvalid(String invalidPostcode) {
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultAddress("Main St", "10", invalidPostcode, "Berlin", "Germany"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Negative: Validation of 'city' field")
    void shouldThrowExceptionWhenCityIsInvalid(String invalidCity) {
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultAddress("Main St", "10", "12345", invalidCity, "Germany"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Negative: Validation of 'country' field")
    void shouldThrowExceptionWhenCountryIsInvalid(String invalidCountry) {
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultAddress("Main St", "10", "12345", "Berlin", invalidCountry));
    }

    @Test
    @DisplayName("Boundary: Extremely long string for street")
    void shouldHandleExtremelyLongStreet() {
        String longStreet = "A".repeat(200);
        Address address = createDefaultAddress(longStreet, "1", "12345", "Berlin", "Germany");
        assertEquals(longStreet, address.getStreet());
    }

    @Test
    @DisplayName("Negative: Validation of 'street' field")
    void shouldThrowExceptionWhenStreetIsTooLong() {
        String longStreet = "A".repeat(251);
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultAddress(longStreet, "1", "12345", "Berlin", "Germany"));
    }

    @Test
    @DisplayName("Negative: Validation of 'number' field")
    void shouldThrowExceptionWhenNumberIsTooLong() {
        String longNumber = "A".repeat(101);
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultAddress("Main St", longNumber, "12345", "Berlin", "Germany"));
    }

    @Test
    @DisplayName("Negative: Validation of 'postcode' field")
    void shouldThrowExceptionWhenPostcodeIsTooLong() {
        String longPostcode = "A".repeat(101);
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultAddress("Main St", "10", longPostcode, "Berlin", "Germany"));
    }

    @Test
    @DisplayName("Negative: Validation of 'city' field")
    void shouldThrowExceptionWhenCityIsTooLong() {
        String longCity = "A".repeat(101);
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultAddress("Main St", "10", "12345", longCity, "Germany"));
    }

    @Test
    @DisplayName("Negative: Validation of 'country' field")
    void shouldThrowExceptionWhenCountryIsTooLong() {
        String longCountry = "A".repeat(101);
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultAddress("Main St", "10", "12345", "Berlin", longCountry));
    }

    @Test
    @DisplayName("Negative: Validation of 'street' field")
    void shouldThrowExceptionWhenStreetIsTooShort() {
        String shortStreet = "A".repeat(0);
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultAddress(shortStreet, "1", "12345", "Berlin", "Germany"));
    }

    @Test
    @DisplayName("Negative: Validation of 'number' field")
    void shouldThrowExceptionWhenNumberIsTooShort() {
        String shortNumber = "A".repeat(0);
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultAddress("Main St", shortNumber, "12345", "Berlin", "Germany"));
    }

    @Test
    @DisplayName("Negative: Validation of 'postcode' field")
    void shouldThrowExceptionWhenPostcodeIsTooShort() {
        String shortPostcode = "A".repeat(0);
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultAddress("Main St", "10", shortPostcode, "Berlin", "Germany"));
    }

    @Test
    @DisplayName("Negative: Validation of 'city' field")
    void shouldThrowExceptionWhenCityIsTooShort() {
        String shortCity = "A".repeat(0);
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultAddress("Main St", "10", "12345", shortCity, "Germany"));
    }

    @Test
    @DisplayName("Negative: Validation of 'country' field")
    void shouldThrowExceptionWhenCountryIsTooShort() {
        String shortCountry = "A".repeat(0);
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultAddress("Main St", "10", "12345", "Berlin", shortCountry));
    }

    @Test
    @DisplayName("Boundary: Minimal valid input (single characters)")
    void shouldHandleSingleCharacterInputs() {
        Address address = createDefaultAddress("S", "1", "1", "B", "Gt");
        assertEquals("S", address.getStreet());
        assertEquals("1", address.getNumber());
        assertEquals("1", address.getPostcode());
        assertEquals("B", address.getCity());
        assertEquals("Gt", address.getCountry());
    }

    @Test
    @DisplayName("Positive: Equals and HashCode consistency")
    void shouldBeEqualWhenContentIsSame() {
        Address address1 = createDefaultAddress("St", "1", "123", "City", "Country");
        Address address2 = createDefaultAddress("St", "1", "123", "City", "Country");

        assertEquals(address1, address2);
        assertEquals(address1.hashCode(), address2.hashCode());
    }

    @Test
    @DisplayName("Positive: Inequality check")
    void shouldNotBeEqualWhenContentIsDifferent() {
        Address address1 = createDefaultAddress("St1", "1", "123", "City", "Country");
        Address address2 = createDefaultAddress("St2", "1", "123", "City", "Country");

        assertNotEquals(address1, address2);
    }
}
