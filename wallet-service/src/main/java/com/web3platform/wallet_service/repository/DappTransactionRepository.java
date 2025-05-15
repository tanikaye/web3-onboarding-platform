package com.web3platform.wallet_service.repository;

import com.web3platform.wallet_service.model.DappTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DappTransactionRepository extends JpaRepository<DappTransaction, Long> {
    List<DappTransaction> findByUserId(Long userId);
    List<DappTransaction> findByWalletAddress(String walletAddress);
    List<DappTransaction> findByUserIdAndStatus(Long userId, DappTransaction.TransactionStatus status);
    List<DappTransaction> findByUserIdAndDappName(Long userId, String dappName);
    Optional<DappTransaction> findByTransactionHash(String transactionHash);
}