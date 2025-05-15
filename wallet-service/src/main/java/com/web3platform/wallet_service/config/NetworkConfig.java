package com.web3platform.wallet_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "blockchain.networks")
public class NetworkConfig {
    private Map<String, NetworkProperties> networks;

    @Data
    public static class NetworkProperties {
        private String rpcUrl;
        private String chainId;
        private String name;
        private String currency;
        private int requiredConfirmations;
        private double blockTime; // in seconds
        private String explorerUrl;
        private Map<String, String> contracts; // contract addresses for this network
    }

    public NetworkProperties getNetworkProperties(String networkId) {
        return networks.get(networkId);
    }

    public boolean isNetworkSupported(String networkId) {
        return networks.containsKey(networkId);
    }
}