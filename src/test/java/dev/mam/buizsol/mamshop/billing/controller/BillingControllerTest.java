package dev.mam.buizsol.mamshop.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mam.buizsol.mamshop.billing.dto.InvoiceItemResponseDTO;
import dev.mam.buizsol.mamshop.billing.dto.InvoiceRequestDTO;
import dev.mam.buizsol.mamshop.billing.dto.InvoiceResponseDTO;
import dev.mam.buizsol.mamshop.billing.exception.InvoicePdfGenerationException;
import dev.mam.buizsol.mamshop.billing.mapper.InvoiceMapper;
import dev.mam.buizsol.mamshop.billing.model.Invoice;
import dev.mam.buizsol.mamshop.billing.model.InvoiceItem;
import dev.mam.buizsol.mamshop.billing.service.InvoicePdfService;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import dev.mam.buizsol.mamshop.customer.dto.AddressResponseDTO;
import dev.mam.buizsol.mamshop.customer.exception.CustomerNotFoundException;
import dev.mam.buizsol.mamshop.customer.model.Address;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.shop.service.ShopService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("BillingController Tests")
@WebMvcTest(
        controllers = BillingController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
class BillingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private ShopService shopService;

    @MockitoBean
    private InvoiceMapper invoiceMapper;

    @MockitoBean
    private InvoicePdfService invoicePdfService;


    @Test
    @DisplayName("Positive: Should generate invoice successfully for valid customer")
    void shouldGenerateInvoiceSuccessfully() throws Exception {

        UUID customerId = UUID.randomUUID();
        Brand brand = Brand.GMX;
        Customer customer = mock(Customer.class);
        Address address = BillingTestFactory.createAddress("Main St", "10", "12345", "Berlin", "Germany");
        Contract contract = mock(Contract.class);
        InvoiceItem item = BillingTestFactory.createInvoiceItem(
                UUID.randomUUID(),
                "Premium Mail",
                contract,
                LocalDate.now(),
                new BigDecimal("5.00"),
                new BigDecimal("10.00"));

        Invoice invoice =
                BillingTestFactory.createInvoice(brand, customer, address, address, List.of(item), BigDecimal.ZERO);

        AddressResponseDTO addressResponseDTO =
                BillingTestFactory.createAddressResponseDTO("Main St", "10", "12345", "Berlin", "Germany");
        InvoiceItemResponseDTO itemResponseDTO = BillingTestFactory.createInvoiceItemResponseDTO(
                UUID.randomUUID(),
                "Premium Mail",
                UUID.randomUUID(),
                LocalDate.now(),
                new BigDecimal("5.00"),
                new BigDecimal("10.00"));

        InvoiceResponseDTO responseDto = BillingTestFactory.createInvoiceResponseDTO(
                brand,
                LocalDate.now(),
                customerId,
                addressResponseDTO,
                addressResponseDTO,
                List.of(itemResponseDTO),
                new BigDecimal("5.00"),
                new BigDecimal("10.00"),
                BigDecimal.ZERO,
                new BigDecimal("15.00"));

        when(shopService.generateInvoice(customerId)).thenReturn(invoice);
        when(invoiceMapper.toInvoiceResponseDTO(invoice)).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/billing/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new InvoiceRequestDTO(customerId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.brand").value(brand.name()))
                .andExpect(jsonPath("$.totalAmount").value(15.00));

        verify(shopService).generateInvoice(customerId);
        verify(invoiceMapper).toInvoiceResponseDTO(invoice);
    }

    @Test
    @DisplayName("Negative: Should return 404 when customer not found")
    void shouldReturn404WhenCustomerNotFound() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(shopService.generateInvoice(customerId)).thenThrow(new CustomerNotFoundException("Customer not found"));

        mockMvc.perform(post("/api/v1/billing/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new InvoiceRequestDTO(customerId))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Customer not found"));

        verify(shopService).generateInvoice(customerId);
    }

    @Test
    @DisplayName("Negative: Should return 400 when customer ID format is invalid")
    void shouldReturn400WhenCustomerIdIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/billing/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("REQUEST_VALIDATION_ERROR"));
    }


    @Test
    @DisplayName("Positive: Should return PDF bytes with correct headers for valid customer")
    void shouldDownloadInvoicePdfSuccessfully() throws Exception {
        final UUID customerId = UUID.randomUUID();
        final Address address = BillingTestFactory.createAddress("Main St", "10", "12345", "Berlin", "Germany");

        final Customer customer = mock(Customer.class);
        when(customer.getId()).thenReturn(customerId);
        when(customer.getFirstName()).thenReturn("Max");
        when(customer.getLastName()).thenReturn("Mustermann");

        final Invoice invoice = BillingTestFactory.createInvoice(
                Brand.GMX, customer, address, address, List.of(), BigDecimal.ZERO);

        // Minimal valid PDF magic bytes
        final byte[] fakePdf = "%PDF-1.4 fake-pdf-content".getBytes();

        when(shopService.generateInvoice(customerId)).thenReturn(invoice);
        when(invoicePdfService.generatePdf(invoice)).thenReturn(fakePdf);

        mockMvc.perform(get("/api/v1/billing/invoices/pdf")
                        .param("customerId", customerId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", startsWith("attachment; filename=\"invoice_max_mustermann_")))
                .andExpect(header().exists("Content-Length"));

        verify(shopService).generateInvoice(customerId);
        verify(invoicePdfService).generatePdf(invoice);
    }

    @Test
    @DisplayName("Negative: Should return 404 when customer not found during PDF download")
    void shouldReturn404WhenCustomerNotFoundForPdf() throws Exception {
        final UUID customerId = UUID.randomUUID();
        when(shopService.generateInvoice(customerId)).thenThrow(new CustomerNotFoundException("Customer not found"));

        mockMvc.perform(get("/api/v1/billing/invoices/pdf")
                        .param("customerId", customerId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"));

        verify(shopService).generateInvoice(customerId);
    }

    @Test
    @DisplayName("Negative: Should return 500 when PDF generation fails")
    void shouldReturn500WhenPdfGenerationFails() throws Exception {
        final UUID customerId = UUID.randomUUID();
        final Address address = BillingTestFactory.createAddress("Main St", "10", "12345", "Berlin", "Germany");

        final Customer customer = mock(Customer.class);
        when(customer.getId()).thenReturn(customerId);
        when(customer.getFirstName()).thenReturn("Max");
        when(customer.getLastName()).thenReturn("Mustermann");

        final Invoice invoice = BillingTestFactory.createInvoice(
                Brand.GMX, customer, address, address, List.of(), BigDecimal.ZERO);

        when(shopService.generateInvoice(customerId)).thenReturn(invoice);
        when(invoicePdfService.generatePdf(any())).thenThrow(
                new InvoicePdfGenerationException("PDF render error", new RuntimeException("IO failure")));

        mockMvc.perform(get("/api/v1/billing/invoices/pdf")
                        .param("customerId", customerId.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INVOICE_PDF_GENERATION_ERROR"));
    }

    @Test
    @DisplayName("Negative: Should return 400 when customerId param is missing for PDF download")
    void shouldReturn400WhenCustomerIdMissingForPdf() throws Exception {
        mockMvc.perform(get("/api/v1/billing/invoices/pdf"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Negative: Should return 400 when customerId param has invalid UUID format for PDF download")
    void shouldReturn400WhenCustomerIdInvalidForPdf() throws Exception {
        mockMvc.perform(get("/api/v1/billing/invoices/pdf")
                        .param("customerId", "not-a-valid-uuid"))
                .andExpect(status().isBadRequest());
    }
}
