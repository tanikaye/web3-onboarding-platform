package com.web3platform.wallet_service.controller;

import com.web3platform.wallet_service.model.DappTransaction;
import com.web3platform.wallet_service.service.DappGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dapp")
public class DappGatewayController {

    @Autowired
    private DappGatewayService dappGatewayService;

    @PostMapping("/transaction")
    public ResponseEntity<?> submitTransaction(
            @AuthenticationPrincipal Long userId,
            @RequestBody SubmitTransactionRequest request) {
        DappTransaction transaction = dappGatewayService.submitTransaction(
            userId,
            request.getWalletAddress(),
            request.getDappName(),
            request.getTransactionType(),
            request.getContractAddress(),
            request.getFunctionName(),
            request.getFunctionParams(),
            request.getValue()
        );
        return ResponseEntity.ok(new SubmitTransactionResponse(transaction));
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String walletAddress,
            @RequestParam(required = false) String dappName,
            @RequestParam(required = false) DappTransaction.TransactionStatus status) {
        List<DappTransaction> transactions;
        if (walletAddress != null) {
            transactions = dappGatewayService.getTransactionsByWallet(walletAddress);
        } else if (dappName != null) {
            transactions = dappGatewayService.getTransactionsByDapp(userId, dappName);
        } else if (status != null) {
            transactions = dappGatewayService.getTransactionsByStatus(userId, status);
        } else {
            transactions = dappGatewayService.getTransactionsByUser(userId);
        }
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transaction/{hash}")
    public ResponseEntity<?> getTransactionStatus(
            @AuthenticationPrincipal Long userId,
            @PathVariable String hash) {
        DappTransaction transaction = dappGatewayService.getTransactionStatus(hash);
        if (!transaction.getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(transaction);
    }

    @Data
    public static class SubmitTransactionRequest {
        private String walletAddress;
        private String dappName;
        private DappTransaction.TransactionType transactionType;
        private String contractAddress;
        private String functionName;
        private Map<String, Object> functionParams;
        private BigDecimal value;
    }

    @Data
    public static class SubmitTransactionResponse {
        private final DappTransaction transaction;
    }
}