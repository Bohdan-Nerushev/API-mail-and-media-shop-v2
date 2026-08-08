package dev.mam.buizsol.mamshop.shop.service;

import dev.mam.buizsol.mamshop.config.ErrorResponse;
import dev.mam.buizsol.mamshop.contract.dto.ContractResponseDTO;
import dev.mam.buizsol.mamshop.contract.mapper.ContractMapper;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import dev.mam.buizsol.mamshop.customer.dto.CustomerRequestDTO;
import dev.mam.buizsol.mamshop.customer.dto.CustomerResponseDTO;
import dev.mam.buizsol.mamshop.customer.dto.PurchaseRequestDTO;
import dev.mam.buizsol.mamshop.customer.mapper.CustomerMapper;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.product.dto.ProductResponseDTO;
import dev.mam.buizsol.mamshop.product.mapper.ProductMapper;
import dev.mam.buizsol.mamshop.product.model.Product;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@Tag(name = "Shop", description = "Shop API")
@RestController
@RequestMapping(value = "/api/v1/shop")
public class ShopController {

    private final ShopService shopService;
    private final CustomerMapper customerMapper;
    private final ProductMapper productMapper;
    private final ContractMapper contractMapper;

    public ShopController(
            final ShopService shopService,
            final CustomerMapper customerMapper,
            final ProductMapper productMapper,
            final ContractMapper contractMapper) {
        this.shopService = shopService;
        this.customerMapper = customerMapper;
        this.productMapper = productMapper;
        this.contractMapper = contractMapper;
    }

    @Operation(summary = "Register a new customer", description = "Creates a new customer with the given data.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Customer registered successfully",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CustomerResponseDTO.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid customer data",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PostMapping(value = "/customers")
    @ResponseStatus(value = HttpStatus.CREATED)
    public @NotNull CustomerResponseDTO registerCustomer(
            @RequestBody @Valid final CustomerRequestDTO customerRequestDTO) {
        log.debug("Registering customer: {}", customerRequestDTO);

        final Customer customer = customerMapper.toCustomer(customerRequestDTO);
        log.debug("Customer: {}", customer);

        final Customer registeredCustomer = shopService.registerCustomer(customer);
        log.debug("Registered customer: {}", registeredCustomer);

        final CustomerResponseDTO customerResponseDTO = customerMapper.toResponseDTO(registeredCustomer);
        log.debug("Customer response DTO: {}", customerResponseDTO);

        log.info("Customer registered successfully: {}", customerResponseDTO);
        return customerResponseDTO;
    }

    @Operation(summary = "Load a customer by ID", description = "Returns the customer with the specified ID.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Customer loaded successfully",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CustomerResponseDTO.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Customer not found",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GetMapping(value = "/customers/{customerId}")
    @PreAuthorize("hasRole('USER')")
    public @NotNull CustomerResponseDTO loadCustomerByCustomerId(
            @PathVariable(value = "customerId") @NotNull final UUID customerId) {
        log.debug("Loading customer: {}", customerId);

        final Customer customer = shopService.loadCustomer(customerId);
        log.debug("Customer: {}", customer);

        final CustomerResponseDTO customerResponseDTO = customerMapper.toResponseDTO(customer);
        log.debug("Customer response DTO: {}", customerResponseDTO);

        log.info("Customer loaded successfully: {}", customerResponseDTO);
        return customerResponseDTO;
    }

    @Operation(summary = "Remove a customer by ID", description = "Customer is deleted by their ID.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "Customer removed successfully"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Customer not found",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)))
            })
    @DeleteMapping(value = "/customers/{customerId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void removeCustomerByCustomerId(@PathVariable(value = "customerId") @NotNull final UUID customerId) {
        log.debug("Removing customer: {}", customerId);

        shopService.removeCustomer(customerId);
        log.info("Customer removed successfully: {}", customerId);
    }

    @Operation(
            summary = "Activate a customer by ID",
            description = "Changes the status of the specified customer to ACTIVE.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "Customer activated successfully"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Customer not found",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PutMapping(value = "/customers/{customerId}/activate")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void activateCustomerByCustomerId(@PathVariable(value = "customerId") @NotNull final UUID customerId) {
        log.debug("Activating customer: {}", customerId);

        shopService.activateCustomer(customerId);
        log.info("Customer activated successfully: {}", customerId);
    }

    @Operation(summary = "Load all products for a brand", description = "Returns all products for the specified brand.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Products loaded successfully",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProductResponseDTO.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Brand not found",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GetMapping(value = "/products")
    public @NotNull List<ProductResponseDTO> loadAllProductsForBrand(
            @RequestParam(value = "brand") @NotNull final Brand brand) {
        log.debug("Loading all products for brand: {}", brand);

        final List<Product> productList = shopService.loadAllProductsForBrand(brand);
        log.debug("Product list: {}", productList);

        final List<ProductResponseDTO> productResponseDTOList =
                productList.stream().map(productMapper::toProductResponseDTO).toList();
        log.debug("Product response DTO list: {}", productResponseDTOList);

        log.info("Products loaded successfully: {}", productResponseDTOList);
        return productResponseDTOList;
    }

    @Operation(
            summary = "Purchase a product",
            description = "Creates a new contract for the specified customer and product.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Product purchased successfully",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ContractResponseDTO.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Customer or product not found",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid purchase data",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PostMapping(value = "/customers/{customerId}/purchases")
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public @NotNull ContractResponseDTO purchaseProduct(
            @PathVariable(value = "customerId") @NotNull final UUID customerId,
            @RequestBody @Valid final PurchaseRequestDTO request) {
        log.debug("Purchasing product for customer: {}", customerId);

        final Contract contract = shopService.purchaseProduct(customerId, request.productId());
        log.debug("Contract: {}", contract);

        final ContractResponseDTO contractResponseDTO = contractMapper.toContractResponseDTO(contract);
        log.debug("Contract response DTO: {}", contractResponseDTO);

        log.info("Product purchased successfully: {}", contractResponseDTO);
        return contractResponseDTO;
    }

    @Operation(
            summary = "Load all contracts for a customer",
            description = "Returns all contracts associated with the specified customer ID.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Contracts loaded successfully",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ContractResponseDTO.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Customer not found",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GetMapping(value = "/contracts/{customerId}")
    @PreAuthorize("hasRole('USER')")
    public @NotNull List<ContractResponseDTO> loadAllContractsByCustomerId(
            @PathVariable(value = "customerId") @NotNull final UUID customerId) {
        log.debug("Loading all contracts for customer: {}", customerId);

        final List<Contract> contractList = shopService.loadAllContracts(customerId);
        log.debug("Contract list: {}", contractList);

        final List<ContractResponseDTO> contractResponseDTOList =
                contractList.stream().map(contractMapper::toContractResponseDTO).toList();
        log.debug("Contract response DTO list: {}", contractResponseDTOList);

        log.info("Contracts loaded successfully: {}", contractResponseDTOList);
        return contractResponseDTOList;
    }
}
