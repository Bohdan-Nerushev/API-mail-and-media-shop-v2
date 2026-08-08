package dev.mam.buizsol.mamshop.contract.service;

import dev.mam.buizsol.mamshop.contract.exception.BrandMismatchException;
import dev.mam.buizsol.mamshop.contract.exception.ContractNotFoundException;
import dev.mam.buizsol.mamshop.contract.model.Contract;
import dev.mam.buizsol.mamshop.contract.model.ContractStatus;
import dev.mam.buizsol.mamshop.customer.exception.CustomerNotActiveException;
import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.customer.model.Customer;
import dev.mam.buizsol.mamshop.customer.model.CustomerStatus;
import dev.mam.buizsol.mamshop.product.model.Product;
import dev.mam.buizsol.mamshop.product.model.StandardMailProduct;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationInterceptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContractService Tests")
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private ContractServiceImpl contractServiceImpl;

    private ContractService contractService;
    private LocalValidatorFactoryBean validatorFactory;

    private Customer activeCustomer;
    private Product matchingProduct;

    @BeforeEach
    void setUp() {
        validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.afterPropertiesSet();

        ProxyFactory proxyFactory = new ProxyFactory(contractServiceImpl);
        proxyFactory.addAdvice(new MethodValidationInterceptor((Validator) validatorFactory));
        contractService = (ContractService) proxyFactory.getProxy();

        activeCustomer = mock(Customer.class);

        matchingProduct = new StandardMailProduct("Test Product", Brand.WEB_DE, new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("Success: createContract generates and saves a contract")
    void shouldGenerateAndSaveContractWhenCustomerIsActiveAndDataIsValid() {
        setupActiveCustomer();
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Contract created = contractServiceImpl.createContract(activeCustomer, matchingProduct);

        assertNotNull(created);
        assertEquals(activeCustomer.getId(), created.getCustomer().getId());
        assertEquals(matchingProduct.getId(), created.getProductId());
        assertEquals(ContractStatus.INACTIVE, created.getStatus());
        verify(contractRepository).save(any(Contract.class));
    }

    @Test
    @DisplayName("Success: findContractById returns contract when present")
    void shouldReturnContractWhenIdExists() {
        UUID contractId = UUID.randomUUID();
        Contract contract = mock(Contract.class);
        when(contractRepository.findWithDetailsById(contractId)).thenReturn(Optional.of(contract));

        Optional<Contract> result = contractService.findContractById(contractId);

        assertTrue(result.isPresent());
        assertEquals(contract, result.get());
    }

    @Test
    @DisplayName("Success: findContractsByCustomerId returns list of contracts")
    void shouldReturnListOfContractsWhenCustomerHasThem() {
        UUID customerId = UUID.randomUUID();
        List<Contract> contracts = List.of(mock(Contract.class), mock(Contract.class));
        when(contractRepository.findByCustomerId(customerId)).thenReturn(contracts);

        List<Contract> result = contractService.findContractsByCustomerId(customerId);

        assertEquals(2, result.size());
        assertEquals(contracts, result);
    }

    @Test
    @DisplayName("Success: updateContractStatus updates status correctly")
    void shouldUpdateStatusWhenContractExists() throws ContractNotFoundException {
        setupActiveCustomer();
        UUID contractId = UUID.randomUUID();
        Contract contract = Contract.create(activeCustomer, matchingProduct);
        when(contractRepository.findWithDetailsById(contractId)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Contract updated = contractService.updateContractStatus(contractId, ContractStatus.INACTIVE);

        assertEquals(ContractStatus.INACTIVE, updated.getStatus());
        verify(contractRepository).save(any(Contract.class));
    }

    @Test
    @DisplayName("Negative: updateContractStatus throws exception when contract not found")
    void shouldThrowExceptionWhenUpdatingStatusOfNonExistentContract() {
        UUID contractId = UUID.randomUUID();
        when(contractRepository.findWithDetailsById(contractId)).thenReturn(Optional.empty());

        assertThrows(
                ContractNotFoundException.class,
                () -> contractService.updateContractStatus(contractId, ContractStatus.INACTIVE));
    }

    @Test
    @DisplayName("Negative: createContract throws ConstraintViolationException when customer is null")
    void shouldThrowConstraintViolationWhenCustomerIsNull() {
        assertThrows(ConstraintViolationException.class, () -> contractService.createContract(null, matchingProduct));
    }

    @Test
    @DisplayName("Negative: createContract throws ConstraintViolationException when product is null")
    void shouldThrowConstraintViolationWhenProductIsNull() {
        assertThrows(ConstraintViolationException.class, () -> contractService.createContract(activeCustomer, null));
    }

    @Test
    @DisplayName("Negative: createContract throws ConstraintViolationException when both parameters are null")
    void shouldThrowConstraintViolationWhenBothParametersAreNull() {
        assertThrows(ConstraintViolationException.class, () -> contractService.createContract(null, null));
    }

    @Test
    @DisplayName("Negative: createContract throws exception on brand mismatch")
    void shouldThrowExceptionWhenCreatingContractWithBrandMismatch() {
        setupActiveCustomer();
        Product mismatchProduct = new StandardMailProduct("Mismatch Brand", Brand.GMX, new BigDecimal("10.00"));

        assertThrows(
                BrandMismatchException.class,
                () -> contractServiceImpl.createContract(activeCustomer, mismatchProduct));
    }

    @Test
    @DisplayName("Negative: createContract throws exception when customer is inactive")
    void shouldThrowExceptionWhenCreatingContractForInactiveCustomer() {
        setupActiveCustomer();
        when(activeCustomer.getStatus()).thenReturn(CustomerStatus.INACTIVE);

        assertThrows(
                CustomerNotActiveException.class,
                () -> contractServiceImpl.createContract(activeCustomer, matchingProduct));
    }

    @Test
    @DisplayName("Boundary: findContractsByCustomerId returns empty list when no contracts exist")
    void shouldReturnEmptyListWhenNoContractsForCustomer() {
        UUID customerId = UUID.randomUUID();
        when(contractRepository.findByCustomerId(customerId)).thenReturn(List.of());

        List<Contract> result = contractService.findContractsByCustomerId(customerId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Success: findContractsByProductId returns list of contracts")
    void shouldReturnListOfContractsWhenProductHasThem() {
        UUID productId = UUID.randomUUID();
        List<Contract> contracts = List.of(mock(Contract.class));
        when(contractRepository.findByProductId(productId)).thenReturn(contracts);

        List<Contract> result = contractService.findContractsByProductId(productId);

        assertEquals(1, result.size());
        assertEquals(contracts, result);
    }

    @Test
    @DisplayName("Boundary: findContractsByProductId returns empty list when no contracts exist")
    void shouldReturnEmptyListWhenNoContractsForProduct() {
        UUID productId = UUID.randomUUID();
        when(contractRepository.findByProductId(productId)).thenReturn(List.of());

        List<Contract> result = contractService.findContractsByProductId(productId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Success: update status when status is already the same")
    void shouldUpdateStatusSuccessfullyWhenStatusIsTheSame() throws ContractNotFoundException {
        setupActiveCustomer();
        UUID contractId = UUID.randomUUID();
        Contract contract = Contract.create(activeCustomer, matchingProduct).withStatus(ContractStatus.ACTIVE);
        when(contractRepository.findWithDetailsById(contractId)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Contract updated = contractService.updateContractStatus(contractId, ContractStatus.ACTIVE);

        assertEquals(ContractStatus.ACTIVE, updated.getStatus());
        verify(contractRepository).save(any(Contract.class));
    }

    @Test
    @DisplayName("Success: handle multiple consecutive status updates")
    void shouldHandleMultipleConsecutiveStatusUpdates() throws ContractNotFoundException {
        setupActiveCustomer();
        UUID contractId = UUID.randomUUID();
        Contract contract = Contract.create(activeCustomer, matchingProduct);

        when(contractRepository.findWithDetailsById(contractId)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Contract updated1 = contractService.updateContractStatus(contractId, ContractStatus.ACTIVE);
        assertEquals(ContractStatus.ACTIVE, updated1.getStatus());

        Contract updated2 = contractService.updateContractStatus(contractId, ContractStatus.INACTIVE);
        assertEquals(ContractStatus.INACTIVE, updated2.getStatus());

        Contract updated3 = contractService.updateContractStatus(contractId, ContractStatus.ACTIVE);
        assertEquals(ContractStatus.ACTIVE, updated3.getStatus());

        verify(contractRepository, times(3)).save(any(Contract.class));
    }

    @Test
    @DisplayName("Boundary: contract creation date should always be current date")
    void shouldSetCreationDateToCurrentDateWhenCreatingContract() {
        setupActiveCustomer();
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Contract created = contractServiceImpl.createContract(activeCustomer, matchingProduct);

        assertEquals(LocalDate.now(), created.getCreationDate());
        verify(contractRepository).save(any(Contract.class));
    }

    private void setupActiveCustomer() {
        lenient().when(activeCustomer.getStatus()).thenReturn(CustomerStatus.ACTIVE);
        lenient().when(activeCustomer.getBrand()).thenReturn(Brand.WEB_DE);
        lenient().when(activeCustomer.getId()).thenReturn(UUID.randomUUID());
    }

    @Test
    @DisplayName("Success: findAllContracts returns list of contracts")
    void shouldReturnListOfContractsWhenContractsExist() {
        List<Contract> contracts = List.of(mock(Contract.class));
        when(contractRepository.findAll()).thenReturn(contracts);

        List<Contract> result = contractService.findAllContracts();

        assertEquals(1, result.size());
        assertEquals(contracts, result);
    }

    @Test
    @DisplayName("Boundary: findAllContracts returns empty list when no contracts exist")
    void shouldReturnEmptyListWhenNoContractsExist() {
        when(contractRepository.findAll()).thenReturn(List.of());

        List<Contract> result = contractService.findAllContracts();

        assertTrue(result.isEmpty());
    }
}
