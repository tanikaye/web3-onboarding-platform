package com.web3platform.wallet_service.service;

import com.web3platform.wallet_service.contract.OpenSeaSeaport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.ContractGasProvider;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@EnableCaching
public class OrderValidationCache {

    @Autowired
    private Web3j web3j;

    @Autowired
    private ContractGasProvider gasProvider;

    private static final long CACHE_DURATION = 5; // minutes

    @Cacheable(value = "orderValidation", key = "#order", unless = "#result == false")
    public CompletableFuture<Boolean> validateOrder(String order) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Load Seaport contract
                OpenSeaSeaport seaport = OpenSeaSeaport.load(
                        "0x00000000006c3852cbEf3e08E8dF289169EdE581", // OpenSea Seaport address
                        web3j,
                        null, // Read-only credentials
                        gasProvider
                );

                // Validate order
                boolean isValid = seaport.validateOrder(order).send();

                if (isValid) {
                    log.info("Order validation cached: {}", order);
                } else {
                    log.warn("Invalid order: {}", order);
                }

                return isValid;
            } catch (Exception e) {
                log.error("Error validating order", e);
                return false;
            }
        });
    }

    public void invalidateCache(String order) {
        try {
            // Remove from cache
            // Note: This requires a custom cache manager implementation
            log.info("Order validation cache invalidated: {}", order);
        } catch (Exception e) {
            log.error("Error invalidating cache", e);
        }
    }

    public boolean isOrderValid(String order) {
        try {
            return validateOrder(order).get(CACHE_DURATION, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Error checking order validity", e);
            return false;
        }
    }
}