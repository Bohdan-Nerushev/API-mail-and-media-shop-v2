package dev.mam.buizsol.mamshop.contract.mapper;

import dev.mam.buizsol.mamshop.contract.dto.ContractResponseDTO;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import dev.mam.buizsol.mamshop.contract.model.ContractStatus;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContractMapperTest {

    private ContractMapper contractMapper;

    @BeforeEach
    void setUp() {
        contractMapper = new ContractMapper();
    }

    @Test
    @DisplayName("Should map Contract entity to ContractResponseDTO")
    void shouldMapContractToResponseDTO() {
        final UUID contractId = UUID.randomUUID();
        final UUID customerId = UUID.randomUUID();
        final UUID productId = UUID.randomUUID();
        final LocalDate creationDate = LocalDate.now();

        final Customer customer = mock(Customer.class);
        when(customer.getId()).thenReturn(customerId);

        final Contract contract = Contract.builder()
                .id(contractId)
                .customer(customer)
                .productId(productId)
                .creationDate(creationDate)
                .status(ContractStatus.ACTIVE)
                .build();

        final ContractResponseDTO result = contractMapper.toContractResponseDTO(contract);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(contractId);
        assertThat(result.customerId()).isEqualTo(customerId);
        assertThat(result.productId()).isEqualTo(productId);
        assertThat(result.creationDate()).isEqualTo(creationDate);
        assertThat(result.status()).isEqualTo(ContractStatus.ACTIVE);
    }
}
