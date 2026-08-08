package dev.mam.buizsol.mamshop.product.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

@DisplayName("PartnerProduct Tests")
class PartnerProductTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    private PartnerProduct createPartnerProduct(
            final String name, final Brand brand, final BigDecimal setupFee, final BigDecimal monthlyFee) {
        PartnerProduct product = new PartnerProduct(name, brand, setupFee, monthlyFee);
        Set<ConstraintViolation<PartnerProduct>> violations = validator.validate(product);
        if (violations.isEmpty()) {
            return product;
        }
        String message = violations.iterator().next().getMessage();
        throw new ProductValidationException(message);
    }

    @DisplayName("Success: Create PartnerProduct with valid diverse data")
    @ParameterizedTest(name = "[{index}] Name: {0}, Brand: {1}")
    @CsvSource({"Cloud Storage, GMX", "Video Streaming, WEB_DE", "Security Pack, GMX"})
    void shouldCreatePartnerProductWhenValidDataProvided(String name, String brandName) {
        Brand brand = Brand.valueOf(brandName);
        BigDecimal setupFee = new BigDecimal("9.99");
        BigDecimal monthlyFee = new BigDecimal("4.99");

        PartnerProduct product = createPartnerProduct(name, brand, setupFee, monthlyFee);

        assertNotNull(product.getId());
        assertEquals(name, product.getName());
        assertEquals(brand, product.getBrand());
        assertEquals(setupFee, product.getSetupFee());
        assertEquals(monthlyFee, product.getMonthlyFee());
    }

    @Test
    @DisplayName("Success: Create PartnerProduct with predefined ID")
    void shouldCreatePartnerProductWithPredefinedId() {
        final UUID id = UUID.randomUUID();
        final String name = "Partner Storage";
        final Brand brand = Brand.GMX;
        final BigDecimal setupFee = new BigDecimal("10.00");
        final BigDecimal monthlyFee = new BigDecimal("5.00");

        final PartnerProduct product = new PartnerProduct(id, name, brand, setupFee, monthlyFee);

        assertEquals(id, product.getId());
        assertEquals(name, product.getName());
        assertEquals(brand, product.getBrand());
        assertEquals(setupFee, product.getSetupFee());
        assertEquals(monthlyFee, product.getMonthlyFee());
    }

    @Test
    @DisplayName("Boundary: Success with minimum allowed monthly fee (0.11 €)")
    void shouldCreatePartnerProductWhenMonthlyFeeIsAtMinimumAllowed() {
        BigDecimal minFee = new BigDecimal("0.11");
        PartnerProduct product = createPartnerProduct("Budget Cloud", Brand.GMX, BigDecimal.ZERO, minFee);

        assertEquals(minFee, product.getMonthlyFee());
    }

    @DisplayName("Boundary/Negative: Failure with monthly fee 0.10 € or less")
    @ParameterizedTest(name = "[{index}] Monthly fee {0} €")
    @ValueSource(strings = {"0.10", "0.09", "0.00", "-0.01", "-10.00"})
    void shouldThrowExceptionWhenMonthlyFeeIsInvalid(String fee) {
        BigDecimal invalidFee = new BigDecimal(fee);

        assertThrows(
                ProductValidationException.class,
                () -> createPartnerProduct("Invalid Fee Product", Brand.WEB_DE, BigDecimal.ONE, invalidFee));
    }

    @DisplayName("Negative: Failure with null, empty or blank name")
    @ParameterizedTest(name = "[{index}] Invalid Name: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void shouldThrowExceptionWhenNameIsInvalid(String name) {
        BigDecimal country = new BigDecimal("1.00");
        assertThrows(
                ProductValidationException.class,
                () -> createPartnerProduct(name, Brand.GMX, BigDecimal.ZERO, country));
    }

    @Test
    @DisplayName("Negative: Failure with null brand")
    void shouldThrowExceptionWhenBrandIsNull() {
        BigDecimal country = new BigDecimal("1.00");
        assertThrows(
                ProductValidationException.class,
                () -> createPartnerProduct("No Brand", null, BigDecimal.ZERO, country));
    }

    @Test
    @DisplayName("Negative: Failure with null setup fee")
    void shouldThrowExceptionWhenSetupFeeIsNull() {
        BigDecimal country = new BigDecimal("1.00");
        assertThrows(
                ProductValidationException.class,
                () -> createPartnerProduct("No Setup Fee", Brand.WEB_DE, null, country));
    }

    @Test
    @DisplayName("Negative: Failure with null monthly fee")
    void shouldThrowExceptionWhenMonthlyFeeIsNull() {
        assertThrows(
                ProductValidationException.class,
                () -> createPartnerProduct("No Monthly Fee", Brand.GMX, BigDecimal.ZERO, null));
    }

    @Test
    @DisplayName("Negative: Failure with negative setup fee")
    void shouldThrowExceptionWhenSetupFeeIsNegative() {
        BigDecimal negativeFee = new BigDecimal("-1.00");
        BigDecimal country = new BigDecimal("1.00");
        assertThrows(
                ProductValidationException.class,
                () -> createPartnerProduct("Bad Setup Fee", Brand.GMX, negativeFee, country));
    }

    @Test
    @DisplayName("Negative: Failure with too long name (> 100 characters)")
    void shouldThrowExceptionWhenProductNameIsTooLong() {
        String longName = "A".repeat(101);

        ProductValidationException exception = assertThrows(
                ProductValidationException.class,
                () -> createPartnerProduct(longName, Brand.GMX, BigDecimal.ZERO, BigDecimal.ONE));

        assertEquals("Product name must not exceed 100 characters", exception.getMessage());
    }

    @Test
    @DisplayName("Success: Verify withMonthlyFee returns new instance with updated fee")
    void shouldReturnNewInstanceWithUpdatedMonthlyFee() {
        final PartnerProduct initial =
                createPartnerProduct("Cloud", Brand.GMX, new BigDecimal("5.00"), new BigDecimal("10.00"));

        final BigDecimal newFee = new BigDecimal("12.00");
        final PartnerProduct updated = initial.withMonthlyFee(newFee);

        assertEquals(initial.getId(), updated.getId());
        assertEquals(initial.getName(), updated.getName());
        assertEquals(initial.getBrand(), updated.getBrand());
        assertEquals(initial.getSetupFee(), updated.getSetupFee());
        assertEquals(newFee, updated.getMonthlyFee());
    }
}
