package dev.mam.buizsol.mamshop.customer.controller;

import dev.mam.buizsol.mamshop.config.ErrorResponse;
import dev.mam.buizsol.mamshop.customer.dto.AddressRequestDTO;
import dev.mam.buizsol.mamshop.customer.dto.CommunicationDetailsRequestDTO;
import dev.mam.buizsol.mamshop.customer.mapper.CustomerMapper;
import dev.mam.buizsol.mamshop.customer.model.Address;
import dev.mam.buizsol.mamshop.customer.model.CommunicationDetails;
import dev.mam.buizsol.mamshop.shop.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Customer", description = "Customer API")
@RestController
@RequestMapping(value = "/api/v1/customers")
public class CustomerController {

    private final ShopService shopService;
    private final CustomerMapper customerMapper;

    public CustomerController(final ShopService shopService, final CustomerMapper customerMapper) {
        this.shopService = shopService;
        this.customerMapper = customerMapper;
    }

    @Operation(
            summary = "Deactivate a customer by ID",
            description = "Changes the status of the specified customer to INACTIVE.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Customer deactivated successfully"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Customer not found",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PutMapping(value = "/{customerId}/deactivate")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void deactivateCustomer(@PathVariable(value = "customerId") @NotNull final UUID customerId) {
        log.debug("Deactivating customer: {}", customerId);

        shopService.deactivateCustomer(customerId);
        log.info("Customer deactivated successfully: {}", customerId);
    }

    @Operation(
            summary = "Update Address of Customer by id",
            description = "Updates the address of the specified customer.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Address updated successfully"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Customer not found",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PutMapping(value = "/{customerId}/address")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void updateAddress(
            @PathVariable(value = "customerId") @NotNull final UUID customerId,
            @RequestBody @Valid final AddressRequestDTO addressRequestDTO) {
        log.debug("Updating address for customer: {}", customerId);

        final Address address = customerMapper.toAddress(addressRequestDTO);
        log.debug("New Address: {}", address);

        shopService.updateAddress(customerId, address);
        log.info("Address updated successfully: {}", customerId);
    }

    @Operation(
            summary = "Update Invoice Address of Customer by id",
            description = "Updates the invoice address of the specified customer.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Invoice address updated successfully"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Customer not found",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PutMapping(value = "/{customerId}/invoice-address")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void updateInvoiceAddress(
            @PathVariable(value = "customerId") @NotNull final UUID customerId,
            @RequestBody @Valid final AddressRequestDTO addressRequestDTO) {
        log.debug("Updating invoice address for customer: {}", customerId);

        final Address address = customerMapper.toAddress(addressRequestDTO);
        log.debug("New Invoice Address: {}", address);

        shopService.updateInvoiceAddress(customerId, address);
        log.info("Invoice address updated successfully: {}", customerId);
    }

    @Operation(
            summary = "Update Communication Details of Customer by id",
            description = "Updates the communication details of the specified customer.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Communication details updated successfully"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Customer not found",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PutMapping(value = "/{customerId}/communication-details")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void updateCommunicationDetails(
            @PathVariable(value = "customerId") @NotNull final UUID customerId,
            @RequestBody @Valid final CommunicationDetailsRequestDTO communicationDetailsRequestDTO) {
        log.debug("Updating communication details for customer: {}", customerId);

        final CommunicationDetails communicationDetails =
                customerMapper.toCommunicationDetails(communicationDetailsRequestDTO);
        log.debug("New Communication Details: {}", communicationDetails);

        shopService.updateCommunicationDetails(customerId, communicationDetails);
        log.info("Communication details updated successfully: {}", customerId);
    }
}
