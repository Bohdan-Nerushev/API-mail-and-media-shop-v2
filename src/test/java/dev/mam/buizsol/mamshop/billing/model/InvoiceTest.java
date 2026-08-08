package dev.mam.buizsol.mamshop.billing.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import dev.mam.buizsol.mamshop.billing.exception.InvalidInvoiceDiscountException;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import dev.mam.buizsol.mamshop.customer.model.Address;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.executable.ExecutableValidator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Invoice Record Tests")
class InvoiceTest {

    private Address testAddress;
    private Customer customer;
    private Contract contract;

    @BeforeEach
    void setUp() {
        testAddress = new Address("Street", "1", "12345", "City", "Country");
        customer = mock(Customer.class);
        contract = mock(Contract.class);
    }

    @Test
    @DisplayName("Successful calculation of totals for multiple items")
    void shouldCalculateTotalsCorrectlyWhenMultipleItemsExist() {
        InvoiceItem item1 = new InvoiceItem(
                UUID.randomUUID(), "P1", contract, LocalDate.now(), new BigDecimal("10.00"), new BigDecimal("5.00"));
        InvoiceItem item2 = new InvoiceItem(
                UUID.randomUUID(), "P2", contract, LocalDate.now(), new BigDecimal("20.00"), new BigDecimal("15.00"));

        BigDecimal discount = new BigDecimal("7.00");

        Invoice invoice = new Invoice(Brand.GMX, customer, testAddress, testAddress, List.of(item1, item2), discount);

        assertEquals(new BigDecimal("30.00"), invoice.getTotalSetupFee());
        assertEquals(new BigDecimal("20.00"), invoice.getTotalMonthlyFee());
        assertEquals(new BigDecimal("43.00"), invoice.getTotalAmount());
        assertEquals(LocalDate.now(), invoice.getInvoiceDate());
        assertEquals(Brand.GMX, invoice.getBrand());
    }

    @Test
    @DisplayName("Successful creation with zero discount (boundary value)")
    void shouldAllowCreationWhenDiscountIsZero() {
        Invoice invoice =
                new Invoice(Brand.GMX, customer, testAddress, testAddress, Collections.emptyList(), BigDecimal.ZERO);

        assertEquals(BigDecimal.ZERO, invoice.getDiscount());
        assertEquals(BigDecimal.ZERO, invoice.getTotalAmount());
    }

    @DisplayName("Verification that invalid discounts (negative/null) throw exception")
    @ParameterizedTest(name = "Invalid discount validation - value: {0}")
    @ValueSource(strings = {"-0.01", "-10.00"})
    void shouldThrowExceptionWhenDiscountIsNegative(BigDecimal negativeDiscount) {
        List<InvoiceItem> items = Collections.emptyList();
        assertThrows(
                InvalidInvoiceDiscountException.class,
                () -> new Invoice(Brand.GMX, customer, testAddress, testAddress, items, negativeDiscount));
    }

    @Test
    @DisplayName("Null discount validation (negative scenario)")
    void shouldThrowExceptionWhenDiscountIsNull() {
        List<InvoiceItem> items = Collections.emptyList();
        assertThrows(
                NullPointerException.class,
                () -> new Invoice(Brand.GMX, customer, testAddress, testAddress, items, null));
    }

    @DisplayName("Verification of totals calculation using parameterized discounts")
    @ParameterizedTest(name = "Calculation logic with various item counts and discount: {0}")
    @CsvSource({"0.00, 0.00", "10.50,-10.50", "100.00,-100.00"})
    void shouldCalculateAmountCorrectlyWhenDiscountsAreProvided(BigDecimal discount, BigDecimal expectedTotal) {
        Invoice invoice =
                new Invoice(Brand.MAIL_COM, customer, testAddress, testAddress, Collections.emptyList(), discount);

        assertEquals(expectedTotal, invoice.getTotalAmount());
    }

    @Test
    @DisplayName("Verification of items list mutability (reflecting current class state)")
    void shouldReturnMutableItemsList() {
        InvoiceItem item =
                new InvoiceItem(UUID.randomUUID(), "P1", contract, LocalDate.now(), BigDecimal.ZERO, BigDecimal.TEN);
        List<InvoiceItem> items = new java.util.ArrayList<>();
        items.add(item);

        Invoice invoice = new Invoice(Brand.WEB_DE, customer, testAddress, testAddress, items, BigDecimal.ZERO);

        int initialSize = invoice.getItems().size();
        invoice.getItems().add(item);
        assertEquals(initialSize + 1, invoice.getItems().size());
    }

