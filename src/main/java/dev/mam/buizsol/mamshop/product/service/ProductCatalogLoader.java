package dev.mam.buizsol.mamshop.product.service;

import dev.mam.buizsol.mamshop.product.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ProductCatalogLoader {

    private final ProductRepository productRepository;

    public ProductCatalogLoader(final ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void load() {
        log.info("Initializing product catalog from database...");
        final List<Product> products = productRepository.findAll();
        log.info("Successfully loaded {} products from database.", products.size());
    }
}
