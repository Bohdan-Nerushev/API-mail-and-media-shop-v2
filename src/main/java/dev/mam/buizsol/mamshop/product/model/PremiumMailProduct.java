package dev.mam.buizsol.mamshop.product.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "premium_mail_products")
@JsonIgnoreProperties(
        value = {"hibernateLazyInitializer", "handler"},
        ignoreUnknown = true)
@DiscriminatorValue("PremiumMailProduct")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PremiumMailProduct extends MailProduct {

    private static final BigDecimal FIXED_SETUP_FEE = new BigDecimal("9.99");
    private static final Long FIXED_STORAGE_SIZE = 8L;

    public PremiumMailProduct(
            @NotNull final String name, @NotNull final Brand brand, @NotNull final BigDecimal monthlyFee) {
        super(UUID.randomUUID(), name, brand, FIXED_SETUP_FEE, monthlyFee, FIXED_STORAGE_SIZE);
    }

    public PremiumMailProduct(
            final UUID id,
            @NotNull final String name,
            @NotNull final Brand brand,
            @NotNull final BigDecimal setupFee,
            @NotNull final BigDecimal monthlyFee,
            @NotNull final Long storageSize) {
        super(id, name, brand, setupFee, monthlyFee, storageSize);
    }

    @Override
    public PremiumMailProduct withMonthlyFee(@NotNull final BigDecimal monthlyFee) {
        return this.toBuilder().monthlyFee(monthlyFee).build();
    }
}
