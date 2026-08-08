package dev.mam.buizsol.mamshop.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequestDTO(
        @NotBlank @Size(max = 250) String street,
        @NotBlank @Size(max = 100) String number,
        @NotBlank @Size(max = 100) String postcode,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(min = 2, max = 100) String country) {}
