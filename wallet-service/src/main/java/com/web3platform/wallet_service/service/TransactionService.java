package com.web3platform.wallet_service.service;

import com.web3platform.wallet_service.model.DappTransaction;
import com.web3platform.wallet_service.repository.DappTransactionRepository;
import com.web3platform.wallet_service.dto.TransactionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private DappTransactionRepository transactionRepository;

    public List<TransactionDTO> getRecentTransactions(int limit) {
        return transactionRepository.findAll(
            PageRequest.of(0, limit, Sort.by("timestamp").descending())
        ).getContent().stream()
            .map(TransactionDTO::fromEntity)
            .collect(Collectors.toList());
    }

    public TransactionDTO getTransactionDetails(Long transactionId) {
        DappTransaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return TransactionDTO.fromEntity(transaction);
    }

    public Page<TransactionDTO> getTransactionsByUser(Long userId, int page, int size) {
        return transactionRepository.findByUserId(userId, PageRequest.of(page, size, Sort.by("timestamp").descending()))
            .map(TransactionDTO::fromEntity);
    }

    public Page<TransactionDTO> getTransactionsByWallet(String walletAddress, int page, int size) {
        return transactionRepository.findByWalletAddress(walletAddress, PageRequest.of(page, size, Sort.by("timestamp").descending()))
            .map(TransactionDTO::fromEntity);
    }

    public Page<TransactionDTO> getTransactionsByStatus(DappTransaction.TransactionStatus status, int page, int size) {
        return transactionRepository.findByStatus(status, PageRequest.of(page, size, Sort.by("timestamp").descending()))
            .map(TransactionDTO::fromEntity);
    }

    public Page<TransactionDTO> getTransactionsByType(DappTransaction.TransactionType type, int page, int size) {
        return transactionRepository.findByType(type, PageRequest.of(page, size, Sort.by("timestamp").descending()))
            .map(TransactionDTO::fromEntity);
    }

    public Page<TransactionDTO> getTransactionsByTypeAndStatus(
            DappTransaction.TransactionType type,
            DappTransaction.TransactionStatus status,
            int page,
            int size) {
        return transactionRepository.findByTypeAndStatus(type, status, PageRequest.of(page, size, Sort.by("timestamp").descending()))
            .map(TransactionDTO::fromEntity);
    }

    public List<TransactionDTO> getTransactions(String type, String status, int page, int size) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'getTransactions'");
    }
}