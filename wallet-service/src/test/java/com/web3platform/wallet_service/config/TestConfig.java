package com.web3platform.wallet_service.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import com.web3platform.wallet_service.service.WebSocketMessageEncryptionService;
import com.web3platform.wallet_service.service.WebSocketSessionManager;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.mockito.Mockito;
import com.web3platform.wallet_service.service.TokenBalanceService;
import com.web3platform.wallet_service.repository.WalletRepository;
import com.web3platform.wallet_service.repository.DappTransactionRepository;

@TestConfiguration
@EnableWebSocketMessageBroker
public class TestConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }

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

    @Bean
    @Primary
    public SimpMessagingTemplate messagingTemplate() {
        return Mockito.mock(SimpMessagingTemplate.class);
    }

    @Bean
    @Primary
    public WebSocketMessageEncryptionService encryptionService() {
        return new WebSocketMessageEncryptionService();
    }

    @Bean
    @Primary
    public WebSocketSessionManager sessionManager() {
        return new WebSocketSessionManager();
    }

    @Bean
    public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192);
        container.setMaxBinaryMessageBufferSize(8192);
        return container;
    }
}