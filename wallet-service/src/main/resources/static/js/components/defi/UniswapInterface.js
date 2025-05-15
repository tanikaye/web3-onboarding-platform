import React, { useState, useEffect } from 'react';
import BaseDefiInterface from './BaseDefiInterface';

const UniswapInterface = () => {
    const [formData, setFormData] = useState({
        fromToken: 'ETH',
        toToken: 'USDC',
        amount: '',
        slippage: '0.5'
    });

    const [tokenList, setTokenList] = useState([
        { symbol: 'ETH', name: 'Ethereum', address: '0x...' },
        { symbol: 'USDC', name: 'USD Coin', address: '0x...' },
        { symbol: 'DAI', name: 'Dai Stablecoin', address: '0x...' },
        { symbol: 'WETH', name: 'Wrapped Ether', address: '0x...' }
    ]);

    const [priceImpact, setPriceImpact] = useState(null);
    const [exchangeRate, setExchangeRate] = useState(null);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleTokenSwap = () => {
        setFormData(prev => ({
            ...prev,
            fromToken: prev.toToken,
            toToken: prev.fromToken
        }));
    };

    const getEstimatedGas = async (data) => {
        // TODO: Implement actual gas estimation
        return '0.005';
    };

    const getTokenBalance = async (data) => {
        // TODO: Implement actual balance checking
        return {
            [data.fromToken]: '1.5',
            [data.toToken]: '1000'
        };
    };

    const onTransactionSubmit = async (data) => {
        // TODO: Implement actual transaction submission
        return {
            id: '123',
            status: 'PENDING',
            transactionHash: '0x...'
        };
    };

    const renderForm = ({ loading, error, estimatedGas, tokenBalances, onSubmit }) => (
        <form onSubmit={(e) => {
            e.preventDefault();
            onSubmit(formData);
        }} className="space-y-6">
            {/* From Token */}
            <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">
                    From
                </label>
                <div className="flex space-x-2">
                    <select
                        name="fromToken"
                        value={formData.fromToken}
                        onChange={handleInputChange}
                        className="flex-1 rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                        disabled={loading}
                    >
                        {tokenList.map(token => (
                            <option key={token.symbol} value={token.symbol}>
                                {token.symbol}
                            </option>
                        ))}
                    </select>
                    <input
                        type="number"
                        name="amount"
                        value={formData.amount}
                        onChange={handleInputChange}
                        placeholder="0.0"
                        className="flex-1 rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                        disabled={loading}
                    />
                </div>
                {tokenBalances[formData.fromToken] && (
                    <p className="text-sm text-gray-500">
                        Balance: {tokenBalances[formData.fromToken]} {formData.fromToken}
                    </p>
                )}
            </div>

            {/* Swap Button */}
            <div className="flex justify-center">
                <button
                    type="button"
                    onClick={handleTokenSwap}
                    className="p-2 rounded-full hover:bg-gray-100"
                    disabled={loading}
                >
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4" />
                    </svg>
                </button>
            </div>

            {/* To Token */}
            <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">
                    To
                </label>
                <div className="flex space-x-2">
                    <select
                        name="toToken"
                        value={formData.toToken}
                        onChange={handleInputChange}
                        className="flex-1 rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                        disabled={loading}
                    >
                        {tokenList.map(token => (
                            <option key={token.symbol} value={token.symbol}>
                                {token.symbol}
                            </option>
                        ))}
                    </select>
                    <input
                        type="text"
                        value={exchangeRate ? `${exchangeRate} ${formData.toToken}` : '0.0'}
                        className="flex-1 rounded-md border-gray-300 bg-gray-50 shadow-sm"
                        disabled
                    />
                </div>
                {tokenBalances[formData.toToken] && (
                    <p className="text-sm text-gray-500">
                        Balance: {tokenBalances[formData.toToken]} {formData.toToken}
                    </p>
                )}
            </div>

            {/* Slippage Tolerance */}
            <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">
                    Slippage Tolerance
                </label>
                <div className="flex space-x-2">
                    <input
                        type="number"
                        name="slippage"
                        value={formData.slippage}
                        onChange={handleInputChange}
                        step="0.1"
                        min="0.1"
                        max="100"
                        className="flex-1 rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                        disabled={loading}
                    />
                    <span className="flex items-center text-gray-500">%</span>
                </div>
            </div>

            {/* Price Impact */}
            {priceImpact && (
                <div className="text-sm text-gray-600">
                    Price Impact: {priceImpact}%
                </div>
            )}

            {/* Submit Button */}
            <button
                type="submit"
                disabled={loading || !formData.amount}
                className="w-full bg-blue-500 text-white py-2 px-4 rounded hover:bg-blue-600 disabled:bg-blue-300"
            >
                {loading ? 'Swapping...' : 'Swap'}
            </button>
        </form>
    );

    return (
        <BaseDefiInterface
            protocolName="Uniswap"
            protocolIcon="/images/uniswap-logo.png"
            onTransactionSubmit={onTransactionSubmit}
            getEstimatedGas={getEstimatedGas}
            getTokenBalance={getTokenBalance}
        >
            {renderForm}
        </BaseDefiInterface>
    );
};

export default UniswapInterface;