package com.web3platform.wallet_service.repository;

import com.web3platform.wallet_service.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByAddress(String address);
    Optional<Wallet> findByUserId(Long userId);
}