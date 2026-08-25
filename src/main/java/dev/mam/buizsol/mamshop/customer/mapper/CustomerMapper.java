package dev.mam.buizsol.mamshop.customer.mapper;

import dev.mam.buizsol.mamshop.customer.dto.AddressRequestDTO;
import dev.mam.buizsol.mamshop.customer.dto.AddressResponseDTO;
import dev.mam.buizsol.mamshop.customer.dto.CommunicationDetailsRequestDTO;
import dev.mam.buizsol.mamshop.customer.dto.CommunicationDetailsResponseDTO;
import dev.mam.buizsol.mamshop.customer.dto.CustomerRequestDTO;
import dev.mam.buizsol.mamshop.customer.dto.CustomerResponseDTO;
import dev.mam.buizsol.mamshop.customer.model.Address;
import dev.mam.buizsol.mamshop.customer.model.CommunicationDetails;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toCustomer(@NotNull final CustomerRequestDTO dto) {
        final Customer customer = Customer.create(
                dto.firstName(),
                dto.lastName(),
                dto.birthDate(),
                toAddress(dto.address()),
                dto.invoiceAddress() != null ? toAddress(dto.invoiceAddress()) : null,
                toCommunicationDetails(dto.communicationDetails()),
                dto.brand());
        customer.setPassword(dto.password());
        return customer;
    }

    public CustomerResponseDTO toResponseDTO(@NotNull final Customer customer) {
        return new CustomerResponseDTO(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getBirthDate(),
                toAddressResponseDTO(customer.getAddress()),
                toAddressResponseDTO(customer.getInvoiceAddress()),
                toCommunicationResponseDTO(customer.getCommunicationDetails()),
                customer.getBrand(),
                customer.getStatus());
    }

    public Address toAddress(@NotNull final AddressRequestDTO dto) {
        return new Address(dto.street(), dto.number(), dto.postcode(), dto.city(), dto.country());
    }

    public AddressRequestDTO toAddressDTO(@NotNull final Address address) {
        return new AddressRequestDTO(
                address.getStreet(),
                address.getNumber(),
                address.getPostcode(),
                address.getCity(),
                address.getCountry());
    }

    public CommunicationDetails toCommunicationDetails(@NotNull final CommunicationDetailsRequestDTO dto) {
        return new CommunicationDetails(dto.email(), dto.telephone());
    }

    public CommunicationDetailsRequestDTO toCommunicationDetailsDTO(
            @NotNull final CommunicationDetails communicationDetails) {
        return new CommunicationDetailsRequestDTO(communicationDetails.getEmail(), communicationDetails.getTelephone());
    }

    public AddressResponseDTO toAddressResponseDTO(
            @NotNull final String street,
            @NotNull final String number,
            @NotNull final String postcode,
            @NotNull final String city,
            @NotNull final String country) {
        return new AddressResponseDTO(street, number, postcode, city, country);
    }

    public AddressResponseDTO toAddressResponseDTO(@NotNull final Address address) {
        return new AddressResponseDTO(
                address.getStreet(),
                address.getNumber(),
                address.getPostcode(),
                address.getCity(),
                address.getCountry());
    }

    public CommunicationDetailsResponseDTO toCommunicationResponseDTO(
            @NotNull final String email, @NotNull final String telephone) {
        return new CommunicationDetailsResponseDTO(email, telephone);
    }

    public CommunicationDetailsResponseDTO toCommunicationResponseDTO(
            @NotNull final CommunicationDetails communicationDetails) {
        return new CommunicationDetailsResponseDTO(
                communicationDetails.getEmail(), communicationDetails.getTelephone());
    }
}
