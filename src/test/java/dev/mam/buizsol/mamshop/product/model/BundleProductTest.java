package dev.mam.buizsol.mamshop.product.model;

import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.product.exception.ProductValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.executable.ExecutableValidator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("BundleProduct Tests")
class BundleProductTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    private StandardMailProduct createDefaultStandardMailProduct(
            final String name, final Brand brand, final BigDecimal monthlyFee) {
        StandardMailProduct product = new StandardMailProduct(name, brand, monthlyFee);
        Set<ConstraintViolation<StandardMailProduct>> violations = validator.validate(product);
        if (!violations.isEmpty()) {
            throw new ProductValidationException("Validation failed");
        }
        return product;
    }

    private PremiumMailProduct createDefaultPremiumMailProduct(
            final String name, final Brand brand, final BigDecimal monthlyFee) {
        PremiumMailProduct product = new PremiumMailProduct(name, brand, monthlyFee);
        Set<ConstraintViolation<PremiumMailProduct>> violations = validator.validate(product);
        if (!violations.isEmpty()) {
            throw new ProductValidationException("Validation failed");
        }
        return product;
    }

    private PartnerProduct createDefaultPartnerProduct(
            final String name, final Brand brand, final BigDecimal setupFee, final BigDecimal monthlyFee) {
        PartnerProduct product = new PartnerProduct(name, brand, setupFee, monthlyFee);
        Set<ConstraintViolation<PartnerProduct>> violations = validator.validate(product);
        if (!violations.isEmpty()) {
            throw new ProductValidationException("Validation failed");
        }
        return product;
    }

    private BundleProduct createDefaultBundleProduct(final MailProduct mail, final PartnerProduct partner) {
        BundleProduct product = new BundleProduct(mail, partner);
        Set<ConstraintViolation<BundleProduct>> violations = validator.validate(product);
        if (!violations.isEmpty()) {
            throw new ProductValidationException("Validation failed");
        }
        return product;
    }

    @DisplayName("Success: Create BundleProduct and verify summation logic for different brands")
    @ParameterizedTest(name = "[{index}] Brand: {0}, Mail: {1}, Partner: {3}")
    @CsvSource({
            "GMX, S-Mail, 1.00, P-Cloud, 5.00, 2.00, 9.99, 3.00",
            "WEB_DE, P-Mail, 10.00, P-Music, 15.00, 5.00, 24.99, 15.00"
    })
    void shouldCreateBundleProductAndCalculateTotalsWhenValidDataProvided(
            final Brand brand,
            final String mailName,
            final BigDecimal mailMonthly,
            final String partnerName,
            final BigDecimal partnerSetup,
            final BigDecimal partnerMonthly,
            final BigDecimal expectedSetup,
            final BigDecimal expectedMonthly) {

        final MailProduct mail = mailName.startsWith("S")
                ? createDefaultStandardMailProduct(mailName, brand, mailMonthly)
                : createDefaultPremiumMailProduct(mailName, brand, mailMonthly);

        final PartnerProduct partner = createDefaultPartnerProduct(partnerName, brand, partnerSetup, partnerMonthly);
        final BundleProduct bundle = createDefaultBundleProduct(mail, partner);

        assertNotNull(bundle.getId());
        assertEquals(brand, bundle.getBrand());
        assertEquals("Bundle: " + mail.getName() + " " + partner.getName(), bundle.getName());
        assertEquals(expectedSetup, bundle.getSetupFee());
        assertEquals(expectedMonthly, bundle.getMonthlyFee());
    }

    @Test
    @DisplayName("Negative: Failure when creating bundle with mismatched brands")
    void shouldThrowExceptionWhenCreatingBundleWithMismatchedBrands() {
        StandardMailProduct mail = createDefaultStandardMailProduct("Mail", Brand.GMX, new BigDecimal("1.00"));
        PartnerProduct partner =
                createDefaultPartnerProduct("Cloud", Brand.WEB_DE, BigDecimal.ZERO, new BigDecimal("2.00"));

        assertThrows(ProductValidationException.class, () -> createDefaultBundleProduct(mail, partner));
    }

    @Test
    @DisplayName("Negative: Failure with null MailProduct")
    void shouldThrowExceptionWhenCreatingBundleWithNullMail() {
        final PartnerProduct partner = createDefaultPartnerProduct("P", Brand.GMX, BigDecimal.ZERO, BigDecimal.ONE);

        assertThrows(ProductValidationException.class, () -> createDefaultBundleProduct(null, partner));
    }

    @Test
    @DisplayName("Negative: Failure with null PartnerProduct")
    void shouldThrowExceptionWhenCreatingBundleWithNullPartner() {
        final MailProduct mail = createDefaultStandardMailProduct("M", Brand.GMX, BigDecimal.ONE);

        assertThrows(ProductValidationException.class, () -> createDefaultBundleProduct(mail, null));
    }

    @Test
    @DisplayName("Negative: Failure with both components null")
    void shouldThrowExceptionWhenCreatingBundleWithBothComponentsNull() {
        assertThrows(ProductValidationException.class, () -> createDefaultBundleProduct(null, null));
    }

    @Test
    @DisplayName("Verify @NotNull constructor annotations via ExecutableValidator")
    void shouldValidateConstructorAnnotationsWithExecutableValidator() throws NoSuchMethodException {
        ExecutableValidator executableValidator = validator.forExecutables();
        Constructor<BundleProduct> constructor =
                BundleProduct.class.getConstructor(MailProduct.class, PartnerProduct.class);

        var violations = executableValidator.validateConstructorParameters(constructor, new Object[]{null, null});

        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());
    }

    @Test
    @DisplayName("Boundary: Success with large fees summation")
    void shouldCalculateTotalsCorrectlyWhenLargeFeesProvided() {
        BigDecimal largeFee = new BigDecimal("1000000.00");
        PremiumMailProduct mail = createDefaultPremiumMailProduct("Rich Mail", Brand.GMX, largeFee);
        PartnerProduct partner = createDefaultPartnerProduct("Rich Cloud", Brand.GMX, largeFee, largeFee);

        BundleProduct bundle = createDefaultBundleProduct(mail, partner);

        assertEquals(new BigDecimal("9.99").add(largeFee), bundle.getSetupFee());
        assertEquals(largeFee.add(largeFee), bundle.getMonthlyFee());
    }

    @Test
    @DisplayName("Polymorphism: Verify calls via Product base class")
    void shouldCalculateTotalsCorrectlyWhenCalledPolymorphically() {
        MailProduct mail = createDefaultStandardMailProduct("Base Mail", Brand.WEB_DE, new BigDecimal("1.50"));
        PartnerProduct partner = createDefaultPartnerProduct(
                "Base Cloud", Brand.WEB_DE, new BigDecimal("10.00"), new BigDecimal("3.50"));

        Product product = createDefaultBundleProduct(mail, partner);
        BigDecimal partnerSetupFee = partner.getSetupFee();
        BigDecimal productSetupFee = product.getSetupFee();
        BigDecimal partnerMonthlyFee = partner.getMonthlyFee();
        BigDecimal productMonthlyFee = product.getMonthlyFee();
        BigDecimal mailSetupFee = mail.getSetupFee();
        BigDecimal mailMonthlyFee = mail.getMonthlyFee();
        assertEquals(mailSetupFee.add(partnerSetupFee), productSetupFee);
        assertEquals(mailMonthlyFee.add(partnerMonthlyFee), productMonthlyFee);
    }

    @Test
    @DisplayName("Success: Verify getters for components")
    void shouldReturnCorrectComponentsWhenGettersCalled() {
        StandardMailProduct mail = createDefaultStandardMailProduct("Mail", Brand.GMX, new BigDecimal("1.00"));
        PartnerProduct partner =
                createDefaultPartnerProduct("Partner", Brand.GMX, BigDecimal.ZERO, new BigDecimal("1.00"));

        BundleProduct bundle = createDefaultBundleProduct(mail, partner);

        assertEquals(mail, bundle.getMailProduct());
        assertEquals(partner, bundle.getPartnerProduct());
    }

    @Test
    @DisplayName("Negative: setMonthlyFee throws UnsupportedOperationException")
    void shouldThrowExceptionWhenAttemptingToSetMonthlyFee() {
        StandardMailProduct mail = createDefaultStandardMailProduct("Mail", Brand.GMX, new BigDecimal("1.00"));
        PartnerProduct partner =
                createDefaultPartnerProduct("Partner", Brand.GMX, BigDecimal.ZERO, new BigDecimal("1.00"));
        BundleProduct bundle = createDefaultBundleProduct(mail, partner);
        BigDecimal newFee = new BigDecimal("5.00");
        assertThrows(UnsupportedOperationException.class, () -> bundle.withMonthlyFee(newFee));
    }

    @Test
    @DisplayName("Immutability: Bundle fee calculated from components at creation")
    void shouldCalculateBundleFeeFromComponentsAtCreation() {
        BigDecimal initialFee = new BigDecimal("1.00");
        StandardMailProduct mail = createDefaultStandardMailProduct("Mail", Brand.GMX, initialFee);
        PartnerProduct partner =
                createDefaultPartnerProduct("Partner", Brand.GMX, BigDecimal.ZERO, new BigDecimal("1.00"));
        BundleProduct bundle = createDefaultBundleProduct(mail, partner);

        assertEquals(new BigDecimal("2.00"), bundle.getMonthlyFee());

        BigDecimal newFee = new BigDecimal("5.00");
        StandardMailProduct updatedMail = mail.withMonthlyFee(newFee);
        BundleProduct updatedBundle = createDefaultBundleProduct(updatedMail, partner);

        assertEquals(
                new BigDecimal("6.00"),
                updatedBundle.getMonthlyFee(),
                "New bundle fee should reflect component change");
        assertEquals(new BigDecimal("2.00"), bundle.getMonthlyFee(), "Original bundle fee should remain unchanged");
    }

    @Test
    @DisplayName("Coverage: Test private calculation methods with nulls using reflection")
    void shouldReturnZeroOrThrowWhenCallingPrivateMethodsWithNulls() throws Exception {

        var setupFeeMethod =
                BundleProduct.class.getDeclaredMethod("calculateTotalSetupFee", Product.class, Product.class);
        setupFeeMethod.setAccessible(true);
        assertEquals(BigDecimal.ZERO, setupFeeMethod.invoke(null, null, null));

        var monthlyFeeMethod =
                BundleProduct.class.getDeclaredMethod("calculateTotalMonthlyFee", Product.class, Product.class);
        monthlyFeeMethod.setAccessible(true);
        assertEquals(BigDecimal.ZERO, monthlyFeeMethod.invoke(null, null, null));

        var validateBrandsMethod =
                BundleProduct.class.getDeclaredMethod("validateBrands", Product.class, Product.class);
        validateBrandsMethod.setAccessible(true);
        var exception = assertThrows(
                java.lang.reflect.InvocationTargetException.class, () -> validateBrandsMethod.invoke(null, null, null));
        Assertions.assertThat(exception.getCause())
                .isInstanceOf(ProductValidationException.class)
                .hasMessage("Mail and Partner products must not be null");
    }
}
