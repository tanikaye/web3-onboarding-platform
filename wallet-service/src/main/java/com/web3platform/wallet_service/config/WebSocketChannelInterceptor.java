package com.web3platform.wallet_service.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    // Validate token and create authentication
                    Authentication auth = validateToken(token);
                    accessor.setUser(auth);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (Exception e) {
                    log.error("WebSocket authentication failed: {}", e.getMessage());
                    return null; // Reject the message
                }
            }
        }

        return message;
    }

    private Authentication validateToken(String token) {
        // TODO: Implement token validation logic
        // For now, return a mock authentication
        return new UsernamePasswordAuthenticationToken(
            "user",
            null,
            java.util.Collections.emptyList()
        );
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        if (ex != null) {
            log.error("Error sending WebSocket message: {}", ex.getMessage());
        }
    }
}