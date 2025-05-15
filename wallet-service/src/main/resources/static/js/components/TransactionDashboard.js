import React, { useState, useEffect } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';

const TransactionDashboard = ({ userId }) => {
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // WebSocket connection for real-time updates
    const { lastMessage } = useWebSocket(`/ws/transactions/${userId}`);

    useEffect(() => {
        fetchTransactions();
    }, [userId]);

    useEffect(() => {
        if (lastMessage) {
            const update = JSON.parse(lastMessage.data);
            updateTransactionStatus(update);
        }
    }, [lastMessage]);

    const fetchTransactions = async () => {
        try {
            setLoading(true);
            const response = await fetch(`/api/v1/transactions/history/${userId}?network=ethereum&page=0&size=10`);
            if (!response.ok) throw new Error('Failed to fetch transactions');

            const data = await response.json();
            setTransactions(data.transactions);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const updateTransactionStatus = (update) => {
        setTransactions(prev => prev.map(tx =>
            tx.id === update.transactionId
                ? { ...tx, status: update.status, message: update.message }
                : tx
        ));
    };

    const getStatusColor = (status) => {
        switch (status) {
            case 'CONFIRMED': return 'text-green-500';
            case 'FAILED': return 'text-red-500';
            case 'PENDING': return 'text-yellow-500';
            default: return 'text-gray-500';
        }
    };

    if (loading) return <div className="p-4">Loading transactions...</div>;
    if (error) return <div className="p-4 text-red-500">Error: {error}</div>;

    return (
        <div className="p-4">
            <h2 className="text-2xl font-bold mb-4">Recent Transactions</h2>
            <div className="space-y-4">
                {transactions.map(tx => (
                    <div key={tx.id} className="bg-white rounded-lg shadow p-4">
                        <div className="flex justify-between items-start">
                            <div>
                                <h3 className="font-semibold">{tx.dappName}</h3>
                                <p className="text-sm text-gray-600">{tx.transactionType}</p>
                            </div>
                            <span className={`font-medium ${getStatusColor(tx.status)}`}>
                                {tx.status}
                            </span>
                        </div>

                        <div className="mt-2 text-sm">
                            <p>Hash: {tx.transactionHash}</p>
                            {tx.value && (
                                <p>Value: {tx.value} ETH</p>
                            )}
                            {tx.message && (
                                <p className="text-gray-600 mt-1">{tx.message}</p>
                            )}
                        </div>

                        <div className="mt-2 text-xs text-gray-500">
                            {new Date(tx.createdAt).toLocaleString()}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default TransactionDashboard;