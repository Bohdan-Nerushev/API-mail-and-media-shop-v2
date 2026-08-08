package dev.mam.buizsol.mamshop.billing.service;

import dev.mam.buizsol.mamshop.billing.model.Invoice;
import dev.mam.buizsol.mamshop.billing.validation.InvoiceDiscount;
import dev.mam.buizsol.mamshop.customer.exception.CustomerNotFoundException;
import dev.mam.buizsol.mamshop.product.exception.ProductNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;

@Transactional
@Validated
public interface BillingService {

    @NotNull
    Invoice generateInvoice(@NotNull UUID customerId) throws CustomerNotFoundException, ProductNotFoundException;

    @NotNull
    Invoice generateInvoice(@NotNull UUID customerId, @NotNull @InvoiceDiscount BigDecimal discount)
            throws CustomerNotFoundException, ProductNotFoundException;
}
