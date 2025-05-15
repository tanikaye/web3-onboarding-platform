package com.web3platform.wallet_service.controller;

import com.web3platform.wallet_service.model.FiatTransaction;
import com.web3platform.wallet_service.service.FiatOnrampService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fiat")
public class FiatOnrampController {

    @Autowired
    private FiatOnrampService fiatOnrampService;

    @PostMapping("/initiate")
    public ResponseEntity<?> initiateTransaction(
            @AuthenticationPrincipal Long userId,
            @RequestBody InitiateTransactionRequest request) {
        FiatTransaction transaction = fiatOnrampService.initiateTransaction(
            userId,
            request.getWalletAddress(),
            request.getCurrency(),
            request.getAmount(),
            request.getCryptoCurrency()
        );
        return ResponseEntity.ok(new InitiateTransactionResponse(transaction));
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String walletAddress,
            @RequestParam(required = false) FiatTransaction.TransactionStatus status) {
        List<FiatTransaction> transactions;
        if (walletAddress != null) {
            transactions = fiatOnrampService.getTransactionsByWallet(walletAddress);
        } else if (status != null) {
            transactions = fiatOnrampService.getTransactionsByStatus(userId, status);
        } else {
            transactions = fiatOnrampService.getTransactionsByUser(userId);
        }
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<?> getTransaction(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        FiatTransaction transaction = fiatOnrampService.getTransaction(id);
        if (!transaction.getUserId().equals(userId)) {
            return ResponseEntity.forbidden().build();
        }
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestHeader("X-MoonPay-Signature") String signature,
            @RequestBody String payload) {
        fiatOnrampService.handleWebhook(signature, payload);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class InitiateTransactionRequest {
        private String walletAddress;
        private String currency;
        private BigDecimal amount;
        private String cryptoCurrency;
    }

    @Data
    public static class InitiateTransactionResponse {
        private final FiatTransaction transaction;
    }
}