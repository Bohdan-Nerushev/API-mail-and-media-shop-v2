package dev.mam.buizsol.mamshop.billing.mapper;

import dev.mam.buizsol.mamshop.billing.dto.InvoiceItemResponseDTO;
import dev.mam.buizsol.mamshop.billing.dto.InvoiceResponseDTO;
import dev.mam.buizsol.mamshop.billing.model.Invoice;
import dev.mam.buizsol.mamshop.billing.model.InvoiceItem;
import dev.mam.buizsol.mamshop.customer.mapper.CustomerMapper;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    private final CustomerMapper customerMapper;

    public InvoiceMapper(final CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    public InvoiceResponseDTO toInvoiceResponseDTO(@NotNull Invoice invoice) {
        final List<InvoiceItemResponseDTO> itemDTOs =
                invoice.getItems().stream().map(this::toInvoiceItemResponseDTO).toList();

        return new InvoiceResponseDTO(
                invoice.getBrand(),
                invoice.getInvoiceDate(),
                invoice.getCustomer().getId(),
                customerMapper.toAddressResponseDTO(invoice.getAddress()),
                customerMapper.toAddressResponseDTO(invoice.getInvoiceAddress()),
                itemDTOs,
                invoice.getTotalSetupFee(),
                invoice.getTotalMonthlyFee(),
                invoice.getDiscount(),
                invoice.getTotalAmount());
    }

    private InvoiceItemResponseDTO toInvoiceItemResponseDTO(@NotNull InvoiceItem item) {
        return new InvoiceItemResponseDTO(
                item.getProductId(),
                item.getProductName(),
                item.getContract().getId(),
                item.getContractCreationDate(),
                item.getSetupFee(),
                item.getMonthlyFee());
    }
}
