package com.web3platform.wallet_service.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import com.web3platform.wallet_service.model.MfaInfo;
import com.web3platform.wallet_service.repository.MfaInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class MfaService {

    @Autowired
    private MfaInfoRepository mfaInfoRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.mfa.issuer}")
    private String issuer;

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    @Transactional
    public String setupMfa(Long userId, String email) {
        // Check if MFA is already set up
        if (mfaInfoRepository.existsByUserId(userId)) {
            throw new RuntimeException("MFA is already set up for this user");
        }

        // Generate new secret key
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        String secretKey = key.getKey();

        // Create MFA info
        MfaInfo mfaInfo = new MfaInfo();
        mfaInfo.setUserId(userId);
        mfaInfo.setSecretKey(secretKey);
        mfaInfo.setEnabled(false);
        mfaInfoRepository.save(mfaInfo);

        // Generate QR code
        String qrCodeUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL(issuer, email, key);
        String qrCodeBase64 = generateQRCodeBase64(qrCodeUrl);

        // Send setup instructions via email
        emailService.sendMfaSetupInstructions(email, secretKey, qrCodeBase64);

        return qrCodeBase64;
    }

    @Transactional
    public boolean verifyAndEnableMfa(Long userId, int code) {
        MfaInfo mfaInfo = mfaInfoRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("MFA not set up for this user"));

        if (mfaInfo.isEnabled()) {
            throw new RuntimeException("MFA is already enabled");
        }

        boolean isValid = gAuth.authorize(mfaInfo.getSecretKey(), code);
        if (isValid) {
            mfaInfo.setEnabled(true);
            mfaInfoRepository.save(mfaInfo);
        }

        return isValid;
    }

    public boolean verifyCode(Long userId, int code) {
        MfaInfo mfaInfo = mfaInfoRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("MFA not set up for this user"));

        if (!mfaInfo.isEnabled()) {
            throw new RuntimeException("MFA is not enabled");
        }

        return gAuth.authorize(mfaInfo.getSecretKey(), code);
    }

    @Transactional
    public void disableMfa(Long userId, int code) {
        MfaInfo mfaInfo = mfaInfoRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("MFA not set up for this user"));

        if (!mfaInfo.isEnabled()) {
            throw new RuntimeException("MFA is not enabled");
        }

        if (!gAuth.authorize(mfaInfo.getSecretKey(), code)) {
            throw new RuntimeException("Invalid verification code");
        }

        mfaInfo.setEnabled(false);
        mfaInfoRepository.save(mfaInfo);
    }

    private String generateQRCodeBase64(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}