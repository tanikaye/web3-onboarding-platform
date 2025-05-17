package com.web3platform.wallet_service.service;

import com.web3platform.wallet_service.model.Wallet;
import com.web3platform.wallet_service.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.utils.Convert;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private Web3j web3j;

    @Autowired
    private TokenBalanceService tokenBalanceService;

    @Autowired
    private WalletEncryptionService walletEncryptionService;

    @Transactional
    public Wallet createWallet(Long userId, String network, String recoveryType) {
        try {
            // Generate new wallet credentials
            String password = "temp-password"; // In production, use a secure password
            String walletFile = WalletUtils.generateNewWalletFile(password, null);
            Credentials credentials = WalletUtils.loadCredentials(password, walletFile);
            String address = credentials.getAddress();
            String privateKey = credentials.getEcKeyPair().getPrivateKey().toString(16);

            // Create new wallet entity
            Wallet wallet = new Wallet();
            wallet.setAddress(address);
            wallet.setBalance(0.0);
            wallet.setNetwork(network);
            wallet.setUserId(userId);
            wallet.setActive(true);

            // Encrypt private key
            String encryptedPrivateKey = walletEncryptionService.encryptPrivateKey(privateKey);
            wallet.setEncryptedPrivateKey(encryptedPrivateKey);

            // Save wallet
            return walletRepository.save(wallet);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create wallet: " + e.getMessage());
        }
    }

    public Optional<Wallet> getWalletByAddress(String address) {
        return walletRepository.findByAddress(address);
    }

    public List<Wallet> getUserWallets(Long userId) {
        return walletRepository.findByUserId(userId);
    }

    public List<Wallet> getActiveUserWallets(Long userId) {
        return walletRepository.findByUserIdAndIsActive(userId, true);
    }

    @Transactional
    public Wallet updateWalletBalance(String address, double newBalance) {
        Wallet wallet = walletRepository.findByAddress(address)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setBalance(newBalance);
        return walletRepository.save(wallet);
    }

    @Transactional
    public void deactivateWallet(String address) {
        Wallet wallet = walletRepository.findByAddress(address)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setActive(false);
        walletRepository.save(wallet);
    }

    public Map<String, String> getWalletBalances(String address) {
        Map<String, String> balances = new HashMap<>();

        try {
            // Get the wallet
            Wallet wallet = getWalletByAddress(address)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

            // Get ETH balance
            BigInteger ethBalance = web3j.ethGetBalance(wallet.getAddress(), DefaultBlockParameterName.LATEST)
                    .send()
                    .getBalance();
            balances.put("ETH", Convert.fromWei(ethBalance.toString(), Convert.Unit.ETHER).toString());

            // Get WETH balance
            BigDecimal wethBalance = tokenBalanceService.getBalance(
                wallet.getAddress(),
                "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2" // WETH contract address
            );
            balances.put("WETH", wethBalance.toString());

            // Get USDC balance
            BigDecimal usdcBalance = tokenBalanceService.getBalance(
                wallet.getAddress(),
                "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48" // USDC contract address
            );
            balances.put("USDC", usdcBalance.toString());

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch wallet balances", e);
        }

        return balances;
    }

    public Optional<Wallet> getWalletByUserIdAndNetwork(Long userId, String network) {
        return walletRepository.findByUserIdAndNetwork(userId, network);
    }

    public List<Wallet> getWalletsByNetwork(String network) {
        return walletRepository.findByNetwork(network);
    }

    public List<Wallet> getActiveWallets() {
        return walletRepository.findByIsActive(true);
    }
}