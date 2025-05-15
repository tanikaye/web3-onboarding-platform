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

    public enum TransactionType {
        SWAP,
        TRANSFER,
        LEND,
        BORROW,
        NFT_LIST,
        NFT_BUY,
        NFT_SELL
    }

    public enum TransactionStatus {
        PENDING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "transaction_hash", unique = true)
    private String transactionHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String amount;

    @Column(nullable = false)
    private String token;

    @Column(name = "from_address", nullable = false)
    private String fromAddress;

    @Column(name = "to_address", nullable = false)
    private String toAddress;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wallet_address", nullable = false)
    private String walletAddress;

    @Column(name = "network", nullable = false)
    private String network;

    @Column(name = "dapp_name")
    private String dappName;

    @Column(name = "contract_address")
    private String contractAddress;

    @Column(name = "value", precision = 36, scale = 18)
    private BigDecimal value;

    @Column(name = "gas_price")
    private BigDecimal gasPrice;

    @Column(name = "gas_limit")
    private Long gasLimit;

    @Column(name = "nonce")
    private Long nonce;

    @Column(name = "block_number")
    private Long blockNumber;

    @Column(name = "block_hash")
    private String blockHash;

    @Column(name = "error_message")
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}