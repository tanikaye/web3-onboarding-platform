import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useWebSocket } from '../hooks/useWebSocket';

const FiatOnboardingConfirmation = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [transaction, setTransaction] = useState(null);
    const [estimatedArrival, setEstimatedArrival] = useState(null);

    // Get transaction ID from URL or state
    const transactionId = location.state?.transactionId;

    // WebSocket connection for real-time updates
    const { lastMessage } = useWebSocket(`/ws/transactions/${transactionId}`);

    useEffect(() => {
        if (transactionId) {
            fetchTransactionDetails();
        } else {
            setError('No transaction ID provided');
            setLoading(false);
        }
    }, [transactionId]);

    useEffect(() => {
        if (lastMessage) {
            const update = JSON.parse(lastMessage.data);
            updateTransactionStatus(update);
        }
    }, [lastMessage]);

    const fetchTransactionDetails = async () => {
        try {
            setLoading(true);
            const response = await fetch(`/api/v1/transactions/${transactionId}`);
            if (!response.ok) throw new Error('Failed to fetch transaction details');

            const data = await response.json();
            setTransaction(data);
            calculateEstimatedArrival(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const updateTransactionStatus = (update) => {
        setTransaction(prev => ({
            ...prev,
            status: update.status,
            message: update.message
        }));
        calculateEstimatedArrival({ ...transaction, status: update.status });
    };

    const calculateEstimatedArrival = (tx) => {
        if (!tx) return;

        const now = new Date();
        let estimatedTime;

        switch (tx.status) {
            case 'PENDING':
                // Estimate 5-10 minutes for pending transactions
                estimatedTime = new Date(now.getTime() + 10 * 60000);
                break;
            case 'PROCESSING':
                // Estimate 2-5 minutes for processing transactions
                estimatedTime = new Date(now.getTime() + 5 * 60000);
                break;
            case 'CONFIRMED':
                estimatedTime = new Date(tx.updatedAt);
                break;
            default:
                estimatedTime = null;
        }

        setEstimatedArrival(estimatedTime);
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

    const formatTime = (date) => {
        if (!date) return 'N/A';
        return new Date(date).toLocaleString();
    };

    const getStatusMessage = (status) => {
        switch (status) {
            case 'PENDING':
                return 'Your transaction is pending confirmation';
            case 'PROCESSING':
                return 'Your transaction is being processed';
            case 'CONFIRMED':
                return 'Your transaction has been confirmed';
            case 'FAILED':
                return 'Your transaction has failed';
            default:
                return 'Unknown status';
        }
    };

    if (loading) return (
        <div className="flex items-center justify-center min-h-screen">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
        </div>
    );

    if (error) return (
        <div className="max-w-md mx-auto p-6 bg-white rounded-lg shadow-lg">
            <div className="text-red-500 text-center">{error}</div>
            <button
                onClick={() => navigate('/dashboard')}
                className="mt-4 w-full bg-blue-500 text-white py-2 px-4 rounded hover:bg-blue-600"
            >
                Return to Dashboard
            </button>
        </div>
    );

    return (
        <div className="max-w-md mx-auto p-6 bg-white rounded-lg shadow-lg">
            <h2 className="text-2xl font-bold mb-6">Transaction Confirmation</h2>

            <div className="space-y-6">
                {/* Status Section */}
                <div className="text-center">
                    <div className={`text-xl font-semibold ${getStatusColor(transaction.status)}`}>
                        {transaction.status}
                    </div>
                    <p className="text-gray-600 mt-2">
                        {getStatusMessage(transaction.status)}
                    </p>
                </div>

                {/* Transaction Details */}
                <div className="bg-gray-50 p-4 rounded-lg space-y-4">
                    <div>
                        <h3 className="text-sm font-medium text-gray-500">Amount</h3>
                        <p className="text-lg font-semibold">
                            {transaction.amount} {transaction.currency}
                        </p>
                    </div>

                    <div>
                        <h3 className="text-sm font-medium text-gray-500">Destination Address</h3>
                        <p className="text-sm font-mono break-all">
                            {transaction.walletAddress}
                        </p>
                    </div>

                    <div>
                        <h3 className="text-sm font-medium text-gray-500">Transaction ID</h3>
                        <p className="text-sm font-mono break-all">
                            {transaction.transactionHash}
                        </p>
                    </div>

                    <div>
                        <h3 className="text-sm font-medium text-gray-500">Created At</h3>
                        <p className="text-sm">{formatTime(transaction.createdAt)}</p>
                    </div>

                    {estimatedArrival && transaction.status !== 'CONFIRMED' && (
                        <div>
                            <h3 className="text-sm font-medium text-gray-500">Estimated Arrival</h3>
                            <p className="text-sm">{formatTime(estimatedArrival)}</p>
                        </div>
                    )}
                </div>

                {/* Action Buttons */}
                <div className="space-y-3">
                    <button
                        onClick={() => navigate('/dashboard')}
                        className="w-full bg-blue-500 text-white py-2 px-4 rounded hover:bg-blue-600"
                    >
                        Return to Dashboard
                    </button>

                    <button
                        onClick={() => window.open(`https://etherscan.io/tx/${transaction.transactionHash}`, '_blank')}
                        className="w-full bg-gray-100 text-gray-700 py-2 px-4 rounded hover:bg-gray-200"
                    >
                        View on Etherscan
                    </button>
                </div>

                {/* Additional Information */}
                {transaction.message && (
                    <div className="text-sm text-gray-600 mt-4">
                        <p>{transaction.message}</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default FiatOnboardingConfirmation;