package com.web3platform.wallet_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WebSocketSessionManager {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private WebSocketMessageEncryptionService encryptionService;

    private final Map<String, Queue<Map<String, Object>>> messageQueues = new ConcurrentHashMap<>();
    private final Map<String, Long> lastActivityTime = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static final long SESSION_TIMEOUT = 300000; // 5 minutes
    private static final long CLEANUP_INTERVAL = 60000; // 1 minute

    public WebSocketSessionManager() {
        // Start cleanup task
        scheduler.scheduleAtFixedRate(this::cleanupInactiveSessions,
            CLEANUP_INTERVAL, CLEANUP_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public void handleSessionConnect(String userId, String sessionId) {
        log.info("User {} connected with session {}", userId, sessionId);
        messageQueues.putIfAbsent(userId, new ConcurrentLinkedQueue<>());
        updateLastActivityTime(userId);
    }

    public void handleSessionDisconnect(String userId, String sessionId) {
        log.info("User {} disconnected from session {}", userId, sessionId);
        // Keep the message queue for reconnection
        updateLastActivityTime(userId);
    }

    public void queueMessage(String userId, Map<String, Object> message) {
        Queue<Map<String, Object>> queue = messageQueues.get(userId);
        if (queue != null) {
            queue.offer(message);
            log.debug("Message queued for user {}: {}", userId, message);
        }
    }

    public void processQueuedMessages(String userId) {
        Queue<Map<String, Object>> queue = messageQueues.get(userId);
        if (queue != null) {
            while (!queue.isEmpty()) {
                Map<String, Object> message = queue.poll();
                try {
                    String destination = "/user/" + userId + "/queue/messages";
                    String encryptedMessage = encryptionService.encryptMessage(convertToJson(message));
                    messagingTemplate.convertAndSendToUser(userId, destination, encryptedMessage);
                    log.debug("Processed queued message for user {}: {}", userId, message);
                } catch (Exception e) {
                    log.error("Error processing queued message for user {}: {}", userId, e.getMessage());
                    // Re-queue the message if processing fails
                    queue.offer(message);
                    break;
                }
            }
        }
    }

    private void updateLastActivityTime(String userId) {
        lastActivityTime.put(userId, System.currentTimeMillis());
    }

    private void cleanupInactiveSessions() {
        long currentTime = System.currentTimeMillis();
        lastActivityTime.forEach((userId, lastActivity) -> {
            if (currentTime - lastActivity > SESSION_TIMEOUT) {
                log.info("Cleaning up inactive session for user {}", userId);
                messageQueues.remove(userId);
                lastActivityTime.remove(userId);
            }
        });
    }

    private String convertToJson(Map<String, Object> data) {
        StringBuilder json = new StringBuilder("{");
        data.forEach((key, value) -> {
            json.append("\"").append(key).append("\":");
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else {
                json.append(value);
            }
            json.append(",");
        });
        json.setLength(json.length() - 1);
        json.append("}");
        return json.toString();
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}