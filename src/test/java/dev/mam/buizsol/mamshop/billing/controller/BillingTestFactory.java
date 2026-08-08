package dev.mam.buizsol.mamshop.billing.controller;

import dev.mam.buizsol.mamshop.billing.dto.InvoiceItemResponseDTO;
import dev.mam.buizsol.mamshop.billing.dto.InvoiceResponseDTO;
import dev.mam.buizsol.mamshop.billing.model.Invoice;
import dev.mam.buizsol.mamshop.billing.model.InvoiceItem;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import dev.mam.buizsol.mamshop.customer.dto.AddressResponseDTO;
import dev.mam.buizsol.mamshop.customer.model.Address;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class BillingTestFactory {

    private BillingTestFactory() {}

    public static Invoice createInvoice(
            Brand brand,
            Customer customer,
            Address address,
            Address invoiceAddress,
            List<InvoiceItem> items,
            BigDecimal discount) {
        return new Invoice(brand, customer, address, invoiceAddress, items, discount);
    }

    public static InvoiceResponseDTO createInvoiceResponseDTO(
            Brand brand,
            LocalDate invoiceDate,
            UUID customerId,
            AddressResponseDTO address,
            AddressResponseDTO invoiceAddress,
            List<InvoiceItemResponseDTO> items,
            BigDecimal totalSetupFee,
            BigDecimal totalMonthlyFee,
            BigDecimal discount,
            BigDecimal totalAmount) {
        return new InvoiceResponseDTO(
                brand,
                invoiceDate,
                customerId,
                address,
                invoiceAddress,
                items,
                totalSetupFee,
                totalMonthlyFee,
                discount,
                totalAmount);
    }

    public static Address createAddress(String street, String number, String postcode, String city, String country) {
        return new Address(street, number, postcode, city, country);
    }

    public static AddressResponseDTO createAddressResponseDTO(
            String street, String number, String postcode, String city, String country) {
        return new AddressResponseDTO(street, number, postcode, city, country);
    }

    public static InvoiceItem createInvoiceItem(
            UUID productId,
            String productName,
            Contract contract,
            LocalDate contractCreationDate,
            BigDecimal setupFee,
            BigDecimal monthlyFee) {
        return new InvoiceItem(productId, productName, contract, contractCreationDate, setupFee, monthlyFee);
    }

    public static InvoiceItemResponseDTO createInvoiceItemResponseDTO(
            UUID productId,
            String productName,
            UUID contractId,
            LocalDate contractCreationDate,
            BigDecimal setupFee,
            BigDecimal monthlyFee) {
        return new InvoiceItemResponseDTO(
                productId, productName, contractId, contractCreationDate, setupFee, monthlyFee);
    }
}
