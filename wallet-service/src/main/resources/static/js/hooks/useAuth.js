import { useState, useEffect } from 'react';

export const useAuth = () => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        checkAuthStatus();
    }, []);

    const checkAuthStatus = async () => {
        try {
            const response = await fetch('/api/v1/auth/status');
            const data = await response.json();
            setIsAuthenticated(data.isAuthenticated);
        } catch (err) {
            setError('Failed to check authentication status');
            console.error('Error checking auth status:', err);
        } finally {
            setIsLoading(false);
        }
    };

    const login = async (walletAddress) => {
        try {
            const response = await fetch('/api/v1/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ walletAddress }),
            });

            if (!response.ok) {
                throw new Error('Login failed');
            }

            const data = await response.json();
            setIsAuthenticated(true);
            return data;
        } catch (err) {
            setError('Login failed');
            console.error('Error during login:', err);
            throw err;
        }
    };

    const logout = async () => {
        try {
            await fetch('/api/v1/auth/logout', {
                method: 'POST',
            });
            setIsAuthenticated(false);
        } catch (err) {
            setError('Logout failed');
            console.error('Error during logout:', err);
            throw err;
        }
    };

    return {
        isAuthenticated,
        isLoading,
        error,
        login,
        logout,
    };
};