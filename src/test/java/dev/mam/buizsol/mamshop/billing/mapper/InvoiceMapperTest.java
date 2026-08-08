package dev.mam.buizsol.mamshop.billing.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.mam.buizsol.mamshop.billing.dto.InvoiceItemResponseDTO;
import dev.mam.buizsol.mamshop.billing.dto.InvoiceResponseDTO;
import dev.mam.buizsol.mamshop.billing.model.Invoice;
import dev.mam.buizsol.mamshop.billing.model.InvoiceItem;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import dev.mam.buizsol.mamshop.customer.dto.AddressResponseDTO;
import dev.mam.buizsol.mamshop.customer.mapper.CustomerMapper;
import dev.mam.buizsol.mamshop.customer.model.Address;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceMapperTest {

    @Mock
    private CustomerMapper customerMapper;

    private InvoiceMapper invoiceMapper;

    @BeforeEach
    void setUp() {
        invoiceMapper = new InvoiceMapper(customerMapper);
    }

    @Test
    @DisplayName("Should map Invoice entity to InvoiceResponseDTO")
    void shouldMapInvoiceToResponseDTO() {
        final UUID customerId = UUID.randomUUID();
        final UUID contractId = UUID.randomUUID();
        final UUID productId = UUID.randomUUID();
        final LocalDate invoiceDate = LocalDate.now();

        final Customer customer = mock(Customer.class);
        when(customer.getId()).thenReturn(customerId);

        final Address address = mock(Address.class);
        final Address invoiceAddress = mock(Address.class);

        final Contract contract = mock(Contract.class);
        when(contract.getId()).thenReturn(contractId);

        final InvoiceItem item = InvoiceItem.builder()
                .productId(productId)
                .productName("Test Product")
                .contract(contract)
                .contractCreationDate(invoiceDate.minusDays(5))
                .setupFee(new BigDecimal("10.00"))
                .monthlyFee(new BigDecimal("5.00"))
                .build();

        final Invoice invoice = mock(Invoice.class);
        when(invoice.getBrand()).thenReturn(Brand.GMX);
        when(invoice.getInvoiceDate()).thenReturn(invoiceDate);
        when(invoice.getCustomer()).thenReturn(customer);
        when(invoice.getAddress()).thenReturn(address);
        when(invoice.getInvoiceAddress()).thenReturn(invoiceAddress);
        when(invoice.getItems()).thenReturn(List.of(item));
        when(invoice.getTotalSetupFee()).thenReturn(new BigDecimal("10.00"));
        when(invoice.getTotalMonthlyFee()).thenReturn(new BigDecimal("5.00"));
        when(invoice.getDiscount()).thenReturn(BigDecimal.ZERO);
        when(invoice.getTotalAmount()).thenReturn(new BigDecimal("15.00"));

        final AddressResponseDTO addressDTO = new AddressResponseDTO("St", "1", "1", "C", "CO");
        when(customerMapper.toAddressResponseDTO(any(Address.class))).thenReturn(addressDTO);

        final InvoiceResponseDTO result = invoiceMapper.toInvoiceResponseDTO(invoice);

        assertThat(result).isNotNull();
        assertThat(result.brand()).isEqualTo(Brand.GMX);
        assertThat(result.invoiceDate()).isEqualTo(invoiceDate);
        assertThat(result.customerId()).isEqualTo(customerId);
        assertThat(result.items()).hasSize(1);

        final InvoiceItemResponseDTO itemDTO = result.items().get(0);
        assertThat(itemDTO.productId()).isEqualTo(productId);
        assertThat(itemDTO.productName()).isEqualTo("Test Product");
        assertThat(itemDTO.contractId()).isEqualTo(contractId);
        assertThat(itemDTO.setupFee()).isEqualByComparingTo("10.00");

        assertThat(result.totalSetupFee()).isEqualByComparingTo("10.00");
        assertThat(result.totalMonthlyFee()).isEqualByComparingTo("5.00");
        assertThat(result.totalAmount()).isEqualByComparingTo("15.00");

        verify(customerMapper, times(2)).toAddressResponseDTO(any(Address.class));
    }
}
