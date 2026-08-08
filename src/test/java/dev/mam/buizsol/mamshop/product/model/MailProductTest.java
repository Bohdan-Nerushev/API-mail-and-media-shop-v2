package dev.mam.buizsol.mamshop.product.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.product.exception.ProductValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("MailProduct Tests")
class MailProductTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    private StandardMailProduct createDefaultStandardMailProduct(String name, Brand brand, BigDecimal monthlyFee) {
        StandardMailProduct product = new StandardMailProduct(name, brand, monthlyFee);
        validate(product);
        return product;
    }

    private PremiumMailProduct createDefaultPremiumMailProduct(String name, Brand brand, BigDecimal monthlyFee) {
        PremiumMailProduct product = new PremiumMailProduct(name, brand, monthlyFee);
        validate(product);
        return product;
    }

    private MailProduct createDefaultMailProduct(
            String name, Brand brand, BigDecimal setupFee, BigDecimal monthlyFee, Long storageSize) {
        MailProduct product = new MailProduct(name, brand, setupFee, monthlyFee, storageSize);
        validate(product);
        return product;
    }

    private void validate(final Product product) {
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        if (violations.isEmpty()) {
            return;
        }
        String message = violations.iterator().next().getMessage();
        throw new ProductValidationException(message);
    }

    @Test
    @DisplayName("Verify StandardMailProduct fixed attributes (Setup Fee, Storage)")
    void shouldVerifyStandardMailProductFixedAttributes() {
        final StandardMailProduct product =
                createDefaultStandardMailProduct("Standard Mail Base", Brand.GMX, new BigDecimal("1.99"));

        assertNotNull(product.getId());
        assertEquals(new BigDecimal("4.99"), product.getSetupFee());
        assertEquals(4L, product.getStorageSize().orElseThrow());
    }

    @Test
    @DisplayName("Verify PremiumMailProduct fixed attributes (Setup Fee, Storage)")
    void shouldVerifyPremiumMailProductFixedAttributes() {
        final PremiumMailProduct product =
                createDefaultPremiumMailProduct("Premium Mail Pro", Brand.WEB_DE, new BigDecimal("5.99"));

        assertNotNull(product.getId());
        assertEquals(new BigDecimal("9.99"), product.getSetupFee());
        assertEquals(8L, product.getStorageSize().orElseThrow());
    }

    @Test
    @DisplayName("Verify MailProduct success at boundary minimum monthly fee (0.11€)")
    void shouldCreateMailProductWhenMonthlyFeeIsAtMinimumAllowed() {

        final BigDecimal minFee = new BigDecimal("0.11");

        final StandardMailProduct product = createDefaultStandardMailProduct("Budget Mail", Brand.GMX, minFee);

        assertEquals(minFee, product.getMonthlyFee());
    }

    @DisplayName("Verify MailProduct failure when monthly fee is below or at the limit (<= 0.10€)")
    @ParameterizedTest(name = "[{index}] Monthly fee {0} € should be invalid")
    @ValueSource(strings = {"0.10", "0.09", "0.00", "-0.01", "-100.00"})
    void shouldThrowExceptionWhenMailProductMonthlyFeeIsInvalid(final String feeString) {

        final BigDecimal invalidFee = new BigDecimal(feeString);

        assertThrows(
                ProductValidationException.class,
                () -> createDefaultStandardMailProduct("Invalid Fee Mail", Brand.WEB_DE, invalidFee));
    }

    @DisplayName("Verify MailProduct failure with null, empty or blank name")
    @ParameterizedTest(name = "[{index}] Name: ''{0}''")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "", "\t", "\n"})
    void shouldThrowExceptionWhenMailProductNameIsInvalid(final String invalidName) {
        BigDecimal monthlyFee = new BigDecimal("2.50");
        assertThrows(
                ProductValidationException.class,
                () -> createDefaultStandardMailProduct(invalidName, Brand.GMX, monthlyFee));
    }

    @Test
    @DisplayName("Verify MailProduct failure with null brand")
    void shouldThrowExceptionWhenMailProductBrandIsNull() {
        BigDecimal monthlyFee = new BigDecimal("3.00");
        assertThrows(
                ProductValidationException.class,
                () -> createDefaultStandardMailProduct("No Brand Mail", null, monthlyFee));
    }

    @Test
    @DisplayName("Verify MailProduct failure with null monthly fee")
    void shouldThrowExceptionWhenMailProductMonthlyFeeIsNull() {

        assertThrows(
                ProductValidationException.class,
                () -> createDefaultStandardMailProduct("No Fee Mail", Brand.GMX, null));
    }

    @DisplayName("Verify MailProduct success with various valid and large monthly fees")
    @ParameterizedTest(name = "[{index}] Monthly fee {0} €")
    @CsvSource(value = {"0.11", "1", "10.50", "999.99", "1000000"})
    void shouldCreateMailProductWhenMonthlyFeeIsValid(final BigDecimal validFee) {

        final StandardMailProduct product =
                createDefaultStandardMailProduct("Dynamic Pricing Mail", Brand.WEB_DE, validFee);

        assertEquals(validFee, product.getMonthlyFee());
    }

    @DisplayName("Verify PremiumMailProduct failure when monthly fee is below or at the limit (<= 0.10€)")
    @ParameterizedTest(name = "[{index}] Monthly fee {0} € should be invalid")
    @ValueSource(strings = {"0.10", "0.09", "0.00", "-0.01", "-100.00"})
    void shouldThrowExceptionWhenPremiumMailProductMonthlyFeeIsInvalid(final String feeString) {

        final BigDecimal invalidFee = new BigDecimal(feeString);

        assertThrows(
                ProductValidationException.class,
                () -> createDefaultPremiumMailProduct("Premium Invalid Fee", Brand.WEB_DE, invalidFee));
    }

    @DisplayName("Verify PremiumMailProduct failure with null, empty or blank name")
    @ParameterizedTest(name = "[{index}] Name: ''{0}''")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    void shouldThrowExceptionWhenPremiumMailProductNameIsInvalid(final String invalidName) {
        BigDecimal monthlyFee = new BigDecimal("9.99");
        assertThrows(
                ProductValidationException.class,
                () -> createDefaultPremiumMailProduct(invalidName, Brand.WEB_DE, monthlyFee));
    }

    @DisplayName("Verify PremiumMailProduct success with various valid monthly fees")
    @ParameterizedTest(name = "[{index}] Monthly fee {0} €")
    @CsvSource(value = {"0.11", "1", "10.50", "99.99", "1000", "10000.50"})
    void shouldCreatePremiumMailProductWhenMonthlyFeeIsValid(final BigDecimal validFee) {

        final PremiumMailProduct product = createDefaultPremiumMailProduct("Premium High Tier", Brand.WEB_DE, validFee);

        assertEquals(validFee, product.getMonthlyFee());
    }

    @DisplayName("Verify StandardMailProduct creation with all available brands")
    @ParameterizedTest(name = "[{index}] Brand: {0}")
    @ValueSource(strings = {"GMX", "WEB_DE"})
    void shouldCreateStandardMailProductForAllBrands(String brandName) {
        Brand brand = Brand.valueOf(brandName);
        final StandardMailProduct product =
                createDefaultStandardMailProduct("Brand Test Mail", brand, new BigDecimal("4.50"));

        assertEquals(brand, product.getBrand());
    }

    @Test
    @DisplayName("Verify PremiumMailProduct failure with null brand")
    void shouldThrowExceptionWhenPremiumMailProductBrandIsNull() {
        BigDecimal monthlyFee = new BigDecimal("12.00");
        assertThrows(
                ProductValidationException.class,
                () -> createDefaultPremiumMailProduct("Premium Null Brand", null, monthlyFee));
    }

    @Test
    @DisplayName("Verify PremiumMailProduct success at boundary minimum monthly fee (0.11€)")
    void shouldCreatePremiumMailProductWhenMonthlyFeeIsAtMinimumAllowed() {

        final BigDecimal minFee = new BigDecimal("0.11");

        final PremiumMailProduct product = createDefaultPremiumMailProduct("Premium Budget", Brand.WEB_DE, minFee);

        assertEquals(minFee, product.getMonthlyFee());
    }

    @Test
    @DisplayName("Negative: Failure with invalid storage size (< 1GB)")
    void shouldThrowExceptionWhenStorageSizeIsInvalid() {
        assertThrows(
                ProductValidationException.class,
                () -> createDefaultMailProduct("Small Storage", Brand.GMX, BigDecimal.ZERO, BigDecimal.ONE, 0L));
    }

    @Test
    @DisplayName("Negative: Failure with invalid storage size (< 1GB)")
    void shouldThrowProductValidationExceptionWhenStorageSizeIsInvalid() {
        assertThrows(
                ProductValidationException.class,
                () -> createDefaultMailProduct("Small Storage", Brand.GMX, BigDecimal.ZERO, BigDecimal.ONE, 0L));
        assertThrows(
                ProductValidationException.class,
                () -> createDefaultMailProduct("Small Storage", Brand.GMX, BigDecimal.ZERO, BigDecimal.ONE, null));
    }

    @Test
    @DisplayName("Negative: StandardMailProduct failure with too long name (> 100 characters)")
    void shouldThrowExceptionWhenStandardMailProductNameIsTooLong() {
        String longName = "A".repeat(101);
        ProductValidationException exception = assertThrows(
                ProductValidationException.class,
                () -> createDefaultStandardMailProduct(longName, Brand.GMX, BigDecimal.ONE));
        assertEquals("Product name must not exceed 100 characters", exception.getMessage());
    }

    @Test
    @DisplayName("Negative: PremiumMailProduct failure with too long name (> 100 characters)")
    void shouldThrowExceptionWhenPremiumMailProductNameIsTooLong() {
        String longName = "A".repeat(101);
        ProductValidationException exception = assertThrows(
                ProductValidationException.class,
                () -> createDefaultPremiumMailProduct(longName, Brand.GMX, BigDecimal.ONE));
        assertEquals("Product name must not exceed 100 characters", exception.getMessage());
    }

    @Test
    @DisplayName("Success: Verify withMonthlyFee return new instance with same data but new fee for premium product")
    void shouldReturnNewInstanceWithUpdatedMonthlyFeeForPremiumProduct() {
        final PremiumMailProduct initial =
                createDefaultPremiumMailProduct("Premium Pro", Brand.WEB_DE, new BigDecimal("10.00"));

        final BigDecimal newFee = new BigDecimal("15.00");
        final PremiumMailProduct updated = initial.withMonthlyFee(newFee);

        assertEquals(initial.getId(), updated.getId());
        assertEquals(initial.getName(), updated.getName());
        assertEquals(initial.getBrand(), updated.getBrand());
        assertEquals(initial.getSetupFee(), updated.getSetupFee());
        assertEquals(newFee, updated.getMonthlyFee());
        assertEquals(initial.getStorageSize(), updated.getStorageSize());
    }

    @Test
    @DisplayName("Success: Create StandardMailProduct with predefined ID")
    void shouldCreateStandardMailProductWithPredefinedId() {
        final String name = "Standard Cloud";
        final Brand brand = Brand.GMX;
        final BigDecimal monthlyFee = new BigDecimal("2.99");

        final StandardMailProduct product = createDefaultStandardMailProduct(name, brand, monthlyFee);

        assertInstanceOf(UUID.class, product.getId());
        assertEquals(name, product.getName());
        assertEquals(brand, product.getBrand());
        assertEquals(new BigDecimal("4.99"), product.getSetupFee());
        assertEquals(monthlyFee, product.getMonthlyFee());
    }

    @Test
    @DisplayName("Success: Create PremiumMailProduct with predefined ID")
    void shouldCreatePremiumMailProductWithPredefinedId() {
        final String name = "Premium Plus";
        final Brand brand = Brand.WEB_DE;
        final BigDecimal monthlyFee = new BigDecimal("7.99");

        final PremiumMailProduct product = createDefaultPremiumMailProduct(name, brand, monthlyFee);

        assertInstanceOf(UUID.class, product.getId());
        assertEquals(name, product.getName());
        assertEquals(brand, product.getBrand());
        assertEquals(new BigDecimal("9.99"), product.getSetupFee());
        assertEquals(monthlyFee, product.getMonthlyFee());
    }

    @Test
    @DisplayName("Success: Create MailProduct via static factory method")
    void shouldCreateMailProductViaStaticFactory() {
        final String name = "Generic Mail";
        final Brand brand = Brand.GMX;
        final BigDecimal setupFee = new BigDecimal("1.00");
        final BigDecimal monthlyFee = new BigDecimal("2.00");
        final Long storageSize = 1024L;

        final MailProduct product = MailProduct.create(name, brand, setupFee, monthlyFee, storageSize);

        assertEquals(name, product.getName());
        assertEquals(brand, product.getBrand());
        assertEquals(setupFee, product.getSetupFee());
        assertEquals(monthlyFee, product.getMonthlyFee());
        assertEquals(storageSize, product.getStorageSize().orElseThrow());
    }

    @Test
    @DisplayName("Success: Verify withMonthlyFee return new instance with same data but new fee for standard product")
    void shouldReturnNewInstanceWithUpdatedMonthlyFeeForStandardProduct() {
        final StandardMailProduct initial =
                createDefaultStandardMailProduct("Standard Basic", Brand.GMX, new BigDecimal("2.00"));

        final BigDecimal newFee = new BigDecimal("3.00");
        final StandardMailProduct updated = initial.withMonthlyFee(newFee);

        assertEquals(initial.getId(), updated.getId());
        assertEquals(initial.getName(), updated.getName());
        assertEquals(initial.getBrand(), updated.getBrand());
        assertEquals(initial.getSetupFee(), updated.getSetupFee());
        assertEquals(newFee, updated.getMonthlyFee());
        assertEquals(initial.getStorageSize(), updated.getStorageSize());
    }
}
