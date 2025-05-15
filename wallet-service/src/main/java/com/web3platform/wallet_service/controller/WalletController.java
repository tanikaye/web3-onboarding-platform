package com.web3platform.wallet_service.controller;

import com.web3platform.wallet_service.service.WalletService;
import com.web3platform.wallet_service.service.TransactionService;
import com.web3platform.wallet_service.dto.WalletBalanceDTO;
import com.web3platform.wallet_service.dto.TransactionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/balance")
    public ResponseEntity<Map<String, String>> getWalletBalance() {
        try {
            Map<String, String> balances = walletService.getWalletBalances();
            return ResponseEntity.ok(balances);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/transactions/recent")
    public ResponseEntity<List<TransactionDTO>> getRecentTransactions(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<TransactionDTO> transactions = transactionService.getRecentTransactions(limit);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<TransactionDTO> getTransactionDetails(
            @PathVariable String transactionId) {
        try {
            TransactionDTO transaction = transactionService.getTransactionDetails(transactionId);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDTO>> getTransactions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<TransactionDTO> transactions = transactionService.getTransactions(type, status, page, size);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}