package com.web3platform.wallet_service.repository;

import com.web3platform.wallet_service.model.FiatTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FiatTransactionRepository extends JpaRepository<FiatTransaction, Long> {
    List<FiatTransaction> findByUserId(Long userId);
    List<FiatTransaction> findByWalletAddress(String walletAddress);
    List<FiatTransaction> findByUserIdAndStatus(Long userId, FiatTransaction.TransactionStatus status);
    Optional<FiatTransaction> findByProviderReference(String providerReference);
}