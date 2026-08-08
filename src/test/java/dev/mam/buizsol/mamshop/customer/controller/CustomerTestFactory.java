package dev.mam.buizsol.mamshop.customer.controller;

import dev.mam.buizsol.mamshop.contract.dto.ContractResponseDTO;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import dev.mam.buizsol.mamshop.contract.model.ContractStatus;
import dev.mam.buizsol.mamshop.customer.dto.AddressRequestDTO;
import dev.mam.buizsol.mamshop.customer.dto.AddressResponseDTO;
import dev.mam.buizsol.mamshop.customer.dto.CommunicationDetailsRequestDTO;
import dev.mam.buizsol.mamshop.customer.dto.CommunicationDetailsResponseDTO;
import dev.mam.buizsol.mamshop.customer.dto.CustomerRequestDTO;
import dev.mam.buizsol.mamshop.customer.dto.CustomerResponseDTO;
import dev.mam.buizsol.mamshop.customer.dto.PurchaseRequestDTO;
import dev.mam.buizsol.mamshop.customer.model.Address;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.customer.model.CommunicationDetails;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.customer.model.CustomerStatus;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomerTestFactory {

    public static CustomerRequestDTO createCustomerRequestDTO(
            final String firstName,
            final String lastName,
            final LocalDate birthDate,
            final AddressRequestDTO address,
            final AddressRequestDTO invoiceAddress,
            final CommunicationDetailsRequestDTO communicationDetails,
            final Brand brand) {
        return new CustomerRequestDTO(
                firstName, lastName, birthDate, address, invoiceAddress, communicationDetails, brand);
    }

    public static AddressRequestDTO createAddressRequestDTO(
            final String street, final String number, final String postcode, final String city, final String country) {
        return new AddressRequestDTO(street, number, postcode, city, country);
    }

    public static CommunicationDetailsRequestDTO createCommunicationDetailsRequestDTO(
            final String email, final String telephone) {
        return new CommunicationDetailsRequestDTO(email, telephone);
    }

    public static AddressResponseDTO createAddressResponseDTO(
            final String street, final String number, final String postcode, final String city, final String country) {
        return new AddressResponseDTO(street, number, postcode, city, country);
    }

    public static CommunicationDetailsResponseDTO createCommunicationDetailsResponseDTO(
            final String email, final String telephone) {
        return new CommunicationDetailsResponseDTO(email, telephone);
    }

    public static Customer createCustomer(
            final UUID id,
            final String firstName,
            final String lastName,
            final LocalDate birthDate,
            final Address address,
            final Address invoiceAddress,
            final CommunicationDetails communicationDetails,
            final Brand brand,
            final CustomerStatus status) {
        return Customer.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .birthDate(birthDate)
                .address(address)
                .invoiceAddress(invoiceAddress)
                .communicationDetails(communicationDetails)
                .brand(brand)
                .status(status)
                .contracts(Collections.emptyList())
                .invoices(Collections.emptyList())
                .build();
    }

    public static Address createAddress(
            final String street, final String number, final String postcode, final String city, final String country) {
        return Address.builder()
                .street(street)
                .number(number)
                .postcode(postcode)
                .city(city)
                .country(country)
                .build();
    }

    public static CommunicationDetails createCommunicationDetails(final String email, final String telephone) {
        return CommunicationDetails.builder().email(email).telephone(telephone).build();
    }

    public static CustomerResponseDTO createCustomerResponseDTO(
            final UUID id,
            final String firstName,
            final String lastName,
            final LocalDate birthDate,
            final AddressResponseDTO address,
            final AddressResponseDTO invoiceAddress,
            final CommunicationDetailsResponseDTO communicationDetails,
            final Brand brand,
            final CustomerStatus status) {
        return new CustomerResponseDTO(
                id, firstName, lastName, birthDate, address, invoiceAddress, communicationDetails, brand, status);
    }

    public static PurchaseRequestDTO createPurchaseRequestDTO(final UUID productId) {
        return new PurchaseRequestDTO(productId);
    }

    public static Contract createContract(
            final UUID id,
            final Customer customer,
            final String productType,
            final UUID productId,
            final LocalDate creationDate,
            final ContractStatus status) {
        return Contract.builder()
                .id(id)
                .customer(customer)
                .productType(productType)
                .productId(productId)
                .creationDate(creationDate)
                .status(status)
                .build();
    }

    public static ContractResponseDTO createContractResponseDTO(
            final UUID id,
            final UUID customerId,
            final UUID productId,
            final LocalDate creationDate,
            final ContractStatus status) {
        return new ContractResponseDTO(id, customerId, productId, creationDate, status);
    }
}
