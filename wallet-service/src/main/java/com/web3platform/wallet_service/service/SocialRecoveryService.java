package com.web3platform.wallet_service.service;

import com.web3platform.wallet_service.model.SocialRecoveryShare;
import com.web3platform.wallet_service.repository.SocialRecoveryShareRepository;
import com.web3platform.wallet_service.util.ShamirSecretSharing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SocialRecoveryService {

    @Value("${app.encryption.key}")
    private String encryptionKey;

    @Autowired
    private SocialRecoveryShareRepository shareRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private MfaService mfaService;

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    @Transactional
    public List<SocialRecoveryShare> createRecoveryShares(String walletAddress, Long userId,
            List<String> trusteeEmails, int requiredShares, int mfaCode) {
        try {
            // Verify MFA code
            if (!mfaService.verifyCode(userId, mfaCode)) {
                throw new RuntimeException("Invalid MFA code");
            }

            // Get the private key for the wallet
            String privateKey = getPrivateKeyForWallet(walletAddress);

            // Convert private key to BigInteger
            BigInteger secret = new BigInteger(privateKey, 16);

            // Generate shares using Shamir's Secret Sharing
            List<ShamirSecretSharing.Share> shares = ShamirSecretSharing.split(
                secret, trusteeEmails.size(), requiredShares);

            List<SocialRecoveryShare> recoveryShares = new ArrayList<>();

            for (int i = 0; i < shares.size(); i++) {
                ShamirSecretSharing.Share share = shares.get(i);
                // Convert share to string format
                String shareData = String.format("%d:%s", share.getX(), share.getY().toString(16));

                // Encrypt the share
                String encryptedShare = encryptShare(shareData.getBytes());

                SocialRecoveryShare recoveryShare = new SocialRecoveryShare();
                recoveryShare.setWalletAddress(walletAddress);
                recoveryShare.setEncryptedShare(encryptedShare);
                recoveryShare.setTrusteeEmail(trusteeEmails.get(i));
                recoveryShare.setShareIndex(share.getX());
                recoveryShare.setTotalShares(trusteeEmails.size());
                recoveryShare.setUserId(userId);

                recoveryShares.add(shareRepository.save(recoveryShare));

                // Send email notification to trustee
                emailService.sendShareCreationNotification(
                    trusteeEmails.get(i),
                    walletAddress,
                    share.getX(),
                    trusteeEmails.size()
                );
            }

            return recoveryShares;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create recovery shares: " + e.getMessage());
        }
    }

    public String recoverPrivateKey(String walletAddress, List<String> shares, Long userId, int mfaCode) {
        try {
            // Verify MFA code
            if (!mfaService.verifyCode(userId, mfaCode)) {
                throw new RuntimeException("Invalid MFA code");
            }

            // Get all trustees for this wallet
            List<SocialRecoveryShare> walletShares = shareRepository.findByWalletAddress(walletAddress);

            // Notify all trustees about the recovery attempt
            for (SocialRecoveryShare share : walletShares) {
                emailService.sendShareVerificationRequest(share.getTrusteeEmail(), walletAddress);
            }

            // Decrypt shares
            List<byte[]> decryptedShares = shares.stream()
                .map(share -> {
                    try {
                        return decryptShare(share);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to decrypt share", e);
                    }
                })
                .collect(Collectors.toList());

            // Parse shares
            List<ShamirSecretSharing.Share> shamirShares = new ArrayList<>();
            for (byte[] decryptedShare : decryptedShares) {
                String[] parts = new String(decryptedShare).split(":");
                int x = Integer.parseInt(parts[0]);
                BigInteger y = new BigInteger(parts[1], 16);
                shamirShares.add(new ShamirSecretSharing.Share(x, y));
            }

            // Reconstruct the secret
            BigInteger secret = ShamirSecretSharing.recover(shamirShares);
            String recoveredKey = secret.toString(16);

            // Notify all trustees about successful recovery
            for (SocialRecoveryShare share : walletShares) {
                emailService.sendRecoveryCompleteNotification(share.getTrusteeEmail(), walletAddress);
            }

            return recoveredKey;
        } catch (Exception e) {
            throw new RuntimeException("Failed to recover private key: " + e.getMessage());
        }
    }

    public List<SocialRecoveryShare> getWalletShares(String walletAddress) {
        return shareRepository.findByWalletAddress(walletAddress);
    }

    @Transactional
    public void verifyShare(Long shareId) {
        SocialRecoveryShare share = shareRepository.findById(shareId)
            .orElseThrow(() -> new RuntimeException("Share not found"));
        share.setVerified(true);
        shareRepository.save(share);
    }

    private String encryptShare(byte[] share) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKey key = generateKey();
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        byte[] encryptedShare = cipher.doFinal(share);
        byte[] combined = new byte[iv.length + encryptedShare.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedShare, 0, combined, iv.length, encryptedShare.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    private byte[] decryptShare(String encryptedShare) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(encryptedShare);

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(decoded, 0, iv, 0, iv.length);

        byte[] encryptedData = new byte[decoded.length - GCM_IV_LENGTH];
        System.arraycopy(decoded, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKey key = generateKey();
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        return cipher.doFinal(encryptedData);
    }

    private SecretKey generateKey() {
        byte[] keyBytes = encryptionKey.getBytes();
        return new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
    }

    private String getPrivateKeyForWallet(String walletAddress) {
        // In a real implementation, this would retrieve the private key from secure storage
        // For now, we'll throw an exception
        throw new RuntimeException("Private key retrieval not implemented");
    }
}