package dev.mam.buizsol.mamshop.billing.service;

import dev.mam.buizsol.mamshop.billing.exception.InvoicePdfGenerationException;
import dev.mam.buizsol.mamshop.billing.model.Invoice;
import dev.mam.buizsol.mamshop.billing.model.InvoiceItem;
import dev.mam.buizsol.mamshop.customer.model.Address;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates Invoice PDF documents entirely in memory using Apache PDFBox 3.x.
 *
 * Font choice: NotoSans-Regular / NotoSans-Bold (bundled in resources/fonts/).
 * NotoSans supports Latin Extended (umlauts ä ö ü ß) and most European scripts.
 */
@Slf4j
@Service
class InvoicePdfServiceImpl implements InvoicePdfService {

    // A4 dimensions in PDF points (1 pt = 1/72 inch)
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();

    private static final float MARGIN = 50f;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;

    private static final float FONT_SIZE_TITLE = 20f;
    private static final float FONT_SIZE_SECTION = 11f;
    private static final float FONT_SIZE_BODY = 9.5f;
    private static final float FONT_SIZE_SMALL = 8f;
    private static final float LINE_HEIGHT_BODY = 14f;
    private static final float LINE_HEIGHT_SECTION = 18f;

    private static final String FONT_REGULAR_PATH = "/fonts/NotoSans-Regular.ttf";
    private static final String FONT_BOLD_PATH = "/fonts/NotoSans-Bold.ttf";

    private static final String CURRENCY_SYMBOL = "EUR";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public byte[] generatePdf(final Invoice invoice) {
        log.debug("Generating PDF for invoice, customerId={}", invoice.getCustomer().getId());

        try (PDDocument document = new PDDocument()) {
            final PDFont fontRegular = loadFont(document, FONT_REGULAR_PATH);
            final PDFont fontBold = loadFont(document, FONT_BOLD_PATH);

            final PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float y = PAGE_HEIGHT - MARGIN;

                y = drawHeader(content, fontBold, fontRegular, invoice, y);
                y = drawDivider(content, y - 10f);
                y = drawAddresses(content, fontBold, fontRegular, invoice, y - 15f);
                y = drawDivider(content, y - 10f);
                y = drawItemsTable(content, fontBold, fontRegular, invoice.getItems(), y - 15f);
                y = drawSummary(content, fontBold, fontRegular, invoice, y);
                drawFooter(content, fontRegular);
            }

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);

