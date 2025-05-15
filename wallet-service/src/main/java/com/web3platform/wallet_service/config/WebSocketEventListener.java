package com.web3platform.wallet_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import com.web3platform.wallet_service.service.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebSocketEventListener {

    @Autowired
    private WebSocketSessionManager sessionManager;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = headerAccessor.getUser().getName();
        String sessionId = headerAccessor.getSessionId();

        if (userId != null && sessionId != null) {
            sessionManager.handleSessionConnect(userId, sessionId);
            log.info("User {} connected with session {}", userId, sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = headerAccessor.getUser().getName();
        String sessionId = headerAccessor.getSessionId();

        if (userId != null && sessionId != null) {
            sessionManager.handleSessionDisconnect(userId, sessionId);
            log.info("User {} disconnected from session {}", userId, sessionId);
        }
    }
}