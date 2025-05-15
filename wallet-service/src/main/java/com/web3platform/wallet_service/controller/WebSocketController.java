package com.web3platform.wallet_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import com.web3platform.wallet_service.service.WebSocketMessageEncryptionService;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Slf4j
@Controller
public class WebSocketController {

    @Autowired
    private WebSocketMessageEncryptionService encryptionService;

    @MessageMapping("/transaction.update")
    public void handleTransactionUpdate(
            @Payload String encryptedMessage,
            SimpMessageHeaderAccessor headerAccessor) {
        try {
            String userId = headerAccessor.getUser().getName();
            if (userId == null) {
                log.error("Unauthorized access attempt");
                return;
            }

            String decryptedMessage = encryptionService.decryptMessage(encryptedMessage);
            if (!encryptionService.validateMessage(decryptedMessage)) {
                log.error("Invalid message format received from user: {}", userId);
                return;
            }

            // Process the decrypted message
            log.info("Received transaction update from user: {}", userId);
            // Add your transaction processing logic here

        } catch (Exception e) {
            log.error("Error processing transaction update: {}", e.getMessage());
        }
    }

    @MessageMapping("/status.update")
    public void handleStatusUpdate(
            @Payload String encryptedMessage,
            SimpMessageHeaderAccessor headerAccessor) {
        try {
            String userId = headerAccessor.getUser().getName();
            if (userId == null) {
                log.error("Unauthorized access attempt");
                return;
            }

            String decryptedMessage = encryptionService.decryptMessage(encryptedMessage);
            if (!encryptionService.validateMessage(decryptedMessage)) {
                log.error("Invalid message format received from user: {}", userId);
                return;
            }

            // Process the decrypted message
            log.info("Received status update from user: {}", userId);
            // Add your status update processing logic here

        } catch (Exception e) {
            log.error("Error processing status update: {}", e.getMessage());
        }
    }

    @MessageMapping("/error.notification")
    public void handleErrorNotification(
            @Payload String encryptedMessage,
            SimpMessageHeaderAccessor headerAccessor) {
        try {
            String userId = headerAccessor.getUser().getName();
            if (userId == null) {
                log.error("Unauthorized access attempt");
                return;
            }

            String decryptedMessage = encryptionService.decryptMessage(encryptedMessage);
            if (!encryptionService.validateMessage(decryptedMessage)) {
                log.error("Invalid message format received from user: {}", userId);
                return;
            }

            // Process the decrypted message
            log.info("Received error notification from user: {}", userId);
            // Add your error notification processing logic here

        } catch (Exception e) {
            log.error("Error processing error notification: {}", e.getMessage());
        }
    }
}