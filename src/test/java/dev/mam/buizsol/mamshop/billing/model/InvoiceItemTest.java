package dev.mam.buizsol.mamshop.billing.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import dev.mam.buizsol.mamshop.contract.model.Contract;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("InvoiceItem Tests")
class InvoiceItemTest {

    @DisplayName("Successful creation of InvoiceItem with valid data")
    @Test
    void shouldCreateInvoiceItemWhenValidDataProvided() {
        UUID productId = UUID.randomUUID();
        String productName = "Test Product";
        Contract contract = mock(Contract.class);
        LocalDate creationDate = LocalDate.now();
        BigDecimal setupFee = new BigDecimal("10.00");
        BigDecimal monthlyFee = new BigDecimal("5.00");

        InvoiceItem item = new InvoiceItem(productId, productName, contract, creationDate, setupFee, monthlyFee);

        assertEquals(productId, item.getProductId());
        assertEquals(productName, item.getProductName());
        assertEquals(contract, item.getContract());
        assertEquals(creationDate, item.getContractCreationDate());
        assertEquals(setupFee, item.getSetupFee());
        assertEquals(monthlyFee, item.getMonthlyFee());
    }

    @ParameterizedTest(name = "Check of fees - setup: {0}, monthly: {1}")
    @DisplayName("Verification of setup and monthly fees using various values")
    @CsvSource({"0.00, 0.11", "4.99, 9.99", "99.99, 149.50", "0.01, 0.11"})
    void shouldSetCorrectFeesWhenValuesAreProvided(BigDecimal setupFee, BigDecimal monthlyFee) {
        InvoiceItem item = new InvoiceItem(
                UUID.randomUUID(), "Product", mock(Contract.class), LocalDate.now(), setupFee, monthlyFee);

        assertEquals(setupFee, item.getSetupFee());
        assertEquals(monthlyFee, item.getMonthlyFee());
    }

    @Test
    @DisplayName("Verification of InvoiceItem with boundary date (yesterday)")
    void shouldHandlePastDateWhenCreatingInvoiceItem() {
        LocalDate pastDate = LocalDate.now().minusYears(1);

        InvoiceItem item = new InvoiceItem(
                UUID.randomUUID(),
                "Old Contract",
                mock(Contract.class),
                pastDate,
                BigDecimal.ZERO,
                new BigDecimal("10.00"));

        assertEquals(pastDate, item.getContractCreationDate());
    }
}
