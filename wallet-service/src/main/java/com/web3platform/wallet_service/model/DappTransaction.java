package com.web3platform.wallet_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DappTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wallet_address", nullable = false)
    private String walletAddress;

    @Column(name = "dapp_name", nullable = false)
    private String dappName; // e.g., "uniswap", "aave", "opensea"

    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(name = "contract_address", nullable = false)
    private String contractAddress;

    @Column(name = "function_name", nullable = false)
    private String functionName;

    @Column(name = "function_params", columnDefinition = "TEXT")
    private String functionParams; // JSON string of function parameters

    @Column(name = "value", precision = 19, scale = 18)
    private BigDecimal value; // ETH value for the transaction

    @Column(name = "gas_limit")
    private Long gasLimit;

    @Column(name = "gas_price", precision = 19, scale = 9)
    private BigDecimal gasPrice;

    @Column(name = "nonce")
    private Long nonce;

    @Column(name = "transaction_hash")
    private String transactionHash;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum TransactionType {
        SWAP,
        LEND,
        BORROW,
        NFT_PURCHASE,
        NFT_SALE,
        NFT_TRANSFER,
        CUSTOM
    }

    public enum TransactionStatus {
        PENDING,
        SUBMITTED,
        CONFIRMED,
        FAILED,
        REVERTED
    }
}