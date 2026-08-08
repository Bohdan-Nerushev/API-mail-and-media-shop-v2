package dev.mam.buizsol.mamshop.customer.service;

import dev.mam.buizsol.mamshop.customer.exception.CustomerNotFoundException;
import dev.mam.buizsol.mamshop.customer.model.Address;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.customer.model.CommunicationDetails;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.customer.model.CustomerStatus;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationInterceptor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Tests")
class CustomerServiceTest {

    public Customer createDefaultCustomer(
            final String firstName,
            final String lastName,
            final LocalDate birthDate,
            final Address address,
            final Address invoiceAddress,
            final CommunicationDetails communicationDetails,
            final Brand brand) {
        return Customer.create(firstName, lastName, birthDate, address, invoiceAddress, communicationDetails, brand);
    }

    private Address address;

    private CommunicationDetails communicationDetails;

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        address = new Address("Street", "1", "12345", "City", "Country");
        communicationDetails = new CommunicationDetails("john.doe@example.com", "123456789");

        final CustomerServiceImpl target = new CustomerServiceImpl(customerRepository);

        final LocalValidatorFactoryBean validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.afterPropertiesSet();

        final ProxyFactory factory = new ProxyFactory();
        factory.setTarget(target);
        factory.addInterface(CustomerService.class);
        factory.addAdvice(new MethodValidationInterceptor(validatorFactory.getValidator()));

        customerService = (CustomerService) factory.getProxy();

