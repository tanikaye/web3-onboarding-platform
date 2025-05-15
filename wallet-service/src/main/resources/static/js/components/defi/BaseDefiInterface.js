import React, { useState, useEffect } from 'react';
import { useWebSocket } from '../../hooks/useWebSocket';

const BaseDefiInterface = ({
    protocolName,
    protocolIcon,
    onTransactionSubmit,
    getTransactionStatus,
    getEstimatedGas,
    getTokenBalance,
    children
}) => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [transaction, setTransaction] = useState(null);
    const [estimatedGas, setEstimatedGas] = useState(null);
    const [tokenBalances, setTokenBalances] = useState({});
    const [isConfirming, setIsConfirming] = useState(false);

    // WebSocket connection for real-time updates
    const { lastMessage } = useWebSocket(transaction ? `/ws/transactions/${transaction.id}` : null);

    useEffect(() => {
        if (lastMessage) {
            const update = JSON.parse(lastMessage.data);
            updateTransactionStatus(update);
        }
    }, [lastMessage]);

    const updateTransactionStatus = (update) => {
        setTransaction(prev => ({
            ...prev,
            status: update.status,
            message: update.message
        }));

        if (update.status === 'CONFIRMED' || update.status === 'FAILED') {
            setIsConfirming(false);
        }
    };

    const handleSubmit = async (formData) => {
        try {
            setLoading(true);
            setError(null);

            // Get gas estimate
            const gasEstimate = await getEstimatedGas(formData);
            setEstimatedGas(gasEstimate);

            // Get token balances
            const balances = await getTokenBalance(formData);
            setTokenBalances(balances);

            // Submit transaction
            const tx = await onTransactionSubmit(formData);
            setTransaction(tx);
            setIsConfirming(true);

        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const getStatusColor = (status) => {
        switch (status) {
            case 'CONFIRMED': return 'text-green-500';
            case 'FAILED': return 'text-red-500';
            case 'PENDING': return 'text-yellow-500';
            case 'PROCESSING': return 'text-blue-500';
            default: return 'text-gray-500';
        }
    };

    return (
        <div className="max-w-2xl mx-auto p-6 bg-white rounded-lg shadow-lg">
            {/* Protocol Header */}
            <div className="flex items-center space-x-4 mb-6">
                <img src={protocolIcon} alt={protocolName} className="w-12 h-12" />
                <h2 className="text-2xl font-bold">{protocolName}</h2>
            </div>

            {/* Main Content */}
            <div className="space-y-6">
                {/* Protocol-specific form */}
                {children({
                    loading,
                    error,
                    estimatedGas,
                    tokenBalances,
                    onSubmit: handleSubmit
                })}

                {/* Transaction Status */}
                {transaction && (
                    <div className="mt-6 p-4 bg-gray-50 rounded-lg">
                        <div className="flex justify-between items-center">
                            <div>
                                <span className={`font-semibold ${getStatusColor(transaction.status)}`}>
                                    {transaction.status}
                                </span>
                                {transaction.message && (
                                    <p className="text-sm text-gray-600 mt-1">{transaction.message}</p>
                                )}
                            </div>
                            {transaction.transactionHash && (
                                <button
                                    onClick={() => window.open(`https://etherscan.io/tx/${transaction.transactionHash}`, '_blank')}
                                    className="text-sm text-blue-500 hover:text-blue-600"
                                >
                                    View on Etherscan
                                </button>
                            )}
                        </div>
                    </div>
                )}

                {/* Loading State */}
                {loading && (
                    <div className="flex items-center justify-center py-4">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
                    </div>
                )}

                {/* Error State */}
                {error && (
                    <div className="p-4 bg-red-50 text-red-500 rounded-lg">
                        {error}
                    </div>
                )}

                {/* Confirmation Modal */}
                {isConfirming && (
                    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
                        <div className="bg-white p-6 rounded-lg max-w-md w-full">
                            <h3 className="text-lg font-semibold mb-4">Confirm Transaction</h3>
                            <div className="space-y-4">
                                <p className="text-sm text-gray-600">
                                    Please confirm this transaction in your wallet.
                                </p>
                                {estimatedGas && (
                                    <div className="text-sm">
                                        <span className="font-medium">Estimated Gas:</span>
                                        <span className="ml-2">{estimatedGas} ETH</span>
                                    </div>
                                )}
                                <div className="flex justify-end space-x-3">
                                    <button
                                        onClick={() => setIsConfirming(false)}
                                        className="px-4 py-2 text-gray-600 hover:text-gray-800"
                                    >
                                        Cancel
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default BaseDefiInterface;