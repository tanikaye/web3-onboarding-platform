package com.web3platform.wallet_service.repository;

import com.web3platform.wallet_service.model.Wallet;
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
    Optional<Wallet> findByUserIdAndNetwork(Long userId, String network);
    List<Wallet> findByNetwork(String network);
    List<Wallet> findByIsActive(boolean isActive);
}