        lenient().when(customerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Create customer with valid data should succeed")
    void shouldCreateCustomerWhenDataIsValid() {
        final Customer customer = createDefaultCustomer(
                "John", "Doe", LocalDate.of(1990, 1, 1), address, null, communicationDetails, Brand.GMX);

        final Customer created = customerService.createCustomer(customer);

        assertNotNull(created);
        assertEquals("John", created.getFirstName());
        assertEquals(CustomerStatus.INACTIVE, created.getStatus());
        verify(customerRepository).save(any(Customer.class));
    }

    @ParameterizedTest
    @EnumSource(Brand.class)
    @DisplayName("Create customer with different brands should succeed")
    void shouldCreateCustomerWithDifferentBrands(final Brand brand) {
        final Customer customer = customerService.createCustomer(createDefaultCustomer(
                "John", "Doe", LocalDate.of(1990, 1, 1), address, null, communicationDetails, brand));

        assertEquals(brand, customer.getBrand());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("Negative: createCustomer throws ConstraintViolationException when customer argument is null")
    void shouldThrowConstraintViolationWhenCustomerIsNull() {
        assertThrows(ConstraintViolationException.class, () -> customerService.createCustomer(null));
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Negative: createCustomer throws ConstraintViolationException when first name is blank or empty")
    void shouldThrowConstraintViolationWhenFirstNameIsBlankOrEmpty(final String firstName) {
        final Customer customer = createDefaultCustomer(
                firstName, "Doe", LocalDate.of(1990, 1, 1), address, null, communicationDetails, Brand.WEB_DE);
        assertThrows(ConstraintViolationException.class, () -> customerService.createCustomer(customer));
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Negative: createCustomer throws ConstraintViolationException when last name is blank or empty")
    void shouldThrowConstraintViolationWhenLastNameIsBlankOrEmpty(final String lastName) {
        final Customer customer = createDefaultCustomer(
                "John", lastName, LocalDate.of(1990, 1, 1), address, null, communicationDetails, Brand.WEB_DE);
        assertThrows(ConstraintViolationException.class, () -> customerService.createCustomer(customer));
    }

    @ParameterizedTest
    @CsvSource({
            "1900-01-01, Boundary: very old birth date",
            "2026-01-14, Boundary: today birth date",
            "2024-05-14, Boundary: future birth date",
            "2010-04-14, Boundary: future birth1 date",
            "2010-04-14, Boundary: future birth1 date",
            "1000-01-01, Boundary: future birth2 date",
            "1500-01-01, Boundary: future birth3 date"
    })
    @DisplayName("Create customer with boundary birth dates should succeed")
    void shouldCreateCustomerWithBoundaryBirthDates(final String dateStr) {
        final LocalDate birthDate = LocalDate.parse(dateStr);

        final Customer customer = customerService.createCustomer(
                createDefaultCustomer("John", "Doe", birthDate, address, null, communicationDetails, Brand.GMX));

        assertEquals(birthDate, customer.getBirthDate());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("Update address should update customer address but keep unique invoice address")
    void shouldUpdateAddressSuccessfully() throws CustomerNotFoundException {
        final UUID customerId = UUID.randomUUID();
        final Customer customerMock = mock(Customer.class);
        final Address newAddress = new Address("New Street", "2", "67890", "Other City", "Other Country");

        when(customerRepository.getById(customerId)).thenReturn(customerMock);

        customerService.updateAddress(customerId, newAddress);

        verify(customerMock).setAddress(newAddress);
        verify(customerRepository).save(customerMock);
    }

    @Test
    @DisplayName("Update invoice address should update only invoice address")
    void shouldUpdateInvoiceAddressSuccessfully() throws CustomerNotFoundException {
        final UUID customerId = UUID.randomUUID();
        final Customer customerMock = mock(Customer.class);
        final Address newInvoiceAddress = new Address("Invoice St", "3", "54321", "Invoice City", "Invoicing");

        when(customerRepository.getById(customerId)).thenReturn(customerMock);

        customerService.updateInvoiceAddress(customerId, newInvoiceAddress);

        verify(customerMock).setInvoiceAddress(newInvoiceAddress);
        verify(customerRepository).save(customerMock);
    }

    @Test
    @DisplayName("Update communication details should update successfully")
    void shouldUpdateCommunicationDetailsSuccessfully() throws CustomerNotFoundException {
        final UUID customerId = UUID.randomUUID();
        final Customer customerMock = mock(Customer.class);
        final CommunicationDetails newDetails = new CommunicationDetails("new@example.com", "987654321");

        when(customerRepository.getById(customerId)).thenReturn(customerMock);

        customerService.updateCommunicationDetails(customerId, newDetails);

        verify(customerMock).setCommunicationDetails(newDetails);
        verify(customerRepository).save(customerMock);
    }

    @Test
    @DisplayName("Activate customer should change status to ACTIVE")
    void shouldActivateCustomerSuccessfully() throws CustomerNotFoundException {
        final UUID customerId = UUID.randomUUID();
        final Customer customerMock = mock(Customer.class);
        when(customerRepository.getById(customerId)).thenReturn(customerMock);

        customerService.activateCustomer(customerId);

        verify(customerMock).setStatus(CustomerStatus.ACTIVE);
        verify(customerRepository).save(customerMock);
    }

    @Test
    @DisplayName("Deactivate customer should change status to INACTIVE")
    void shouldDeactivateCustomerSuccessfully() throws CustomerNotFoundException {
        final UUID customerId = UUID.randomUUID();
        final Customer customerMock = mock(Customer.class);
        when(customerRepository.getById(customerId)).thenReturn(customerMock);

        customerService.deactivateCustomer(customerId);

        verify(customerMock).setStatus(CustomerStatus.INACTIVE);
        verify(customerRepository).save(customerMock);
    }

    @Test
    @DisplayName("Delete customer should remove customer from repository")
    void shouldDeleteCustomerSuccessfully() throws CustomerNotFoundException {
        final UUID customerId = UUID.randomUUID();

        when(customerRepository.existsById(customerId)).thenReturn(true);

        customerService.deleteCustomer(customerId);

        verify(customerRepository).deleteById(customerId);
    }

    @Test
    @DisplayName("Delete non-existent customer should throw CustomerNotFoundException")
    void shouldThrowExceptionWhenDeletingNonExistentCustomer() {
        final UUID customerId = UUID.randomUUID();

        when(customerRepository.existsById(customerId)).thenReturn(false);

        assertThrows(CustomerNotFoundException.class, () -> customerService.deleteCustomer(customerId));
    }

    @Test
    @DisplayName("Operations on non-existent customer should throw CustomerNotFoundException")
    void shouldThrowExceptionWhenPerformingOperationsOnNonExistentCustomer() throws CustomerNotFoundException {
        final UUID randomId = UUID.randomUUID();

        when(customerRepository.getById(randomId)).thenThrow(new CustomerNotFoundException("Not found"));

        assertThrows(CustomerNotFoundException.class, () -> customerService.activateCustomer(randomId));
        assertThrows(CustomerNotFoundException.class, () -> customerService.deactivateCustomer(randomId));
        assertThrows(CustomerNotFoundException.class, () -> customerService.updateAddress(randomId, address));
    }

    @Test
    @DisplayName("Create customer with null first name: validated by service layer")
    void shouldThrowExceptionWhenCreatingCustomerWithNullFirstName() {
        Customer customer =
                createDefaultCustomer(null, "Doe", LocalDate.now(), address, null, communicationDetails, Brand.GMX);
        assertThrows(ConstraintViolationException.class, () -> customerService.createCustomer(customer));
    }

    @Test
    @DisplayName("Create customer with null last name: validated by service layer")
    void shouldThrowExceptionWhenCreatingCustomerWithNullLastName() {
        Customer customer =
                createDefaultCustomer("John", null, LocalDate.now(), address, null, communicationDetails, Brand.GMX);
        assertThrows(ConstraintViolationException.class, () -> customerService.createCustomer(customer));
    }

    @Test
    @DisplayName("Create customer with null birth date: validated by service layer")
    void shouldThrowExceptionWhenCreatingCustomerWithNullBirthDate() {
        Customer customer = createDefaultCustomer("John", "Doe", null, address, null, communicationDetails, Brand.GMX);
        assertThrows(ConstraintViolationException.class, () -> customerService.createCustomer(customer));
    }

    @Test
    @DisplayName("Create customer with null address: validated by service layer")
    void shouldThrowExceptionWhenCreatingCustomerWithNullAddress() {
        Customer customer =
                createDefaultCustomer("John", "Doe", LocalDate.now(), null, null, communicationDetails, Brand.GMX);
        assertThrows(ConstraintViolationException.class, () -> customerService.createCustomer(customer));
    }

    @Test
    @DisplayName("Create customer with null communication details: validated by service layer")
    void shouldThrowExceptionWhenCreatingCustomerWithNullCommunicationDetails() {
        Customer customer = createDefaultCustomer("John", "Doe", LocalDate.now(), address, null, null, Brand.GMX);
        assertThrows(ConstraintViolationException.class, () -> customerService.createCustomer(customer));
    }

    @Test
    @DisplayName("Create customer with null brand: validated by service layer")
    void shouldThrowExceptionWhenCreatingCustomerWithNullBrand() {
        Customer customer =
                createDefaultCustomer("John", "Doe", LocalDate.now(), address, null, communicationDetails, null);
        assertThrows(ConstraintViolationException.class, () -> customerService.createCustomer(customer));
    }

    @Test
    @DisplayName("Activate customer with null ID should throw exception")
    void shouldThrowExceptionWhenActivatingCustomerWithNullId() {
        assertThrows(ConstraintViolationException.class, () -> customerService.activateCustomer(null));
    }

    @Test
    @DisplayName("Deactivate customer with null ID should throw exception")
    void shouldThrowExceptionWhenDeactivatingCustomerWithNullId() {
        assertThrows(ConstraintViolationException.class, () -> customerService.deactivateCustomer(null));
    }

    @Test
    @DisplayName("Delete customer with null ID should throw exception")
    void shouldThrowExceptionWhenDeletingCustomerWithNullId() {
        assertThrows(ConstraintViolationException.class, () -> customerService.deleteCustomer(null));
    }

    @Test
    @DisplayName("Update address with null ID should throw exception")
    void shouldThrowExceptionWhenUpdatingAddressWithNullId() {
        assertThrows(ConstraintViolationException.class, () -> customerService.updateAddress(null, address));
    }

    @Test
    @DisplayName("Update invoice address with null ID should throw exception")
    void shouldThrowExceptionWhenUpdatingInvoiceAddressWithNullId() {
        assertThrows(ConstraintViolationException.class, () -> customerService.updateInvoiceAddress(null, address));
    }

    @Test
    @DisplayName("Update communication details with null ID should throw exception")
    void shouldThrowExceptionWhenUpdatingCommunicationDetailsWithNullId() {
        assertThrows(
                ConstraintViolationException.class,
                () -> customerService.updateCommunicationDetails(null, communicationDetails));
    }

    @Test
    @DisplayName("Find customer by ID should return customer from repository")
    void shouldReturnCustomerWhenFindingByIdSuccessfully() {
        final UUID id = UUID.randomUUID();
        final Customer customer = mock(Customer.class);
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        final Optional<Customer> result = customerService.findCustomerById(id);

        assertTrue(result.isPresent());
        assertEquals(customer, result.get());
        verify(customerRepository).findById(id);
    }

    @Test
    @DisplayName("Find all customers should return all customers from repository")
    void shouldReturnAllCustomersWhenFindingAll() {
        final List<Customer> customers = List.of(mock(Customer.class));
        when(customerRepository.findAll()).thenReturn(customers);

        final List<Customer> result = customerService.findAllCustomers();

        assertEquals(customers.size(), result.size());
        assertEquals(customers, result);
        verify(customerRepository).findAll();
    }

    @Test
    @DisplayName("Negative: createCustomer with null customer")
    void shouldThrowExceptionWhenCreatingNullCustomer() {
        assertThrows(ConstraintViolationException.class, () -> customerService.createCustomer(null));
    }
}
