package dev.mam.buizsol.mamshop.contract.controller;

import dev.mam.buizsol.mamshop.contract.exception.BrandMismatchException;
import dev.mam.buizsol.mamshop.contract.exception.ContractNotFoundException;
import dev.mam.buizsol.mamshop.contract.mapper.ContractMapper;
import dev.mam.buizsol.mamshop.customer.exception.CustomerNotFoundException;
import dev.mam.buizsol.mamshop.shop.service.ShopService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName(value = "ContractController Infrastructure Tests")
@WebMvcTest(
        controllers = ContractController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
class ContractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShopService shopService;

    @MockitoBean
    private ContractMapper contractMapper;

    @Test
    @DisplayName(value = "Positive: should activate contract successfully when IDs are valid")
    void shouldActivateContractSuccessfully() throws Exception {
        final UUID customerId = UUID.randomUUID();
        final UUID contractId = UUID.randomUUID();
        doNothing().when(shopService).activateContract(customerId, contractId);
        mockMvc.perform(put("/api/v1/contracts/{contractId}/{customerId}/activate", contractId, customerId))
                .andExpect(status().isNoContent());

        verify(shopService).activateContract(customerId, contractId);
    }

    @Test
    @DisplayName(value = "Negative: should return 404 when contract not found during activation")
    void shouldReturn404WhenContractNotFoundDuringActivation() throws Exception {
        final UUID customerId = UUID.randomUUID();
        final UUID contractId = UUID.randomUUID();

        doThrow(new ContractNotFoundException("Contract not found"))
                .when(shopService)
                .activateContract(any(UUID.class), any(UUID.class));

        mockMvc.perform(put("/api/v1/contracts/{contractId}/{customerId}/activate", contractId, customerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("CONTRACT_NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("Contract not found")));

        verify(shopService).activateContract(customerId, contractId);
    }

    @Test
    @DisplayName(value = "Negative: should return 404 when customer not found during activation")
    void shouldReturn404WhenCustomerNotFoundDuringActivation() throws Exception {
        final UUID customerId = UUID.randomUUID();
        final UUID contractId = UUID.randomUUID();

        doThrow(new CustomerNotFoundException("Customer not found"))
                .when(shopService)
                .activateContract(any(UUID.class), any(UUID.class));

        mockMvc.perform(put("/api/v1/contracts/{contractId}/{customerId}/activate", contractId, customerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("CUSTOMER_NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("Customer not found")));

        verify(shopService).activateContract(customerId, contractId);
    }

    @Test
    @DisplayName(value = "Negative: should return 422 when contract belongs to a different customer")
    void shouldReturn422WhenContractBelongsToDifferentCustomer() throws Exception {
        final UUID customerId = UUID.randomUUID();
        final UUID contractId = UUID.randomUUID();

        doThrow(new BrandMismatchException("Brand mismatch"))
                .when(shopService)
                .activateContract(any(UUID.class), any(UUID.class));

        mockMvc.perform(put("/api/v1/contracts/{contractId}/{customerId}/activate", contractId, customerId))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.errorCode", is("BRAND_MISMATCH")))
                .andExpect(jsonPath("$.message", is("Brand mismatch")));

        verify(shopService).activateContract(customerId, contractId);
    }
}
