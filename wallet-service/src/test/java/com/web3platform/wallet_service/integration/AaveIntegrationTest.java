package com.web3platform.wallet_service.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.web3platform.wallet_service.service.WebSocketService;
import java.util.Map;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;

class AaveIntegrationTest extends BaseDefiIntegrationTest {

    @Autowired
    private WebSocketService webSocketService;

    @Test
    void testLendingTransactionWithWebSocketUpdates() {
        // Create a lending transaction
        Map<String, Object> lendingTransaction = createTransactionMessage("LEND", "PENDING");
        lendingTransaction.put("asset", "WETH");
        lendingTransaction.put("amount", "10.0");
        lendingTransaction.put("apy", "3.5");
        lendingTransaction.put("interestRateMode", "STABLE");

        // Send initial transaction update
        webSocketService.sendTransactionUpdate(testUserId, lendingTransaction);
        verifyWebSocketMessage("/queue/transactions", lendingTransaction);

        // Simulate transaction confirmation
        Map<String, Object> confirmedTransaction = new HashMap<>(lendingTransaction);
        confirmedTransaction.put("status", "CONFIRMED");
        confirmedTransaction.put("transactionHash", "0x456...def");

        webSocketService.sendStatusUpdate(testUserId, "0x456...def", "CONFIRMED");
        verifyWebSocketMessage("/queue/status", confirmedTransaction);
    }

    @Test
    void testBorrowingTransactionWithWebSocketUpdates() {
        // Create a borrowing transaction
        Map<String, Object> borrowingTransaction = createTransactionMessage("BORROW", "PENDING");
        borrowingTransaction.put("asset", "USDC");
        borrowingTransaction.put("amount", "1000.0");
        borrowingTransaction.put("interestRateMode", "VARIABLE");
        borrowingTransaction.put("collateralAsset", "WETH");
        borrowingTransaction.put("collateralAmount", "1.0");

        // Send initial transaction update
        webSocketService.sendTransactionUpdate(testUserId, borrowingTransaction);
        verifyWebSocketMessage("/queue/transactions", borrowingTransaction);

        // Simulate transaction confirmation
        Map<String, Object> confirmedTransaction = new HashMap<>(borrowingTransaction);
        confirmedTransaction.put("status", "CONFIRMED");
        confirmedTransaction.put("transactionHash", "0x789...ghi");

        webSocketService.sendStatusUpdate(testUserId, "0x789...ghi", "CONFIRMED");
        verifyWebSocketMessage("/queue/status", confirmedTransaction);
    }

    @Test
    void testHealthFactorUpdate() {
        // Create a health factor update
        Map<String, Object> healthUpdate = createTransactionMessage("HEALTH_FACTOR", "UPDATED");
        healthUpdate.put("healthFactor", "1.5");
        healthUpdate.put("liquidationThreshold", "0.8");
        healthUpdate.put("currentLtv", "0.6");

        // Send health factor update
        webSocketService.sendTransactionUpdate(testUserId, healthUpdate);
        verifyWebSocketMessage("/queue/transactions", healthUpdate);
    }

    @Test
    void testLiquidationWarning() {
        // Create a liquidation warning
        Map<String, Object> warning = createTransactionMessage("LIQUIDATION_WARNING", "PENDING");
        warning.put("healthFactor", "1.1");
        warning.put("liquidationThreshold", "0.8");
        warning.put("currentLtv", "0.75");

        // Send warning notification
        webSocketService.sendErrorNotification(testUserId, "Health factor approaching liquidation threshold");
        verifyWebSocketMessage("/queue/errors", Map.of(
            "error", "Health factor approaching liquidation threshold",
            "timestamp", warning.get("timestamp")
        ));
    }

    @Test
    void testRepaymentTransaction() {
        // Create a repayment transaction
        Map<String, Object> repaymentTransaction = createTransactionMessage("REPAY", "PENDING");
        repaymentTransaction.put("asset", "USDC");
        repaymentTransaction.put("amount", "1000.0");
        repaymentTransaction.put("interestRateMode", "VARIABLE");

        // Send initial transaction update
        webSocketService.sendTransactionUpdate(testUserId, repaymentTransaction);
        verifyWebSocketMessage("/queue/transactions", repaymentTransaction);

        // Simulate transaction confirmation
        Map<String, Object> confirmedTransaction = new HashMap<>(repaymentTransaction);
        confirmedTransaction.put("status", "CONFIRMED");
        confirmedTransaction.put("transactionHash", "0xabc...def");

        webSocketService.sendStatusUpdate(testUserId, "0xabc...def", "CONFIRMED");
        verifyWebSocketMessage("/queue/status", confirmedTransaction);
    }
}