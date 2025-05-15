package com.web3platform.wallet_service.service;

import com.web3platform.wallet_service.config.NetworkConfig;
import com.web3platform.wallet_service.model.DappTransaction;
import com.web3platform.wallet_service.repository.DappTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NetworkAwareTransactionMonitor {

    @Autowired
    private NetworkConfig networkConfig;

    @Autowired
    private DappTransactionRepository dappTransactionRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Map<String, Web3j> web3jClients = new ConcurrentHashMap<>();
    private static final String WS_TRANSACTION_TOPIC = "/topic/transactions/";

    @Scheduled(fixedDelay = 10000) // Check every 10 seconds
    public void monitorPendingTransactions() {
        try {
            List<DappTransaction> pendingTransactions = dappTransactionRepository
                    .findByStatus(DappTransaction.TransactionStatus.PENDING);

            if (pendingTransactions.isEmpty()) {
                return;
            }

            log.info("Monitoring {} pending transactions", pendingTransactions.size());

            // Group transactions by network
            Map<String, List<DappTransaction>> transactionsByNetwork = pendingTransactions.stream()
                    .collect(Collectors.groupingBy(DappTransaction::getNetwork));

            // Monitor transactions for each network
            transactionsByNetwork.forEach(this::monitorNetworkTransactions);
        } catch (Exception e) {
            log.error("Error monitoring transactions", e);
        }
    }

    private void monitorNetworkTransactions(String networkId, List<DappTransaction> transactions) {
        try {
            NetworkConfig.NetworkProperties networkProps = networkConfig.getNetworkProperties(networkId);
            if (networkProps == null) {
                log.error("Network not supported: {}", networkId);
                return;
            }

            Web3j web3j = getWeb3jClient(networkId, networkProps.getRpcUrl());

            List<CompletableFuture<Void>> monitoringFutures = transactions.stream()
                    .map(transaction -> monitorTransaction(transaction, web3j, networkProps))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(monitoringFutures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            log.error("Error monitoring transactions for network {}", networkId, e);
        }
    }

    private CompletableFuture<Void> monitorTransaction(
            DappTransaction transaction,
            Web3j web3j,
            NetworkConfig.NetworkProperties networkProps) {
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
                    notifyTransactionStatus(transaction, "pending",
                            String.format("Transaction is pending confirmation on %s", networkProps.getName()));
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

                if (confirmationBlocks >= networkProps.getRequiredConfirmations()) {
                    // Transaction confirmed
                    transaction.setStatus(DappTransaction.TransactionStatus.CONFIRMED);
                    transaction.setBlockNumber(receipt.getBlockNumber().longValue());
                    transaction.setBlockHash(receipt.getBlockHash());
                    dappTransactionRepository.save(transaction);
                    notifyTransactionStatus(transaction, "confirmed",
                            String.format("Transaction confirmed on %s", networkProps.getName()));
                } else {
                    // Transaction mined but not enough confirmations
                    double estimatedTimeRemaining = (networkProps.getRequiredConfirmations() - confirmationBlocks)
                            * networkProps.getBlockTime();
                    notifyTransactionStatus(transaction, "mined",
                            String.format("Transaction mined on %s, waiting for confirmations (%d/%d, ~%.1f seconds remaining)",
                                    networkProps.getName(),
                                    confirmationBlocks,
                                    networkProps.getRequiredConfirmations(),
                                    estimatedTimeRemaining));
                }

            } catch (Exception e) {
                log.error("Error monitoring transaction {} on network {}",
                        transaction.getId(), networkProps.getName(), e);
                notifyTransactionStatus(transaction, "error",
                        "Error monitoring transaction: " + e.getMessage());
            }
        });
    }

    private Web3j getWeb3jClient(String networkId, String rpcUrl) {
        return web3jClients.computeIfAbsent(networkId, k -> Web3j.build(new HttpService(rpcUrl)));
    }

    private void notifyTransactionStatus(DappTransaction transaction, String status, String message) {
        try {
            TransactionStatusUpdate update = new TransactionStatusUpdate(
                    transaction.getId(),
                    transaction.getTransactionHash(),
                    status,
                    message,
                    System.currentTimeMillis(),
                    transaction.getNetwork()
            );

            // Send WebSocket notification
            String topic = WS_TRANSACTION_TOPIC + transaction.getUserId();
            messagingTemplate.convertAndSend(topic, update);

            // Log status update
            log.info("Transaction {} status update on {}: {} - {}",
                    transaction.getId(), transaction.getNetwork(), status, message);
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
        private final String network;
    }
}