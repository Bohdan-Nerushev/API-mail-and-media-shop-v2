package dev.mam.buizsol.mamshop.customer.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PurchaseRequestDTO(@NotNull UUID productId) {}
