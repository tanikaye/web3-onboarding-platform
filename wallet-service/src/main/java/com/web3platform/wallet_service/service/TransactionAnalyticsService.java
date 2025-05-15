package com.web3platform.wallet_service.service;

import com.web3platform.wallet_service.model.DappTransaction;
import com.web3platform.wallet_service.repository.DappTransactionRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransactionAnalyticsService {

    @Autowired
    private DappTransactionRepository dappTransactionRepository;

    public TransactionHistory getTransactionHistory(Long userId, String network, Pageable pageable) {
        Page<DappTransaction> transactions = dappTransactionRepository
                .findByUserIdAndNetworkOrderByCreatedAtDesc(userId, network, pageable);

        return new TransactionHistory(
                transactions.getContent(),
                transactions.getTotalElements(),
                transactions.getTotalPages(),
                calculateAnalytics(transactions.getContent())
        );
    }

    public TransactionAnalytics getAnalytics(Long userId, String network, LocalDateTime startDate, LocalDateTime endDate) {
        List<DappTransaction> transactions = dappTransactionRepository
                .findByUserIdAndNetworkAndCreatedAtBetweenOrderByCreatedAtDesc(
                        userId, network, startDate, endDate);

        return calculateAnalytics(transactions);
    }

    private TransactionAnalytics calculateAnalytics(List<DappTransaction> transactions) {
        if (transactions.isEmpty()) {
            return new TransactionAnalytics();
        }

        TransactionAnalytics analytics = new TransactionAnalytics();

        // Calculate basic metrics
        analytics.setTotalTransactions(transactions.size());
        analytics.setSuccessfulTransactions(
                (int) transactions.stream()
                        .filter(tx -> tx.getStatus() == DappTransaction.TransactionStatus.CONFIRMED)
                        .count()
        );
        analytics.setFailedTransactions(
                (int) transactions.stream()
                        .filter(tx -> tx.getStatus() == DappTransaction.TransactionStatus.FAILED)
                        .count()
        );

        // Calculate gas metrics
        BigDecimal totalGasSpent = transactions.stream()
                .filter(tx -> tx.getGasPrice() != null && tx.getGasLimit() != null)
                .map(tx -> tx.getGasPrice().multiply(BigDecimal.valueOf(tx.getGasLimit())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        analytics.setTotalGasSpent(totalGasSpent);
        analytics.setAverageGasPrice(
                calculateAverageGasPrice(transactions)
        );

        // Calculate confirmation time metrics
        List<Duration> confirmationTimes = transactions.stream()
                .filter(tx -> tx.getStatus() == DappTransaction.TransactionStatus.CONFIRMED)
                .filter(tx -> tx.getCreatedAt() != null && tx.getUpdatedAt() != null)
                .map(tx -> Duration.between(tx.getCreatedAt(), tx.getUpdatedAt()))
                .collect(Collectors.toList());

        if (!confirmationTimes.isEmpty()) {
            analytics.setAverageConfirmationTime(
                    confirmationTimes.stream()
                            .mapToLong(Duration::toSeconds)
                            .average()
                            .orElse(0.0)
            );
            analytics.setMinConfirmationTime(
                    confirmationTimes.stream()
                            .mapToLong(Duration::toSeconds)
                            .min()
                            .orElse(0)
            );
            analytics.setMaxConfirmationTime(
                    confirmationTimes.stream()
                            .mapToLong(Duration::toSeconds)
                            .max()
                            .orElse(0)
            );
        }

        // Calculate transaction type distribution
        Map<DappTransaction.TransactionType, Long> typeDistribution = transactions.stream()
                .collect(Collectors.groupingBy(
                        DappTransaction::getTransactionType,
                        Collectors.counting()
                ));
        analytics.setTransactionTypeDistribution(typeDistribution);

        // Calculate network fee metrics
        BigDecimal totalNetworkFees = transactions.stream()
                .filter(tx -> tx.getGasPrice() != null && tx.getGasLimit() != null)
                .map(tx -> tx.getGasPrice()
                        .multiply(BigDecimal.valueOf(tx.getGasLimit()))
                        .divide(BigDecimal.valueOf(1e18), 18, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        analytics.setTotalNetworkFees(totalNetworkFees);
        analytics.setAverageNetworkFee(
                totalNetworkFees.divide(BigDecimal.valueOf(transactions.size()), 18, RoundingMode.HALF_UP)
        );

        // Calculate success rate
        analytics.setSuccessRate(
                BigDecimal.valueOf(analytics.getSuccessfulTransactions())
                        .divide(BigDecimal.valueOf(transactions.size()), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
        );

        return analytics;
    }

    private BigDecimal calculateAverageGasPrice(List<DappTransaction> transactions) {
        return transactions.stream()
                .filter(tx -> tx.getGasPrice() != null)
                .map(DappTransaction::getGasPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(transactions.size()), 9, RoundingMode.HALF_UP);
    }

    public TransactionSearchResult searchTransactions(
            Long userId,
            TransactionSearchCriteria criteria,
            Pageable pageable) {

        Page<DappTransaction> transactions = dappTransactionRepository.searchTransactions(
                userId,
                criteria.getNetwork(),
                criteria.getStatus(),
                criteria.getType(),
                criteria.getDappName(),
                criteria.getStartDate(),
                criteria.getEndDate(),
                criteria.getMinValue(),
                criteria.getMaxValue(),
                criteria.getWalletAddress(),
                pageable
        );

        return new TransactionSearchResult(
                transactions.getContent(),
                transactions.getTotalElements(),
                transactions.getTotalPages(),
                calculateAnalytics(transactions.getContent())
        );
    }

    public TransactionSearchResult searchTransactionsByQuery(
            Long userId,
            String query,
            Pageable pageable) {

        Page<DappTransaction> transactions = dappTransactionRepository.searchTransactionsByQuery(
                userId,
                query,
                pageable
        );

        return new TransactionSearchResult(
                transactions.getContent(),
                transactions.getTotalElements(),
                transactions.getTotalPages(),
                calculateAnalytics(transactions.getContent())
        );
    }

    public UserTransactionMetadata getUserTransactionMetadata(Long userId) {
        return new UserTransactionMetadata(
                dappTransactionRepository.findUserNetworks(userId),
                dappTransactionRepository.findUserDapps(userId)
        );
    }

    @Data
    public static class TransactionHistory {
        private final List<DappTransaction> transactions;
        private final long totalElements;
        private final int totalPages;
        private final TransactionAnalytics analytics;
    }

    @Data
    public static class TransactionAnalytics {
        private int totalTransactions;
        private int successfulTransactions;
        private int failedTransactions;
        private BigDecimal totalGasSpent;
        private BigDecimal averageGasPrice;
        private double averageConfirmationTime; // in seconds
        private long minConfirmationTime; // in seconds
        private long maxConfirmationTime; // in seconds
        private Map<DappTransaction.TransactionType, Long> transactionTypeDistribution;
        private BigDecimal totalNetworkFees;
        private BigDecimal averageNetworkFee;
        private BigDecimal successRate; // percentage
    }

    @Data
    public static class TransactionSearchResult {
        private final List<DappTransaction> transactions;
        private final long totalElements;
        private final int totalPages;
        private final TransactionAnalytics analytics;
    }

    @Data
    public static class TransactionSearchCriteria {
        private String network;
        private DappTransaction.TransactionStatus status;
        private DappTransaction.TransactionType type;
        private String dappName;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private BigDecimal minValue;
        private BigDecimal maxValue;
        private String walletAddress;
    }

    @Data
    public static class UserTransactionMetadata {
        private final List<String> networks;
        private final List<String> dapps;
    }
}