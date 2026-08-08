package dev.mam.buizsol.mamshop.contract.dto;

import dev.mam.buizsol.mamshop.contract.model.ContractStatus;

import java.time.LocalDate;
import java.util.UUID;

public record ContractResponseDTO(
        UUID id, UUID customerId, UUID productId, LocalDate creationDate, ContractStatus status) {
}
