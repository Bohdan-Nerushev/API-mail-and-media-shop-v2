package dev.mam.buizsol.mamshop.product.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCatalogLoader Tests")
class ProductCatalogLoaderTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductCatalogLoader productCatalogLoader;

    @Test
    @DisplayName("Should load products from database")
    void shouldLoadProductsFromDatabase() {
        when(productRepository.findAll()).thenReturn(List.of());
        productCatalogLoader.load();
        verify(productRepository).findAll();
    }
}
