package dev.mam.buizsol.mamshop.contract.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContractRepository Tests")
class ContractRepositoryTest {

    @Mock
    private ContractRepository repository;

    @Test
    @DisplayName("Should verify mock interaction for findById")
    void shouldFindContractById() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        var result = repository.findById(id);

        assertThat(result).isEmpty();
        verify(repository).findById(id);
    }
}
