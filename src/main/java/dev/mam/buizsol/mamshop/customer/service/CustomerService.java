package dev.mam.buizsol.mamshop.customer.service;

import dev.mam.buizsol.mamshop.customer.exception.CustomerNotFoundException;
import dev.mam.buizsol.mamshop.customer.model.Address;
import dev.mam.buizsol.mamshop.customer.model.CommunicationDetails;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Transactional
@Validated
public interface CustomerService {

    @NotNull
    Customer createCustomer(@Valid @NotNull Customer customer);

    void updateAddress(@NotNull UUID customerId, @Valid @NotNull Address address) throws CustomerNotFoundException;

    void updateInvoiceAddress(@NotNull UUID customerId, @Valid @NotNull Address address)
            throws CustomerNotFoundException;

    void updateCommunicationDetails(@NotNull UUID customerId, @Valid @NotNull CommunicationDetails communicationDetails)
            throws CustomerNotFoundException;

    void activateCustomer(@NotNull UUID customerId) throws CustomerNotFoundException;

    void deactivateCustomer(@NotNull UUID customerId) throws CustomerNotFoundException;

    void deleteCustomer(@NotNull UUID customerId) throws CustomerNotFoundException;

    @NotNull
    Optional<Customer> findCustomerById(@NotNull UUID customerId);

    @NotNull
    List<Customer> findAllCustomers();
}
