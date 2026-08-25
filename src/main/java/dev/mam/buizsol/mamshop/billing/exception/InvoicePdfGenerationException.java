package dev.mam.buizsol.mamshop.billing.exception;

public class InvoicePdfGenerationException extends BillingException {

    public InvoicePdfGenerationException(final String message, final Throwable cause) {
        super(message);
        initCause(cause);
    }
}
