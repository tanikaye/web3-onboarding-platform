package com.web3platform.wallet_service.service;

import com.web3platform.wallet_service.model.EncryptedWalletKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
public class WalletKeyService {

    @Value("${app.encryption.key}")
    private String encryptionKey;

    @Autowired
    private EncryptedWalletKeyRepository keyRepository;

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    @Transactional
    public EncryptedWalletKey encryptAndStoreKey(String privateKey, String walletAddress, Long userId, String recoveryType) {
        try {
            // Generate a random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // Encrypt the private key
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKey key = generateKey();
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            byte[] encryptedKey = cipher.doFinal(privateKey.getBytes());
            byte[] combined = new byte[iv.length + encryptedKey.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedKey, 0, combined, iv.length, encryptedKey.length);

            // Store the encrypted key
            EncryptedWalletKey encryptedWalletKey = new EncryptedWalletKey();
            encryptedWalletKey.setWalletAddress(walletAddress);
            encryptedWalletKey.setEncryptedPrivateKey(Base64.getEncoder().encodeToString(combined));
            encryptedWalletKey.setRecoveryType(recoveryType);
            encryptedWalletKey.setUserId(userId);

            return keyRepository.save(encryptedWalletKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt wallet key: " + e.getMessage());
        }
    }

    public String decryptKey(String encryptedKey) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedKey);

            // Extract IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, iv.length);

            // Extract encrypted data
            byte[] encryptedData = new byte[decoded.length - GCM_IV_LENGTH];
            System.arraycopy(decoded, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);

            // Decrypt
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKey key = generateKey();
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            byte[] decrypted = cipher.doFinal(encryptedData);
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt wallet key: " + e.getMessage());
        }
    }

    public List<EncryptedWalletKey> getUserWalletKeys(Long userId) {
        return keyRepository.findByUserId(userId);
    }

    public EncryptedWalletKey getWalletKey(String walletAddress) {
        return keyRepository.findByWalletAddress(walletAddress)
                .orElseThrow(() -> new RuntimeException("Wallet key not found"));
    }

    private SecretKey generateKey() {
        // In production, use a proper key derivation function
        byte[] keyBytes = encryptionKey.getBytes();
        return new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
    }
}