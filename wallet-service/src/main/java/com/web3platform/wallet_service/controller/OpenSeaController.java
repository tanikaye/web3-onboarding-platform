package com.web3platform.wallet_service.controller;

import com.web3platform.wallet_service.model.DappTransaction;
import com.web3platform.wallet_service.model.NFTData;
import com.web3platform.wallet_service.service.OpenSeaService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/opensea")
public class OpenSeaController {

    @Autowired
    private OpenSeaService openSeaService;

    @PostMapping("/buy")
    public ResponseEntity<DappTransaction> buyNFT(@RequestBody BuyNFTRequest request) {
        DappTransaction transaction = openSeaService.buyNFT(
                request.getUserId(),
                request.getWalletAddress(),
                request.getOrder(),
                request.getValue()
        );
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/sell")
    public ResponseEntity<DappTransaction> sellNFT(@RequestBody SellNFTRequest request) {
        DappTransaction transaction = openSeaService.sellNFT(
                request.getUserId(),
                request.getWalletAddress(),
                request.getOrder()
        );
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/cancel")
    public ResponseEntity<DappTransaction> cancelOrder(@RequestBody CancelOrderRequest request) {
        DappTransaction transaction = openSeaService.cancelOrder(
                request.getUserId(),
                request.getWalletAddress(),
                request.getOrder()
        );
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/batch/buy")
    public ResponseEntity<List<DappTransaction>> batchBuyNFTs(@RequestBody BatchBuyNFTRequest request) {
        List<DappTransaction> transactions = openSeaService.batchBuyNFTs(
                request.getUserId(),
                request.getWalletAddress(),
                request.getOrders()
        );
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/batch/sell")
    public ResponseEntity<List<DappTransaction>> batchSellNFTs(@RequestBody BatchSellNFTRequest request) {
        List<DappTransaction> transactions = openSeaService.batchSellNFTs(
                request.getUserId(),
                request.getWalletAddress(),
                request.getOrders()
        );
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/nft/{contractAddress}/{tokenId}")
    public ResponseEntity<NFTData> getNFTData(
            @PathVariable String contractAddress,
            @PathVariable String tokenId) {
        NFTData nftData = openSeaService.getNFTData(contractAddress, tokenId);
        return ResponseEntity.ok(nftData);
    }

    @Data
    public static class BuyNFTRequest {
        private Long userId;
        private String walletAddress;
        private String order;
        private BigInteger value;
        private OpenSeaService.PaymentToken paymentToken;
    }

    @Data
    public static class SellNFTRequest {
        private Long userId;
        private String walletAddress;
        private String order;
        private OpenSeaService.PaymentToken paymentToken;
    }

    @Data
    public static class CancelOrderRequest {
        private Long userId;
        private String walletAddress;
        private String order;
    }

    @Data
    public static class BatchBuyNFTRequest {
        private Long userId;
        private String walletAddress;
        private List<OpenSeaService.BatchNFTOrder> orders;
    }

    @Data
    public static class BatchSellNFTRequest {
        private Long userId;
        private String walletAddress;
        private List<OpenSeaService.BatchNFTOrder> orders;
    }
}