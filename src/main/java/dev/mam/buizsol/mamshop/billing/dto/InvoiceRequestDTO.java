package dev.mam.buizsol.mamshop.billing.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record InvoiceRequestDTO(@NotNull UUID customerId) {}
