package dev.mam.buizsol.mamshop.billing.service;

import dev.mam.buizsol.mamshop.billing.exception.InvalidInvoiceDiscountException;
import dev.mam.buizsol.mamshop.billing.exception.InvoiceValidationException;
import dev.mam.buizsol.mamshop.billing.model.Invoice;
import dev.mam.buizsol.mamshop.billing.model.InvoiceItem;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import dev.mam.buizsol.mamshop.contract.model.ContractStatus;
import dev.mam.buizsol.mamshop.contract.service.ContractService;
import dev.mam.buizsol.mamshop.customer.exception.CustomerNotFoundException;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.customer.service.CustomerService;
import dev.mam.buizsol.mamshop.product.exception.ProductNotFoundException;
import dev.mam.buizsol.mamshop.product.model.Product;
import dev.mam.buizsol.mamshop.product.service.ProductService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class BillingServiceImpl implements BillingService {

    private final CustomerService customerService;
    private final ProductService productService;
    private final ContractService contractService;

    private final BigDecimal zeroAmount;
    private final BigDecimal minimalDiscountAmount;

    BillingServiceImpl(
            final CustomerService customerService,
            final ProductService productService,
            final ContractService contractService,
            @Value("${billing.zero-amount}") final BigDecimal zero,
            @Value("${billing.minimal-discount-amount}") final BigDecimal discount) {
        this.customerService = customerService;
        this.productService = productService;
        this.contractService = contractService;
        this.zeroAmount = zero;
        this.minimalDiscountAmount = discount;
    }

    @Override
    @Transactional
    public Invoice generateInvoice(final UUID customerId) throws CustomerNotFoundException, ProductNotFoundException {
        if (customerId == null) {
            throw new InvoiceValidationException("Customer ID must not be null");
        }
        return generateInvoice(customerId, zeroAmount);
    }

    @Override
    @Transactional
    public Invoice generateInvoice(final UUID customerId, final BigDecimal discount)
            throws CustomerNotFoundException, ProductNotFoundException {
        if (customerId == null) {
            throw new InvoiceValidationException("Customer ID must not be null");
        }
        if (discount == null) {
            throw new InvalidInvoiceDiscountException("Discount must not be null");
        }
        if (discount.compareTo(zeroAmount) < 0) {
            throw new InvalidInvoiceDiscountException("Discount cannot be negative");
        }
        if (discount.compareTo(zeroAmount) > 0 && discount.compareTo(minimalDiscountAmount) <= 0) {
            throw new InvalidInvoiceDiscountException("Discount must be greater than " + minimalDiscountAmount + " €");
        }
        return createInvoice(customerId, discount);
    }

    private Invoice createInvoice(final UUID customerId, final BigDecimal discount)
            throws CustomerNotFoundException, ProductNotFoundException {
        final Customer customer = customerService
                .findCustomerById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer with ID " + customerId + " not found"));

        final List<Contract> contracts = contractService.findContractsByCustomerId(customerId);
        final List<InvoiceItem> items = new ArrayList<>();

        for (final Contract contract : contracts) {
            if (contract.getStatus() == ContractStatus.ACTIVE) {
                final Product product = productService
                        .findById(contract.getProductId())
                        .orElseThrow(() -> new ProductNotFoundException("Product with ID " + contract.getProductId()
                                + " not found for contract " + contract.getId()));

                items.add(new InvoiceItem(
                        product.getId(),
                        product.getName(),
                        contract,
                        contract.getCreationDate(),
                        product.getSetupFee(),
                        product.getMonthlyFee()));
            }
        }

        return new Invoice(
                customer.getBrand(), customer, customer.getAddress(), customer.getInvoiceAddress(), items, discount);
    }
}
