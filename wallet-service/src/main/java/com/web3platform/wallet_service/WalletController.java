package com.web3platform.wallet_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PostMapping
    public ResponseEntity<Wallet> createWallet(
            @RequestParam Long userId,
            @RequestParam String network) {
        Wallet wallet = walletService.createWallet(userId, network);
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/{address}")
    public ResponseEntity<Wallet> getWallet(@PathVariable String address) {
        return walletService.getWalletByAddress(address)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Wallet>> getUserWallets(@PathVariable Long userId) {
        List<Wallet> wallets = walletService.getUserWallets(userId);
        return ResponseEntity.ok(wallets);
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<Wallet>> getActiveUserWallets(@PathVariable Long userId) {
        List<Wallet> wallets = walletService.getActiveUserWallets(userId);
        return ResponseEntity.ok(wallets);
    }

    @PutMapping("/{address}/balance")
    public ResponseEntity<Wallet> updateBalance(
            @PathVariable String address,
            @RequestParam double newBalance) {
        Wallet wallet = walletService.updateWalletBalance(address, newBalance);
        return ResponseEntity.ok(wallet);
    }

    @DeleteMapping("/{address}")
    public ResponseEntity<Void> deactivateWallet(@PathVariable String address) {
        walletService.deactivateWallet(address);
        return ResponseEntity.ok().build();
    }
}