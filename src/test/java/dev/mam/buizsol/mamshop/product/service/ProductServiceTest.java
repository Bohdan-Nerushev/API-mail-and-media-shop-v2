package dev.mam.buizsol.mamshop.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.product.exception.ProductNotFoundException;
import dev.mam.buizsol.mamshop.product.model.MailProduct;
import dev.mam.buizsol.mamshop.product.model.Product;
import jakarta.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationInterceptor;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        final ProductServiceImpl target = new ProductServiceImpl(productRepository);
        ReflectionTestUtils.setField(target, "minimalDiscountAmount", new BigDecimal("0.10"));

        final LocalValidatorFactoryBean validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.afterPropertiesSet();

        final ProxyFactory factory = new ProxyFactory();
        factory.setTarget(target);
        factory.addInterface(ProductService.class);
        factory.addAdvice(new MethodValidationInterceptor(validatorFactory.getValidator()));

        productService = (ProductService) factory.getProxy();
    }

    private Product createDefaultProduct(
            final String name, final Brand brand, final String setupFee, final String monthlyFee) {
        return new MailProduct(name, brand, new BigDecimal(setupFee), new BigDecimal(monthlyFee), 1024L);
    }

    @Test
    @DisplayName("Should successfully create product and retrieve by ID")
    void shouldSuccessfullyCreateProductAndRetrieveById() {
        final Product product = createDefaultProduct("Test Product", Brand.GMX, "1.00", "0.50");
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        productService.createProduct(product);
        final Optional<Product> foundProduct = productService.findById(product.getId());

        assertTrue(foundProduct.isPresent());
        assertEquals(product, foundProduct.get());
        verify(productRepository).save(product);
        verify(productRepository).findById(product.getId());
    }

    @Test
    @DisplayName("Should return empty Optional when product ID does not exist")
    void shouldReturnEmptyOptionalWhenProductIdDoesNotExist() {
        final UUID nonExistentId = UUID.randomUUID();
        when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        final Optional<Product> foundProduct = productService.findById(nonExistentId);

        assertTrue(foundProduct.isEmpty());
        verify(productRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when creating product with null value")
    void shouldThrowIllegalArgumentExceptionWhenCreatingProductWithNullValue() {
        assertThrows(ConstraintViolationException.class, () -> productService.createProduct(null));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when finding product by null ID")
    void shouldThrowIllegalArgumentExceptionWhenFindingProductByNullId() {
        assertThrows(ConstraintViolationException.class, () -> productService.findById(null));
    }

    @Test
    @DisplayName("Should find all products of specific brand")
    void shouldFindAllProductsOfSpecificBrand() {
        final Product gmxProduct1 = createDefaultProduct("GMX Product 1", Brand.GMX, "1.00", "0.50");
        final Product gmxProduct2 = createDefaultProduct("GMX Product 2", Brand.GMX, "2.00", "0.60");
        when(productRepository.findByBrand(Brand.GMX)).thenReturn(List.of(gmxProduct1, gmxProduct2));

        final Collection<Product> gmxProducts = productService.findByBrand(Brand.GMX);

        assertEquals(2, gmxProducts.size());
        assertTrue(gmxProducts.contains(gmxProduct1));
        assertTrue(gmxProducts.contains(gmxProduct2));
        verify(productRepository).findByBrand(Brand.GMX);
    }

    @Test
    @DisplayName("Should return empty collection when no products exist for brand")
    void shouldReturnEmptyCollectionWhenNoProductsExistForBrand() {
        when(productRepository.findByBrand(Brand.MAIL_COM)).thenReturn(List.of());

        final Collection<Product> mailComProducts = productService.findByBrand(Brand.MAIL_COM);

        assertNotNull(mailComProducts);
        assertTrue(mailComProducts.isEmpty());
        verify(productRepository).findByBrand(Brand.MAIL_COM);
    }

    @ParameterizedTest
    @EnumSource(Brand.class)
    @DisplayName("Should handle search for all available brands")
    void shouldHandleSearchForAllAvailableBrands(final Brand brand) {
        final Product product = createDefaultProduct(brand.name() + " Product", brand, "1.00", "0.50");
        when(productRepository.findByBrand(brand)).thenReturn(List.of(product));

        final Collection<Product> products = productService.findByBrand(brand);

        assertNotNull(products);
        assertFalse(products.isEmpty());
        assertTrue(products.stream().allMatch(p -> p.getBrand() == brand));
        verify(productRepository).findByBrand(brand);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when searching by null brand")
    void shouldThrowIllegalArgumentExceptionWhenSearchingByNullBrand() {
        assertThrows(ConstraintViolationException.class, () -> productService.findByBrand(null));
    }

    @Test
    @DisplayName("Should successfully update monthly fee to valid value")
    void shouldSuccessfullyUpdateMonthlyFeeToValidValue() throws Exception {
        final Product product = createDefaultProduct("Fee Test", Brand.MAIL_COM, "1.00", "0.50");
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        final BigDecimal newFee = new BigDecimal("0.75");
        productService.updateMonthlyFee(product.getId(), newFee);

        verify(productRepository).findById(product.getId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should successfully update monthly fee to minimum valid boundary value")
    void shouldSuccessfullyUpdateMonthlyFeeToMinimumValidBoundaryValue() throws Exception {
        final Product product = createDefaultProduct("Boundary Test", Brand.GMX, "1.00", "0.50");
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        final BigDecimal minimumValidFee = new BigDecimal("0.11");
        productService.updateMonthlyFee(product.getId(), minimumValidFee);

        verify(productRepository).findById(product.getId());
        verify(productRepository).save(any(Product.class));
    }

    @ParameterizedTest
    @CsvSource({"0.11, GMX", "0.50, WEB_DE", "1.00, MAIL_COM", "5.99, GMX", "10.00, WEB_DE", "99.99, MAIL_COM"})
    @DisplayName("Should update monthly fee for various valid values and brands")
    void shouldUpdateMonthlyFeeForVariousValidValuesAndBrands(final String feeValue, final Brand brand)
            throws Exception {
        final Product product = createDefaultProduct("Parameterized Test", brand, "1.00", "0.50");
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        final BigDecimal newFee = new BigDecimal(feeValue);
        productService.updateMonthlyFee(product.getId(), newFee);

        verify(productRepository).findById(product.getId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update monthly fee without affecting other products")
    void shouldUpdateMonthlyFeeWithoutAffectingOtherProducts() throws Exception {
        final Product product1 = createDefaultProduct("Product 1", Brand.GMX, "1.00", "0.50");
        when(productRepository.findById(product1.getId())).thenReturn(Optional.of(product1));

        final BigDecimal newFeeProduct1 = new BigDecimal("0.99");
        productService.updateMonthlyFee(product1.getId(), newFeeProduct1);

        verify(productRepository).findById(product1.getId());
        verify(productRepository).save(any(Product.class));
    }

    @ParameterizedTest
    @CsvSource({"0.10", "0.09", "0.05", "0.01", "0.00"})
    @DisplayName("Should throw IllegalArgumentException when monthly fee is at or below minimum threshold")
    void shouldThrowIllegalArgumentExceptionWhenMonthlyFeeIsAtOrBelowMinimumThreshold(final String feeValue) {
        final Product product = createDefaultProduct("Low Fee Test", Brand.GMX, "1.00", "0.50");

        final BigDecimal invalidFee = new BigDecimal(feeValue);
        UUID productId = product.getId();
        assertThrows(ConstraintViolationException.class, () -> productService.updateMonthlyFee(productId, invalidFee));
    }

    @ParameterizedTest
    @CsvSource({"-0.01", "-0.10", "-1.00", "-10.00", "-99.99"})
    @DisplayName("Should throw IllegalArgumentException when monthly fee is negative")
    void shouldThrowIllegalArgumentExceptionWhenMonthlyFeeIsNegative(final String feeValue) {
        final Product product = createDefaultProduct("Negative Fee Test", Brand.WEB_DE, "1.00", "0.50");

        final BigDecimal negativeFee = new BigDecimal(feeValue);
        UUID productId = product.getId();
        assertThrows(ConstraintViolationException.class, () -> productService.updateMonthlyFee(productId, negativeFee));
    }

    @Test
    @DisplayName("Should throw ProductValidationException when monthly fee fails manual business validation threshold")
    void shouldThrowProductValidationExceptionWhenMonthlyFeeFailsManualThreshold() {
        final ProductServiceImpl target = new ProductServiceImpl(productRepository);
        ReflectionTestUtils.setField(target, "minimalDiscountAmount", new BigDecimal("1.00"));

        final BigDecimal feeBelowManualButAboveAnnotation = new BigDecimal("0.50");
        final UUID productId = UUID.randomUUID();

        assertThrows(
                dev.mam.buizsol.mamshop.product.exception.ProductValidationException.class,
                () -> target.updateMonthlyFee(productId, feeBelowManualButAboveAnnotation));
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when updating non-existent product")
    void shouldThrowProductNotFoundExceptionWhenUpdatingNonExistentProduct() {
        final UUID nonExistentId = UUID.randomUUID();
        final BigDecimal validFee = new BigDecimal("1.00");
        when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        final ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class, () -> productService.updateMonthlyFee(nonExistentId, validFee));

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains(nonExistentId.toString()));
        verify(productRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when product ID is null")
    void shouldThrowIllegalArgumentExceptionWhenProductIdIsNull() {
        final BigDecimal validFee = new BigDecimal("1.00");

        assertThrows(ConstraintViolationException.class, () -> productService.updateMonthlyFee(null, validFee));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when monthly fee is null")
    void shouldThrowIllegalArgumentExceptionWhenMonthlyFeeIsNull() {
        final Product product = createDefaultProduct("Null Fee Test", Brand.MAIL_COM, "1.00", "0.50");
        UUID productId = product.getId();
        assertThrows(ConstraintViolationException.class, () -> productService.updateMonthlyFee(productId, null));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when both ID and fee are null")
    void shouldThrowIllegalArgumentExceptionWhenBothIdAndFeeAreNull() {
        assertThrows(ConstraintViolationException.class, () -> productService.updateMonthlyFee(null, null));
    }

    @Test
    @DisplayName("Should handle minimum valid monthly fee boundary (0.11)")
    void shouldHandleMinimumValidMonthlyFeeBoundary() {
        final BigDecimal minimumValidFee = new BigDecimal("0.11");
        final Product product =
                createDefaultProduct("Minimum Fee Product", Brand.GMX, "0.00", minimumValidFee.toString());
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        productService.createProduct(product);
        final Product retrievedProduct =
                productService.findById(product.getId()).get();

        assertEquals(0, minimumValidFee.compareTo(retrievedProduct.getMonthlyFee()));
        verify(productRepository).save(product);
        verify(productRepository).findById(product.getId());
    }

    @Test
    @DisplayName("Should reject maximum invalid monthly fee boundary (0.10)")
    void shouldRejectMaximumInvalidMonthlyFeeBoundary() {
        final Product product = createDefaultProduct("Boundary Test", Brand.GMX, "1.00", "0.50");

        final BigDecimal boundaryInvalidFee = new BigDecimal("0.10");
        UUID productId = product.getId();
        assertThrows(
                ConstraintViolationException.class,
                () -> productService.updateMonthlyFee(productId, boundaryInvalidFee));
    }

    @Test
    @DisplayName("Should handle large monthly fee values")
    void shouldHandleLargeMonthlyFeeValues() throws Exception {
        final Product product = createDefaultProduct("Large Fee Test", Brand.WEB_DE, "1.00", "0.50");
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        final BigDecimal largeFee = new BigDecimal("999999.99");
        productService.updateMonthlyFee(product.getId(), largeFee);

        verify(productRepository).findById(product.getId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should handle zero setup fee")
    void shouldHandleZeroSetupFee() {
        final Product product = createDefaultProduct("Zero Setup Fee", Brand.MAIL_COM, "0.00", "0.50");
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        productService.createProduct(product);
        final Product retrievedProduct =
                productService.findById(product.getId()).get();

        assertEquals(0, BigDecimal.ZERO.compareTo(retrievedProduct.getSetupFee()));
        verify(productRepository).save(product);
        verify(productRepository).findById(product.getId());
    }
}
