package dev.mam.buizsol.mamshop.contract.service;

import dev.mam.buizsol.mamshop.contract.exception.ContractNotFoundException;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import dev.mam.buizsol.mamshop.contract.model.ContractStatus;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.product.model.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class ContractServiceImpl implements ContractService {

    private final ContractRepository repository;

    ContractServiceImpl(final ContractRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(value = "contracts", allEntries = true),
                @CacheEvict(value = "customerContracts", allEntries = true),
                @CacheEvict(value = "productContracts", allEntries = true)
            })
    public Contract createContract(final Customer customer, final Product product) {
        return repository.save(Contract.create(customer, product));
    }

    @Override
    @Cacheable(value = "contracts", key = "#contractId")
    public Optional<Contract> findContractById(final UUID contractId) {
        return repository.findWithDetailsById(contractId);
    }

    @Override
    @Cacheable(value = "customerContracts", key = "#customerId")
    public List<Contract> findContractsByCustomerId(final UUID customerId) {
        return List.copyOf(repository.findByCustomerId(customerId));
    }

    @Override
    @Cacheable(value = "productContracts", key = "#productId")
    public List<Contract> findContractsByProductId(final UUID productId) {
        return List.copyOf(repository.findByProductId(productId));
    }

    @Override
    @Transactional
    @Caching(
            put = @CachePut(value = "contracts", key = "#contractId"),
            evict = {
                @CacheEvict(value = "customerContracts", allEntries = true),
                @CacheEvict(value = "productContracts", allEntries = true)
            })
    public Contract updateContractStatus(final UUID contractId, final ContractStatus changeStatus)
            throws ContractNotFoundException {
        return repository
                .findWithDetailsById(contractId)
                .map(contract -> repository.save(contract.withStatus(changeStatus)))
                .orElseThrow(() -> new ContractNotFoundException("Contract with ID " + contractId + " not found"));
    }

    @Override
    @Cacheable(value = "contracts")
    public List<Contract> findAllContracts() {
        return List.copyOf(repository.findAll());
    }
}
