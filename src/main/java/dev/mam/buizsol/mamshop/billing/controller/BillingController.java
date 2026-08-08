package dev.mam.buizsol.mamshop.billing.controller;

import dev.mam.buizsol.mamshop.billing.dto.InvoiceRequestDTO;
import dev.mam.buizsol.mamshop.billing.dto.InvoiceResponseDTO;
import dev.mam.buizsol.mamshop.billing.mapper.InvoiceMapper;
import dev.mam.buizsol.mamshop.billing.model.Invoice;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Billing", description = "Billing API")
@RestController
@RequestMapping(value = "/api/v1/billing")
public class BillingController {

    private final ShopService shopService;
    private final InvoiceMapper invoiceMapper;

    public BillingController(final ShopService shopService, final InvoiceMapper invoiceMapper) {
        this.shopService = shopService;
        this.invoiceMapper = invoiceMapper;
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
}
