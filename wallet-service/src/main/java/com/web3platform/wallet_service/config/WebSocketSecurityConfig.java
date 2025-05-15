package com.web3platform.wallet_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.context.AuthenticationPrincipalArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
            // Public endpoints
            .simpDestMatchers("/topic/public/**").permitAll()
            .simpSubscribeDestMatchers("/topic/public/**").permitAll()

            // User-specific endpoints require authentication
            .simpDestMatchers("/topic/user/**").authenticated()
            .simpSubscribeDestMatchers("/topic/user/**").authenticated()

            // Transaction endpoints require authentication
            .simpDestMatchers("/topic/transactions/**").authenticated()
            .simpSubscribeDestMatchers("/topic/transactions/**").authenticated()

            // Network-specific endpoints require authentication
            .simpDestMatchers("/topic/network/**").authenticated()
            .simpSubscribeDestMatchers("/topic/network/**").authenticated()

            // Default deny
            .anyMessage().denyAll();
    }

    @Override
    protected void configureOutbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
            .simpDestMatchers("/app/**").authenticated()
            .anyMessage().authenticated();
    }

    @Override
    protected boolean sameOriginDisabled() {
        // Disable CSRF for WebSocket endpoints
        return true;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new WebSocketChannelInterceptor());
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(new WebSocketChannelInterceptor());
    }
}