package com.web3platform.wallet_service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByAddress(String address);
    List<Wallet> findByUserId(Long userId);
    List<Wallet> findByUserIdAndIsActive(Long userId, boolean isActive);
    boolean existsByAddress(String address);
}