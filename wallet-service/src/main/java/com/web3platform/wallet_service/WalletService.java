package com.web3platform.wallet_service;

import com.web3platform.wallet_service.service.WalletKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import java.util.List;
import java.util.Optional;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletKeyService walletKeyService;

    @Transactional
    public Wallet createWallet(Long userId, String network, String recoveryType) {
        try {
            // Generate new wallet credentials
            Credentials credentials = WalletUtils.generateNewWallet();
            String address = credentials.getAddress();
            String privateKey = credentials.getEcKeyPair().getPrivateKey().toString(16);

            // Create new wallet entity
            Wallet wallet = new Wallet();
            wallet.setAddress(address);
            wallet.setBalance(0.0);
            wallet.setNetwork(network);
            wallet.setUserId(userId);
            wallet.setActive(true);

            // Save wallet
            wallet = walletRepository.save(wallet);

            // Encrypt and store private key
            walletKeyService.encryptAndStoreKey(privateKey, address, userId, recoveryType);

            return wallet;
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
}