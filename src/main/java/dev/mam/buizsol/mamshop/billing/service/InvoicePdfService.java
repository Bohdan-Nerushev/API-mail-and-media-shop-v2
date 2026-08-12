package dev.mam.buizsol.mamshop.billing.service;

import dev.mam.buizsol.mamshop.billing.exception.InvoicePdfGenerationException;
import dev.mam.buizsol.mamshop.billing.model.Invoice;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface InvoicePdfService {

    /**
     * Generates a PDF document for the given invoice and returns it as a byte array.
     * The document is generated entirely in memory — no file is written to disk.
     *
     * @param invoice the invoice domain object to render
     * @return raw PDF bytes ready to be sent as an HTTP response body
     * @throws InvoicePdfGenerationException if the PDF cannot be rendered
     */
    @NotNull
    byte[] generatePdf(@NotNull Invoice invoice);
}
