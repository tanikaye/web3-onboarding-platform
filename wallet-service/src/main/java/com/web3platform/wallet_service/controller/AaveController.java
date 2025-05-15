package com.web3platform.wallet_service.controller;

import com.web3platform.wallet_service.model.AaveUserAccountData;
import com.web3platform.wallet_service.model.DappTransaction;
import com.web3platform.wallet_service.service.AaveService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/v1/aave")
public class AaveController {

    @Autowired
    private AaveService aaveService;

    @PostMapping("/supply")
    public ResponseEntity<DappTransaction> supply(@RequestBody SupplyRequest request) {
        DappTransaction transaction = aaveService.supply(
                request.getUserId(),
                request.getWalletAddress(),
                request.getAsset(),
                request.getAmount(),
                request.getReferralCode()
        );
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<DappTransaction> withdraw(@RequestBody WithdrawRequest request) {
        DappTransaction transaction = aaveService.withdraw(
                request.getUserId(),
                request.getWalletAddress(),
                request.getAsset(),
                request.getAmount()
        );
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/borrow")
    public ResponseEntity<DappTransaction> borrow(@RequestBody BorrowRequest request) {
        DappTransaction transaction = aaveService.borrow(
                request.getUserId(),
                request.getWalletAddress(),
                request.getAsset(),
                request.getAmount(),
                request.getInterestRateMode(),
                request.getReferralCode()
        );
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/repay")
    public ResponseEntity<DappTransaction> repay(@RequestBody RepayRequest request) {
        DappTransaction transaction = aaveService.repay(
                request.getUserId(),
                request.getWalletAddress(),
                request.getAsset(),
                request.getAmount(),
                request.getInterestRateMode()
        );
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/account/{walletAddress}")
    public ResponseEntity<AaveUserAccountData> getUserAccountData(@PathVariable String walletAddress) {
        AaveUserAccountData accountData = aaveService.getUserAccountData(walletAddress);
        return ResponseEntity.ok(accountData);
    }

    @Data
    public static class SupplyRequest {
        private Long userId;
        private String walletAddress;
        private String asset;
        private BigInteger amount;
        private BigInteger referralCode;
    }

    @Data
    public static class WithdrawRequest {
        private Long userId;
        private String walletAddress;
        private String asset;
        private BigInteger amount;
    }

    @Data
    public static class BorrowRequest {
        private Long userId;
        private String walletAddress;
        private String asset;
        private BigInteger amount;
        private BigInteger interestRateMode;
        private BigInteger referralCode;
    }

    @Data
    public static class RepayRequest {
        private Long userId;
        private String walletAddress;
        private String asset;
        private BigInteger amount;
        private BigInteger interestRateMode;
    }
}