package dev.mam.buizsol.mamshop.customer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mam.buizsol.mamshop.customer.dto.AddressRequestDTO;
import dev.mam.buizsol.mamshop.customer.dto.CommunicationDetailsRequestDTO;
import dev.mam.buizsol.mamshop.customer.exception.CustomerNotFoundException;
import dev.mam.buizsol.mamshop.customer.mapper.CustomerMapper;
import dev.mam.buizsol.mamshop.customer.model.Address;
import dev.mam.buizsol.mamshop.customer.model.CommunicationDetails;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.shop.service.ShopService;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName(value = "CustomerController Tests")
@WebMvcTest(
        controllers = CustomerController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
class CustomerControllerTest {

    @Autowired
    @NotNull
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    @NotNull
    private ShopService shopService;

    @MockitoBean
    @NotNull
    private CustomerMapper customerMapper;

    @Test
    @DisplayName(
            "Positive: Given an existing customer ID, when the client requests to deactivate the customer, then the customer status is changed to INACTIVE successfully")
    void shouldDeactivateCustomerSuccessfully() throws Exception {
        final UUID customerId = UUID.randomUUID();

        doNothing().when(shopService).deactivateCustomer(any(UUID.class));

        mockMvc.perform(put("/api/v1/customers/{customerId}/deactivate", customerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(shopService).deactivateCustomer(customerId);
        verifyNoInteractions(customerMapper);
    }

    @Test
    @DisplayName(
            "Negative: Given a non-existing customer ID, when the client requests to deactivate the customer, then a 404 error is returned")
    void shouldReturn404WhenDeactivatingNonExistingCustomer() throws Exception {
        final UUID unknownId = UUID.randomUUID();

        doThrow(new CustomerNotFoundException("Customer not found with ID: " + unknownId))
                .when(shopService)
                .deactivateCustomer(unknownId);

        mockMvc.perform(put("/api/v1/customers/{customerId}/deactivate", unknownId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Customer not found"));

        verify(shopService).deactivateCustomer(unknownId);
        verifyNoInteractions(customerMapper);
    }

    @Test
    @DisplayName(
            "Positive: Given an existing customer ID and valid address data, when the client updates the address, then the address is updated successfully")
    void shouldUpdateCustomerAddressSuccessfully() throws Exception {
        final UUID customerId = UUID.randomUUID();
        final AddressRequestDTO addressRequestDTO =
                CustomerTestFactory.createAddressRequestDTO("Main St", "10", "12345", "Berlin", "Germany");
        final Address address = CustomerTestFactory.createAddress("Main St", "10", "12345", "Berlin", "Germany");

        when(customerMapper.toAddress(addressRequestDTO)).thenReturn(address);
        when(shopService.updateAddress(customerId, address)).thenReturn(mock(Customer.class));

        mockMvc.perform(put("/api/v1/customers/{customerId}/address", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addressRequestDTO)))
                .andExpect(status().isNoContent());

        verify(customerMapper).toAddress(addressRequestDTO);
        verify(shopService).updateAddress(customerId, address);
    }

    @Test
    @DisplayName(
            "Negative: Given a non-existing customer ID, when the client updates the address, then a 404 error is returned")
    void shouldReturn404WhenUpdatingAddressForNonExistingCustomer() throws Exception {
        final UUID unknownId = UUID.randomUUID();
        final AddressRequestDTO addressRequestDTO =
                CustomerTestFactory.createAddressRequestDTO("Main St", "10", "12345", "Berlin", "Germany");
        final Address address = CustomerTestFactory.createAddress("Main St", "10", "12345", "Berlin", "Germany");

        when(customerMapper.toAddress(addressRequestDTO)).thenReturn(address);
        when(shopService.updateAddress(unknownId, address))
                .thenThrow(new CustomerNotFoundException("Customer not found with ID: " + unknownId));

        mockMvc.perform(put("/api/v1/customers/{customerId}/address", unknownId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addressRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Customer not found"));

        verify(customerMapper).toAddress(addressRequestDTO);
        verify(shopService).updateAddress(unknownId, address);
    }

    @Test
    @DisplayName(
            "Positive: Given an existing customer ID and valid invoice address, when the client requests to update the invoice address, then the address is updated successfully")
    void shouldUpdateCustomerInvoiceAddressSuccessfully() throws Exception {
        final UUID customerId = UUID.randomUUID();
        final AddressRequestDTO addressRequestDTO =
                CustomerTestFactory.createAddressRequestDTO("Invoice St", "100", "54321", "Munich", "Germany");
        final Address address = CustomerTestFactory.createAddress("Invoice St", "100", "54321", "Munich", "Germany");

        when(customerMapper.toAddress(addressRequestDTO)).thenReturn(address);
        when(shopService.updateInvoiceAddress(customerId, address)).thenReturn(mock(Customer.class));

        mockMvc.perform(put("/api/v1/customers/{customerId}/invoice-address", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addressRequestDTO)))
                .andExpect(status().isNoContent());

        verify(customerMapper).toAddress(addressRequestDTO);
        verify(shopService).updateInvoiceAddress(customerId, address);
    }

    @Test
    @DisplayName(
            "Negative: Given a non-existing customer ID, when the client updates the invoice address, then a 404 error is returned")
    void shouldReturn404WhenUpdatingInvoiceAddressForNonExistingCustomer() throws Exception {
        final UUID unknownId = UUID.randomUUID();
        final AddressRequestDTO addressRequestDTO =
                CustomerTestFactory.createAddressRequestDTO("Invoice St", "100", "54321", "Munich", "Germany");
        final Address address = CustomerTestFactory.createAddress("Invoice St", "100", "54321", "Munich", "Germany");

        when(customerMapper.toAddress(addressRequestDTO)).thenReturn(address);
        when(shopService.updateInvoiceAddress(unknownId, address))
                .thenThrow(new CustomerNotFoundException("Customer not found with ID: " + unknownId));

        mockMvc.perform(put("/api/v1/customers/{customerId}/invoice-address", unknownId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addressRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Customer not found"));

        verify(customerMapper).toAddress(addressRequestDTO);
        verify(shopService).updateInvoiceAddress(unknownId, address);
    }

    @Test
    @DisplayName(
            "Positive: Given an existing customer ID and valid communication details, when the client updates communication details, then the details are updated successfully")
    void shouldUpdateCustomerCommunicationDetailsSuccessfully() throws Exception {
        final UUID customerId = UUID.randomUUID();
        final CommunicationDetailsRequestDTO communicationDetailsRequestDTO =
                CustomerTestFactory.createCommunicationDetailsRequestDTO("test@example.com", "+123456789");
        final CommunicationDetails communicationDetails =
                CustomerTestFactory.createCommunicationDetails("test@example.com", "+123456789");

        when(customerMapper.toCommunicationDetails(communicationDetailsRequestDTO))
                .thenReturn(communicationDetails);
        when(shopService.updateCommunicationDetails(customerId, communicationDetails))
                .thenReturn(mock(Customer.class));

        mockMvc.perform(put("/api/v1/customers/{customerId}/communication-details", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(communicationDetailsRequestDTO)))
                .andExpect(status().isNoContent());

        verify(customerMapper).toCommunicationDetails(communicationDetailsRequestDTO);
        verify(shopService).updateCommunicationDetails(customerId, communicationDetails);
    }

    @Test
    @DisplayName(
            "Negative: Given a non-existing customer ID, when the client updates communication details, then a 404 error is returned")
    void shouldReturn404WhenUpdatingCommunicationDetailsForNonExistingCustomer() throws Exception {
        final UUID unknownId = UUID.randomUUID();
        final CommunicationDetailsRequestDTO communicationDetailsRequestDTO =
                CustomerTestFactory.createCommunicationDetailsRequestDTO("test@example.com", "+123456789");
        final CommunicationDetails communicationDetails =
                CustomerTestFactory.createCommunicationDetails("test@example.com", "+123456789");

        when(customerMapper.toCommunicationDetails(communicationDetailsRequestDTO))
                .thenReturn(communicationDetails);
        when(shopService.updateCommunicationDetails(unknownId, communicationDetails))
                .thenThrow(new CustomerNotFoundException("Customer not found with ID: " + unknownId));

        mockMvc.perform(put("/api/v1/customers/{customerId}/communication-details", unknownId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(communicationDetailsRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Customer not found"));

        verify(customerMapper).toCommunicationDetails(communicationDetailsRequestDTO);
        verify(shopService).updateCommunicationDetails(unknownId, communicationDetails);
    }
}
