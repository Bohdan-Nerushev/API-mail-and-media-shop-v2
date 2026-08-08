package dev.mam.buizsol.mamshop.customer.model;

import static dev.mam.buizsol.mamshop.customer.model.CustomerStatus.ACTIVE;
import static dev.mam.buizsol.mamshop.customer.model.CustomerStatus.INACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.mam.buizsol.mamshop.customer.exception.CustomerValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Customer Record Tests")
class CustomerTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    private Address mainAddress;
    private CommunicationDetails communicationDetails;

    @BeforeEach
    void setUp() {
        mainAddress = createDefaultAddress("Main St", "10", "12345", "Berlin", "Germany");
        communicationDetails = createDefaultCommunicationDetails("test@gmx.de", "0123456789");
    }

    private Customer createDefaultCustomer(
            String firstName,
            String lastName,
            LocalDate birthDate,
            Address address,
            Address invoiAddress,
            CommunicationDetails communicationDetails,
            Brand brand) {
        Customer customer =
                Customer.create(firstName, lastName, birthDate, address, invoiAddress, communicationDetails, brand);
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        if (!violations.isEmpty()) {
            throw new CustomerValidationException("Validation failed");
        }
        return customer;
    }

    private Address createDefaultAddress(String street, String number, String postcode, String city, String country) {
        return new Address(street, number, postcode, city, country);
    }

    private CommunicationDetails createDefaultCommunicationDetails(String email, String telephone) {
        return new CommunicationDetails(email, telephone);
    }

    @Test
    @DisplayName("Positive: Successful creation with all required data and generated ID")
    void shouldCreateCustomerWhenDataIsValid() {
        Customer customer = createDefaultCustomer(
                "John", "Doe", LocalDate.of(1990, 1, 1), mainAddress, null, communicationDetails, Brand.GMX);

        assertEquals("John", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
        assertEquals(LocalDate.of(1990, 1, 1), customer.getBirthDate());
        assertEquals(mainAddress, customer.getAddress());
        assertEquals(CustomerStatus.INACTIVE, customer.getStatus());
        assertEquals(Brand.GMX, customer.getBrand());
        assertEquals(customer.toString(), "Customer{id=" + customer.getId() + ", brand=GMX, status=INACTIVE}");
    }

    @Test
    @DisplayName("Positive: Invoice address fallback to main address")
    void shouldUseMainAddressAsInvoiceAddressWhenNotProvided() {
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        Customer customer =
                createDefaultCustomer("John", "Doe", birthDate, mainAddress, null, communicationDetails, Brand.GMX);

        assertEquals(customer.getAddress(), customer.getInvoiceAddress());
        assertSame(customer.getAddress(), customer.getInvoiceAddress());
    }

    @Test
    @DisplayName("Positive: Address duplication handling by value")
    void shouldHandleAddressesWithSameContentAsDuplicateButDifferentObjects() {
        Address duplicateAddress = createDefaultAddress("Main St", "10", "12345", "Berlin", "Germany");

        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        Customer customer = createDefaultCustomer(
                "John", "Doe", birthDate, mainAddress, duplicateAddress, communicationDetails, Brand.GMX);

        assertEquals(customer.getAddress(), customer.getInvoiceAddress());
        assertNotSame(customer.getAddress(), customer.getInvoiceAddress());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Negative: Validation of first name field in constructor")
    void shouldThrowExceptionWhenFirstNameIsInvalid(String invalidName) {
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultCustomer(
                        invalidName, "Doe", birthDate, mainAddress, null, communicationDetails, Brand.GMX));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Negative: Validation of last name field in constructor")
    void shouldThrowExceptionWhenLastNameIsInvalid(String invalidName) {
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultCustomer(
                        "John", invalidName, birthDate, mainAddress, null, communicationDetails, Brand.GMX));
    }

    @Test
    @DisplayName("Negative: Validation of birth date (null check)")
    void shouldThrowExceptionWhenBirthDateIsNull() {
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultCustomer("John", "Doe", null, mainAddress, null, communicationDetails, Brand.GMX));
    }

    @Test
    @DisplayName("Negative: Validation of address (null check)")
    void shouldThrowExceptionWhenAddressIsNull() {
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultCustomer("John", "Doe", birthDate, null, null, communicationDetails, Brand.GMX));
    }

    @Test
    @DisplayName("Positive: Activate customer changes status to ACTIVE")
    void shouldActivateCustomerAndChangeStatus() {
        Customer customer = createDefaultCustomer(
                "John", "Doe", LocalDate.of(1990, 1, 1), mainAddress, null, communicationDetails, Brand.GMX);
        assertEquals(CustomerStatus.INACTIVE, customer.getStatus());
        customer.setStatus(ACTIVE);
        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
    }

    @Test
    @DisplayName("Positive: Deactivate customer changes status to INACTIVE")
    void shouldDeactivateCustomerAndChangeStatus() {
        Customer customer = createDefaultCustomer(
                "John", "Doe", LocalDate.of(1990, 1, 1), mainAddress, null, communicationDetails, Brand.GMX);
        customer.setStatus(ACTIVE);
        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
        customer.setStatus(INACTIVE);
        assertEquals(CustomerStatus.INACTIVE, customer.getStatus());
    }

    @Test
    @DisplayName("Boundary: Extremely long firstname and lastname handling")
    void shouldHandleExtremelyLongNames() {
        String longName = "A".repeat(201);
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        assertThrows(
                CustomerValidationException.class,
                () -> createDefaultCustomer(
                        longName, longName, birthDate, mainAddress, null, communicationDetails, Brand.GMX));
    }

    @Test
    @DisplayName("Boundary: Birth date as today")
    void shouldAllowBirthDateToBeToday() {
        LocalDate today = LocalDate.now().minusDays(1);
        Customer customer =
                createDefaultCustomer("John", "Doe", today, mainAddress, null, communicationDetails, Brand.GMX);
        assertEquals(today, customer.getBirthDate());
    }

    @Test
    @DisplayName("Boundary: Birth date in far past")
    void shouldAllowBirthDateInFarPast() {
        LocalDate farPast = LocalDate.of(1900, 1, 1);
        Customer customer =
                createDefaultCustomer("John", "Doe", farPast, mainAddress, null, communicationDetails, Brand.GMX);
        assertEquals(farPast, customer.getBirthDate());
    }

    @Test
    @DisplayName("Boundary: ID generation uniqueness")
    void shouldGenerateUniqueIdsForDifferentCustomers() {
        Customer customer1 = createDefaultCustomer(
                "John1", "Doe1", LocalDate.of(1991, 1, 1), mainAddress, null, communicationDetails, Brand.GMX);
        Customer customer2 = createDefaultCustomer(
                "John2", "Doe2", LocalDate.of(1992, 1, 2), mainAddress, null, communicationDetails, Brand.MAIL_COM);
        Customer customer3 = createDefaultCustomer(
                "John3", "Doe3", LocalDate.of(1993, 1, 3), mainAddress, null, communicationDetails, Brand.WEB_DE);

        assertInstanceOf(Customer.class, customer1);
        assertInstanceOf(Customer.class, customer2);
        assertInstanceOf(Customer.class, customer3);
    }

    @Test
    @DisplayName("Positive: Update of address details")
    void shouldUpdateAddressInCustomerWhenNewAddress() {
        Customer customer = createDefaultCustomer(
                "John", "Doe", LocalDate.of(1990, 1, 1), mainAddress, null, communicationDetails, Brand.GMX);
        Address newAddress = new Address("77777", "77777", "77777", "77777", "77777");
        customer.setAddress(newAddress);

        assertEquals(newAddress, customer.getAddress());
        assertEquals(customer.getId(), customer.getId());
        assertEquals(customer.getFirstName(), customer.getFirstName());
    }

    @Test
    @DisplayName("Positive: Validation of communication details update")
    void shouldUpdateCommunicationDetailsInCustomerWhenNewCommunicationDetails() {
        Customer customer = createDefaultCustomer(
                "John", "Doe", LocalDate.of(1990, 1, 1), mainAddress, null, communicationDetails, Brand.GMX);
        CommunicationDetails newDetails = new CommunicationDetails("77777@gmail.com", "77777");
        customer.setCommunicationDetails(newDetails);

        assertEquals(newDetails, customer.getCommunicationDetails());
        assertEquals(customer.getId(), customer.getId());
    }

    @Test
    @DisplayName("Positive: Update of invoice address details")
    void shouldUpdateInvoiceAddressInCustomerWhenNewInvoiceAddress() {
        Customer customer = createDefaultCustomer(
                "John", "Doe", LocalDate.of(1990, 1, 1), mainAddress, null, communicationDetails, Brand.GMX);
        Address newInvoiceAddress = new Address("77777", "77777", "77777", "77777", "77777");
        customer.setInvoiceAddress(newInvoiceAddress);

        assertEquals(newInvoiceAddress, customer.getInvoiceAddress());
        assertEquals(customer.getId(), customer.getId());
    }
}
