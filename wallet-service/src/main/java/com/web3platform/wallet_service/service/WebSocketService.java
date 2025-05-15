package com.web3platform.wallet_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private WebSocketMessageEncryptionService encryptionService;

    public void sendTransactionUpdate(String userId, Map<String, Object> transaction) {
        try {
            String destination = "/user/" + userId + "/queue/transactions";
            String encryptedMessage = encryptionService.encryptMessage(convertToJson(transaction));
            messagingTemplate.convertAndSendToUser(userId, destination, encryptedMessage);
            log.info("Sent encrypted transaction update to user: {}", userId);
        } catch (Exception e) {
            log.error("Error sending transaction update: {}", e.getMessage());
            throw new RuntimeException("Failed to send transaction update", e);
        }
    }

    public void sendStatusUpdate(String userId, String transactionId, String status) {
        try {
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("transactionId", transactionId);
            statusUpdate.put("status", status);
            statusUpdate.put("timestamp", System.currentTimeMillis());

            String destination = "/user/" + userId + "/queue/status";
            String encryptedMessage = encryptionService.encryptMessage(convertToJson(statusUpdate));
            messagingTemplate.convertAndSendToUser(userId, destination, encryptedMessage);
            log.info("Sent encrypted status update to user: {}", userId);
        } catch (Exception e) {
            log.error("Error sending status update: {}", e.getMessage());
            throw new RuntimeException("Failed to send status update", e);
        }
    }

    public void sendErrorNotification(String userId, String errorMessage) {
        try {
            Map<String, Object> error = new HashMap<>();
            error.put("error", errorMessage);
            error.put("timestamp", System.currentTimeMillis());

            String destination = "/user/" + userId + "/queue/errors";
            String encryptedMessage = encryptionService.encryptMessage(convertToJson(error));
            messagingTemplate.convertAndSendToUser(userId, destination, encryptedMessage);
            log.info("Sent encrypted error notification to user: {}", userId);
        } catch (Exception e) {
            log.error("Error sending error notification: {}", e.getMessage());
            throw new RuntimeException("Failed to send error notification", e);
        }
    }

    private String convertToJson(Map<String, Object> data) {
        // In a real implementation, use a proper JSON library like Jackson
        // This is a simplified version for demonstration
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
        json.setLength(json.length() - 1); // Remove trailing comma
        json.append("}");
        return json.toString();
    }
}