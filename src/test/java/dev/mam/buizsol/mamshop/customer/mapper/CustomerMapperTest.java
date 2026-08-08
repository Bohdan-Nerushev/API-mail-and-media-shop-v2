package dev.mam.buizsol.mamshop.customer.mapper;

import dev.mam.buizsol.mamshop.customer.dto.AddressRequestDTO;
import dev.mam.buizsol.mamshop.customer.dto.AddressResponseDTO;
import dev.mam.buizsol.mamshop.customer.dto.CommunicationDetailsRequestDTO;
import dev.mam.buizsol.mamshop.customer.dto.CommunicationDetailsResponseDTO;
import dev.mam.buizsol.mamshop.customer.dto.CustomerRequestDTO;
import dev.mam.buizsol.mamshop.customer.dto.CustomerResponseDTO;
import dev.mam.buizsol.mamshop.customer.model.Address;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.customer.model.CommunicationDetails;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.customer.model.CustomerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerMapperTest {

    private CustomerMapper customerMapper;

    @BeforeEach
    void setUp() {
        customerMapper = new CustomerMapper();
    }

    @Test
    @DisplayName("Should map CustomerRequestDTO to Customer entity")
    void shouldMapDtoToCustomer() {
        final AddressRequestDTO addressDTO = new AddressRequestDTO("Street", "1", "12345", "City", "Country");
        final CommunicationDetailsRequestDTO commDTO = new CommunicationDetailsRequestDTO("test@mail.dev", "123456");
        final CustomerRequestDTO requestDTO =
                new CustomerRequestDTO("John", "Doe", LocalDate.of(1990, 1, 1), addressDTO, null, commDTO, Brand.GMX);

        final Customer result = customerMapper.toCustomer(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(result.getBrand()).isEqualTo(Brand.GMX);
        assertThat(result.getStatus()).isEqualTo(CustomerStatus.INACTIVE);
        assertThat(result.getAddress().getStreet()).isEqualTo("Street");
        assertThat(result.getCommunicationDetails().getEmail()).isEqualTo("test@mail.dev");
    }

    @Test
    @DisplayName("Should map CustomerRequestDTO with different invoice address")
    void shouldMapDtoToCustomerWithDifferentInvoiceAddress() {
        final AddressRequestDTO mainAddress = new AddressRequestDTO("Main St", "1", "123", "City", "DE");
        final AddressRequestDTO invAddress = new AddressRequestDTO("Inv St", "99", "999", "Other", "DE");
        final CommunicationDetailsRequestDTO commDTO = new CommunicationDetailsRequestDTO("test@mail.dev", "123456");
        final CustomerRequestDTO requestDTO = new CustomerRequestDTO(
                "John", "Doe", LocalDate.of(1990, 1, 1), mainAddress, invAddress, commDTO, Brand.GMX);

        final Customer result = customerMapper.toCustomer(requestDTO);

        assertThat(result.getAddress().getStreet()).isEqualTo("Main St");
        assertThat(result.getInvoiceAddress().getStreet()).isEqualTo("Inv St");
    }

    @Test
    @DisplayName("Should map Customer entity to CustomerResponseDTO")
    void shouldMapCustomerToResponseDTO() {
        final Address address = new Address("Street", "1", "12345", "City", "Country");
        final CommunicationDetails comm = new CommunicationDetails("test@mail.dev", "123456");
        final UUID customerId = UUID.randomUUID();
        final Customer customer = Customer.builder()
                .id(customerId)
                .firstName("Jane")
                .lastName("Doe")
                .birthDate(LocalDate.of(1995, 5, 5))
                .address(address)
                .invoiceAddress(address)
                .communicationDetails(comm)
                .brand(Brand.WEB_DE)
                .status(CustomerStatus.ACTIVE)
                .build();

        final CustomerResponseDTO result = customerMapper.toResponseDTO(customer);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(customerId);
        assertThat(result.firstName()).isEqualTo("Jane");
        assertThat(result.lastName()).isEqualTo("Doe");
        assertThat(result.birthDate()).isEqualTo(LocalDate.of(1995, 5, 5));
        assertThat(result.brand()).isEqualTo(Brand.WEB_DE);
        assertThat(result.status()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(result.address().street()).isEqualTo("Street");
        assertThat(result.invoiceAddress().street()).isEqualTo("Street");
        assertThat(result.communicationDetails().email()).isEqualTo("test@mail.dev");
    }

    @Test
    @DisplayName("Should map AddressRequestDTO to Address entity")
    void shouldMapAddressDTOToEntity() {
        final AddressRequestDTO dto = new AddressRequestDTO("Main St", "10", "10115", "Berlin", "Germany");

        final Address result = customerMapper.toAddress(dto);

        assertThat(result).isNotNull();
        assertThat(result.getStreet()).isEqualTo("Main St");
        assertThat(result.getNumber()).isEqualTo("10");
        assertThat(result.getPostcode()).isEqualTo("10115");
        assertThat(result.getCity()).isEqualTo("Berlin");
        assertThat(result.getCountry()).isEqualTo("Germany");
    }

    @Test
    @DisplayName("Should map CommunicationDetailsRequestDTO to CommunicationDetails entity")
    void shouldMapCommunicationDTOToEntity() {
        final CommunicationDetailsRequestDTO dto = new CommunicationDetailsRequestDTO("dev@test.com", "0987654321");

        final CommunicationDetails result = customerMapper.toCommunicationDetails(dto);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("dev@test.com");
        assertThat(result.getTelephone()).isEqualTo("0987654321");
    }

    @Test
    @DisplayName("Should map raw address fields to AddressResponseDTO")
    void shouldMapRawFieldsToAddressResponseDTO() {
        final AddressResponseDTO result = customerMapper.toAddressResponseDTO("St", "5", "54321", "Munich", "DE");

        assertThat(result).isNotNull();
        assertThat(result.street()).isEqualTo("St");
        assertThat(result.number()).isEqualTo("5");
        assertThat(result.postcode()).isEqualTo("54321");
        assertThat(result.city()).isEqualTo("Munich");
        assertThat(result.country()).isEqualTo("DE");
    }

    @Test
    @DisplayName("Should map raw communication fields to CommunicationDetailsResponseDTO")
    void shouldMapRawFieldsToCommResponseDTO() {
        final CommunicationDetailsResponseDTO result = customerMapper.toCommunicationResponseDTO("email@com", "111");

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("email@com");
        assertThat(result.telephone()).isEqualTo("111");
    }
}
