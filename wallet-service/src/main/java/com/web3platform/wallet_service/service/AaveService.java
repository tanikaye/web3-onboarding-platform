package com.web3platform.wallet_service.service;

import com.web3platform.wallet_service.config.AaveConfig;
import com.web3platform.wallet_service.contract.AaveV3LendingPool;
import com.web3platform.wallet_service.contract.ERC20;
import com.web3platform.wallet_service.model.AaveUserAccountData;
import com.web3platform.wallet_service.model.DappTransaction;
import com.web3platform.wallet_service.repository.DappTransactionRepository;
import com.web3platform.wallet_service.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.Optional;

@Slf4j
@Service
public class AaveService {

    @Autowired
    private Web3j web3j;

    @Autowired
    private AaveConfig aaveConfig;

    @Autowired
    private DappTransactionRepository dappTransactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletEncryptionService walletEncryptionService;

    @Autowired
    private ContractGasProvider gasProvider;

    public DappTransaction supply(
            Long userId,
            String walletAddress,
            String asset,
            BigInteger amount,
            BigInteger referralCode) {
        try {
            // Create transaction record
            DappTransaction transaction = new DappTransaction();
            transaction.setUserId(userId);
            transaction.setWalletAddress(walletAddress);
            transaction.setDappName("aave");
            transaction.setTransactionType(DappTransaction.TransactionType.LEND);
            transaction.setStatus(DappTransaction.TransactionStatus.PENDING);
            transaction.setContractAddress(aaveConfig.getLendingPoolAddress());
            transaction.setFunctionName("supply");
            transaction = dappTransactionRepository.save(transaction);

            // Get wallet credentials
            Credentials credentials = getWalletCredentials(walletAddress);

            // Load contracts
            AaveV3LendingPool lendingPool = AaveV3LendingPool.load(
                    aaveConfig.getLendingPoolAddress(),
                    web3j,
                    credentials,
                    gasProvider
            );

            ERC20 token = ERC20.load(
                    asset,
                    web3j,
                    credentials,
                    gasProvider
            );

            // Approve token spending
            TransactionReceipt approveReceipt = token.approve(
                    aaveConfig.getLendingPoolAddress(),
                    amount
            ).send();

            if (!approveReceipt.isStatusOK()) {
                transaction.setStatus(DappTransaction.TransactionStatus.FAILED);
                transaction.setErrorMessage("Token approval failed");
                return dappTransactionRepository.save(transaction);
            }

            // Supply asset
            TransactionReceipt supplyReceipt = lendingPool.supply(
                    asset,
                    amount,
                    walletAddress,
                    referralCode
            ).send();

            if (!supplyReceipt.isStatusOK()) {
                transaction.setStatus(DappTransaction.TransactionStatus.FAILED);
                transaction.setErrorMessage("Supply transaction failed");
                return dappTransactionRepository.save(transaction);
            }

            // Update transaction status
            transaction.setStatus(DappTransaction.TransactionStatus.CONFIRMED);
            transaction.setTransactionHash(supplyReceipt.getTransactionHash());
            return dappTransactionRepository.save(transaction);

        } catch (Exception e) {
            log.error("Error in supply operation", e);
            throw new RuntimeException("Supply operation failed: " + e.getMessage());
        }
    }

    public DappTransaction withdraw(
            Long userId,
            String walletAddress,
            String asset,
            BigInteger amount) {
        try {
            // Create transaction record
            DappTransaction transaction = new DappTransaction();
            transaction.setUserId(userId);
            transaction.setWalletAddress(walletAddress);
            transaction.setDappName("aave");
            transaction.setTransactionType(DappTransaction.TransactionType.LEND);
            transaction.setStatus(DappTransaction.TransactionStatus.PENDING);
            transaction.setContractAddress(aaveConfig.getLendingPoolAddress());
            transaction.setFunctionName("withdraw");
            transaction = dappTransactionRepository.save(transaction);

            // Get wallet credentials
            Credentials credentials = getWalletCredentials(walletAddress);

            // Load lending pool contract
            AaveV3LendingPool lendingPool = AaveV3LendingPool.load(
                    aaveConfig.getLendingPoolAddress(),
                    web3j,
                    credentials,
                    gasProvider
            );

            // Withdraw asset
            TransactionReceipt withdrawReceipt = lendingPool.withdraw(
                    asset,
                    amount,
                    walletAddress
            ).send();

            if (!withdrawReceipt.isStatusOK()) {
                transaction.setStatus(DappTransaction.TransactionStatus.FAILED);
                transaction.setErrorMessage("Withdraw transaction failed");
                return dappTransactionRepository.save(transaction);
            }

            // Update transaction status
            transaction.setStatus(DappTransaction.TransactionStatus.CONFIRMED);
            transaction.setTransactionHash(withdrawReceipt.getTransactionHash());
            return dappTransactionRepository.save(transaction);

        } catch (Exception e) {
            log.error("Error in withdraw operation", e);
            throw new RuntimeException("Withdraw operation failed: " + e.getMessage());
        }
    }

