package com.web3platform.wallet_service.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.web3platform.wallet_service.service.WebSocketService;
import java.util.Map;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;

class UniswapIntegrationTest extends BaseDefiIntegrationTest {

    @Autowired
    private WebSocketService webSocketService;

    @Test
    void testSwapTransactionWithWebSocketUpdates() {
        // Create a swap transaction
        Map<String, Object> swapTransaction = createTransactionMessage("SWAP", "PENDING");
        swapTransaction.put("tokenIn", "WETH");
        swapTransaction.put("tokenOut", "USDC");
        swapTransaction.put("amountIn", "1.0");
        swapTransaction.put("amountOut", "1800.0");
        swapTransaction.put("slippage", "0.5");

        // Send initial transaction update
        webSocketService.sendTransactionUpdate(testUserId, swapTransaction);
        verifyWebSocketMessage("/queue/transactions", swapTransaction);

        // Simulate transaction confirmation
        Map<String, Object> confirmedTransaction = new HashMap<>(swapTransaction);
        confirmedTransaction.put("status", "CONFIRMED");
        confirmedTransaction.put("transactionHash", "0x123...abc");

        webSocketService.sendStatusUpdate(testUserId, "0x123...abc", "CONFIRMED");
        verifyWebSocketMessage("/queue/status", confirmedTransaction);
    }

    @Test
    void testSwapTransactionWithDisconnection() {
        // Create a swap transaction
        Map<String, Object> swapTransaction = createTransactionMessage("SWAP", "PENDING");
        swapTransaction.put("tokenIn", "WETH");
        swapTransaction.put("tokenOut", "USDC");
        swapTransaction.put("amountIn", "1.0");
        swapTransaction.put("amountOut", "1800.0");

        // Simulate disconnection
        simulateDisconnection();

        // Send transaction update (should be queued)
        webSocketService.sendTransactionUpdate(testUserId, swapTransaction);
        verifyMessageQueued();

        // Simulate reconnection
        simulateReconnection();

        // Verify queued message was delivered
        verifyMessageDelivered();
    }

    @Test
    void testSwapTransactionWithError() {
        // Create a swap transaction
        Map<String, Object> swapTransaction = createTransactionMessage("SWAP", "PENDING");
        swapTransaction.put("tokenIn", "WETH");
        swapTransaction.put("tokenOut", "USDC");
        swapTransaction.put("amountIn", "1.0");
        swapTransaction.put("amountOut", "1800.0");

        // Send initial transaction update
        webSocketService.sendTransactionUpdate(testUserId, swapTransaction);
        verifyWebSocketMessage("/queue/transactions", swapTransaction);

        // Simulate transaction failure
        webSocketService.sendErrorNotification(testUserId, "Insufficient liquidity");
        verifyWebSocketMessage("/queue/errors", Map.of(
            "error", "Insufficient liquidity",
            "timestamp", swapTransaction.get("timestamp")
        ));
    }

    @Test
    void testMultipleSwapTransactions() {
        // Create multiple swap transactions
        for (int i = 0; i < 3; i++) {
            Map<String, Object> swapTransaction = createTransactionMessage("SWAP", "PENDING");
            swapTransaction.put("tokenIn", "WETH");
            swapTransaction.put("tokenOut", "USDC");
            swapTransaction.put("amountIn", String.valueOf(i + 1));
            swapTransaction.put("amountOut", String.valueOf((i + 1) * 1800));

            // Send transaction update
            webSocketService.sendTransactionUpdate(testUserId, swapTransaction);
            verifyWebSocketMessage("/queue/transactions", swapTransaction);

            // Simulate transaction confirmation
            Map<String, Object> confirmedTransaction = new HashMap<>(swapTransaction);
            confirmedTransaction.put("status", "CONFIRMED");
            confirmedTransaction.put("transactionHash", "0x" + i + "...abc");

            webSocketService.sendStatusUpdate(testUserId, "0x" + i + "...abc", "CONFIRMED");
            verifyWebSocketMessage("/queue/status", confirmedTransaction);
        }
    }
}