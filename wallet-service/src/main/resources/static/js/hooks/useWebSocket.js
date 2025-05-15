import { useState, useEffect, useRef, useCallback } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const useWebSocket = (userId, onMessage) => {
    const [isConnected, setIsConnected] = useState(false);
    const [lastMessage, setLastMessage] = useState(null);
    const [error, setError] = useState(null);
    const clientRef = useRef(null);
    const reconnectTimeoutRef = useRef(null);
    const reconnectAttemptsRef = useRef(0);
    const maxReconnectAttempts = 10;
    const baseDelay = 1000; // 1 second

    const connect = useCallback(() => {
        if (clientRef.current?.connected) {
            return;
        }

        const client = new Client({
            webSocketFactory: () => new SockJS('/ws'),
            connectHeaders: {
                Authorization: `Bearer ${localStorage.getItem('token')}`,
            },
            debug: (str) => {
                console.log(str);
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        client.onConnect = () => {
            console.log('Connected to WebSocket');
            setIsConnected(true);
            setError(null);
            reconnectAttemptsRef.current = 0;

            // Subscribe to user-specific topics
            client.subscribe(`/user/${userId}/queue/transactions`, (message) => {
                const decryptedMessage = decryptMessage(message.body);
                setLastMessage(decryptedMessage);
                onMessage?.(decryptedMessage);
            });

            client.subscribe(`/user/${userId}/queue/status`, (message) => {
                const decryptedMessage = decryptMessage(message.body);
                setLastMessage(decryptedMessage);
                onMessage?.(decryptedMessage);
            });

            client.subscribe(`/user/${userId}/queue/errors`, (message) => {
                const decryptedMessage = decryptMessage(message.body);
                setLastMessage(decryptedMessage);
                onMessage?.(decryptedMessage);
            });
        };

        client.onStompError = (frame) => {
            console.error('STOMP error:', frame);
            setError(frame.headers.message);
            handleReconnect();
        };

        client.onWebSocketClose = () => {
            console.log('WebSocket connection closed');
            setIsConnected(false);
            handleReconnect();
        };

        client.onWebSocketError = (event) => {
            console.error('WebSocket error:', event);
            setError('WebSocket connection error');
            handleReconnect();
        };

        clientRef.current = client;
        client.activate();
    }, [userId, onMessage]);

    const handleReconnect = useCallback(() => {
        if (reconnectAttemptsRef.current >= maxReconnectAttempts) {
            setError('Maximum reconnection attempts reached');
            return;
        }

        const delay = Math.min(baseDelay * Math.pow(2, reconnectAttemptsRef.current), 30000);
        reconnectAttemptsRef.current += 1;

        console.log(`Attempting to reconnect in ${delay}ms (attempt ${reconnectAttemptsRef.current})`);

        if (reconnectTimeoutRef.current) {
            clearTimeout(reconnectTimeoutRef.current);
        }

        reconnectTimeoutRef.current = setTimeout(() => {
            connect();
        }, delay);
    }, [connect]);

    const disconnect = useCallback(() => {
        if (clientRef.current) {
            clientRef.current.deactivate();
        }
        if (reconnectTimeoutRef.current) {
            clearTimeout(reconnectTimeoutRef.current);
        }
    }, []);

    const sendMessage = useCallback((destination, message) => {
        if (clientRef.current?.connected) {
            const encryptedMessage = encryptMessage(message);
            clientRef.current.publish({
                destination: `/app/${destination}`,
                body: encryptedMessage,
            });
        } else {
            setError('Cannot send message: WebSocket is not connected');
        }
    }, []);

    useEffect(() => {
        connect();

        return () => {
            disconnect();
        };
    }, [connect, disconnect]);

    return {
        isConnected,
        lastMessage,
        error,
        sendMessage,
        reconnect: connect,
    };
};

// Encryption/Decryption functions (using the same key as the server)
const encryptMessage = (message) => {
    // In a real implementation, use the same encryption key as the server
    // This is a placeholder for demonstration
    return btoa(JSON.stringify(message));
};

const decryptMessage = (encryptedMessage) => {
    try {
        // In a real implementation, use the same decryption key as the server
        // This is a placeholder for demonstration
        return JSON.parse(atob(encryptedMessage));
    } catch (error) {
        console.error('Error decrypting message:', error);
        return null;
    }
};

export default useWebSocket;