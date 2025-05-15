package com.web3platform.wallet_service.websocket;

import com.web3platform.wallet_service.model.DappTransaction;
import com.web3platform.wallet_service.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TransactionService transactionService;

    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private CompletableFuture<DappTransaction> transactionFuture;

    @BeforeEach
    void setUp() throws Exception {
        // Set up WebSocket client
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        // Connect to WebSocket server
        stompSession = stompClient.connect(
            "ws://localhost:" + port + "/ws",
            new StompSessionHandlerAdapter() {},
            new Object[0]
        ).get(1, TimeUnit.SECONDS);

        // Set up transaction future
        transactionFuture = new CompletableFuture<>();
    }

    @Test
    void testTransactionUpdate() throws Exception {
        // Subscribe to transaction updates
        stompSession.subscribe("/topic/transactions", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return DappTransaction.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                transactionFuture.complete((DappTransaction) payload);
            }
        });

        // Create and send a test transaction
        DappTransaction testTransaction = new DappTransaction();
        testTransaction.setId(1L);
        testTransaction.setType("SWAP");
        testTransaction.setStatus("PENDING");
        testTransaction.setTransactionHash("0x123");
        testTransaction.setTimestamp(java.time.LocalDateTime.now());
        testTransaction.setAmount("1.0");
        testTransaction.setToken("ETH");
        testTransaction.setFromAddress("0x123");
        testTransaction.setToAddress("0x456");

        // Send transaction update
        stompSession.send("/app/transactions", testTransaction);

        // Wait for and verify the transaction update
        DappTransaction receivedTransaction = transactionFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(receivedTransaction);
        assertEquals(testTransaction.getType(), receivedTransaction.getType());
        assertEquals(testTransaction.getStatus(), receivedTransaction.getStatus());
        assertEquals(testTransaction.getTransactionHash(), receivedTransaction.getTransactionHash());
    }

    @Test
    void testTransactionStatusUpdate() throws Exception {
        // Subscribe to transaction updates
        stompSession.subscribe("/topic/transactions", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return DappTransaction.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                transactionFuture.complete((DappTransaction) payload);
            }
        });

        // Create and send a test transaction
        DappTransaction testTransaction = new DappTransaction();
        testTransaction.setId(2L);
        testTransaction.setType("TRANSFER");
        testTransaction.setStatus("PENDING");
        testTransaction.setTransactionHash("0x456");
        testTransaction.setTimestamp(java.time.LocalDateTime.now());
        testTransaction.setAmount("0.5");
        testTransaction.setToken("USDC");
        testTransaction.setFromAddress("0x456");
        testTransaction.setToAddress("0x789");

        // Send initial transaction
        stompSession.send("/app/transactions", testTransaction);

        // Update transaction status
        testTransaction.setStatus("COMPLETED");
        stompSession.send("/app/transactions", testTransaction);

        // Wait for and verify the status update
        DappTransaction receivedTransaction = transactionFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(receivedTransaction);
        assertEquals("COMPLETED", receivedTransaction.getStatus());
    }

    @Test
    void testMultipleTransactionUpdates() throws Exception {
        // Set up multiple transaction futures
        List<CompletableFuture<DappTransaction>> futures = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            futures.add(new CompletableFuture<>());
        }

        // Subscribe to transaction updates
        stompSession.subscribe("/topic/transactions", new StompFrameHandler() {
            private int count = 0;

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return DappTransaction.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                if (count < futures.size()) {
                    futures.get(count).complete((DappTransaction) payload);
                    count++;
                }
            }
        });

        // Send multiple transactions
        for (int i = 0; i < 3; i++) {
            DappTransaction transaction = new DappTransaction();
            transaction.setId((long) i);
            transaction.setType("SWAP");
            transaction.setStatus("PENDING");
            transaction.setTransactionHash("0x" + i);
            transaction.setTimestamp(java.time.LocalDateTime.now());
            transaction.setAmount(String.valueOf(i + 1));
            transaction.setToken("ETH");
            transaction.setFromAddress("0x" + i);
            transaction.setToAddress("0x" + (i + 1));

            stompSession.send("/app/transactions", transaction);
        }

        // Verify all transactions were received
        for (CompletableFuture<DappTransaction> future : futures) {
            DappTransaction transaction = future.get(5, TimeUnit.SECONDS);
            assertNotNull(transaction);
            assertEquals("SWAP", transaction.getType());
            assertEquals("PENDING", transaction.getStatus());
        }
    }
}