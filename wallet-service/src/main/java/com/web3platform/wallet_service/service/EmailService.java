package com.web3platform.wallet_service.service;

import com.web3platform.wallet_service.model.FiatTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendShareCreationNotification(String trusteeEmail, String walletAddress, int shareIndex, int totalShares) {
        String subject = "You've been assigned as a recovery trustee";
        String content = String.format(
            "You have been assigned as a recovery trustee for wallet %s.\n\n" +
            "Your share index is %d out of %d total shares.\n\n" +
            "Please keep this information secure and do not share it with anyone.\n\n" +
            "You will be notified when your share is needed for wallet recovery.",
            walletAddress, shareIndex, totalShares
        );
        sendEmail(trusteeEmail, subject, content);
    }

    public void sendShareVerificationRequest(String trusteeEmail, String walletAddress) {
        String subject = "Action Required: Verify Your Recovery Share";
        String content = String.format(
            "A recovery attempt has been initiated for wallet %s.\n\n" +
            "Please verify your share by clicking the verification link in your dashboard.\n\n" +
            "If you did not expect this request, please contact support immediately.",
            walletAddress
        );
        sendEmail(trusteeEmail, subject, content);
    }

    public void sendRecoveryCompleteNotification(String trusteeEmail, String walletAddress) {
        String subject = "Wallet Recovery Completed";
        String content = String.format(
            "The recovery process for wallet %s has been completed successfully.\n\n" +
            "Thank you for your assistance in the recovery process.\n\n" +
            "If you did not participate in this recovery, please contact support immediately.",
            walletAddress
        );
        sendEmail(trusteeEmail, subject, content);
    }

    public void sendMfaSetupInstructions(String email, String secretKey, String qrCodeBase64) {
        String subject = "Set Up Two-Factor Authentication";
        String content = String.format(
            "To set up two-factor authentication for your account, please follow these steps:\n\n" +
            "1. Install an authenticator app like Google Authenticator or Authy\n" +
            "2. Scan the QR code below or manually enter this secret key: %s\n" +
            "3. Enter the 6-digit code from your authenticator app to complete setup\n\n" +
            "If you did not request this setup, please contact support immediately.\n\n" +
            "QR Code:\n%s",
            secretKey,
            qrCodeBase64
        );
        sendEmail(email, subject, content);
    }

    public void sendFiatTransactionInitiated(String email, String checkoutUrl, FiatTransaction transaction) {
        String subject = "Complete Your Crypto Purchase";
        String content = String.format(
            "Your fiat-to-crypto transaction has been initiated.\n\n" +
            "Details:\n" +
            "Amount: %s %s\n" +
            "Crypto: %s %s\n" +
            "Wallet: %s\n\n" +
            "Please complete your purchase by clicking the link below:\n%s\n\n" +
            "This link will expire in 30 minutes.",
            transaction.getAmount().setScale(2, RoundingMode.HALF_UP),
            transaction.getCurrency(),
            transaction.getCryptoAmount().setScale(8, RoundingMode.HALF_UP),
            transaction.getCryptoCurrency(),
            transaction.getWalletAddress(),
            checkoutUrl
        );
        sendEmail(email, subject, content);
    }

    public void sendFiatTransactionStatusUpdate(String email, FiatTransaction transaction) {
        String subject = "Transaction Status Update";
        String content = String.format(
            "Your fiat-to-crypto transaction status has been updated.\n\n" +
            "Transaction ID: %s\n" +
            "Status: %s\n" +
            "Amount: %s %s\n" +
            "Crypto: %s %s\n" +
            "Wallet: %s\n\n" +
            "If you have any questions, please contact support.",
            transaction.getId(),
            transaction.getStatus(),
            transaction.getAmount().setScale(2, RoundingMode.HALF_UP),
            transaction.getCurrency(),
            transaction.getCryptoAmount().setScale(8, RoundingMode.HALF_UP),
            transaction.getCryptoCurrency(),
            transaction.getWalletAddress()
        );
        sendEmail(email, subject, content);
    }

    private void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}