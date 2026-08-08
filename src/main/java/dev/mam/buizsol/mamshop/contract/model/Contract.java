package dev.mam.buizsol.mamshop.contract.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.mam.buizsol.mamshop.contract.exception.BrandMismatchException;
import dev.mam.buizsol.mamshop.contract.exception.ContractValidationException;
import dev.mam.buizsol.mamshop.customer.exception.CustomerNotActiveException;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.customer.model.CustomerStatus;
import dev.mam.buizsol.mamshop.product.model.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contracts")
@JsonIgnoreProperties(
        value = {"hibernateLazyInitializer", "handler"},
        ignoreUnknown = true)
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull
    @Column(name = "product_type", nullable = false)
    private String productType;

    @NotNull
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @NotNull
    @Column(name = "creation_date", nullable = false)
    private LocalDate creationDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ContractStatus status;

    public static Contract create(@NotNull @Valid final Customer customer, @NotNull @Valid final Product product) {

        if (!customer.getBrand().equals(product.getBrand())) {
            throw new BrandMismatchException(String.format(
                    "Customer brand %s does not match product brand %s", customer.getBrand(), product.getBrand()));
        }
        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new CustomerNotActiveException("Customer is not active");
        }

        return Contract.builder()
                .customer(customer)
                .productType(product.getName())
                .productId(product.getId())
                .creationDate(LocalDate.now())
                .status(ContractStatus.INACTIVE)
                .build();
    }

    @NotNull
    public Contract withStatus(@NotNull final ContractStatus newStatus) {
        if (newStatus == null) {
            throw new ContractValidationException("Status must not be null");
        }
        this.setStatus(newStatus);
        return this;
    }
}
