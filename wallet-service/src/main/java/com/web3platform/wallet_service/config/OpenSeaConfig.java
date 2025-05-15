package com.web3platform.wallet_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenSeaConfig {

    @Value("${opensea.seaport.address}")
    private String seaportAddress;

    @Value("${opensea.api.key}")
    private String apiKey;

    @Value("${opensea.api.url}")
    private String apiUrl;

    @Value("${opensea.conduit.key}")
    private String conduitKey;

    @Value("${opensea.weth.address}")
    private String wethAddress;

    public String getSeaportAddress() {
        return seaportAddress;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getConduitKey() {
        return conduitKey;
    }

    public String getWethAddress() {
        return wethAddress;
    }
}