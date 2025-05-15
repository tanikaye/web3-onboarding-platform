package com.web3platform.wallet_service.service;

import com.web3platform.wallet_service.config.OpenSeaConfig;
import com.web3platform.wallet_service.contract.OpenSeaSeaport;
import com.web3platform.wallet_service.model.DappTransaction;
import com.web3platform.wallet_service.model.NFTData;
import com.web3platform.wallet_service.repository.DappTransactionRepository;
import com.web3platform.wallet_service.repository.NFTDataRepository;
import com.web3platform.wallet_service.repository.WalletRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OpenSeaService {

    @Autowired
    private Web3j web3j;

    @Autowired
    private OpenSeaConfig openSeaConfig;

    @Autowired
    private DappTransactionRepository dappTransactionRepository;

    @Autowired
    private NFTDataRepository nftDataRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletEncryptionService walletEncryptionService;

    @Autowired
    private ContractGasProvider gasProvider;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TokenBalanceService tokenBalanceService;

    @Autowired
    private TransactionRetryService transactionRetryService;

    @Autowired
    private OrderValidationCache orderValidationCache;

    public enum PaymentToken {
        ETH("0x0000000000000000000000000000000000000000"),
        WETH("0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2"),
        USDC("0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48");

        private final String address;

        PaymentToken(String address) {
            this.address = address;
        }

        public String getAddress() {
            return address;
        }
    }

    public List<DappTransaction> batchBuyNFTs(
            Long userId,
            String walletAddress,
            List<BatchNFTOrder> orders) {
        try {
            // Create transaction record for batch
            DappTransaction batchTransaction = new DappTransaction();
            batchTransaction.setUserId(userId);
            batchTransaction.setWalletAddress(walletAddress);
            batchTransaction.setDappName("opensea");
            batchTransaction.setTransactionType(DappTransaction.TransactionType.NFT_PURCHASE);
            batchTransaction.setStatus(DappTransaction.TransactionStatus.PENDING);
            batchTransaction.setContractAddress(openSeaConfig.getSeaportAddress());
            batchTransaction.setFunctionName("batchFulfillOrders");
            batchTransaction = dappTransactionRepository.save(batchTransaction);

            // Group orders by payment token for efficient balance checking
            Map<PaymentToken, List<BatchNFTOrder>> ordersByToken = orders.stream()
                    .collect(Collectors.groupingBy(BatchNFTOrder::getPaymentToken));

            // Check balances for each payment token
            List<CompletableFuture<Boolean>> balanceChecks = ordersByToken.entrySet().stream()
                    .map(entry -> {
                        PaymentToken token = entry.getKey();
                        BigInteger totalAmount = entry.getValue().stream()
                                .map(BatchNFTOrder::getValue)
                                .reduce(BigInteger.ZERO, BigInteger::add);
                        return tokenBalanceService.hasSufficientBalance(walletAddress, token, totalAmount);
                    })
                    .collect(Collectors.toList());

            CompletableFuture.allOf(balanceChecks.toArray(new CompletableFuture[0])).join();

            boolean allBalancesSufficient = balanceChecks.stream()
                    .map(CompletableFuture::join)
                    .allMatch(sufficient -> sufficient);

            if (!allBalancesSufficient) {
                batchTransaction.setStatus(DappTransaction.TransactionStatus.FAILED);
                batchTransaction.setErrorMessage("Insufficient balance for one or more payment tokens");
                return Collections.singletonList(dappTransactionRepository.save(batchTransaction));
            }

            // Estimate gas for the batch operation
            BigInteger estimatedGas = tokenBalanceService.estimateGasForBatchOperation(
                    walletAddress,
                    orders.get(0).getPaymentToken(),
                    orders.size()
            );

            // Get current gas price
            BigDecimal gasPrice = tokenBalanceService.getGasPriceInGwei();
            log.info("Estimated gas for batch operation: {} units, Current gas price: {} gwei",
                    estimatedGas, gasPrice);

            // Get wallet credentials
            Credentials credentials = getWalletCredentials(walletAddress);

            // Load Seaport contract
            OpenSeaSeaport seaport = OpenSeaSeaport.load(
                    openSeaConfig.getSeaportAddress(),
                    web3j,
                    credentials,
                    gasProvider
            );

            // Validate all orders
            List<CompletableFuture<Boolean>> validationFutures = orders.stream()
                    .map(order -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return seaport.validateOrder(order.getOrder()).send();
                        } catch (Exception e) {
                            log.error("Error validating order", e);
                            return false;
                        }
                    }))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(validationFutures.toArray(new CompletableFuture[0])).join();

            boolean allValid = validationFutures.stream()
                    .map(CompletableFuture::join)
                    .allMatch(valid -> valid);

            if (!allValid) {
                batchTransaction.setStatus(DappTransaction.TransactionStatus.FAILED);
                batchTransaction.setErrorMessage("One or more orders are invalid");
                return Collections.singletonList(dappTransactionRepository.save(batchTransaction));
            }

            // Fulfill all orders
            List<CompletableFuture<TransactionReceipt>> fulfillmentFutures = orders.stream()
                    .map(order -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return seaport.fulfillOrder(
                                    order.getOrder(),
                                    openSeaConfig.getConduitKey(),
                                    order.getValue()
                            ).send();
                        } catch (Exception e) {
                            log.error("Error fulfilling order", e);
                            return null;
                        }
                    }))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(fulfillmentFutures.toArray(new CompletableFuture[0])).join();

            List<TransactionReceipt> receipts = fulfillmentFutures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (receipts.size() != orders.size()) {
                batchTransaction.setStatus(DappTransaction.TransactionStatus.FAILED);
                batchTransaction.setErrorMessage("Some orders failed to fulfill");
                return Collections.singletonList(dappTransactionRepository.save(batchTransaction));
            }

            // Create individual transaction records
            List<DappTransaction> transactions = new ArrayList<>();
            for (int i = 0; i < receipts.size(); i++) {
                DappTransaction transaction = new DappTransaction();
                transaction.setUserId(userId);
                transaction.setWalletAddress(walletAddress);
                transaction.setDappName("opensea");
                transaction.setTransactionType(DappTransaction.TransactionType.NFT_PURCHASE);
                transaction.setStatus(DappTransaction.TransactionStatus.CONFIRMED);
                transaction.setContractAddress(openSeaConfig.getSeaportAddress());
                transaction.setFunctionName("fulfillOrder");
                transaction.setTransactionHash(receipts.get(i).getTransactionHash());
                transactions.add(dappTransactionRepository.save(transaction));
            }

            // Update batch transaction
            batchTransaction.setStatus(DappTransaction.TransactionStatus.CONFIRMED);
            dappTransactionRepository.save(batchTransaction);

            return transactions;

        } catch (Exception e) {
            log.error("Error in batchBuyNFTs operation", e);
            throw new RuntimeException("Batch buy NFTs operation failed: " + e.getMessage());
        }
    }

    public List<DappTransaction> batchSellNFTs(
            Long userId,
            String walletAddress,
            List<BatchNFTOrder> orders) {
        try {
            // Create transaction record for batch
            DappTransaction batchTransaction = new DappTransaction();
            batchTransaction.setUserId(userId);
            batchTransaction.setWalletAddress(walletAddress);
            batchTransaction.setDappName("opensea");
            batchTransaction.setTransactionType(DappTransaction.TransactionType.NFT_SALE);
            batchTransaction.setStatus(DappTransaction.TransactionStatus.PENDING);
            batchTransaction.setContractAddress(openSeaConfig.getSeaportAddress());
            batchTransaction.setFunctionName("batchFulfillOrders");
            batchTransaction = dappTransactionRepository.save(batchTransaction);

            // Group orders by payment token for efficient balance checking
            Map<PaymentToken, List<BatchNFTOrder>> ordersByToken = orders.stream()
                    .collect(Collectors.groupingBy(BatchNFTOrder::getPaymentToken));

            // Check balances for each payment token
            List<CompletableFuture<Boolean>> balanceChecks = ordersByToken.entrySet().stream()
                    .map(entry -> {
                        PaymentToken token = entry.getKey();
                        BigInteger totalAmount = entry.getValue().stream()
                                .map(BatchNFTOrder::getValue)
                                .reduce(BigInteger.ZERO, BigInteger::add);
                        return tokenBalanceService.hasSufficientBalance(walletAddress, token, totalAmount);
                    })
                    .collect(Collectors.toList());

            CompletableFuture.allOf(balanceChecks.toArray(new CompletableFuture[0])).join();

            boolean allBalancesSufficient = balanceChecks.stream()
                    .map(CompletableFuture::join)
                    .allMatch(sufficient -> sufficient);

            if (!allBalancesSufficient) {
                batchTransaction.setStatus(DappTransaction.TransactionStatus.FAILED);
                batchTransaction.setErrorMessage("Insufficient balance for one or more payment tokens");
                return Collections.singletonList(dappTransactionRepository.save(batchTransaction));
            }

            // Estimate gas for the batch operation
            BigInteger estimatedGas = tokenBalanceService.estimateGasForBatchOperation(
                    walletAddress,
                    orders.get(0).getPaymentToken(),
                    orders.size()
            );

            // Get current gas price
            BigDecimal gasPrice = tokenBalanceService.getGasPriceInGwei();
            log.info("Estimated gas for batch operation: {} units, Current gas price: {} gwei",
                    estimatedGas, gasPrice);

            // Get wallet credentials
            Credentials credentials = getWalletCredentials(walletAddress);

            // Load Seaport contract
            OpenSeaSeaport seaport = OpenSeaSeaport.load(
                    openSeaConfig.getSeaportAddress(),
                    web3j,
                    credentials,
                    gasProvider
            );

            // Validate all orders
            List<CompletableFuture<Boolean>> validationFutures = orders.stream()
                    .map(order -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return seaport.validateOrder(order.getOrder()).send();
                        } catch (Exception e) {
                            log.error("Error validating order", e);
                            return false;
                        }
                    }))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(validationFutures.toArray(new CompletableFuture[0])).join();

            boolean allValid = validationFutures.stream()
                    .map(CompletableFuture::join)
                    .allMatch(valid -> valid);

            if (!allValid) {
                batchTransaction.setStatus(DappTransaction.TransactionStatus.FAILED);
                batchTransaction.setErrorMessage("One or more orders are invalid");
                return Collections.singletonList(dappTransactionRepository.save(batchTransaction));
            }

            // Fulfill all orders
            List<CompletableFuture<TransactionReceipt>> fulfillmentFutures = orders.stream()
                    .map(order -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return seaport.fulfillOrder(
                                    order.getOrder(),
                                    openSeaConfig.getConduitKey(),
                                    BigInteger.ZERO
                            ).send();
                        } catch (Exception e) {
                            log.error("Error fulfilling order", e);
                            return null;
                        }
                    }))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(fulfillmentFutures.toArray(new CompletableFuture[0])).join();

            List<TransactionReceipt> receipts = fulfillmentFutures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (receipts.size() != orders.size()) {
                batchTransaction.setStatus(DappTransaction.TransactionStatus.FAILED);
                batchTransaction.setErrorMessage("Some orders failed to fulfill");
                return Collections.singletonList(dappTransactionRepository.save(batchTransaction));
            }

            // Create individual transaction records
            List<DappTransaction> transactions = new ArrayList<>();
            for (int i = 0; i < receipts.size(); i++) {
                DappTransaction transaction = new DappTransaction();
                transaction.setUserId(userId);
                transaction.setWalletAddress(walletAddress);
                transaction.setDappName("opensea");
                transaction.setTransactionType(DappTransaction.TransactionType.NFT_SALE);
                transaction.setStatus(DappTransaction.TransactionStatus.CONFIRMED);
                transaction.setContractAddress(openSeaConfig.getSeaportAddress());
                transaction.setFunctionName("fulfillOrder");
                transaction.setTransactionHash(receipts.get(i).getTransactionHash());
                transactions.add(dappTransactionRepository.save(transaction));
            }

            // Update batch transaction
            batchTransaction.setStatus(DappTransaction.TransactionStatus.CONFIRMED);
            dappTransactionRepository.save(batchTransaction);

            return transactions;

        } catch (Exception e) {
            log.error("Error in batchSellNFTs operation", e);
            throw new RuntimeException("Batch sell NFTs operation failed: " + e.getMessage());
        }
    }

    public DappTransaction buyNFT(
            Long userId,
            String walletAddress,
            String order,
            BigInteger value) {
        try {
            // Create transaction record
            DappTransaction transaction = new DappTransaction();
            transaction.setUserId(userId);
            transaction.setWalletAddress(walletAddress);
            transaction.setDappName("opensea");
            transaction.setTransactionType(DappTransaction.TransactionType.NFT_PURCHASE);
            transaction.setStatus(DappTransaction.TransactionStatus.PENDING);
            transaction.setContractAddress(openSeaConfig.getSeaportAddress());
            transaction.setFunctionName("fulfillOrder");
            transaction = dappTransactionRepository.save(transaction);

            // Validate order using cache
            boolean isValid = orderValidationCache.isOrderValid(order);
            if (!isValid) {
                transaction.setStatus(DappTransaction.TransactionStatus.FAILED);
                transaction.setErrorMessage("Invalid order");
                return dappTransactionRepository.save(transaction);
            }

            // Get wallet credentials
            Credentials credentials = getWalletCredentials(walletAddress);

            // Load Seaport contract
            OpenSeaSeaport seaport = OpenSeaSeaport.load(
                    openSeaConfig.getSeaportAddress(),
                    web3j,
                    credentials,
                    gasProvider
            );

            // Fulfill order
            TransactionReceipt receipt = seaport.fulfillOrder(
                    order,
                    openSeaConfig.getConduitKey(),
                    value
            ).send();

            if (!receipt.isStatusOK()) {
                transaction.setStatus(DappTransaction.TransactionStatus.FAILED);
                transaction.setErrorMessage("Transaction failed");
                return dappTransactionRepository.save(transaction);
            }

            // Update transaction status
            transaction.setStatus(DappTransaction.TransactionStatus.CONFIRMED);
            transaction.setTransactionHash(receipt.getTransactionHash());
            transaction.setBlockNumber(receipt.getBlockNumber().longValue());
            transaction.setBlockHash(receipt.getBlockHash());
            return dappTransactionRepository.save(transaction);

        } catch (Exception e) {
            log.error("Error in buyNFT operation", e);
            throw new RuntimeException("Buy NFT operation failed: " + e.getMessage());
        }
    }

    public DappTransaction sellNFT(
            Long userId,
            String walletAddress,
            String order) {
        try {
            // Create transaction record
            DappTransaction transaction = new DappTransaction();
            transaction.setUserId(userId);
            transaction.setWalletAddress(walletAddress);
            transaction.setDappName("opensea");
            transaction.setTransactionType(DappTransaction.TransactionType.NFT_SALE);
            transaction.setStatus(DappTransaction.TransactionStatus.PENDING);
            transaction.setContractAddress(openSeaConfig.getSeaportAddress());
            transaction.setFunctionName("fulfillOrder");
            transaction = dappTransactionRepository.save(transaction);

            // Validate order using cache
            boolean isValid = orderValidationCache.isOrderValid(order);
            if (!isValid) {
                transaction.setStatus(DappTransaction.TransactionStatus.FAILED);
                transaction.setErrorMessage("Invalid order");
                return dappTransactionRepository.save(transaction);
            }

            // Get wallet credentials
            Credentials credentials = getWalletCredentials(walletAddress);

            // Load Seaport contract
            OpenSeaSeaport seaport = OpenSeaSeaport.load(
                    openSeaConfig.getSeaportAddress(),
                    web3j,
                    credentials,
                    gasProvider
            );

            // Fulfill order
            TransactionReceipt receipt = seaport.fulfillOrder(
                    order,
                    openSeaConfig.getConduitKey(),
                    BigInteger.ZERO
            ).send();

            if (!receipt.isStatusOK()) {
                transaction.setStatus(DappTransaction.TransactionStatus.FAILED);
                transaction.setErrorMessage("Transaction failed");
                return dappTransactionRepository.save(transaction);
            }

            // Update transaction status
            transaction.setStatus(DappTransaction.TransactionStatus.CONFIRMED);
            transaction.setTransactionHash(receipt.getTransactionHash());
            transaction.setBlockNumber(receipt.getBlockNumber().longValue());
            transaction.setBlockHash(receipt.getBlockHash());
            return dappTransactionRepository.save(transaction);

        } catch (Exception e) {
            log.error("Error in sellNFT operation", e);
            throw new RuntimeException("Sell NFT operation failed: " + e.getMessage());
        }
    }

    public DappTransaction cancelOrder(
            Long userId,
            String walletAddress,
            String order) {
        try {
            // Create transaction record
            DappTransaction transaction = new DappTransaction();
            transaction.setUserId(userId);
            transaction.setWalletAddress(walletAddress);
            transaction.setDappName("opensea");
            transaction.setTransactionType(DappTransaction.TransactionType.NFT_SALE);
            transaction.setStatus(DappTransaction.TransactionStatus.PENDING);
            transaction.setContractAddress(openSeaConfig.getSeaportAddress());
            transaction.setFunctionName("cancelOrder");
            transaction = dappTransactionRepository.save(transaction);

            // Get wallet credentials
            Credentials credentials = getWalletCredentials(walletAddress);

            // Load Seaport contract
            OpenSeaSeaport seaport = OpenSeaSeaport.load(
                    openSeaConfig.getSeaportAddress(),
                    web3j,
                    credentials,
                    gasProvider
            );

            // Cancel order
            TransactionReceipt receipt = seaport.cancelOrder(order).send();

            if (!receipt.isStatusOK()) {
                transaction.setStatus(DappTransaction.TransactionStatus.FAILED);
                transaction.setErrorMessage("Transaction failed");
                return dappTransactionRepository.save(transaction);
            }

            // Update transaction status
            transaction.setStatus(DappTransaction.TransactionStatus.CONFIRMED);
            transaction.setTransactionHash(receipt.getTransactionHash());
            return dappTransactionRepository.save(transaction);

        } catch (Exception e) {
            log.error("Error in cancelOrder operation", e);
            throw new RuntimeException("Cancel order operation failed: " + e.getMessage());
        }
    }

    public NFTData getNFTData(String contractAddress, String tokenId) {
        try {
            // Check if we have cached data
            NFTData cachedData = nftDataRepository.findByContractAddressAndTokenId(contractAddress, tokenId);
            if (cachedData != null) {
                return cachedData;
            }

            // Fetch from OpenSea API
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-KEY", openSeaConfig.getApiKey());
            HttpEntity<?> entity = new HttpEntity<>(headers);

            String url = openSeaConfig.getApiUrl() + "/api/v1/asset/" + contractAddress + "/" + tokenId + "/";
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = response.getBody();
                NFTData nftData = new NFTData();
                nftData.setTokenId(tokenId);
                nftData.setContractAddress(contractAddress);
                nftData.setName((String) data.get("name"));
                nftData.setDescription((String) data.get("description"));
                nftData.setImageUrl((String) data.get("image_url"));
                nftData.setMetadataUrl((String) data.get("metadata_url"));
                nftData.setOwnerAddress((String) data.get("owner_address"));
                nftData.setTokenStandard(NFTData.TokenStandard.valueOf((String) data.get("token_standard")));

                Map<String, Object> lastSale = (Map<String, Object>) data.get("last_sale");
                if (lastSale != null) {
                    nftData.setLastSalePrice(new java.math.BigDecimal(lastSale.get("total_price").toString()));
                    nftData.setLastSaleCurrency((String) lastSale.get("payment_token"));
                }

                return nftDataRepository.save(nftData);
            }

            throw new RuntimeException("Failed to fetch NFT data from OpenSea");

        } catch (Exception e) {
            log.error("Error fetching NFT data", e);
            throw new RuntimeException("Failed to fetch NFT data: " + e.getMessage());
        }
    }

    private Credentials getWalletCredentials(String walletAddress) {
        return walletRepository.findByAddress(walletAddress)
                .map(wallet -> walletEncryptionService.decryptPrivateKey(wallet.getEncryptedPrivateKey()))
                .map(Credentials::create)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    @Data
    public static class BatchNFTOrder {
        private String order;
        private BigInteger value;
        private PaymentToken paymentToken;

        public String getOrder() {
            return order;
        }

        public BigInteger getValue() {
            return value;
        }

        public PaymentToken getPaymentToken() {
            return paymentToken;
        }
    }
}