package dev.mam.buizsol.mamshop.product.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "partner_products")
@JsonIgnoreProperties(
        value = {"hibernateLazyInitializer", "handler"},
        ignoreUnknown = true)
@DiscriminatorValue("PartnerProduct")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PartnerProduct extends Product {

    public PartnerProduct(
            @NotNull final String name,
            @NotNull final Brand brand,
            @NotNull final BigDecimal setupFee,
            @NotNull final BigDecimal monthlyFee) {
        super(UUID.randomUUID(), name, brand, setupFee, monthlyFee);
    }

    public PartnerProduct(
            final UUID id,
            @NotNull final String name,
            @NotNull final Brand brand,
            @NotNull final BigDecimal setupFee,
            @NotNull final BigDecimal monthlyFee) {
        super(id, name, brand, setupFee, monthlyFee);
    }

    @Override
    public PartnerProduct withMonthlyFee(@NotNull final BigDecimal monthlyFee) {
        return this.toBuilder().monthlyFee(monthlyFee).build();
    }
}
