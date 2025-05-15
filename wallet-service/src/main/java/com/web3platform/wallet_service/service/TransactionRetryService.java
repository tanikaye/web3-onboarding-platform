package com.web3platform.wallet_service.service;

import com.web3platform.wallet_service.model.DappTransaction;
import com.web3platform.wallet_service.repository.DappTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransactionRetryService {

    @Autowired
    private Web3j web3j;

    @Autowired
    private DappTransactionRepository dappTransactionRepository;

    @Autowired
    private ContractGasProvider gasProvider;

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY = 30000; // 30 seconds
    private static final double RETRY_DELAY_MULTIPLIER = 2.0;

    @Scheduled(fixedDelay = 60000) // Check every minute
    public void processRetryableTransactions() {
        try {
            List<DappTransaction> failedTransactions = dappTransactionRepository
                    .findByStatus(DappTransaction.TransactionStatus.FAILED);

            if (failedTransactions.isEmpty()) {
                return;
            }

            log.info("Processing {} failed transactions for retry", failedTransactions.size());

            List<CompletableFuture<Void>> retryFutures = failedTransactions.stream()
                    .filter(this::shouldRetry)
                    .map(this::retryTransaction)
                    .collect(Collectors.toList());

            CompletableFuture.allOf(retryFutures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            log.error("Error processing retryable transactions", e);
        }
    }

    private boolean shouldRetry(DappTransaction transaction) {
        // Check if transaction has been retried less than MAX_RETRIES times
        int retryCount = transaction.getRetryCount() != null ? transaction.getRetryCount() : 0;
        return retryCount < MAX_RETRIES;
    }

    private CompletableFuture<Void> retryTransaction(DappTransaction transaction) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Calculate retry delay with exponential backoff
                int retryCount = transaction.getRetryCount() != null ? transaction.getRetryCount() : 0;
                long retryDelay = (long) (INITIAL_RETRY_DELAY * Math.pow(RETRY_DELAY_MULTIPLIER, retryCount));

                // Wait for the calculated delay
                Thread.sleep(retryDelay);

                // Update gas price for retry
                BigInteger currentGasPrice = web3j.ethGasPrice().send().getGasPrice();
                BigInteger newGasPrice = currentGasPrice.multiply(BigInteger.valueOf(120)).divide(BigInteger.valueOf(100)); // 20% increase

                // Prepare transaction for retry
                transaction.setStatus(DappTransaction.TransactionStatus.PENDING);
                transaction.setRetryCount(retryCount + 1);
                transaction.setGasPrice(newGasPrice);
                transaction = dappTransactionRepository.save(transaction);

                log.info("Retrying transaction {} (attempt {}/{})",
                        transaction.getId(), retryCount + 1, MAX_RETRIES);

                // Resubmit transaction with new gas price
                TransactionReceipt receipt = resubmitTransaction(transaction);

                if (receipt != null && receipt.isStatusOK()) {
                    transaction.setStatus(DappTransaction.TransactionStatus.CONFIRMED);
                    transaction.setTransactionHash(receipt.getTransactionHash());
                    transaction.setBlockNumber(receipt.getBlockNumber().longValue());
                    transaction.setBlockHash(receipt.getBlockHash());
                } else {
                    transaction.setStatus(DappTransaction.TransactionStatus.FAILED);
                    transaction.setErrorMessage("Retry failed");
                }

                dappTransactionRepository.save(transaction);

            } catch (Exception e) {
                log.error("Error retrying transaction {}", transaction.getId(), e);
                transaction.setStatus(DappTransaction.TransactionStatus.FAILED);
                transaction.setErrorMessage("Retry error: " + e.getMessage());
                dappTransactionRepository.save(transaction);
            }
        });
    }

    private TransactionReceipt resubmitTransaction(DappTransaction transaction) {
        try {
            // Implement transaction resubmission logic based on transaction type
            switch (transaction.getTransactionType()) {
                case NFT_PURCHASE:
                case NFT_SALE:
                    return resubmitNFTTransaction(transaction);
                case TOKEN_TRANSFER:
                    return resubmitTokenTransfer(transaction);
                default:
                    log.warn("Unsupported transaction type for retry: {}", transaction.getTransactionType());
                    return null;
            }
        } catch (Exception e) {
            log.error("Error resubmitting transaction", e);
            return null;
        }
    }

    private TransactionReceipt resubmitNFTTransaction(DappTransaction transaction) {
        // Implement NFT transaction resubmission logic
        // This would interact with the OpenSea contract
        return null; // Placeholder
    }

    private TransactionReceipt resubmitTokenTransfer(DappTransaction transaction) {
        // Implement token transfer resubmission logic
        // This would interact with the ERC20 contract
        return null; // Placeholder
    }
}