package com.web3platform.wallet_service.service;

import com.web3platform.wallet_service.model.DappTransaction;
import com.web3platform.wallet_service.repository.DappTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransactionMonitorService {

    @Autowired
    private Web3j web3j;

    @Autowired
    private DappTransactionRepository dappTransactionRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private static final String WS_TRANSACTION_TOPIC = "/topic/transactions/";
    private static final int MAX_CONFIRMATIONS = 12; // ~3 minutes on Ethereum

    @Scheduled(fixedDelay = 10000) // Check every 10 seconds
    public void monitorPendingTransactions() {
        try {
            List<DappTransaction> pendingTransactions = dappTransactionRepository
                    .findByStatus(DappTransaction.TransactionStatus.PENDING);

            if (pendingTransactions.isEmpty()) {
                return;
            }

            log.info("Monitoring {} pending transactions", pendingTransactions.size());

            List<CompletableFuture<Void>> monitoringFutures = pendingTransactions.stream()
                    .map(this::monitorTransaction)
                    .collect(Collectors.toList());

            CompletableFuture.allOf(monitoringFutures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            log.error("Error monitoring transactions", e);
        }
    }

    private CompletableFuture<Void> monitorTransaction(DappTransaction transaction) {
        return CompletableFuture.runAsync(() -> {
            try {
                String txHash = transaction.getTransactionHash();
                if (txHash == null) {
                    log.warn("Transaction {} has no hash", transaction.getId());
                    return;
                }

                TransactionReceipt receipt = web3j.ethGetTransactionReceipt(txHash)
                        .send()
                        .getTransactionReceipt()
                        .orElse(null);

                if (receipt == null) {
                    // Transaction not yet mined
                    notifyTransactionStatus(transaction, "pending", "Transaction is pending confirmation");
                    return;
                }

                if (!receipt.isStatusOK()) {
                    // Transaction failed
                    transaction.setStatus(DappTransaction.TransactionStatus.FAILED);
                    transaction.setErrorMessage("Transaction reverted");
                    dappTransactionRepository.save(transaction);
                    notifyTransactionStatus(transaction, "failed", "Transaction failed");
                    return;
                }

                // Get current block number
                long currentBlock = web3j.ethBlockNumber().send().getBlockNumber().longValue();
                long confirmationBlocks = currentBlock - receipt.getBlockNumber().longValue();

                if (confirmationBlocks >= MAX_CONFIRMATIONS) {
                    // Transaction confirmed
                    transaction.setStatus(DappTransaction.TransactionStatus.CONFIRMED);
                    transaction.setBlockNumber(receipt.getBlockNumber().longValue());
                    transaction.setBlockHash(receipt.getBlockHash());
                    dappTransactionRepository.save(transaction);
                    notifyTransactionStatus(transaction, "confirmed", "Transaction confirmed");
                } else {
                    // Transaction mined but not enough confirmations
                    notifyTransactionStatus(transaction, "mined",
                            String.format("Transaction mined, waiting for confirmations (%d/%d)",
                                    confirmationBlocks, MAX_CONFIRMATIONS));
                }

            } catch (Exception e) {
                log.error("Error monitoring transaction {}", transaction.getId(), e);
                notifyTransactionStatus(transaction, "error", "Error monitoring transaction: " + e.getMessage());
            }
        });
    }

    private void notifyTransactionStatus(DappTransaction transaction, String status, String message) {
        try {
            TransactionStatusUpdate update = new TransactionStatusUpdate(
                    transaction.getId(),
                    transaction.getTransactionHash(),
                    status,
                    message,
                    System.currentTimeMillis()
            );

            // Send WebSocket notification
            String topic = WS_TRANSACTION_TOPIC + transaction.getUserId();
            messagingTemplate.convertAndSend(topic, update);

            // Log status update
            log.info("Transaction {} status update: {} - {}",
                    transaction.getId(), status, message);
        } catch (Exception e) {
            log.error("Error sending transaction status update", e);
        }
    }

    @lombok.Data
    public static class TransactionStatusUpdate {
        private final Long transactionId;
        private final String transactionHash;
        private final String status;
        private final String message;
        private final long timestamp;
    }
}