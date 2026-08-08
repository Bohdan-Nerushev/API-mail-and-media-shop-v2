package dev.mam.buizsol.mamshop.contract.service;

import dev.mam.buizsol.mamshop.contract.model.Contract;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContractRepository extends JpaRepository<Contract, UUID> {

    @NotNull
    @EntityGraph(
            attributePaths = {"customer", "customer.address", "customer.invoiceAddress", "customer.communicationDetails"
            })
    List<Contract> findByCustomerId(@NotNull UUID customerId);

    @NotNull
    @EntityGraph(
            attributePaths = {"customer", "customer.address", "customer.invoiceAddress", "customer.communicationDetails"
            })
    Optional<Contract> findWithDetailsById(@NotNull UUID id);

    @NotNull
    List<Contract> findByProductId(@NotNull UUID productId);
}
