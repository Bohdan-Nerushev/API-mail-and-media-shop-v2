package dev.mam.buizsol.mamshop.billing.controller;

import dev.mam.buizsol.mamshop.billing.dto.InvoiceRequestDTO;
import dev.mam.buizsol.mamshop.billing.dto.InvoiceResponseDTO;
import dev.mam.buizsol.mamshop.billing.mapper.InvoiceMapper;
import dev.mam.buizsol.mamshop.billing.model.Invoice;
import dev.mam.buizsol.mamshop.billing.service.InvoicePdfService;
import dev.mam.buizsol.mamshop.config.ErrorResponse;
import dev.mam.buizsol.mamshop.shop.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Tag(name = "Billing", description = "Billing API")
@RestController
@RequestMapping(value = "/api/v1/billing")
public class BillingController {

    private final ShopService shopService;
    private final InvoiceMapper invoiceMapper;
    private final InvoicePdfService invoicePdfService;

    public BillingController(
            final ShopService shopService,
            final InvoiceMapper invoiceMapper,
            final InvoicePdfService invoicePdfService) {
        this.shopService = shopService;
        this.invoiceMapper = invoiceMapper;
        this.invoicePdfService = invoicePdfService;
    }

    @Operation(
            summary = "Generate Invoice for a customer",
            description = "Generates an invoice for the specified customer.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Invoice generated successfully",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = InvoiceResponseDTO.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Customer not found",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PostMapping(value = "/invoices")
    @PreAuthorize("hasRole('USER')")
    public @NotNull InvoiceResponseDTO generateInvoice(@RequestBody @Valid final InvoiceRequestDTO invoiceRequestDTO) {
        log.debug("Generating invoice for customer: {}", invoiceRequestDTO.customerId());

        Invoice invoice = shopService.generateInvoice(invoiceRequestDTO.customerId());
        log.info("Invoice generated successfully: {}", invoice);

        InvoiceResponseDTO invoiceResponseDTO = invoiceMapper.toInvoiceResponseDTO(invoice);
        log.debug("Invoice response DTO: {}", invoiceResponseDTO);
        return invoiceResponseDTO;
    }

    @Operation(
            summary = "Download Invoice as PDF",
            description = "Generates and downloads the invoice as a PDF document for the specified customer.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "PDF generated and returned successfully",
                            content = @Content(mediaType = "application/pdf")),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Customer not found",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = "500",
                            description = "PDF generation failed",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GetMapping(value = "/invoices/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasRole('USER')")
    public @NotNull ResponseEntity<byte[]> downloadInvoicePdf(
            @RequestParam @NotNull final UUID customerId) {

        log.debug("Downloading invoice PDF for customer: {}", customerId);

        final Invoice invoice = shopService.generateInvoice(customerId);
        final byte[] pdfBytes = invoicePdfService.generatePdf(invoice);

        final String firstName = invoice.getCustomer().getFirstName();
        final String lastName = invoice.getCustomer().getLastName();
        final long timestamp = Instant.now().getEpochSecond();
        final String filename = String.format(
                "invoice_%s_%s_%d.pdf",
                firstName.toLowerCase().replaceAll("[^a-z0-9]", ""),
                lastName.toLowerCase().replaceAll("[^a-z0-9]", ""),
                timestamp);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.setContentLength(pdfBytes.length);

        log.info("Invoice PDF generated for customer: {}, filename: {}", customerId, filename);
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
