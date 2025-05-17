package com.web3platform.wallet_service.service;

import com.web3platform.wallet_service.contract.ERC20;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Convert;
import org.web3j.protocol.core.DefaultBlockParameterName;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class TokenBalanceService {

    @Autowired
    private Web3j web3j;

    @Autowired
    private ContractGasProvider gasProvider;

    public CompletableFuture<Boolean> hasSufficientBalance(
            String walletAddress,
            OpenSeaService.PaymentToken paymentToken,
            BigInteger requiredAmount) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                BigInteger balance;
                if (paymentToken == OpenSeaService.PaymentToken.ETH) {
                    EthGetBalance ethBalance = web3j.ethGetBalance(walletAddress, null).send();
                    balance = ethBalance.getBalance();
                } else {
                    ERC20 token = ERC20.load(
                            paymentToken.getAddress(),
                            web3j,
                            Credentials.create("0x0"), // Read-only credentials
                            gasProvider
                    );
                    balance = token.balanceOf(walletAddress).send();
                }

                // Add buffer for gas fees (20% for ETH, 5% for tokens)
                BigInteger buffer = paymentToken == OpenSeaService.PaymentToken.ETH
                        ? requiredAmount.multiply(BigInteger.valueOf(120)).divide(BigInteger.valueOf(100))
                        : requiredAmount.multiply(BigInteger.valueOf(105)).divide(BigInteger.valueOf(100));

                return balance.compareTo(buffer) >= 0;
            } catch (Exception e) {
                log.error("Error checking token balance", e);
                return false;
            }
        });
    }

    public BigInteger estimateGasForBatchOperation(
            String walletAddress,
            OpenSeaService.PaymentToken paymentToken,
            int numberOfOrders) {
        try {
            // Base gas cost for a single order
            BigInteger baseGas = BigInteger.valueOf(21000); // 21k gas for basic ETH transfer

            // Additional gas for token transfers
            if (paymentToken != OpenSeaService.PaymentToken.ETH) {
                baseGas = baseGas.add(BigInteger.valueOf(65000)); // ~65k gas for ERC20 transfer
            }

            // Gas for OpenSea Seaport operations
            BigInteger seaportGas = BigInteger.valueOf(150000); // ~150k gas for Seaport operations

            // Calculate total gas for all orders
            BigInteger totalGas = baseGas.add(seaportGas).multiply(BigInteger.valueOf(numberOfOrders));

            // Add buffer for contract interactions and potential retries
            return totalGas.multiply(BigInteger.valueOf(120)).divide(BigInteger.valueOf(100));
        } catch (Exception e) {
            log.error("Error estimating gas for batch operation", e);
            throw new RuntimeException("Failed to estimate gas: " + e.getMessage());
        }
    }

    public BigDecimal getGasPriceInGwei() {
        try {
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
            return Convert.fromWei(new BigDecimal(gasPrice), Convert.Unit.GWEI);
        } catch (Exception e) {
            log.error("Error getting gas price", e);
            throw new RuntimeException("Failed to get gas price: " + e.getMessage());
        }
    }

    public BigDecimal getBalance(String walletAddress, String tokenAddress) {
        try {
            // For native token (ETH), use eth_getBalance
            if (tokenAddress.equalsIgnoreCase("0x0000000000000000000000000000000000000000")) {
                BigInteger balance = web3j.ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST)
                        .send()
                        .getBalance();
                return Convert.fromWei(balance.toString(), Convert.Unit.ETHER);
            }

            // For ERC20 tokens, use the balanceOf function
            String data = "0x70a08231000000000000000000000000" + walletAddress.substring(2);
            String response = web3j.ethCall(
                    org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                            null,
                            tokenAddress,
                            data),
                    DefaultBlockParameterName.LATEST)
                    .send()
                    .getValue();

            BigInteger balance = new BigInteger(response.substring(2), 16);
            return Convert.fromWei(balance.toString(), Convert.Unit.ETHER);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get token balance: " + e.getMessage());
        }
    }
}