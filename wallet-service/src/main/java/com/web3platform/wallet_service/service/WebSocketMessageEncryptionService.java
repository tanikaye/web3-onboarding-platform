package com.web3platform.wallet_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WebSocketMessageEncryptionService {

    @Value("${websocket.encryption.key}")
    private String encryptionKey;

    private static final String ALGORITHM = "AES";

    public String encryptMessage(String message) {
        try {
            SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Error encrypting message: {}", e.getMessage());
            throw new RuntimeException("Failed to encrypt message", e);
        }
    }

    public String decryptMessage(String encryptedMessage) {
        try {
            SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
            return new String(decryptedBytes);
        } catch (Exception e) {
            log.error("Error decrypting message: {}", e.getMessage());
            throw new RuntimeException("Failed to decrypt message", e);
        }
    }

    public boolean validateMessage(String message) {
        try {
            // Basic message validation
            if (message == null || message.trim().isEmpty()) {
                return false;
            }

            // Add more validation rules as needed
            // For example, check message format, size limits, etc.

            return true;
        } catch (Exception e) {
            log.error("Error validating message: {}", e.getMessage());
            return false;
        }
    }
}