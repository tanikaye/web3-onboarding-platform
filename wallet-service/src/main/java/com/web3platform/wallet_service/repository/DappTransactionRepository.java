package com.web3platform.wallet_service.repository;

import com.web3platform.wallet_service.model.DappTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DappTransactionRepository extends JpaRepository<DappTransaction, Long> {
    List<DappTransaction> findByUserId(Long userId);
    List<DappTransaction> findByWalletAddress(String walletAddress);
    List<DappTransaction> findByStatus(DappTransaction.TransactionStatus status);
    List<DappTransaction> findByUserIdAndStatus(Long userId, DappTransaction.TransactionStatus status);
    List<DappTransaction> findByWalletAddressAndStatus(String walletAddress, DappTransaction.TransactionStatus status);
    List<DappTransaction> findByUserIdAndDappName(Long userId, String dappName);
    Optional<DappTransaction> findByTransactionHash(String transactionHash);
    Page<DappTransaction> findByUserIdAndNetworkOrderByCreatedAtDesc(Long userId, String network, Pageable pageable);

    List<DappTransaction> findByUserIdAndNetworkAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId, String network, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT t FROM DappTransaction t WHERE " +
            "(:userId IS NULL OR t.userId = :userId) AND " +
            "(:network IS NULL OR t.network = :network) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:type IS NULL OR t.transactionType = :type) AND " +
            "(:dappName IS NULL OR t.dappName = :dappName) AND " +
            "(:startDate IS NULL OR t.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR t.createdAt <= :endDate) AND " +
            "(:minValue IS NULL OR t.value >= :minValue) AND " +
            "(:maxValue IS NULL OR t.value <= :maxValue) AND " +
            "(:walletAddress IS NULL OR t.walletAddress = :walletAddress)")
    Page<DappTransaction> searchTransactions(
            @Param("userId") Long userId,
            @Param("network") String network,
            @Param("status") DappTransaction.TransactionStatus status,
            @Param("type") DappTransaction.TransactionType type,
            @Param("dappName") String dappName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minValue") BigDecimal minValue,
            @Param("maxValue") BigDecimal maxValue,
            @Param("walletAddress") String walletAddress,
            Pageable pageable);

    @Query("SELECT DISTINCT t.network FROM DappTransaction t WHERE t.userId = :userId")
    List<String> findUserNetworks(@Param("userId") Long userId);

    @Query("SELECT DISTINCT t.dappName FROM DappTransaction t WHERE t.userId = :userId")
    List<String> findUserDapps(@Param("userId") Long userId);

    @Query("SELECT t FROM DappTransaction t WHERE " +
            "t.userId = :userId AND " +
            "LOWER(t.transactionHash) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(t.walletAddress) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(t.contractAddress) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<DappTransaction> searchTransactionsByQuery(
            @Param("userId") Long userId,
            @Param("query") String query,
            Pageable pageable);

    Page<DappTransaction> findByTypeAndStatus(String type, String status, Pageable pageable);
    Page<DappTransaction> findByType(String type, Pageable pageable);
    Page<DappTransaction> findByStatus(String status, Pageable pageable);
}