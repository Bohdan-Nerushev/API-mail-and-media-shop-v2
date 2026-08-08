package dev.mam.buizsol.mamshop.product.dto;

import dev.mam.buizsol.mamshop.customer.model.Brand;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponseDTO(
        UUID id, String name, Brand brand, BigDecimal setupFee, BigDecimal monthlyFee, Long storageSize) {}
