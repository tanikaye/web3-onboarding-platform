package com.web3platform.wallet_service.repository;

import com.web3platform.wallet_service.model.EncryptedWalletKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EncryptedWalletKeyRepository extends JpaRepository<EncryptedWalletKey, Long> {
    Optional<EncryptedWalletKey> findByWalletAddress(String walletAddress);
    List<EncryptedWalletKey> findByUserId(Long userId);
    List<EncryptedWalletKey> findByUserIdAndRecoveryType(Long userId, String recoveryType);
    boolean existsByWalletAddress(String walletAddress);
}