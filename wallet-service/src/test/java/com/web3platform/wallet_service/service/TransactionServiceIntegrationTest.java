package com.web3platform.wallet_service.service;

import com.web3platform.wallet_service.model.DappTransaction;
import com.web3platform.wallet_service.repository.DappTransactionRepository;
import com.web3platform.wallet_service.dto.TransactionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @MockBean
    private DappTransactionRepository transactionRepository;

    private DappTransaction testTransaction1;
    private DappTransaction testTransaction2;

    @BeforeEach
    void setUp() {
        // Create test transactions
        testTransaction1 = new DappTransaction();
        testTransaction1.setId(1L);
        testTransaction1.setType("SWAP");
        testTransaction1.setStatus("COMPLETED");
        testTransaction1.setTransactionHash("0x123");
        testTransaction1.setTimestamp(LocalDateTime.now());
        testTransaction1.setAmount("1.0");
        testTransaction1.setToken("ETH");
        testTransaction1.setFromAddress("0x123");
        testTransaction1.setToAddress("0x456");

        testTransaction2 = new DappTransaction();
        testTransaction2.setId(2L);
        testTransaction2.setType("TRANSFER");
        testTransaction2.setStatus("PENDING");
        testTransaction2.setTransactionHash("0x456");
        testTransaction2.setTimestamp(LocalDateTime.now());
        testTransaction2.setAmount("0.5");
        testTransaction2.setToken("USDC");
        testTransaction2.setFromAddress("0x456");
        testTransaction2.setToAddress("0x789");

        // Mock repository responses
        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by("timestamp").descending());
        Page<DappTransaction> transactionPage = new PageImpl<>(Arrays.asList(testTransaction1, testTransaction2));

        when(transactionRepository.findAll(any(PageRequest.class))).thenReturn(transactionPage);
        when(transactionRepository.findById(1L)).thenReturn(java.util.Optional.of(testTransaction1));
        when(transactionRepository.findByTypeAndStatus("SWAP", "COMPLETED", pageRequest))
            .thenReturn(new PageImpl<>(List.of(testTransaction1)));
        when(transactionRepository.findByType("SWAP", pageRequest))
            .thenReturn(new PageImpl<>(List.of(testTransaction1)));
        when(transactionRepository.findByStatus("COMPLETED", pageRequest))
            .thenReturn(new PageImpl<>(List.of(testTransaction1)));
    }

    @Test
    void testGetRecentTransactions() {
        // When
        List<TransactionDTO> transactions = transactionService.getRecentTransactions(5);

        // Then
        assertNotNull(transactions);
        assertEquals(2, transactions.size());
        assertEquals("SWAP", transactions.get(0).getType());
        assertEquals("TRANSFER", transactions.get(1).getType());

        // Verify repository interaction
        verify(transactionRepository).findAll(any(PageRequest.class));
    }

    @Test
    void testGetTransactionDetails() {
        // When
        TransactionDTO transaction = transactionService.getTransactionDetails("1");

        // Then
        assertNotNull(transaction);
        assertEquals("SWAP", transaction.getType());
        assertEquals("COMPLETED", transaction.getStatus());
        assertEquals("0x123", transaction.getTransactionHash());

        // Verify repository interaction
        verify(transactionRepository).findById(1L);
    }

    @Test
    void testGetTransactionsWithFilters() {
        // When
        List<TransactionDTO> transactions = transactionService.getTransactions("SWAP", "COMPLETED", 0, 5);

        // Then
        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        assertEquals("SWAP", transactions.get(0).getType());
        assertEquals("COMPLETED", transactions.get(0).getStatus());

        // Verify repository interaction
        verify(transactionRepository).findByTypeAndStatus("SWAP", "COMPLETED", any(PageRequest.class));
    }

    @Test
    void testGetTransactionsByType() {
        // When
        List<TransactionDTO> transactions = transactionService.getTransactions("SWAP", null, 0, 5);

        // Then
        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        assertEquals("SWAP", transactions.get(0).getType());

        // Verify repository interaction
        verify(transactionRepository).findByType("SWAP", any(PageRequest.class));
    }

    @Test
    void testGetTransactionsByStatus() {
        // When
        List<TransactionDTO> transactions = transactionService.getTransactions(null, "COMPLETED", 0, 5);

        // Then
        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        assertEquals("COMPLETED", transactions.get(0).getStatus());

        // Verify repository interaction
        verify(transactionRepository).findByStatus("COMPLETED", any(PageRequest.class));
    }

    @Test
    void testGetTransactionDetailsNotFound() {
        // Given
        when(transactionRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        // When/Then
        assertThrows(RuntimeException.class, () -> transactionService.getTransactionDetails("999"));
    }
}