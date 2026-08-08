package dev.mam.buizsol.mamshop.product.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.product.exception.ProductValidationException;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "bundle_products")
@JsonIgnoreProperties(
        value = {"hibernateLazyInitializer", "handler"},
        ignoreUnknown = true)
@DiscriminatorValue("BundleProduct")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BundleProduct extends Product {

    @Valid
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mail_product_id", nullable = false)
    private MailProduct mailProduct;

    @Valid
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_product_id", nullable = false)
    private PartnerProduct partnerProduct;

    public BundleProduct(
            @NotNull @Valid final MailProduct mailProduct, @NotNull @Valid final PartnerProduct partnerProduct) {
        super(
                UUID.randomUUID(),
                generateBundleName(mailProduct, partnerProduct),
                validateBrands(mailProduct, partnerProduct),
                calculateTotalSetupFee(mailProduct, partnerProduct),
                calculateTotalMonthlyFee(mailProduct, partnerProduct));
        this.mailProduct = mailProduct;
        this.partnerProduct = partnerProduct;
    }

    @Override
    @NotNull
    public BundleProduct withMonthlyFee(@NotNull final BigDecimal monthlyFee) {
        throw new UnsupportedOperationException("Monthly fee is calculated from components and cannot be set manually");
    }

    private static String generateBundleName(final Product mail, final Product partner) {
        if (mail == null || partner == null) {
            throw new ProductValidationException("Mail and Partner products must not be null");
        }
        return "Bundle: " + mail.getName() + " " + partner.getName();
    }

    private static BigDecimal calculateTotalSetupFee(final Product mail, final Product partner) {
        if (mail == null || partner == null) {
            return BigDecimal.ZERO;
        }
        return mail.getSetupFee().add(partner.getSetupFee());
    }

    private static BigDecimal calculateTotalMonthlyFee(final Product mail, final Product partner) {
        if (mail == null || partner == null) {
            return BigDecimal.ZERO;
        }
        return mail.getMonthlyFee().add(partner.getMonthlyFee());
    }

    private static Brand validateBrands(final Product mail, final Product partner) {
        if (mail == null || partner == null) {
            throw new ProductValidationException("Mail and Partner products must not be null");
        }
        if (mail.getBrand() != partner.getBrand()) {
            throw new ProductValidationException("Brands must match for bundle product");
        }
        return mail.getBrand();
    }
}
