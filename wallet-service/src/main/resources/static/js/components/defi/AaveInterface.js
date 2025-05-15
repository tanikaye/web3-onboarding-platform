import React, { useState, useEffect } from 'react';
import BaseDefiInterface from './BaseDefiInterface';

const AaveInterface = () => {
    const [activeTab, setActiveTab] = useState('lend'); // 'lend' or 'borrow'
    const [formData, setFormData] = useState({
        asset: 'ETH',
        amount: '',
        interestRateMode: 'stable', // 'stable' or 'variable'
        useAsCollateral: true
    });

    const [marketData, setMarketData] = useState({
        assets: [
            {
                symbol: 'ETH',
                name: 'Ethereum',
                address: '0x...',
                lendingAPY: '3.2',
                borrowingAPY: {
                    stable: '4.5',
                    variable: '3.8'
                },
                ltv: '80',
                liquidationThreshold: '82.5',
                liquidationPenalty: '5',
                availableLiquidity: '1000'
            },
            {
                symbol: 'USDC',
                name: 'USD Coin',
                address: '0x...',
                lendingAPY: '2.8',
                borrowingAPY: {
                    stable: '4.2',
                    variable: '3.5'
                },
                ltv: '85',
                liquidationThreshold: '87.5',
                liquidationPenalty: '5',
                availableLiquidity: '5000000'
            }
        ]
    });

    const [userPosition, setUserPosition] = useState({
        healthFactor: '1.5',
        totalCollateral: '2.5',
        totalBorrowed: '1.0',
        availableToBorrow: '1.5',
        positions: [
            {
                asset: 'ETH',
                supplied: '2.0',
                borrowed: '0.0',
                collateral: true
            },
            {
                asset: 'USDC',
                supplied: '0.0',
                borrowed: '1000',
                collateral: false
            }
        ]
    });

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const getEstimatedGas = async (data) => {
        // TODO: Implement actual gas estimation
        return '0.008';
    };

    const getTokenBalance = async (data) => {
        // TODO: Implement actual balance checking
        return {
            [data.asset]: '1.5'
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

    const getHealthFactorColor = (factor) => {
        const numFactor = parseFloat(factor);
        if (numFactor >= 1.5) return 'text-green-500';
        if (numFactor >= 1.1) return 'text-yellow-500';
        return 'text-red-500';
    };

    const renderUserPosition = () => (
        <div className="bg-gray-50 p-4 rounded-lg space-y-4 mb-6">
            <h3 className="font-semibold">Your Position</h3>

            <div className="grid grid-cols-2 gap-4">
                <div>
                    <p className="text-sm text-gray-500">Health Factor</p>
                    <p className={`font-semibold ${getHealthFactorColor(userPosition.healthFactor)}`}>
                        {userPosition.healthFactor}
                    </p>
                </div>
                <div>
                    <p className="text-sm text-gray-500">Total Collateral</p>
                    <p className="font-semibold">{userPosition.totalCollateral} ETH</p>
                </div>
                <div>
                    <p className="text-sm text-gray-500">Total Borrowed</p>
                    <p className="font-semibold">{userPosition.totalBorrowed} ETH</p>
                </div>
                <div>
                    <p className="text-sm text-gray-500">Available to Borrow</p>
                    <p className="font-semibold">{userPosition.availableToBorrow} ETH</p>
                </div>
            </div>

            <div className="mt-4">
                <h4 className="text-sm font-medium mb-2">Your Positions</h4>
                <div className="space-y-2">
                    {userPosition.positions.map((position, index) => (
                        <div key={index} className="flex justify-between items-center text-sm">
                            <span>{position.asset}</span>
                            <div className="space-x-4">
                                {position.supplied > 0 && (
                                    <span>Supplied: {position.supplied}</span>
                                )}
                                {position.borrowed > 0 && (
                                    <span>Borrowed: {position.borrowed}</span>
                                )}
                                {position.collateral && (
                                    <span className="text-green-500">Collateral</span>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );

    const renderForm = ({ loading, error, estimatedGas, tokenBalances, onSubmit }) => (
        <form onSubmit={(e) => {
            e.preventDefault();
            onSubmit(formData);
        }} className="space-y-6">
            {/* Tabs */}
            <div className="flex space-x-4 border-b">
                <button
                    type="button"
                    onClick={() => setActiveTab('lend')}
                    className={`pb-2 px-4 ${activeTab === 'lend' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-gray-500'}`}
                >
                    Lend
                </button>
                <button
                    type="button"
                    onClick={() => setActiveTab('borrow')}
                    className={`pb-2 px-4 ${activeTab === 'borrow' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-gray-500'}`}
                >
                    Borrow
                </button>
            </div>

            {/* Asset Selection */}
            <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">
                    Asset
                </label>
                <select
                    name="asset"
                    value={formData.asset}
                    onChange={handleInputChange}
                    className="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                    disabled={loading}
                >
                    {marketData.assets.map(asset => (
                        <option key={asset.symbol} value={asset.symbol}>
                            {asset.symbol} - {asset.name}
                        </option>
                    ))}
                </select>
            </div>

            {/* Amount Input */}
            <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">
                    Amount
                </label>
                <div className="flex space-x-2">
                    <input
                        type="number"
                        name="amount"
                        value={formData.amount}
                        onChange={handleInputChange}
                        placeholder="0.0"
                        className="flex-1 rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                        disabled={loading}
                    />
                    <button
                        type="button"
                        onClick={() => setFormData(prev => ({ ...prev, amount: tokenBalances[formData.asset] }))}
                        className="px-3 py-2 text-sm text-blue-500 hover:text-blue-600"
                        disabled={loading}
                    >
                        Max
                    </button>
                </div>
                {tokenBalances[formData.asset] && (
                    <p className="text-sm text-gray-500">
                        Balance: {tokenBalances[formData.asset]} {formData.asset}
                    </p>
                )}
            </div>

            {/* Borrowing Options */}
            {activeTab === 'borrow' && (
                <div className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700">
                            Interest Rate Mode
                        </label>
                        <div className="mt-2 space-x-4">
                            <label className="inline-flex items-center">
                                <input
                                    type="radio"
                                    name="interestRateMode"
                                    value="stable"
                                    checked={formData.interestRateMode === 'stable'}
                                    onChange={handleInputChange}
                                    className="form-radio text-blue-500"
                                    disabled={loading}
                                />
                                <span className="ml-2">Stable</span>
                            </label>
                            <label className="inline-flex items-center">
                                <input
                                    type="radio"
                                    name="interestRateMode"
                                    value="variable"
                                    checked={formData.interestRateMode === 'variable'}
                                    onChange={handleInputChange}
                                    className="form-radio text-blue-500"
                                    disabled={loading}
                                />
                                <span className="ml-2">Variable</span>
                            </label>
                        </div>
                    </div>

                    <div className="flex items-center">
                        <input
                            type="checkbox"
                            name="useAsCollateral"
                            checked={formData.useAsCollateral}
                            onChange={handleInputChange}
                            className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                            disabled={loading}
                        />
                        <label className="ml-2 block text-sm text-gray-900">
                            Use as collateral
                        </label>
                    </div>
                </div>
            )}

            {/* Market Data */}
            <div className="bg-gray-50 p-4 rounded-lg space-y-2">
                <h4 className="font-medium">Market Information</h4>
                {marketData.assets.find(a => a.symbol === formData.asset) && (
                    <div className="grid grid-cols-2 gap-4 text-sm">
                        <div>
                            <p className="text-gray-500">Lending APY</p>
                            <p className="font-semibold">
                                {marketData.assets.find(a => a.symbol === formData.asset).lendingAPY}%
                            </p>
                        </div>
                        {activeTab === 'borrow' && (
                            <div>
                                <p className="text-gray-500">Borrowing APY</p>
                                <p className="font-semibold">
                                    {marketData.assets.find(a => a.symbol === formData.asset)
                                        .borrowingAPY[formData.interestRateMode]}%
                                </p>
                            </div>
                        )}
                        <div>
                            <p className="text-gray-500">LTV</p>
                            <p className="font-semibold">
                                {marketData.assets.find(a => a.symbol === formData.asset).ltv}%
                            </p>
                        </div>
                        <div>
                            <p className="text-gray-500">Liquidation Threshold</p>
                            <p className="font-semibold">
                                {marketData.assets.find(a => a.symbol === formData.asset).liquidationThreshold}%
                            </p>
                        </div>
                    </div>
                )}
            </div>

            {/* Submit Button */}
            <button
                type="submit"
                disabled={loading || !formData.amount}
                className="w-full bg-blue-500 text-white py-2 px-4 rounded hover:bg-blue-600 disabled:bg-blue-300"
            >
                {loading ? 'Processing...' : activeTab === 'lend' ? 'Supply' : 'Borrow'}
            </button>
        </form>
    );

    return (
        <BaseDefiInterface
            protocolName="Aave"
            protocolIcon="/images/aave-logo.png"
            onTransactionSubmit={onTransactionSubmit}
            getEstimatedGas={getEstimatedGas}
            getTokenBalance={getTokenBalance}
        >
            {({ loading, error, estimatedGas, tokenBalances, onSubmit }) => (
                <>
                    {renderUserPosition()}
                    {renderForm({ loading, error, estimatedGas, tokenBalances, onSubmit })}
                </>
            )}
        </BaseDefiInterface>
    );
};

export default AaveInterface;