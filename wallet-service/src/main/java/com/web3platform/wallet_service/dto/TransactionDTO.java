package com.web3platform.wallet_service.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionDTO {
    private String id;
    private String type;
    private String status;
    private String transactionHash;
    private LocalDateTime timestamp;
    private String amount;
    private String token;
    private String fromAddress;
    private String toAddress;
}