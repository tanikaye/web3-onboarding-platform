package com.web3platform.wallet_service.service;

import com.web3platform.wallet_service.config.UniswapConfig;
import com.web3platform.wallet_service.contract.ERC20;
import com.web3platform.wallet_service.contract.UniswapV3Quoter;
import com.web3platform.wallet_service.contract.UniswapV3Router;
import com.web3platform.wallet_service.model.DappTransaction;
import com.web3platform.wallet_service.repository.WalletRepository;
import com.web3platform.wallet_service.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Numeric;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UniswapService {

    @Autowired
    private UniswapConfig uniswapConfig;

    @Autowired
    private Web3j web3j;

    @Autowired
    private ContractGasProvider gasProvider;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletEncryptionService walletEncryptionService;

    @Value("${ethereum.chain.id}")
    private int chainId;

    @Value("${app.encryption.key}")
    private String encryptionKey;

    private static final BigInteger DEFAULT_FEE = BigInteger.valueOf(3000); // 0.3%
    private static final BigInteger ZERO_SQRT_PRICE_LIMIT = BigInteger.ZERO;

    public DappTransaction swapExactTokensForTokens(
            Long userId,
            String walletAddress,
            String tokenIn,
            String tokenOut,
            BigDecimal amountIn,
            BigDecimal amountOutMin,
            BigDecimal deadline) {

        try {
            // Create transaction parameters
            Map<String, Object> params = new HashMap<>();
            params.put("tokenIn", tokenIn);
            params.put("tokenOut", tokenOut);
            params.put("amountIn", amountIn);
            params.put("amountOutMin", amountOutMin);
            params.put("deadline", deadline);

            // Get quote for the swap
            BigInteger amountOut = getQuote(tokenIn, tokenOut, amountIn);

            // Create path for the swap
            String[] path = new String[]{tokenIn, tokenOut};

            // Create deadline timestamp
            BigInteger deadlineTimestamp = BigInteger.valueOf(System.currentTimeMillis() / 1000)
                .add(deadline.toBigInteger());

            // Create transaction
            DappTransaction transaction = new DappTransaction();
            transaction.setUserId(userId);
            transaction.setWalletAddress(walletAddress);
            transaction.setDappName("uniswap");
            transaction.setTransactionType(DappTransaction.TransactionType.SWAP);
            transaction.setContractAddress(uniswapConfig.getRouterAddress());
            transaction.setFunctionName("swapExactTokensForTokens");
            transaction.setFunctionParams(Arrays.toString(path));
            transaction.setValue(BigDecimal.ZERO); // No ETH value for token swaps
            transaction.setStatus(DappTransaction.TransactionStatus.PENDING);

            // Get wallet credentials
            Credentials credentials = getWalletCredentials(walletAddress);

            // Approve token spending if needed
            approveTokenSpending(credentials, tokenIn, amountIn);

            // Submit transaction
            String txHash = submitSwapTransaction(credentials, transaction, path, amountIn, amountOutMin, deadlineTimestamp);
            transaction.setTransactionHash(txHash);
            transaction.setStatus(DappTransaction.TransactionStatus.SUBMITTED);

            return transaction;
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute swap: " + e.getMessage());
        }
    }

    public DappTransaction swapExactETHForTokens(
            Long userId,
            String walletAddress,
            String tokenOut,
            BigDecimal amountOutMin,
            BigDecimal deadline) {

        try {
            // Create transaction parameters
            Map<String, Object> params = new HashMap<>();
            params.put("tokenOut", tokenOut);
            params.put("amountOutMin", amountOutMin);
            params.put("deadline", deadline);

            // Get quote for the swap
            BigInteger amountOut = getQuote(uniswapConfig.getWethAddress(), tokenOut, amountOutMin);

            // Create path for the swap
            String[] path = new String[]{uniswapConfig.getWethAddress(), tokenOut};

            // Create deadline timestamp
            BigInteger deadlineTimestamp = BigInteger.valueOf(System.currentTimeMillis() / 1000)
                .add(deadline.toBigInteger());

            // Create transaction
            DappTransaction transaction = new DappTransaction();
            transaction.setUserId(userId);
            transaction.setWalletAddress(walletAddress);
            transaction.setDappName("uniswap");
            transaction.setTransactionType(DappTransaction.TransactionType.SWAP);
            transaction.setContractAddress(uniswapConfig.getRouterAddress());
            transaction.setFunctionName("swapExactETHForTokens");
            transaction.setFunctionParams(Arrays.toString(path));
            transaction.setValue(amountOutMin); // ETH value for the swap
            transaction.setStatus(DappTransaction.TransactionStatus.PENDING);

            // Get wallet credentials
            Credentials credentials = getWalletCredentials(walletAddress);

            // Submit transaction
            String txHash = submitSwapTransaction(credentials, transaction, path, amountOutMin, amountOutMin, deadlineTimestamp);
            transaction.setTransactionHash(txHash);
            transaction.setStatus(DappTransaction.TransactionStatus.SUBMITTED);

            return transaction;
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute ETH swap: " + e.getMessage());
        }
    }

    private BigInteger getQuote(String tokenIn, String tokenOut, BigDecimal amountIn) {
        try {
            UniswapV3Quoter quoter = UniswapV3Quoter.load(
                uniswapConfig.getQuoterAddress(),
                web3j,
                Credentials.create("0x0000000000000000000000000000000000000000"), // Use zero address for read-only calls
                gasProvider
            );

            BigInteger amountInWei = amountIn.multiply(BigDecimal.valueOf(1e18)).toBigInteger();
            return quoter.quoteExactInputSingle(
                tokenIn,
                tokenOut,
                DEFAULT_FEE,
                amountInWei,
                ZERO_SQRT_PRICE_LIMIT
            ).send();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get quote: " + e.getMessage());
        }
    }

    private void approveTokenSpending(Credentials credentials, String tokenAddress, BigDecimal amount) {
        try {
            ERC20 token = ERC20.load(tokenAddress, web3j, credentials, gasProvider);
            BigInteger amountWei = amount.multiply(BigDecimal.valueOf(1e18)).toBigInteger();

            // Check current allowance
            BigInteger currentAllowance = token.allowance(credentials.getAddress(), uniswapConfig.getRouterAddress()).send();

            // Only approve if current allowance is less than required amount
            if (currentAllowance.compareTo(amountWei) < 0) {
                Boolean success = token.approve(uniswapConfig.getRouterAddress(), amountWei).send();
                if (!success) {
                    throw new RuntimeException("Token approval failed");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to approve token spending: " + e.getMessage());
        }
    }

    private String submitSwapTransaction(
            Credentials credentials,
            DappTransaction transaction,
            String[] path,
            BigDecimal amountIn,
            BigDecimal amountOutMin,
            BigInteger deadline) {
        try {
            UniswapV3Router router = UniswapV3Router.load(
                uniswapConfig.getRouterAddress(),
                web3j,
                credentials,
                gasProvider
            );

            BigInteger amountInWei = amountIn.multiply(BigDecimal.valueOf(1e18)).toBigInteger();
            BigInteger amountOutMinWei = amountOutMin.multiply(BigDecimal.valueOf(1e18)).toBigInteger();

            // For token-to-token swaps
            if (path.length == 2) {
                BigInteger amountOut = router.exactInputSingle(
                    path[0],
                    path[1],
                    DEFAULT_FEE,
                    credentials.getAddress(),
                    deadline,
                    amountInWei,
                    amountOutMinWei,
                    ZERO_SQRT_PRICE_LIMIT
                ).send();

                // Get transaction hash from the transaction
                String txHash = web3j.ethGetTransactionByHash(amountOut.toString()).send().getTransaction().get().getHash();
                return txHash;
            } else {
                // For multi-hop swaps
                byte[] encodedPath = encodePath(path);
                BigInteger amountOut = router.exactInput(
                    encodedPath,
                    credentials.getAddress(),
                    deadline,
                    amountInWei,
                    amountOutMinWei
                ).send();

                // Get transaction hash from the transaction
                String txHash = web3j.ethGetTransactionByHash(amountOut.toString()).send().getTransaction().get().getHash();
                return txHash;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to submit swap transaction: " + e.getMessage());
        }
    }

    private Credentials getWalletCredentials(String walletAddress) {
        Optional<Wallet> walletOpt = walletRepository.findByAddress(walletAddress);
        if (!walletOpt.isPresent()) {
            throw new RuntimeException("Wallet not found: " + walletAddress);
        }

        Wallet wallet = walletOpt.get();
        if (!wallet.isActive()) {
            throw new RuntimeException("Wallet is not active: " + walletAddress);
        }

        try {
            // Decrypt the private key
            String privateKey = walletEncryptionService.decryptPrivateKey(wallet.getEncryptedPrivateKey());

            // Create credentials from the decrypted private key
            return Credentials.create(privateKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve wallet credentials: " + e.getMessage());
        }
    }

    private byte[] encodePath(String[] path) {
        if (path.length < 2) {
            throw new IllegalArgumentException("Path must contain at least 2 tokens");
        }

        StringBuilder encodedPath = new StringBuilder();

        // Add first token
        encodedPath.append(Numeric.cleanHexPrefix(path[0]));

        // Add fee and subsequent tokens
        for (int i = 1; i < path.length; i++) {
            // Add fee (0.3% = 3000)
            encodedPath.append(String.format("%08x", DEFAULT_FEE.intValue()));
            // Add token address
            encodedPath.append(Numeric.cleanHexPrefix(path[i]));
        }

        return Numeric.hexStringToByteArray(encodedPath.toString());
    }
}