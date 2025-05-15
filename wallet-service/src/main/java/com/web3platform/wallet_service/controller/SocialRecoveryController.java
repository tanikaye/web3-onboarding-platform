package com.web3platform.wallet_service.controller;

import com.web3platform.wallet_service.model.SocialRecoveryShare;
import com.web3platform.wallet_service.service.SocialRecoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import lombok.Data;
import java.util.List;

@RestController
@RequestMapping("/api/v1/recovery")
public class SocialRecoveryController {

    @Autowired
    private SocialRecoveryService socialRecoveryService;

    @PostMapping("/shares")
    public ResponseEntity<?> createRecoveryShares(
            @AuthenticationPrincipal Long userId,
            @RequestBody CreateSharesRequest request) {
        List<SocialRecoveryShare> shares = socialRecoveryService.createRecoveryShares(
            request.getWalletAddress(),
            userId,
            request.getTrusteeEmails(),
            request.getRequiredShares(),
            request.getMfaCode()
        );
        return ResponseEntity.ok(shares);
    }

    @PostMapping("/recover")
    public ResponseEntity<?> recoverPrivateKey(
            @AuthenticationPrincipal Long userId,
            @RequestBody RecoverKeyRequest request) {
        String privateKey = socialRecoveryService.recoverPrivateKey(
            request.getWalletAddress(),
            request.getShares(),
            userId,
            request.getMfaCode()
        );
        return ResponseEntity.ok(new RecoverKeyResponse(privateKey));
    }

    @GetMapping("/shares/{walletAddress}")
    public ResponseEntity<?> getWalletShares(@PathVariable String walletAddress) {
        List<SocialRecoveryShare> shares = socialRecoveryService.getWalletShares(walletAddress);
        return ResponseEntity.ok(shares);
    }

    @PostMapping("/verify/{shareId}")
    public ResponseEntity<?> verifyShare(@PathVariable Long shareId) {
        socialRecoveryService.verifyShare(shareId);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class CreateSharesRequest {
        private String walletAddress;
        private List<String> trusteeEmails;
        private int requiredShares;
        private int mfaCode;
    }

    @Data
    public static class RecoverKeyRequest {
        private String walletAddress;
        private List<String> shares;
        private int mfaCode;
    }

    @Data
    public static class RecoverKeyResponse {
        private final String privateKey;
    }
}