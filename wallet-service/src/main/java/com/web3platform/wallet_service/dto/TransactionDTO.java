package com.web3platform.wallet_service.dto;

import com.web3platform.wallet_service.model.DappTransaction;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private Long id;
    private DappTransaction.TransactionType type;
    private DappTransaction.TransactionStatus status;
    private String transactionHash;
    private LocalDateTime timestamp;
    private String amount;
    private String token;
    private String fromAddress;
    private String toAddress;
    private Long userId;
    private String walletAddress;
    private String network;
    private String dappName;
    private String contractAddress;
    private String functionName;
    private String functionParams;
    private BigDecimal value;
    private BigDecimal gasPrice;
    private Long gasLimit;
    private Long nonce;
    private Long blockNumber;
    private String blockHash;
    private String errorMessage;

    public static TransactionDTO fromEntity(DappTransaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setType(transaction.getType());
        dto.setStatus(transaction.getStatus());
        dto.setTransactionHash(transaction.getTransactionHash());
        dto.setTimestamp(transaction.getTimestamp());
        dto.setAmount(transaction.getAmount());
        dto.setToken(transaction.getToken());
        dto.setFromAddress(transaction.getFromAddress());
        dto.setToAddress(transaction.getToAddress());
        dto.setUserId(transaction.getUserId());
        dto.setWalletAddress(transaction.getWalletAddress());
        dto.setNetwork(transaction.getNetwork());
        dto.setDappName(transaction.getDappName());
        dto.setContractAddress(transaction.getContractAddress());
        dto.setFunctionName(transaction.getFunctionName());
        dto.setFunctionParams(transaction.getFunctionParams());
        dto.setValue(transaction.getValue());
        dto.setGasPrice(transaction.getGasPrice());
        dto.setGasLimit(transaction.getGasLimit());
        dto.setNonce(transaction.getNonce());
        dto.setBlockNumber(transaction.getBlockNumber());
        dto.setBlockHash(transaction.getBlockHash());
        dto.setErrorMessage(transaction.getErrorMessage());
        return dto;
    }
}