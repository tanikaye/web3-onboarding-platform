package com.web3platform.wallet_service.controller;

import com.web3platform.wallet_service.model.DappTransaction;
import com.web3platform.wallet_service.service.UniswapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/uniswap")
public class UniswapController {

    @Autowired
    private UniswapService uniswapService;

    @PostMapping("/swap/tokens")
    public ResponseEntity<?> swapTokens(
            @AuthenticationPrincipal Long userId,
            @RequestBody SwapTokensRequest request) {
        DappTransaction transaction = uniswapService.swapExactTokensForTokens(
            userId,
            request.getWalletAddress(),
            request.getTokenIn(),
            request.getTokenOut(),
            request.getAmountIn(),
            request.getAmountOutMin(),
            request.getDeadline()
        );
        return ResponseEntity.ok(new SwapResponse(transaction));
    }

    @PostMapping("/swap/eth")
    public ResponseEntity<?> swapETH(
            @AuthenticationPrincipal Long userId,
            @RequestBody SwapETHRequest request) {
        DappTransaction transaction = uniswapService.swapExactETHForTokens(
            userId,
            request.getWalletAddress(),
            request.getTokenOut(),
            request.getAmountOutMin(),
            request.getDeadline()
        );
        return ResponseEntity.ok(new SwapResponse(transaction));
    }

    @Data
    public static class SwapTokensRequest {
        private String walletAddress;
        private String tokenIn;
        private String tokenOut;
        private BigDecimal amountIn;
        private BigDecimal amountOutMin;
        private BigDecimal deadline;
    }

    @Data
    public static class SwapETHRequest {
        private String walletAddress;
        private String tokenOut;
        private BigDecimal amountOutMin;
        private BigDecimal deadline;
    }

    @Data
    public static class SwapResponse {
        private final DappTransaction transaction;
    }
}