            final byte[] pdfBytes = outputStream.toByteArray();
            log.debug("PDF generated successfully, size={} bytes", pdfBytes.length);
            return pdfBytes;

        } catch (IOException e) {
            log.error("Failed to generate PDF for customerId={}", invoice.getCustomer().getId(), e);
            throw new InvoicePdfGenerationException("PDF generation failed for customer: "
                    + invoice.getCustomer().getId(), e);
        }
    }

    // ---------------------------
    // Section renderers
    // ---------------------------

    private float drawHeader(
            final PDPageContentStream content,
            final PDFont fontBold,
            final PDFont fontRegular,
            final Invoice invoice,
            final float startY) throws IOException {

        float y = startY;

        // Brand label (top-left)
        writeText(content, fontBold, FONT_SIZE_TITLE,
                MARGIN, y, invoice.getBrand().name() + " INVOICE");

        // Invoice date (top-right, aligned)
        final String dateText = "Date: " + invoice.getInvoiceDate().format(DATE_FORMATTER);
        final float dateX = PAGE_WIDTH - MARGIN - textWidth(fontRegular, FONT_SIZE_SMALL, dateText);
        writeText(content, fontRegular, FONT_SIZE_SMALL, dateX, y, dateText);

        y -= LINE_HEIGHT_BODY;

        // Exact timestamp of PDF generation
        final java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of("Europe/Berlin"));
        final String timeText = "Generated: " + now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z"));
        final float timeX = PAGE_WIDTH - MARGIN - textWidth(fontRegular, FONT_SIZE_SMALL, timeText);
        writeText(content, fontRegular, FONT_SIZE_SMALL, timeX, y, timeText);

        // Customer name subtitle (drawn at first subtitle y level)
        final String customerName = invoice.getCustomer().getFirstName()
                + " " + invoice.getCustomer().getLastName();
        writeText(content, fontRegular, FONT_SIZE_BODY, MARGIN, startY - LINE_HEIGHT_SECTION, "Customer: " + customerName);

        y = startY - LINE_HEIGHT_SECTION - LINE_HEIGHT_BODY;

        // Customer contact details (email & telephone)
        if (invoice.getCustomer().getCommunicationDetails() != null) {
            final String email = "Email: " + invoice.getCustomer().getCommunicationDetails().getEmail();
            final String phone = "Phone: " + invoice.getCustomer().getCommunicationDetails().getTelephone();
            writeText(content, fontRegular, FONT_SIZE_SMALL, MARGIN, y, email);
            y -= LINE_HEIGHT_BODY;
            writeText(content, fontRegular, FONT_SIZE_SMALL, MARGIN, y, phone);
            y -= LINE_HEIGHT_BODY;
        }

        return y;
    }

    private float drawAddresses(
            final PDPageContentStream content,
            final PDFont fontBold,
            final PDFont fontRegular,
            final Invoice invoice,
            final float startY) throws IOException {

        float y = startY;
        final float colWidth = CONTENT_WIDTH / 2f;

        // Left column: Customer Address
        writeText(content, fontBold, FONT_SIZE_SECTION, MARGIN, y, "Customer Address");
        // Right column: Billing Destination
        writeText(content, fontBold, FONT_SIZE_SECTION, MARGIN + colWidth, y, "Billing Destination");

        y -= LINE_HEIGHT_BODY;

        final List<String> leftLines = formatAddress(invoice.getAddress());
        final List<String> rightLines = formatAddress(invoice.getInvoiceAddress());

        final int maxLines = Math.max(leftLines.size(), rightLines.size());
        for (int i = 0; i < maxLines; i++) {
            if (i < leftLines.size()) {
                writeText(content, fontRegular, FONT_SIZE_BODY, MARGIN, y, leftLines.get(i));
            }
            if (i < rightLines.size()) {
                writeText(content, fontRegular, FONT_SIZE_BODY, MARGIN + colWidth, y, rightLines.get(i));
            }
            y -= LINE_HEIGHT_BODY;
        }

        return y;
    }

    private float drawItemsTable(
            final PDPageContentStream content,
            final PDFont fontBold,
            final PDFont fontRegular,
            final List<InvoiceItem> items,
            final float startY) throws IOException {

        // Column widths (proportional to CONTENT_WIDTH) - Contract ID gets more room to avoid truncation
        final float[] colWidths = {
            CONTENT_WIDTH * 0.22f, // Service Plan
            CONTENT_WIDTH * 0.35f, // Contract ID (full UUID format takes 36 characters)
            CONTENT_WIDTH * 0.15f, // Purchase Date
            CONTENT_WIDTH * 0.14f, // Setup Fee
            CONTENT_WIDTH * 0.14f  // Monthly Fee
        };

        float y = startY;

        // Table header
        drawTableRow(content, fontBold, FONT_SIZE_SMALL, y,
                colWidths,
                new String[]{"Service Plan", "Contract ID", "Purchase Date", "Setup Fee (EUR)", "Monthly Fee (EUR)"},
                true);
        y -= LINE_HEIGHT_BODY;
        drawDivider(content, y + 4f);

        if (items.isEmpty()) {
            y -= LINE_HEIGHT_BODY;
            writeText(content, fontRegular, FONT_SIZE_BODY, MARGIN, y,
                    "No active services found for this billing period.");
            y -= LINE_HEIGHT_BODY;
        } else {
            for (final InvoiceItem item : items) {
                y -= LINE_HEIGHT_BODY;
                drawTableRow(content, fontRegular, FONT_SIZE_SMALL, y,
                        colWidths,
                        new String[]{
                            item.getProductName(),
                            item.getContract().getId().toString(), // Full UUID format
                            item.getContractCreationDate().format(DATE_FORMATTER),
                            formatAmount(item.getSetupFee()),
                            formatAmount(item.getMonthlyFee())
                        },
                        false);
            }
            y -= LINE_HEIGHT_BODY;
        }

        return y;
    }

    private float drawSummary(
            final PDPageContentStream content,
            final PDFont fontBold,
            final PDFont fontRegular,
            final Invoice invoice,
            final float startY) throws IOException {

        final float labelX = PAGE_WIDTH - MARGIN - 220f;
        final float valueX = PAGE_WIDTH - MARGIN;
        float y = startY - 10f;

        drawDivider(content, y + 5f);
        y -= 5f;

        y = drawSummaryLine(content, fontRegular, "Total Setup Fee:", formatAmount(invoice.getTotalSetupFee()),
                labelX, valueX, y);
        y = drawSummaryLine(content, fontRegular, "Total Monthly Fee:", formatAmount(invoice.getTotalMonthlyFee()),
                labelX, valueX, y);
        y = drawSummaryLine(content, fontRegular, "Discount:", "- " + formatAmount(invoice.getDiscount()),
                labelX, valueX, y);

        drawDivider(content, y + 2f);
        y -= 8f; // Add more spacing after the divider to avoid overlap

        y = drawSummaryLine(content, fontBold, "Total Amount (" + CURRENCY_SYMBOL + "):",
                formatAmount(invoice.getTotalAmount()), labelX, valueX, y);

        return y;
    }


    private void drawFooter(final PDPageContentStream content, final PDFont fontRegular) throws IOException {
        final String footerText = "This document was generated automatically. It is not a tax invoice.";
        final float footerX = MARGIN;
        final float footerY = MARGIN - 15f;
        writeText(content, fontRegular, FONT_SIZE_SMALL, footerX, footerY, footerText);
    }

    // ---------------------------
    // Drawing helpers
    // ---------------------------

    private float drawSummaryLine(
            final PDPageContentStream content,
            final PDFont font,
            final String label,
            final String value,
            final float labelX,
            final float rightEdgeX,
            final float y) throws IOException {

        writeText(content, font, FONT_SIZE_BODY, labelX, y, label);
        final float valueX = rightEdgeX - textWidth(font, FONT_SIZE_BODY, value);
        writeText(content, font, FONT_SIZE_BODY, valueX, y, value);
        return y - LINE_HEIGHT_BODY;
    }

    private void drawTableRow(
            final PDPageContentStream content,
            final PDFont font,
            final float fontSize,
            final float y,
            final float[] colWidths,
            final String[] cells,
            final boolean isHeader) throws IOException {

        float x = MARGIN;
        for (int i = 0; i < cells.length; i++) {
            final String cell = truncateText(font, fontSize, cells[i], colWidths[i] - 4f);
            // Right-align last two numeric columns
            if (i >= 3) {
                final float cellRight = x + colWidths[i];
                final float textX = cellRight - textWidth(font, fontSize, cell) - 2f;
                writeText(content, font, fontSize, textX, y, cell);
            } else {
                writeText(content, font, fontSize, x + 2f, y, cell);
            }
            x += colWidths[i];
        }
    }

    private float drawDivider(final PDPageContentStream content, final float y) throws IOException {
        content.setLineWidth(0.5f);
        content.moveTo(MARGIN, y);
        content.lineTo(PAGE_WIDTH - MARGIN, y);
        content.stroke();
        return y;
    }

    private void writeText(
            final PDPageContentStream content,
            final PDFont font,
            final float fontSize,
            final float x,
            final float y,
            final String text) throws IOException {

        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(sanitize(text));
        content.endText();
    }

    // ---------------------------
    // Utility helpers
    // ---------------------------

    private PDFont loadFont(final PDDocument document, final String resourcePath) throws IOException {
        try (InputStream fontStream = getClass().getResourceAsStream(resourcePath)) {
            if (fontStream == null) {
                throw new IOException("Font resource not found on classpath: " + resourcePath);
            }
            return PDType0Font.load(document, fontStream);
        }
    }

    private List<String> formatAddress(final Address address) {
        return List.of(
                address.getStreet() + " " + address.getNumber(),
                address.getPostcode() + " " + address.getCity(),
                address.getCountry()
        );
    }

    private String formatAmount(final BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return String.format("%.2f", amount);
    }

    /**
     * Returns the text width in PDF points for the given font and size.
     */
    private float textWidth(final PDFont font, final float fontSize, final String text) throws IOException {
        return font.getStringWidth(sanitize(text)) / 1000f * fontSize;
    }

    /**
     * Truncates text to fit within maxWidth PDF points.
     * Appends ellipsis if truncation occurred.
     */
    private String truncateText(final PDFont font, final float fontSize, final String text,
            final float maxWidth) throws IOException {
        if (textWidth(font, fontSize, text) <= maxWidth) {
            return text;
        }
        final String ellipsis = "...";
        final StringBuilder truncated = new StringBuilder();
        for (final char c : text.toCharArray()) {
            final String candidate = truncated + String.valueOf(c) + ellipsis;
            if (textWidth(font, fontSize, candidate) > maxWidth) {
                break;
            }
            truncated.append(c);
        }
        return truncated + ellipsis;
    }

    /**
     * Replaces unsupported characters with a safe fallback to prevent PDFBox encoding errors.
     * NotoSans covers Latin Extended, but not all Unicode ranges.
     */
    private String sanitize(final String text) {
        if (text == null) {
            return "";
        }
        // Replace characters outside BMP (surrogate pairs) with '?'
        return text.replaceAll("[\\p{So}\\p{Cs}]", "?");
    }
}
