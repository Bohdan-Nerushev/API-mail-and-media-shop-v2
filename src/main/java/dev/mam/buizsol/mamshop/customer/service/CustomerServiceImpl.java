package dev.mam.buizsol.mamshop.customer.service;

import dev.mam.buizsol.mamshop.customer.exception.CustomerNotFoundException;
import dev.mam.buizsol.mamshop.customer.exception.CustomerValidationException;
import dev.mam.buizsol.mamshop.customer.model.Address;
import dev.mam.buizsol.mamshop.customer.model.CommunicationDetails;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.customer.model.CustomerStatus;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    CustomerServiceImpl(final CustomerRepository customerRepository) {
        if (customerRepository == null) {
            throw new CustomerValidationException("Customer repository must not be null");
        }
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    @CacheEvict(value = "customers", allEntries = true)
    public Customer createCustomer(final Customer customer) {
        if (customer == null) {
            throw new CustomerValidationException("Customer must not be null");
        }
        return customerRepository.save(customer);
    }

    @Override
    @Transactional
    @CacheEvict(value = "customers", key = "#customerId")
    public void updateAddress(final UUID customerId, final Address address) throws CustomerNotFoundException {
        if (customerId == null || address == null) {
            throw new CustomerValidationException("Parameters must not be null");
        }
        final Customer customer = customerRepository.getById(customerId);
        customer.setAddress(address);
        customerRepository.save(customer);
    }

    @Override
    @Transactional
    @CacheEvict(value = "customers", key = "#customerId")
    public void updateInvoiceAddress(final UUID customerId, final Address address) throws CustomerNotFoundException {
        if (customerId == null || address == null) {
            throw new CustomerValidationException("Parameters must not be null");
        }
        final Customer customer = customerRepository.getById(customerId);
        customer.setInvoiceAddress(address);
        customerRepository.save(customer);
    }

    @Override
    @Transactional
    @CacheEvict(value = "customers", key = "#customerId")
    public void updateCommunicationDetails(final UUID customerId, final CommunicationDetails communicationDetails)
            throws CustomerNotFoundException {
        if (customerId == null || communicationDetails == null) {
            throw new CustomerValidationException("Parameters must not be null");
        }
        final Customer customer = customerRepository.getById(customerId);
        customer.setCommunicationDetails(communicationDetails);
        customerRepository.save(customer);
    }

    @Override
    @Transactional
    @CacheEvict(value = "customers", key = "#customerId")
    public void activateCustomer(final UUID customerId) throws CustomerNotFoundException {
        if (customerId == null) {
            throw new CustomerValidationException("Customer ID must not be null");
        }
        final Customer customer = customerRepository.getById(customerId);
        customer.setStatus(CustomerStatus.ACTIVE);
        customerRepository.save(customer);
    }

    @Override
    @Transactional
    @CacheEvict(value = "customers", key = "#customerId")
    public void deactivateCustomer(final UUID customerId) throws CustomerNotFoundException {
        if (customerId == null) {
            throw new CustomerValidationException("Customer ID must not be null");
        }
        final Customer customer = customerRepository.getById(customerId);
        customer.setStatus(CustomerStatus.INACTIVE);
        customerRepository.save(customer);
    }

    @Override
    @Transactional
    @CacheEvict(value = "customers", key = "#customerId")
    public void deleteCustomer(final UUID customerId) throws CustomerNotFoundException {
        if (customerId == null) {
            throw new CustomerValidationException("Customer ID must not be null");
        }
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException("Customer with ID " + customerId + " not found");
        }
        customerRepository.deleteById(customerId);
    }

    @Override
    @Cacheable(value = "customers", key = "#customerId")
    public Optional<Customer> findCustomerById(final UUID customerId) {
        return customerRepository.findById(customerId);
    }

    @Override
    @Cacheable(value = "customersList")
    public List<Customer> findAllCustomers() {
        return List.copyOf(customerRepository.findAll());
    }
}
