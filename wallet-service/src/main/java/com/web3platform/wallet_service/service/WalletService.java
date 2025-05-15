package com.web3platform.wallet_service.service;

import com.web3platform.wallet_service.model.Wallet;
import com.web3platform.wallet_service.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.utils.Convert;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private Web3j web3j;

    @Autowired
    private TokenBalanceService tokenBalanceService;

    public Map<String, String> getWalletBalances() {
        Map<String, String> balances = new HashMap<>();

        try {
            // Get the user's wallet
            Wallet wallet = getCurrentUserWallet();
            if (wallet == null) {
                throw new RuntimeException("Wallet not found");
            }

            // Get ETH balance
            BigInteger ethBalance = web3j.ethGetBalance(wallet.getAddress(), DefaultBlockParameterName.LATEST)
                    .send()
                    .getBalance();
            balances.put("ETH", Convert.fromWei(ethBalance, Convert.Unit.ETHER).toString());

            // Get WETH balance
            BigDecimal wethBalance = tokenBalanceService.getTokenBalance(
                wallet.getAddress(),
                "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2" // WETH contract address
            );
            balances.put("WETH", wethBalance.toString());

            // Get USDC balance
            BigDecimal usdcBalance = tokenBalanceService.getTokenBalance(
                wallet.getAddress(),
                "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48" // USDC contract address
            );
            balances.put("USDC", usdcBalance.toString());

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch wallet balances", e);
        }

        return balances;
    }

    private Wallet getCurrentUserWallet() {
        // TODO: Implement user context to get the current user's wallet
        // For now, return the first wallet in the database
        List<Wallet> wallets = walletRepository.findAll();
        return wallets.isEmpty() ? null : wallets.get(0);
    }

    public void updateWalletBalance(String address, String token, BigDecimal balance) {
        Wallet wallet = walletRepository.findByAddress(address)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // Update the balance in the database
        // TODO: Implement balance update logic based on your requirements
    }
}