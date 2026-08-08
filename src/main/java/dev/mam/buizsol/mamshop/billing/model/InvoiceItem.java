package dev.mam.buizsol.mamshop.billing.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "invoice_items")
@JsonIgnoreProperties(
        value = {"hibernateLazyInitializer", "handler"},
        ignoreUnknown = true)
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @NotNull
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @NotBlank
    @Size(min = 1, max = 150)
    @Column(name = "product_name", nullable = false)
    private String productName;

    @NotNull
    @Column(name = "contract_creation_date", nullable = false)
    private LocalDate contractCreationDate;

    @NotNull
    @Column(name = "setup_fee", nullable = false)
    private BigDecimal setupFee;

    @NotNull
    @Column(name = "monthly_fee", nullable = false)
    private BigDecimal monthlyFee;

    public InvoiceItem(
            final UUID productId,
            final String productName,
            final Contract contract,
            final LocalDate contractCreationDate,
            final BigDecimal setupFee,
            final BigDecimal monthlyFee) {
        this.productId = productId;
        this.productName = productName;
        this.contract = contract;
        this.contractCreationDate = contractCreationDate;
        this.setupFee = setupFee;
        this.monthlyFee = monthlyFee;
    }
}
