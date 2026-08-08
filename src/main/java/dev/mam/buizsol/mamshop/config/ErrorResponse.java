package dev.mam.buizsol.mamshop.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ErrorResponse(
        @NotNull String correlationId,
        @NotNull String errorCode,
        @NotBlank String message,
        @NotNull LocalDateTime timestamp) {
}
