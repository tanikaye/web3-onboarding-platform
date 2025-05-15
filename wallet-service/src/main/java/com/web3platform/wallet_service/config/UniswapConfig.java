package com.web3platform.wallet_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UniswapConfig {

    @Value("${uniswap.v3.factory.address}")
    private String factoryAddress;

    @Value("${uniswap.v3.router.address}")
    private String routerAddress;

    @Value("${uniswap.v3.quoter.address}")
    private String quoterAddress;

    @Value("${uniswap.v3.weth.address}")
    private String wethAddress;

    public String getFactoryAddress() {
        return factoryAddress;
    }

    public String getRouterAddress() {
        return routerAddress;
    }

    public String getQuoterAddress() {
        return quoterAddress;
    }

    public String getWethAddress() {
        return wethAddress;
    }
}