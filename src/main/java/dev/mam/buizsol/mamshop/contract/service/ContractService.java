package dev.mam.buizsol.mamshop.contract.service;

import dev.mam.buizsol.mamshop.contract.exception.ContractNotFoundException;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import dev.mam.buizsol.mamshop.contract.model.ContractStatus;
import dev.mam.buizsol.mamshop.contract.validation.BrandMatch;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.product.model.Product;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;

@Transactional
@Validated
public interface ContractService {

    @NotNull
    @BrandMatch
    Contract createContract(@NotNull @Valid Customer customer, @NotNull @Valid Product product);

    @NotNull
    Optional<Contract> findContractById(@NotNull UUID id);

    @NotNull
    List<Contract> findContractsByCustomerId(@NotNull UUID customerId);

    @NotNull
    List<Contract> findContractsByProductId(@NotNull UUID productId);

    @NotNull
    Contract updateContractStatus(@NotNull UUID contractId, @NotNull ContractStatus status)
            throws ContractNotFoundException;

    @NotNull
    List<Contract> findAllContracts();
}
