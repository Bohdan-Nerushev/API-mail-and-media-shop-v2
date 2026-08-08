package dev.mam.buizsol.mamshop.shop.service.controller;

import dev.mam.buizsol.mamshop.billing.dto.InvoiceItemResponseDTO;
import dev.mam.buizsol.mamshop.billing.dto.InvoiceResponseDTO;
import dev.mam.buizsol.mamshop.billing.model.Invoice;
import dev.mam.buizsol.mamshop.billing.model.InvoiceItem;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import dev.mam.buizsol.mamshop.customer.dto.AddressResponseDTO;
import dev.mam.buizsol.mamshop.customer.model.Address;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.product.dto.ProductResponseDTO;
import dev.mam.buizsol.mamshop.product.model.PremiumMailProduct;
import dev.mam.buizsol.mamshop.product.model.Product;
import dev.mam.buizsol.mamshop.product.model.StandardMailProduct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.mockito.Mockito;

public final class ShopTestFactory {

    private ShopTestFactory() {}

    public static PremiumMailProduct createPremiumProduct(String name, Brand brand, BigDecimal price) {
        return new PremiumMailProduct(name, brand, price);
    }

    public static StandardMailProduct createStandardProduct(String name, Brand brand, BigDecimal price) {
        return new StandardMailProduct(name, brand, price);
    }

    public static List<Product> createProductList(Product... products) {
        return List.of(products);
    }

    public static ProductResponseDTO createDtoFromProduct(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getSetupFee(),
                product.getMonthlyFee(),
                product.getStorageSize().orElse(0L));
    }

    public static ProductResponseDTO createDtoFromProduct(
            UUID id, String name, Brand brand, BigDecimal setupFee, BigDecimal monthlyFee, Long storageSize) {
        return new ProductResponseDTO(id, name, brand, setupFee, monthlyFee, storageSize);
    }

    public static Invoice createInvoice(
            Brand brand,
            UUID customerId,
            Address address,
            Address invoiceAddress,
            List<InvoiceItem> items,
            BigDecimal discount) {
        Customer customer = Mockito.mock(Customer.class);
        Mockito.when(customer.getId()).thenReturn(customerId);
        return new Invoice(brand, customer, address, invoiceAddress, items, discount);
    }

    public static InvoiceResponseDTO createInvoiceResponseDTO(
            Brand brand,
            LocalDate invoiceDate,
            UUID customerId,
            Address address,
            Address invoiceAddress,
            List<InvoiceItem> items,
            BigDecimal totalSetupFee,
            BigDecimal totalMonthlyFee,
            BigDecimal discount,
            BigDecimal totalAmount) {
        return new InvoiceResponseDTO(
                brand,
                invoiceDate,
                customerId,
                new AddressResponseDTO(
                        address.getStreet(),
                        address.getNumber(),
                        address.getPostcode(),
                        address.getCity(),
                        address.getCountry()),
                new AddressResponseDTO(
                        invoiceAddress.getStreet(),
                        invoiceAddress.getNumber(),
                        invoiceAddress.getPostcode(),
                        invoiceAddress.getCity(),
                        invoiceAddress.getCountry()),
                items.stream()
                        .map(i -> new InvoiceItemResponseDTO(
                                i.getProductId(),
                                i.getProductName(),
                                i.getContract().getId(),
                                i.getContractCreationDate(),
                                i.getSetupFee(),
                                i.getMonthlyFee()))
                        .toList(),
                totalSetupFee,
                totalMonthlyFee,
                discount,
                totalAmount);
    }

    public static Address createAddress(String street, String number, String postcode, String city, String country) {
        return new Address(street, number, postcode, city, country);
    }

    public static InvoiceItem createInvoiceItem(
            UUID productId,
            String productName,
            UUID contractId,
            LocalDate contractCreationDate,
            BigDecimal setupFee,
            BigDecimal monthlyFee) {
        Contract contract = Mockito.mock(Contract.class);
        Mockito.when(contract.getId()).thenReturn(contractId);
        Mockito.when(contract.getCreationDate()).thenReturn(contractCreationDate);
        return new InvoiceItem(productId, productName, contract, contractCreationDate, setupFee, monthlyFee);
    }
}
