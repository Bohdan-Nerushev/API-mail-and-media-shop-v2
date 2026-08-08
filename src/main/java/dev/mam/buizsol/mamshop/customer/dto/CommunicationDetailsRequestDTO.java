package dev.mam.buizsol.mamshop.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommunicationDetailsRequestDTO(
        @Email(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
                message = "Email contains invalid characters or invalid format")
        @NotBlank(message = "Email is mandatory")
        @Size(max = 255)
        String email,

        @NotBlank(message = "Telephone is mandatory") @Size(min = 5, max = 30)
        String telephone) {}
