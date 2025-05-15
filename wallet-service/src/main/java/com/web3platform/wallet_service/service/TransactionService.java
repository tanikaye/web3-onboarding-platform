package com.web3platform.wallet_service.service;

import com.web3platform.wallet_service.model.DappTransaction;
import com.web3platform.wallet_service.repository.DappTransactionRepository;
import com.web3platform.wallet_service.dto.TransactionDTO;
import org.springframework.beans.factory.annotation.Autowired;
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
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public TransactionDTO getTransactionDetails(String transactionId) {
        DappTransaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return convertToDTO(transaction);
    }

    public List<TransactionDTO> getTransactions(String type, String status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("timestamp").descending());

        if (type != null && status != null) {
            return transactionRepository.findByTypeAndStatus(type, status, pageRequest)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } else if (type != null) {
            return transactionRepository.findByType(type, pageRequest)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } else if (status != null) {
            return transactionRepository.findByStatus(status, pageRequest)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } else {
            return transactionRepository.findAll(pageRequest)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        }
    }

    private TransactionDTO convertToDTO(DappTransaction transaction) {
        return TransactionDTO.builder()
            .id(transaction.getId())
            .type(transaction.getType())
            .status(transaction.getStatus())
            .transactionHash(transaction.getTransactionHash())
            .timestamp(transaction.getTimestamp())
            .amount(transaction.getAmount())
            .token(transaction.getToken())
            .fromAddress(transaction.getFromAddress())
            .toAddress(transaction.getToAddress())
            .build();
    }
}