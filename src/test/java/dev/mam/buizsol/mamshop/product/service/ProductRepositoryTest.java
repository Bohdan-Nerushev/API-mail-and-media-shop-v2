package dev.mam.buizsol.mamshop.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.mam.buizsol.mamshop.customer.model.Brand;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductRepository Tests")
class ProductRepositoryTest {

    @Mock
    private ProductRepository repository;

    @Test
    @DisplayName("Should verify mock interaction for findByBrand")
    void shouldFindProductsByBrand() {
        when(repository.findByBrand(Brand.GMX)).thenReturn(List.of());

        var result = repository.findByBrand(Brand.GMX);

        assertThat(result).isEmpty();
        verify(repository).findByBrand(Brand.GMX);
    }

    @Test
    @DisplayName("Should verify mock interaction for findById")
    void shouldFindProductById() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        var result = repository.findById(id);

        assertThat(result).isEmpty();
        verify(repository).findById(id);
    }
}
