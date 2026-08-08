package dev.mam.buizsol.mamshop.customer.dto;

import dev.mam.buizsol.mamshop.customer.model.Brand;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CustomerRequestDTO(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotNull @Past LocalDate birthDate,
        @NotNull @Valid AddressRequestDTO address,
        @Nullable @Valid AddressRequestDTO invoiceAddress,
        @NotNull @Valid CommunicationDetailsRequestDTO communicationDetails,
        @NotNull Brand brand) {}
