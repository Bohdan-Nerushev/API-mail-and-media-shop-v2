package dev.mam.buizsol.mamshop.shop.service;

import dev.mam.buizsol.mamshop.billing.model.Invoice;
import dev.mam.buizsol.mamshop.billing.service.BillingService;
import dev.mam.buizsol.mamshop.contract.exception.ContractNotFoundException;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import dev.mam.buizsol.mamshop.contract.model.ContractStatus;
import dev.mam.buizsol.mamshop.contract.service.ContractService;
import dev.mam.buizsol.mamshop.customer.exception.CustomerNotActiveException;
import dev.mam.buizsol.mamshop.customer.exception.CustomerNotFoundException;
import dev.mam.buizsol.mamshop.customer.exception.CustomerValidationException;
import dev.mam.buizsol.mamshop.customer.model.Address;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.customer.model.CommunicationDetails;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.customer.model.CustomerStatus;
import dev.mam.buizsol.mamshop.customer.service.CustomerService;
import dev.mam.buizsol.mamshop.product.exception.ProductNotFoundException;
import dev.mam.buizsol.mamshop.product.model.Product;
import dev.mam.buizsol.mamshop.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
class ShopServiceImpl implements ShopService {

    private final CustomerService customerService;
    private final ProductService productService;
    private final ContractService contractService;
    private final BillingService billingService;

    ShopServiceImpl(
            final CustomerService customerService,
            final ProductService productService,
            final ContractService contractService,
            final BillingService billingService) {
        this.customerService = customerService;
        this.productService = productService;
        this.contractService = contractService;
        this.billingService = billingService;
    }

    @Override
    @Transactional
    public Customer registerCustomer(final Customer customer) {
        return customerService.createCustomer(customer);
    }

    @Override
    @Cacheable(value = "customers", key = "#customerId", unless = "#result == null")
    public Customer loadCustomer(final UUID customerId) throws CustomerNotFoundException {
        return customerService
                .findCustomerById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
    }

    @Override
    @Transactional
    @CacheEvict(value = "customers", key = "#customerId")
    public void removeCustomer(final UUID customerId) throws CustomerNotFoundException {
        final Customer customer = loadCustomer(customerId);
        final List<Contract> contracts = contractService.findContractsByCustomerId(customerId);

        final boolean hasActiveContracts = contracts.stream().anyMatch(c -> c.getStatus() == ContractStatus.ACTIVE);

        if (customer.getStatus() == CustomerStatus.ACTIVE && hasActiveContracts) {
            throw new CustomerValidationException("Cannot remove active customer with active contracts");
        }
        customerService.deleteCustomer(customerId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "customers", key = "#customerId")
    public void activateCustomer(final UUID customerId) throws CustomerNotFoundException {
        final Customer customer = loadCustomer(customerId);
        if (customer.getStatus() == CustomerStatus.ACTIVE) {
            log.info("Customer {} is already active, skipping", customerId);
            return;
        }
        customerService.activateCustomer(customerId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "customers", key = "#customerId")
    public void deactivateCustomer(final UUID customerId) throws CustomerNotFoundException {
        customerService.deactivateCustomer(customerId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "customers", key = "#customerId")
    public Customer updateAddress(final UUID customerId, final Address address) throws CustomerNotFoundException {
        checkCustomerActive(customerId);
        customerService.updateAddress(customerId, address);
        return loadCustomer(customerId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "customers", key = "#customerId")
    public Customer updateInvoiceAddress(final UUID customerId, final Address invoiceAddress)
            throws CustomerNotFoundException {
        checkCustomerActive(customerId);
        customerService.updateInvoiceAddress(customerId, invoiceAddress);
        return loadCustomer(customerId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "customers", key = "#customerId")
    public Customer updateCommunicationDetails(final UUID customerId, final CommunicationDetails details)
            throws CustomerNotFoundException {
        checkCustomerActive(customerId);
        customerService.updateCommunicationDetails(customerId, details);
        return loadCustomer(customerId);
    }

    @Override
    public List<Contract> loadAllContracts(final UUID customerId) throws CustomerNotFoundException {
        checkCustomerActive(customerId);
        return List.copyOf(contractService.findContractsByCustomerId(customerId));
    }

    @Override
    @Transactional
    public Invoice generateInvoice(final UUID customerId) throws CustomerNotFoundException, ProductNotFoundException {
        checkCustomerActive(customerId);
        return billingService.generateInvoice(customerId);
    }

    @Override
    @Transactional
    public void activateContract(final UUID customerId, final UUID contractId)
            throws CustomerNotFoundException, ContractNotFoundException {
        final Contract contract = contractService
                .findContractById(contractId)
                .orElseThrow(() -> new ContractNotFoundException("Contract not found: " + contractId));

        if (!contract.getCustomer().getId().equals(customerId)) {
            throw new CustomerValidationException("Contract does not belong to the specified customer");
        }

        if (contract.getStatus() == ContractStatus.ACTIVE) {
            log.info("Contract {} is already active, skipping", contractId);
            return;
        }

        contractService.updateContractStatus(contractId, ContractStatus.ACTIVE);
    }

    @Override
    @Cacheable(value = "products", key = "#brand")
    public List<Product> loadAllProductsForBrand(final Brand brand) {
        return List.copyOf(productService.findByBrand(brand));
    }

    @Override
    @Transactional
    public Contract purchaseProduct(final UUID customerId, final UUID productId)
            throws CustomerNotFoundException, ProductNotFoundException {

        final Customer customer = loadCustomer(customerId);
        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new CustomerNotActiveException("Customer " + customerId + " is not active");
        }

        final Product product = productService
                .findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        final boolean alreadyHasProduct = contractService.findContractsByCustomerId(customerId).stream()
                .anyMatch(c -> c.getProductId().equals(productId));

        if (alreadyHasProduct) {
            log.info("Customer {} already has a contract for product {}, skipping creation", customerId, productId);

            return contractService.findContractsByCustomerId(customerId).stream()
                    .filter(c -> c.getProductId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new ContractNotFoundException(
                            "Contract not found for product: " + productId + " for customer " + customerId));
        }

        return contractService.createContract(customer, product);
    }

    private void checkCustomerActive(final UUID customerId) throws CustomerNotFoundException {
        final Customer customer = loadCustomer(customerId);

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new CustomerNotActiveException("Customer " + customerId + " is not active");
        }
    }
}
