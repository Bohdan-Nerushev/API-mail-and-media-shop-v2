package dev.mam.buizsol.mamshop.billing.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.mam.buizsol.mamshop.billing.exception.InvalidInvoiceDiscountException;
import dev.mam.buizsol.mamshop.customer.model.Address;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invoices")
@JsonIgnoreProperties(
        value = {"hibernateLazyInitializer", "handler"},
        ignoreUnknown = true)
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "brand", nullable = false)
    private Brand brand;

    @NotNull
    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull
    @Valid
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @NotNull
    @Valid
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "invoice_address_id", nullable = false)
    private Address invoiceAddress;

    @NotNull
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<@Valid InvoiceItem> items = new ArrayList<>();

    @NotNull
    @Column(name = "total_setup_fee", nullable = false)
    private BigDecimal totalSetupFee;

    @NotNull
    @Column(name = "total_monthly_fee", nullable = false)
    private BigDecimal totalMonthlyFee;

    @NotNull
    @PositiveOrZero
    @Column(name = "discount", nullable = false)
    private BigDecimal discount;

    @NotNull
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    public Invoice(
            @NotNull final Brand brand,
            @NotNull final Customer customer,
            @NotNull @Valid final Address address,
            @NotNull @Valid final Address invoiceAddress,
            @NotNull final List<@Valid InvoiceItem> items,
            @NotNull final BigDecimal discount) {

        validateDiscount(discount);

        this.brand = brand;
        this.invoiceDate = LocalDate.now();
        this.customer = customer;
        this.address = address;
        this.invoiceAddress = invoiceAddress;
        this.items = new ArrayList<>(items);
        this.items.forEach(item -> item.setInvoice(this));
        this.discount = discount;
        this.totalSetupFee = calculateTotalSetupFee(items);
        this.totalMonthlyFee = calculateTotalMonthlyFee(items);
        this.totalAmount = calculateTotalAmount(this.totalSetupFee, this.totalMonthlyFee, discount);
    }

    private static void validateDiscount(BigDecimal discount) {
        if (discount.compareTo(BigDecimal.ZERO) < 0)
            throw new InvalidInvoiceDiscountException("Discount must not be negative");
    }

    private static BigDecimal calculateTotalSetupFee(List<InvoiceItem> items) {
        return items.stream().map(InvoiceItem::getSetupFee).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal calculateTotalMonthlyFee(List<InvoiceItem> items) {
        return items.stream().map(InvoiceItem::getMonthlyFee).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal calculateTotalAmount(BigDecimal setup, BigDecimal monthly, BigDecimal discount) {
        return setup.add(monthly).subtract(discount);
    }
}
