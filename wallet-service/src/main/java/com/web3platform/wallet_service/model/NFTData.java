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
public class NFTData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_id", nullable = false)
    private String tokenId;

    @Column(name = "contract_address", nullable = false)
    private String contractAddress;

    @Column(name = "token_standard", nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenStandard tokenStandard;

    @Column(name = "owner_address", nullable = false)
    private String ownerAddress;

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "metadata_url")
    private String metadataUrl;

    @Column(name = "last_sale_price", precision = 19, scale = 18)
    private BigDecimal lastSalePrice;

    @Column(name = "last_sale_currency")
    private String lastSaleCurrency;

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

    public enum TokenStandard {
        ERC721,
        ERC1155
    }
}