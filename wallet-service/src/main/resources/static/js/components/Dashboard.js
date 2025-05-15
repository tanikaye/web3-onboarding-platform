import React, { useState, useEffect } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';
import UniswapInterface from './defi/UniswapInterface';
import AaveInterface from './defi/AaveInterface';
import OpenSeaInterface from './defi/OpenSeaInterface';

const Dashboard = () => {
    const [activeTab, setActiveTab] = useState('uniswap');
    const [walletBalance, setWalletBalance] = useState({
        ETH: '0.0',
        USDC: '0.0',
        WETH: '0.0'
    });
    const [recentTransactions, setRecentTransactions] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    // WebSocket connection for real-time updates
    const { lastMessage, isConnected } = useWebSocket('/ws/transactions');

    useEffect(() => {
        fetchWalletData();
        fetchRecentTransactions();
    }, []);

    useEffect(() => {
        if (lastMessage) {
            const transaction = JSON.parse(lastMessage.data);
            setRecentTransactions(prev => [transaction, ...prev].slice(0, 5));
        }
    }, [lastMessage]);

    const fetchWalletData = async () => {
        try {
            const response = await fetch('/api/v1/wallets/balance');
            if (!response.ok) {
                throw new Error('Failed to fetch wallet balance');
            }
            const data = await response.json();
            setWalletBalance(data);
        } catch (err) {
            setError('Failed to fetch wallet balance: ' + err.message);
            console.error('Error fetching wallet balance:', err);
        }
    };

    const fetchRecentTransactions = async () => {
        try {
            const response = await fetch('/api/v1/transactions/recent?limit=5');
            if (!response.ok) {
                throw new Error('Failed to fetch recent transactions');
            }
            const data = await response.json();
            setRecentTransactions(data);
        } catch (err) {
            setError('Failed to fetch recent transactions: ' + err.message);
            console.error('Error fetching recent transactions:', err);
        } finally {
            setIsLoading(false);
        }
    };

    // Add polling for wallet balance updates
    useEffect(() => {
        const balanceInterval = setInterval(fetchWalletData, 30000); // Poll every 30 seconds
        return () => clearInterval(balanceInterval);
    }, []);

    // Handle WebSocket connection status
    useEffect(() => {
        if (!isConnected) {
            console.warn('WebSocket connection lost. Attempting to reconnect...');
        }
    }, [isConnected]);

    // Handle WebSocket messages
    useEffect(() => {
        if (lastMessage) {
            try {
                const transaction = JSON.parse(lastMessage.data);
                setRecentTransactions(prev => {
                    const updated = [transaction, ...prev.filter(tx => tx.id !== transaction.id)];
                    return updated.slice(0, 5); // Keep only the 5 most recent transactions
                });

                // Update wallet balance if the transaction affects it
                if (transaction.status === 'COMPLETED') {
                    fetchWalletData();
                }
            } catch (err) {
                console.error('Error processing WebSocket message:', err);
            }
        }
    }, [lastMessage]);

    const renderWalletOverview = () => (
        <div className="bg-white rounded-lg shadow p-6 mb-6">
            <h2 className="text-xl font-semibold mb-4">Wallet Overview</h2>
            <div className="grid grid-cols-3 gap-4">
                {Object.entries(walletBalance).map(([token, balance]) => (
                    <div key={token} className="bg-gray-50 p-4 rounded-lg">
                        <p className="text-sm text-gray-500">{token}</p>
                        <p className="text-lg font-semibold">{balance}</p>
                    </div>
                ))}
            </div>
        </div>
    );

    const renderRecentTransactions = () => (
        <div className="bg-white rounded-lg shadow p-6 mb-6">
            <h2 className="text-xl font-semibold mb-4">Recent Transactions</h2>
            {recentTransactions.length > 0 ? (
                <div className="space-y-4">
                    {recentTransactions.map(tx => (
                        <div key={tx.id} className="flex justify-between items-center p-4 bg-gray-50 rounded-lg">
                            <div>
                                <p className="font-medium">{tx.type}</p>
                                <p className="text-sm text-gray-500">
                                    {new Date(tx.timestamp).toLocaleString()}
                                </p>
                            </div>
                            <div className="text-right">
                                <p className={`font-semibold ${
                                    tx.status === 'COMPLETED' ? 'text-green-500' :
                                    tx.status === 'PENDING' ? 'text-yellow-500' :
                                    'text-red-500'
                                }`}>
                                    {tx.status}
                                </p>
                                <a
                                    href={`https://etherscan.io/tx/${tx.transactionHash}`}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="text-sm text-blue-500 hover:text-blue-600"
                                >
                                    View on Etherscan
                                </a>
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <p className="text-gray-500">No recent transactions</p>
            )}
        </div>
    );

    const renderDeFiInterface = () => {
        switch (activeTab) {
            case 'uniswap':
                return <UniswapInterface />;
            case 'aave':
                return <AaveInterface />;
            case 'opensea':
                return <OpenSeaInterface />;
            default:
                return null;
        }
    };

    if (isLoading) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="bg-red-50 p-4 rounded-lg max-w-md">
                    <div className="flex">
                        <div className="flex-shrink-0">
                            <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                            </svg>
                        </div>
                        <div className="ml-3">
                            <h3 className="text-sm font-medium text-red-800">Error</h3>
                            <div className="mt-2 text-sm text-red-700">
                                <p>{error}</p>
                            </div>
                            <div className="mt-4">
                                <button
                                    onClick={() => {
                                        setError(null);
                                        fetchWalletData();
                                        fetchRecentTransactions();
                                    }}
                                    className="inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-red-700 bg-red-100 hover:bg-red-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
                                >
                                    Try Again
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-100">
            {/* Navigation */}
            <nav className="bg-white shadow">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between h-16">
                        <div className="flex">
                            <div className="flex-shrink-0 flex items-center">
                                <img
                                    className="h-8 w-auto"
                                    src="/images/logo.png"
                                    alt="Web3 Platform"
                                />
                            </div>
                            <div className="hidden sm:ml-6 sm:flex sm:space-x-8">
                                <button
                                    onClick={() => setActiveTab('uniswap')}
                                    className={`${
                                        activeTab === 'uniswap'
                                            ? 'border-blue-500 text-gray-900'
                                            : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'
                                    } inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium`}
                                >
                                    Uniswap
                                </button>
                                <button
                                    onClick={() => setActiveTab('aave')}
                                    className={`${
                                        activeTab === 'aave'
                                            ? 'border-blue-500 text-gray-900'
                                            : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'
                                    } inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium`}
                                >
                                    Aave
                                </button>
                                <button
                                    onClick={() => setActiveTab('opensea')}
                                    className={`${
                                        activeTab === 'opensea'
                                            ? 'border-blue-500 text-gray-900'
                                            : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'
                                    } inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium`}
                                >
                                    OpenSea
                                </button>
                            </div>
                        </div>
                        <div className="flex items-center">
                            <div className="flex-shrink-0">
                                <button
                                    onClick={() => {/* TODO: Implement wallet disconnect */}}
                                    className="relative inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
                                >
                                    Disconnect Wallet
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </nav>

            {/* Main Content */}
            <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
                <div className="px-4 py-6 sm:px-0">
                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                        {/* Left Column - Wallet Overview and Recent Transactions */}
                        <div className="lg:col-span-1">
                            {renderWalletOverview()}
                            {renderRecentTransactions()}
                        </div>

                        {/* Right Column - DeFi Interface */}
                        <div className="lg:col-span-2">
                            <div className="bg-white rounded-lg shadow">
                                {renderDeFiInterface()}
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
};

export default Dashboard;