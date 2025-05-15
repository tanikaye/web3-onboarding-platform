package com.web3platform.wallet_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AaveConfig {

    @Value("${aave.v3.lending-pool.address}")
    private String lendingPoolAddress;

    @Value("${aave.v3.lending-pool-data-provider.address}")
    private String dataProviderAddress;

    @Value("${aave.v3.incentives-controller.address}")
    private String incentivesControllerAddress;

    @Value("${aave.v3.weth-gateway.address}")
    private String wethGatewayAddress;

    // Common token addresses
    @Value("${aave.v3.weth.address}")
    private String wethAddress;

    @Value("${aave.v3.usdc.address}")
    private String usdcAddress;

    @Value("${aave.v3.dai.address}")
    private String daiAddress;

    public String getLendingPoolAddress() {
        return lendingPoolAddress;
    }

    public String getDataProviderAddress() {
        return dataProviderAddress;
    }

    public String getIncentivesControllerAddress() {
        return incentivesControllerAddress;
    }

    public String getWethGatewayAddress() {
        return wethGatewayAddress;
    }

    public String getWethAddress() {
        return wethAddress;
    }

    public String getUsdcAddress() {
        return usdcAddress;
    }

    public String getDaiAddress() {
        return daiAddress;
    }
}