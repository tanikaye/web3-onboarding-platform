package com.web3platform.wallet_service.repository;

import com.web3platform.wallet_service.model.SocialRecoveryShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SocialRecoveryShareRepository extends JpaRepository<SocialRecoveryShare, Long> {
    List<SocialRecoveryShare> findByWalletAddress(String walletAddress);
    List<SocialRecoveryShare> findByUserId(Long userId);
    List<SocialRecoveryShare> findByTrusteeEmail(String trusteeEmail);
    Optional<SocialRecoveryShare> findByWalletAddressAndShareIndex(String walletAddress, int shareIndex);
}