    public DappTransaction borrow(
            Long userId,
            String walletAddress,
            String asset,
            BigInteger amount,
            BigInteger interestRateMode,
            BigInteger referralCode) {
        try {
            // Create transaction record
            DappTransaction transaction = new DappTransaction();
            transaction.setUserId(userId);
            transaction.setWalletAddress(walletAddress);
            transaction.setDappName("aave");
            transaction.setTransactionType(DappTransaction.TransactionType.BORROW);
            transaction.setStatus(DappTransaction.TransactionStatus.PENDING);
            transaction.setContractAddress(aaveConfig.getLendingPoolAddress());
            transaction.setFunctionName("borrow");
            transaction = dappTransactionRepository.save(transaction);

            // Get wallet credentials
            Credentials credentials = getWalletCredentials(walletAddress);

            // Load lending pool contract
            AaveV3LendingPool lendingPool = AaveV3LendingPool.load(
                    aaveConfig.getLendingPoolAddress(),
                    web3j,
                    credentials,
                    gasProvider
            );

            // Borrow asset
            TransactionReceipt borrowReceipt = lendingPool.borrow(
                    asset,
                    amount,
                    interestRateMode,
                    referralCode,
                    walletAddress
            ).send();

            if (!borrowReceipt.isStatusOK()) {
                transaction.setStatus(DappTransaction.TransactionStatus.FAILED);
                transaction.setErrorMessage("Borrow transaction failed");
                return dappTransactionRepository.save(transaction);
            }

            // Update transaction status
            transaction.setStatus(DappTransaction.TransactionStatus.CONFIRMED);
            transaction.setTransactionHash(borrowReceipt.getTransactionHash());
            return dappTransactionRepository.save(transaction);

        } catch (Exception e) {
            log.error("Error in borrow operation", e);
            throw new RuntimeException("Borrow operation failed: " + e.getMessage());
        }
    }

    public DappTransaction repay(
            Long userId,
            String walletAddress,
            String asset,
            BigInteger amount,
            BigInteger interestRateMode) {
        try {
            // Create transaction record
            DappTransaction transaction = new DappTransaction();
            transaction.setUserId(userId);
            transaction.setWalletAddress(walletAddress);
            transaction.setDappName("aave");
            transaction.setTransactionType(DappTransaction.TransactionType.BORROW);
            transaction.setStatus(DappTransaction.TransactionStatus.PENDING);
            transaction.setContractAddress(aaveConfig.getLendingPoolAddress());
            transaction.setFunctionName("repay");
            transaction = dappTransactionRepository.save(transaction);

            // Get wallet credentials
            Credentials credentials = getWalletCredentials(walletAddress);

            // Load contracts
            AaveV3LendingPool lendingPool = AaveV3LendingPool.load(
                    aaveConfig.getLendingPoolAddress(),
                    web3j,
                    credentials,
                    gasProvider
            );

            ERC20 token = ERC20.load(
                    asset,
                    web3j,
                    credentials,
                    gasProvider
            );

            // Approve token spending
            TransactionReceipt approveReceipt = token.approve(
                    aaveConfig.getLendingPoolAddress(),
                    amount
            ).send();

            if (!approveReceipt.isStatusOK()) {
                transaction.setStatus(DappTransaction.TransactionStatus.FAILED);
                transaction.setErrorMessage("Token approval failed");
                return dappTransactionRepository.save(transaction);
            }

            // Repay asset
            TransactionReceipt repayReceipt = lendingPool.repay(
                    asset,
                    amount,
                    interestRateMode,
                    walletAddress
            ).send();

            if (!repayReceipt.isStatusOK()) {
                transaction.setStatus(DappTransaction.TransactionStatus.FAILED);
                transaction.setErrorMessage("Repay transaction failed");
                return dappTransactionRepository.save(transaction);
            }

            // Update transaction status
            transaction.setStatus(DappTransaction.TransactionStatus.CONFIRMED);
            transaction.setTransactionHash(repayReceipt.getTransactionHash());
            return dappTransactionRepository.save(transaction);

        } catch (Exception e) {
            log.error("Error in repay operation", e);
            throw new RuntimeException("Repay operation failed: " + e.getMessage());
        }
    }

    public AaveUserAccountData getUserAccountData(String walletAddress) {
        try {
            // Get wallet credentials
            Credentials credentials = getWalletCredentials(walletAddress);

            // Load lending pool contract
            AaveV3LendingPool lendingPool = AaveV3LendingPool.load(
                    aaveConfig.getLendingPoolAddress(),
                    web3j,
                    credentials,
                    gasProvider
            );

            // Get user account data
            return lendingPool.getUserAccountData(walletAddress).send();

        } catch (Exception e) {
            log.error("Error getting user account data", e);
            throw new RuntimeException("Failed to get user account data: " + e.getMessage());
        }
    }

    private Credentials getWalletCredentials(String walletAddress) {
        return walletRepository.findByAddress(walletAddress)
                .map(wallet -> walletEncryptionService.decryptPrivateKey(wallet.getEncryptedPrivateKey()))
                .map(Credentials::create)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }
}