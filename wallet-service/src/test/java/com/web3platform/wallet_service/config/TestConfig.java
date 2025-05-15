package com.web3platform.wallet_service.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.mockito.Mockito;
import com.web3platform.wallet_service.service.TokenBalanceService;
import com.web3platform.wallet_service.repository.WalletRepository;
import com.web3platform.wallet_service.repository.DappTransactionRepository;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public Web3j web3j() {
        return Web3j.build(new HttpService("http://localhost:8545"));
    }

    @Bean
    @Primary
    public TokenBalanceService tokenBalanceService() {
        return Mockito.mock(TokenBalanceService.class);
    }

    @Bean
    @Primary
    public WalletRepository walletRepository() {
        return Mockito.mock(WalletRepository.class);
    }

    @Bean
    @Primary
    public DappTransactionRepository dappTransactionRepository() {
        return Mockito.mock(DappTransactionRepository.class);
    }
}