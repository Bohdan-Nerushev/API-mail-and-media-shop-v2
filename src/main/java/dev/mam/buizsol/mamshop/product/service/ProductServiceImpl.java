package dev.mam.buizsol.mamshop.product.service;

import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.product.exception.ProductNotFoundException;
import dev.mam.buizsol.mamshop.product.exception.ProductValidationException;
import dev.mam.buizsol.mamshop.product.model.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    @Value("${billing.minimal-discount-amount}")
    private BigDecimal minimalDiscountAmount;

    ProductServiceImpl(final ProductRepository repository) {
        if (repository == null) {
            throw new ProductValidationException("Product repository must not be null");
        }
        this.repository = repository;
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void createProduct(final Product product) {
        if (product == null) {
            throw new ProductValidationException("Product must not be null");
        }
        repository.save(product);
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public Optional<Product> findById(final UUID id) {
        if (id == null) {
            throw new ProductValidationException("Product ID must not be null");
        }
        return repository.findById(id);
    }

    @Override
    @Cacheable(value = "productsByBrand", key = "#brand")
    public List<Product> findByBrand(final Brand brand) {
        if (brand == null) {
            throw new ProductValidationException("Brand must not be null");
        }
        return List.copyOf(repository.findByBrand(brand));
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(value = "products", key = "#id"),
                @CacheEvict(value = "productsByBrand", allEntries = true)
            })
    public void updateMonthlyFee(final UUID id, final BigDecimal monthlyFee) throws ProductNotFoundException {
        if (id == null) {
            throw new ProductValidationException("Product ID must not be null");
        }
        if (monthlyFee == null) {
            throw new ProductValidationException("Monthly fee must not be null");
        }
        if (monthlyFee.compareTo(minimalDiscountAmount) <= 0) {
            throw new ProductValidationException("Monthly fee must be greater than " + minimalDiscountAmount);
        }

        Product product = repository
                .findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with ID " + id + " not found"));

        product.setMonthlyFee(monthlyFee);
        repository.save(product);
    }
}
