package dev.mam.buizsol.mamshop.product.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.product.validation.ValidStorageSize;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "mail_products")
@JsonIgnoreProperties(
        value = {"hibernateLazyInitializer", "handler"},
        ignoreUnknown = true)
@DiscriminatorValue("MailProduct")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MailProduct extends Product {

    @NotNull
    @ValidStorageSize
    @Column(name = "storage_size", nullable = false)
    private Long storageSize;

    public MailProduct(
            final UUID id,
            @NotNull final String name,
            @NotNull final Brand brand,
            @NotNull final BigDecimal setupFee,
            @NotNull final BigDecimal monthlyFee,
            @NotNull final Long storageSize) {
        super(id, name, brand, setupFee, monthlyFee);
        this.storageSize = storageSize;
    }

    public MailProduct(
            @NotNull final String name,
            @NotNull final Brand brand,
            @NotNull final BigDecimal setupFee,
            @NotNull final BigDecimal monthlyFee,
            @NotNull final Long storageSize) {
        this(UUID.randomUUID(), name, brand, setupFee, monthlyFee, storageSize);
    }

    public static MailProduct create(
            final String name,
            final Brand brand,
            final BigDecimal setupFee,
            final BigDecimal monthlyFee,
            final Long storageSize) {
        return new MailProduct(name, brand, setupFee, monthlyFee, storageSize);
    }

    @Override
    public Optional<Long> getStorageSize() {
        return Optional.ofNullable(storageSize);
    }

    @Override
    public MailProduct withMonthlyFee(@NotNull final BigDecimal monthlyFee) {
        return this.toBuilder().monthlyFee(monthlyFee).build();
    }
}
