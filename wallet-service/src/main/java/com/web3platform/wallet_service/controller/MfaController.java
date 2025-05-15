package com.web3platform.wallet_service.controller;

import com.web3platform.wallet_service.service.MfaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import lombok.Data;

@RestController
@RequestMapping("/api/v1/mfa")
public class MfaController {

    @Autowired
    private MfaService mfaService;

    @PostMapping("/setup")
    public ResponseEntity<?> setupMfa(
            @AuthenticationPrincipal Long userId,
            @RequestBody SetupMfaRequest request) {
        String qrCode = mfaService.setupMfa(userId, request.getEmail());
        return ResponseEntity.ok(new SetupMfaResponse(qrCode));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyAndEnableMfa(
            @AuthenticationPrincipal Long userId,
            @RequestBody VerifyMfaRequest request) {
        boolean isValid = mfaService.verifyAndEnableMfa(userId, request.getCode());
        return ResponseEntity.ok(new VerifyMfaResponse(isValid));
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateCode(
            @AuthenticationPrincipal Long userId,
            @RequestBody ValidateMfaRequest request) {
        boolean isValid = mfaService.verifyCode(userId, request.getCode());
        return ResponseEntity.ok(new ValidateMfaResponse(isValid));
    }

    @PostMapping("/disable")
    public ResponseEntity<?> disableMfa(
            @AuthenticationPrincipal Long userId,
            @RequestBody DisableMfaRequest request) {
        mfaService.disableMfa(userId, request.getCode());
        return ResponseEntity.ok().build();
    }

    @Data
    public static class SetupMfaRequest {
        private String email;
    }

    @Data
    public static class SetupMfaResponse {
        private final String qrCode;
    }

    @Data
    public static class VerifyMfaRequest {
        private int code;
    }

    @Data
    public static class VerifyMfaResponse {
        private final boolean valid;
    }

    @Data
    public static class ValidateMfaRequest {
        private int code;
    }

    @Data
    public static class ValidateMfaResponse {
        private final boolean valid;
    }

    @Data
    public static class DisableMfaRequest {
        private int code;
    }
}