package com.web3platform.wallet_service.controller;

import com.web3platform.wallet_service.model.DappTransaction;
import com.web3platform.wallet_service.service.TransactionAnalyticsService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionAnalyticsController {

    @Autowired
    private TransactionAnalyticsService transactionAnalyticsService;

    @GetMapping("/history/{userId}")
    public ResponseEntity<TransactionAnalyticsService.TransactionHistory> getTransactionHistory(
            @PathVariable Long userId,
            @RequestParam String network,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(transactionAnalyticsService.getTransactionHistory(
                userId,
                network,
                PageRequest.of(page, size)
        ));
    }

    @GetMapping("/analytics/{userId}")
    public ResponseEntity<TransactionAnalyticsService.TransactionAnalytics> getTransactionAnalytics(
            @PathVariable Long userId,
            @RequestParam String network,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        return ResponseEntity.ok(transactionAnalyticsService.getAnalytics(
                userId,
                network,
                startDate,
                endDate
        ));
    }

    @PostMapping("/search/{userId}")
    public ResponseEntity<TransactionAnalyticsService.TransactionSearchResult> searchTransactions(
            @PathVariable Long userId,
            @RequestBody TransactionSearchRequest request) {

        return ResponseEntity.ok(transactionAnalyticsService.searchTransactions(
                userId,
                request.getCriteria(),
                PageRequest.of(
                        request.getPage(),
                        request.getSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        ));
    }

    @GetMapping("/search/{userId}/query")
    public ResponseEntity<TransactionAnalyticsService.TransactionSearchResult> searchTransactionsByQuery(
            @PathVariable Long userId,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(transactionAnalyticsService.searchTransactionsByQuery(
                userId,
                query,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        ));
    }

    @GetMapping("/metadata/{userId}")
    public ResponseEntity<TransactionAnalyticsService.UserTransactionMetadata> getUserTransactionMetadata(
            @PathVariable Long userId) {

        return ResponseEntity.ok(transactionAnalyticsService.getUserTransactionMetadata(userId));
    }

    @Data
    public static class TransactionSearchRequest {
        private TransactionAnalyticsService.TransactionSearchCriteria criteria;
        private int page = 0;
        private int size = 20;
    }

    @Data
    public static class DateRange {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime startDate;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime endDate;
    }
}