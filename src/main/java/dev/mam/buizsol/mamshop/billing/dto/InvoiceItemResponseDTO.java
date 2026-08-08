package dev.mam.buizsol.mamshop.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceItemResponseDTO(
        UUID productId,
        String productName,
        UUID contractId,
        LocalDate contractCreationDate,
        BigDecimal setupFee,
        BigDecimal monthlyFee) {}
