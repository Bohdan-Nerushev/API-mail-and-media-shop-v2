package dev.mam.buizsol.mamshop.shop.service;

import dev.mam.buizsol.mamshop.billing.model.Invoice;
import dev.mam.buizsol.mamshop.contract.exception.BrandMismatchException;
import dev.mam.buizsol.mamshop.contract.exception.ContractNotFoundException;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import dev.mam.buizsol.mamshop.customer.exception.CustomerNotFoundException;
import dev.mam.buizsol.mamshop.customer.model.Address;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.customer.model.CommunicationDetails;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.product.exception.ProductNotFoundException;
import dev.mam.buizsol.mamshop.product.model.Product;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;

@Validated
public interface ShopService {

    @NotNull
    Customer registerCustomer(@NotNull @Valid Customer customer);

    @NotNull
    Customer loadCustomer(@NotNull UUID customerId) throws CustomerNotFoundException;

    void removeCustomer(@NotNull UUID customerId) throws CustomerNotFoundException;

    void activateCustomer(@NotNull UUID customerId) throws CustomerNotFoundException;

    void deactivateCustomer(@NotNull UUID customerId) throws CustomerNotFoundException;

    @NotNull
    Customer updateAddress(@NotNull UUID customerId, @NotNull @Valid Address address) throws CustomerNotFoundException;

    @NotNull
    Customer updateInvoiceAddress(@NotNull UUID customerId, @NotNull @Valid Address invoiceAddress)
            throws CustomerNotFoundException;

    @NotNull
    Customer updateCommunicationDetails(@NotNull UUID customerId, @NotNull @Valid CommunicationDetails details)
            throws CustomerNotFoundException;

    @NotNull
    List<Contract> loadAllContracts(@NotNull UUID customerId) throws CustomerNotFoundException;

    @NotNull
    Invoice generateInvoice(@NotNull UUID customerId) throws CustomerNotFoundException, ProductNotFoundException;

    void activateContract(@NotNull UUID customerId, @NotNull UUID contractId)
            throws CustomerNotFoundException, ContractNotFoundException;

    @NotNull
    List<Product> loadAllProductsForBrand(@NotNull Brand brand);

    @NotNull
    Contract purchaseProduct(@NotNull UUID customerId, @NotNull UUID productId)
            throws CustomerNotFoundException, ProductNotFoundException, BrandMismatchException;
}
