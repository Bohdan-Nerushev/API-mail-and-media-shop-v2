package dev.mam.buizsol.mamshop.product.service;

import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.product.exception.ProductNotFoundException;
import dev.mam.buizsol.mamshop.product.model.Product;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Validated
public interface ProductService {

    void createProduct(@NotNull @Valid Product product);

    @NotNull
    Optional<Product> findById(@NotNull UUID id);

    @NotNull
    List<Product> findByBrand(@NotNull Brand brand);

    void updateMonthlyFee(
            @NotNull UUID id, @NotNull @DecimalMin(value = "0.10", inclusive = false) BigDecimal monthlyFee)
            throws ProductNotFoundException;
}
