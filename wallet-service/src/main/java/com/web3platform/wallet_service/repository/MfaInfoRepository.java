package com.web3platform.wallet_service.repository;

import com.web3platform.wallet_service.model.MfaInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MfaInfoRepository extends JpaRepository<MfaInfo, Long> {
    Optional<MfaInfo> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}