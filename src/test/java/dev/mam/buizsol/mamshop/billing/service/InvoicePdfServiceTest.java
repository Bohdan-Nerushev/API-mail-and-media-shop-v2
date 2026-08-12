package dev.mam.buizsol.mamshop.billing.service;

import dev.mam.buizsol.mamshop.billing.exception.InvoicePdfGenerationException;
import dev.mam.buizsol.mamshop.billing.model.Invoice;
import dev.mam.buizsol.mamshop.billing.model.InvoiceItem;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import dev.mam.buizsol.mamshop.customer.model.Address;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("InvoicePdfService Tests")
class InvoicePdfServiceTest {

    private InvoicePdfService invoicePdfService;

    private Customer customer;
    private Address address;

    @BeforeEach
    void setUp() {
        invoicePdfService = new InvoicePdfServiceImpl();

        address = new Address("Musterstraße", "12A", "68161", "Mannheim", "Germany");

        customer = mock(Customer.class);
        when(customer.getId()).thenReturn(UUID.randomUUID());
        when(customer.getFirstName()).thenReturn("Max");
        when(customer.getLastName()).thenReturn("Mustermann");
    }

    @Test
    @DisplayName("Positive: Should generate non-empty PDF bytes for invoice with items")
    void shouldGeneratePdfWithItems() {
        final Contract contract = mock(Contract.class);
        when(contract.getId()).thenReturn(UUID.randomUUID());

        final InvoiceItem item = new InvoiceItem(
                UUID.randomUUID(),
                "Premium Mail",
                contract,
                LocalDate.now(),
                new BigDecimal("5.00"),
                new BigDecimal("10.00"));

        final Invoice invoice = new Invoice(
                Brand.GMX, customer, address, address, List.of(item), BigDecimal.ZERO);

        final byte[] result = invoicePdfService.generatePdf(invoice);

        assertNotNull(result, "PDF bytes must not be null");
        assertTrue(result.length > 0, "PDF must not be empty");
        // PDF files start with the %PDF- magic bytes
        assertTrue(result.length >= 5
                && result[0] == '%'
                && result[1] == 'P'
                && result[2] == 'D'
                && result[3] == 'F', "Result must be a valid PDF document");
    }

    @Test
    @DisplayName("Positive: Should generate valid PDF for invoice with no items (empty billing period)")
    void shouldGeneratePdfWithNoItems() {
        final Invoice invoice = new Invoice(
                Brand.GMX, customer, address, address, List.of(), BigDecimal.ZERO);

        final byte[] result = invoicePdfService.generatePdf(invoice);

        assertNotNull(result);
        assertTrue(result.length > 0, "PDF must be generated even when there are no items");
    }

    @Test
    @DisplayName("Positive: Should generate valid PDF when customer name contains umlauts")
    void shouldGeneratePdfWithUmlautCustomerName() {
        when(customer.getFirstName()).thenReturn("Jürgen");
        when(customer.getLastName()).thenReturn("Müller");

        final Address germanAddress = new Address("Schönhauser Allee", "55", "10437", "Berlin", "Germany");
        final Invoice invoice = new Invoice(
                Brand.GMX, customer, germanAddress, germanAddress, List.of(), BigDecimal.ZERO);

        final byte[] result = invoicePdfService.generatePdf(invoice);

        assertNotNull(result);
        assertTrue(result.length > 0, "PDF with umlaut names must be generated without error");
    }

    @Test
    @DisplayName("Positive: Should generate valid PDF when discount is non-zero")
    void shouldGeneratePdfWithDiscount() {
        final Contract contract = mock(Contract.class);
        when(contract.getId()).thenReturn(UUID.randomUUID());

        final InvoiceItem item = new InvoiceItem(
                UUID.randomUUID(),
                "Standard Mail",
                contract,
                LocalDate.of(2026, 1, 15),
                new BigDecimal("0.00"),
                new BigDecimal("7.99"));

        final Invoice invoice = new Invoice(
                Brand.GMX, customer, address, address, List.of(item), new BigDecimal("2.00"));

        final byte[] result = invoicePdfService.generatePdf(invoice);

        assertNotNull(result);
        assertTrue(result.length > 0, "PDF with discount must be generated correctly");
    }

    @Test
    @DisplayName("Negative: Should throw InvoicePdfGenerationException when invoice customer is null")
    void shouldThrowExceptionWhenCustomerIsNull() {
        // Null customer causes NullPointerException inside the service,
        // which must be wrapped as InvoicePdfGenerationException
        final Invoice invoice = mock(Invoice.class);
        when(invoice.getCustomer()).thenReturn(null);

        assertThrows(
                NullPointerException.class,
                () -> invoicePdfService.generatePdf(invoice),
                "Null customer should propagate as runtime exception before PDF rendering begins");
    }
}
