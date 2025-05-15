package com.web3platform.wallet_service.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.web3platform.wallet_service.service.WebSocketService;
import java.util.Map;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;

class OpenSeaIntegrationTest extends BaseDefiIntegrationTest {

    @Autowired
    private WebSocketService webSocketService;

    @Test
    void testNFTListingWithWebSocketUpdates() {
        // Create an NFT listing transaction
        Map<String, Object> listingTransaction = createTransactionMessage("NFT_LIST", "PENDING");
        listingTransaction.put("collection", "CryptoPunks");
        listingTransaction.put("tokenId", "1234");
        listingTransaction.put("price", "10.0");
        listingTransaction.put("paymentToken", "WETH");
        listingTransaction.put("royaltyFee", "2.5");

        // Send initial transaction update
        webSocketService.sendTransactionUpdate(testUserId, listingTransaction);
        verifyWebSocketMessage("/queue/transactions", listingTransaction);

        // Simulate transaction confirmation
        Map<String, Object> confirmedTransaction = new HashMap<>(listingTransaction);
        confirmedTransaction.put("status", "CONFIRMED");
        confirmedTransaction.put("transactionHash", "0xdef...789");

        webSocketService.sendStatusUpdate(testUserId, "0xdef...789", "CONFIRMED");
        verifyWebSocketMessage("/queue/status", confirmedTransaction);
    }

    @Test
    void testNFTPurchaseWithWebSocketUpdates() {
        // Create an NFT purchase transaction
        Map<String, Object> purchaseTransaction = createTransactionMessage("NFT_BUY", "PENDING");
        purchaseTransaction.put("collection", "Bored Ape Yacht Club");
        purchaseTransaction.put("tokenId", "5678");
        purchaseTransaction.put("price", "50.0");
        purchaseTransaction.put("paymentToken", "WETH");
        purchaseTransaction.put("royaltyFee", "2.5");

        // Send initial transaction update
        webSocketService.sendTransactionUpdate(testUserId, purchaseTransaction);
        verifyWebSocketMessage("/queue/transactions", purchaseTransaction);

        // Simulate transaction confirmation
        Map<String, Object> confirmedTransaction = new HashMap<>(purchaseTransaction);
        confirmedTransaction.put("status", "CONFIRMED");
        confirmedTransaction.put("transactionHash", "0xabc...123");

        webSocketService.sendStatusUpdate(testUserId, "0xabc...123", "CONFIRMED");
        verifyWebSocketMessage("/queue/status", confirmedTransaction);
    }

    @Test
    void testOfferCreationWithWebSocketUpdates() {
        // Create an offer transaction
        Map<String, Object> offerTransaction = createTransactionMessage("NFT_OFFER", "PENDING");
        offerTransaction.put("collection", "Doodles");
        offerTransaction.put("tokenId", "9012");
        offerTransaction.put("offerAmount", "5.0");
        offerTransaction.put("paymentToken", "WETH");
        offerTransaction.put("expirationTime", "1680000000");

        // Send initial transaction update
        webSocketService.sendTransactionUpdate(testUserId, offerTransaction);
        verifyWebSocketMessage("/queue/transactions", offerTransaction);

        // Simulate transaction confirmation
        Map<String, Object> confirmedTransaction = new HashMap<>(offerTransaction);
        confirmedTransaction.put("status", "CONFIRMED");
        confirmedTransaction.put("transactionHash", "0x456...789");

        webSocketService.sendStatusUpdate(testUserId, "0x456...789", "CONFIRMED");
        verifyWebSocketMessage("/queue/status", confirmedTransaction);
    }

    @Test
    void testOfferAcceptanceWithWebSocketUpdates() {
        // Create an offer acceptance transaction
        Map<String, Object> acceptanceTransaction = createTransactionMessage("NFT_OFFER_ACCEPT", "PENDING");
        acceptanceTransaction.put("collection", "Doodles");
        acceptanceTransaction.put("tokenId", "9012");
        acceptanceTransaction.put("offerAmount", "5.0");
        acceptanceTransaction.put("paymentToken", "WETH");
        acceptanceTransaction.put("offerer", "0x123...456");

        // Send initial transaction update
        webSocketService.sendTransactionUpdate(testUserId, acceptanceTransaction);
        verifyWebSocketMessage("/queue/transactions", acceptanceTransaction);

        // Simulate transaction confirmation
        Map<String, Object> confirmedTransaction = new HashMap<>(acceptanceTransaction);
        confirmedTransaction.put("status", "CONFIRMED");
        confirmedTransaction.put("transactionHash", "0x789...012");

        webSocketService.sendStatusUpdate(testUserId, "0x789...012", "CONFIRMED");
        verifyWebSocketMessage("/queue/status", confirmedTransaction);
    }

    @Test
    void testListingCancellationWithWebSocketUpdates() {
        // Create a listing cancellation transaction
        Map<String, Object> cancellationTransaction = createTransactionMessage("NFT_LIST_CANCEL", "PENDING");
        cancellationTransaction.put("collection", "CryptoPunks");
        cancellationTransaction.put("tokenId", "1234");
        cancellationTransaction.put("listingId", "0xdef...789");

        // Send initial transaction update
        webSocketService.sendTransactionUpdate(testUserId, cancellationTransaction);
        verifyWebSocketMessage("/queue/transactions", cancellationTransaction);

        // Simulate transaction confirmation
        Map<String, Object> confirmedTransaction = new HashMap<>(cancellationTransaction);
        confirmedTransaction.put("status", "CONFIRMED");
        confirmedTransaction.put("transactionHash", "0x345...678");

        webSocketService.sendStatusUpdate(testUserId, "0x345...678", "CONFIRMED");
        verifyWebSocketMessage("/queue/status", confirmedTransaction);
    }
}