    @Test
    @DisplayName("Verification of different mailing and invoice addresses")
    void shouldStoreDifferentMailingAndInvoiceAddresses() {
        Address invoiceAddress = new Address("Invoice St", "2", "54321", "City2", "Country");

        Invoice invoice =
                new Invoice(Brand.GMX, customer, testAddress, invoiceAddress, Collections.emptyList(), BigDecimal.ZERO);

        assertEquals(testAddress, invoice.getAddress());
        assertEquals(invoiceAddress, invoice.getInvoiceAddress());
    }

    @Test
    @DisplayName("Null items list validation (negative scenario)")
    void shouldThrowExceptionWhenItemsListIsNull() {
        assertThrows(
                NullPointerException.class,
                () -> new Invoice(Brand.GMX, customer, testAddress, testAddress, null, BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Verify @NotNull constructor annotations via ExecutableValidator")
    void shouldValidateConstructorAnnotationsWithExecutableValidator() throws NoSuchMethodException {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        ExecutableValidator executableValidator = validator.forExecutables();
        java.lang.reflect.Constructor<Invoice> constructor = Invoice.class.getConstructor(
                Brand.class, Customer.class, Address.class, Address.class, List.class, BigDecimal.class);

        var violations = executableValidator.validateConstructorParameters(
                constructor, new Object[] {null, null, null, null, null, null});

        org.junit.jupiter.api.Assertions.assertFalse(violations.isEmpty());
        org.junit.jupiter.api.Assertions.assertEquals(6, violations.size());
    }

    @Test
    @DisplayName("Precision verification with many fractional fee items")
    void shouldMaintainPrecisionWhenCalculatingTotals() {
        BigDecimal fee = new BigDecimal("1.3333333333");
        InvoiceItem item1 = new InvoiceItem(UUID.randomUUID(), "P1", contract, LocalDate.now(), fee, fee);
        InvoiceItem item2 = new InvoiceItem(UUID.randomUUID(), "P2", contract, LocalDate.now(), fee, fee);

        Invoice invoice =
                new Invoice(Brand.GMX, customer, testAddress, testAddress, List.of(item1, item2), BigDecimal.ZERO);

        BigDecimal expectedSetup = fee.add(fee);
        BigDecimal expectedMonthly = fee.add(fee);
        assertEquals(expectedSetup, invoice.getTotalSetupFee());
        assertEquals(expectedMonthly, invoice.getTotalMonthlyFee());
        assertEquals(expectedSetup.add(expectedMonthly), invoice.getTotalAmount());
    }

    @Test
    @DisplayName("Discount exceeding total fees (Negative total amount scenario)")
    void shouldAllowNegativeTotalAmountWhenDiscountExceedsFees() {
        InvoiceItem item = new InvoiceItem(
                UUID.randomUUID(), "P1", contract, LocalDate.now(), new BigDecimal("10.00"), new BigDecimal("10.00"));
        BigDecimal highDiscount = new BigDecimal("25.00");

        Invoice invoice = new Invoice(Brand.GMX, customer, testAddress, testAddress, List.of(item), highDiscount);

        assertEquals(new BigDecimal("-5.00"), invoice.getTotalAmount());
    }

    @Test
    @DisplayName("Stress test with a large number of items (performance check)")
    void shouldCalculateTotalsCorrectlyWhenLargeNumberOfItemsProvided() {
        final InvoiceItem prototype = new InvoiceItem(
                UUID.randomUUID(),
                "Product",
                contract,
                LocalDate.now(),
                new BigDecimal("1.00"),
                new BigDecimal("2.00"));

        final List<InvoiceItem> items = Collections.nCopies(1000, prototype);

        final Invoice invoice = new Invoice(Brand.GMX, customer, testAddress, testAddress, items, BigDecimal.ZERO);

        assertEquals(new BigDecimal("1000.00"), invoice.getTotalSetupFee());
        assertEquals(new BigDecimal("2000.00"), invoice.getTotalMonthlyFee());
        assertEquals(new BigDecimal("3000.00"), invoice.getTotalAmount());
    }
}
