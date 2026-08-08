package dev.mam.buizsol.mamshop.product.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.product.dto.ProductResponseDTO;
import dev.mam.buizsol.mamshop.product.model.MailProduct;
import dev.mam.buizsol.mamshop.product.model.PartnerProduct;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductMapperTest {

    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        productMapper = new ProductMapper();
    }

    @Test
    @DisplayName("Should map MailProduct to ProductResponseDTO with storage size")
    void shouldMapMailProductToResponseDTO() {
        final UUID id = UUID.randomUUID();
        final MailProduct product = MailProduct.builder()
                .id(id)
                .name("Mail Plan L")
                .brand(Brand.GMX)
                .setupFee(new BigDecimal("9.99"))
                .monthlyFee(new BigDecimal("4.99"))
                .storageSize(50L)
                .build();

        final ProductResponseDTO result = productMapper.toProductResponseDTO(product);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Mail Plan L");
        assertThat(result.brand()).isEqualTo(Brand.GMX);
        assertThat(result.setupFee()).isEqualByComparingTo("9.99");
        assertThat(result.monthlyFee()).isEqualByComparingTo("4.99");
        assertThat(result.storageSize()).isEqualTo(50L);
    }

    @Test
    @DisplayName("Should map PartnerProduct to ProductResponseDTO without storage size")
    void shouldMapPartnerProductToResponseDTO() {
        final UUID id = UUID.randomUUID();
        final PartnerProduct product = PartnerProduct.builder()
                .id(id)
                .name("Partner Service")
                .brand(Brand.WEB_DE)
                .setupFee(BigDecimal.ZERO)
                .monthlyFee(new BigDecimal("19.99"))
                .build();

        final ProductResponseDTO result = productMapper.toProductResponseDTO(product);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.storageSize()).isNull();
    }
}
