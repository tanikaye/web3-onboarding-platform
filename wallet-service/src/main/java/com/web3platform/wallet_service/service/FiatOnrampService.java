package com.web3platform.wallet_service.service;

import com.web3platform.wallet_service.model.FiatTransaction;
import com.web3platform.wallet_service.repository.FiatTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FiatOnrampService {

    @Autowired
    private FiatTransactionRepository transactionRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${moonpay.api.key}")
    private String moonpayApiKey;

    @Value("${moonpay.api.url}")
    private String moonpayApiUrl;

    public FiatTransaction initiateTransaction(
            Long userId,
            String walletAddress,
            String currency,
            BigDecimal amount,
            String cryptoCurrency) {
        // Create transaction record
        FiatTransaction transaction = new FiatTransaction();
        transaction.setUserId(userId);
        transaction.setWalletAddress(walletAddress);
        transaction.setCurrency(currency);
        transaction.setAmount(amount);
        transaction.setCryptoCurrency(cryptoCurrency);
        transaction.setStatus(FiatTransaction.TransactionStatus.PENDING);
        transaction = transactionRepository.save(transaction);

        try {
            // Get quote from MoonPay
            Map<String, Object> quote = getQuote(currency, amount, cryptoCurrency);
            BigDecimal cryptoAmount = new BigDecimal(quote.get("cryptoAmount").toString());
            transaction.setCryptoAmount(cryptoAmount);

            // Create MoonPay transaction
            Map<String, Object> moonpayTransaction = createMoonpayTransaction(
                walletAddress,
                currency,
                amount,
                cryptoCurrency,
                cryptoAmount
            );

            // Update transaction with provider reference
            transaction.setProviderReference(moonpayTransaction.get("id").toString());
            transaction.setCheckoutUrl(moonpayTransaction.get("checkoutUrl").toString());
            transaction = transactionRepository.save(transaction);

            // Send email with checkout link
            emailService.sendFiatTransactionInitiated(
                userId.toString(), // TODO: Get user email from user service
                transaction.getCheckoutUrl(),
                transaction
            );

            return transaction;
        } catch (Exception e) {
            transaction.setStatus(FiatTransaction.TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new RuntimeException("Failed to initiate transaction: " + e.getMessage());
        }
    }

    public void handleWebhook(String signature, String payload) {
        try {
            // Verify webhook signature
            // TODO: Implement signature verification

            Map<String, Object> webhookData = objectMapper.readValue(payload, Map.class);
            String transactionId = webhookData.get("transactionId").toString();
            String status = webhookData.get("status").toString();

            Optional<FiatTransaction> transactionOpt = transactionRepository.findByProviderReference(transactionId);
            if (transactionOpt.isPresent()) {
                FiatTransaction transaction = transactionOpt.get();
                transaction.setStatus(FiatTransaction.TransactionStatus.valueOf(status.toUpperCase()));
                transaction = transactionRepository.save(transaction);

                // Send email notification
                emailService.sendFiatTransactionStatusUpdate(
                    transaction.getUserId().toString(), // TODO: Get user email from user service
                    transaction
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process webhook: " + e.getMessage());
        }
    }

    public List<FiatTransaction> getTransactionsByUser(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    public List<FiatTransaction> getTransactionsByWallet(String walletAddress) {
        return transactionRepository.findByWalletAddress(walletAddress);
    }

    public List<FiatTransaction> getTransactionsByStatus(Long userId, FiatTransaction.TransactionStatus status) {
        return transactionRepository.findByUserIdAndStatus(userId, status);
    }

    public FiatTransaction getTransaction(Long id) {
        return transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    private Map<String, Object> getQuote(String currency, BigDecimal amount, String cryptoCurrency) {
        String url = String.format("%s/v3/currencies/%s/quote?baseCurrencyAmount=%s&baseCurrencyCode=%s",
            moonpayApiUrl, cryptoCurrency, amount, currency);
        return restTemplate.getForObject(url, Map.class);
    }

    private Map<String, Object> createMoonpayTransaction(
            String walletAddress,
            String currency,
            BigDecimal amount,
            String cryptoCurrency,
            BigDecimal cryptoAmount) {
        String url = String.format("%s/v1/transactions", moonpayApiUrl);
        Map<String, Object> request = Map.of(
            "walletAddress", walletAddress,
            "baseCurrencyAmount", amount,
            "baseCurrencyCode", currency,
            "currencyCode", cryptoCurrency,
            "cryptoAmount", cryptoAmount
        );
        return restTemplate.postForObject(url, request, Map.class);
    }
}