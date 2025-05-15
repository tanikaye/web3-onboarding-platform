import React, { useState, useEffect } from 'react';
import BaseDefiInterface from './BaseDefiInterface';

const OpenSeaInterface = () => {
    const [activeTab, setActiveTab] = useState('buy'); // 'buy', 'sell', or 'my-listings'
    const [formData, setFormData] = useState({
        collection: '',
        tokenId: '',
        price: '',
        paymentToken: 'ETH',
        duration: '7', // days
        isBatch: false
    });

    const [batchOrders, setBatchOrders] = useState([]);
    const [collections, setCollections] = useState([
        {
            name: 'Bored Ape Yacht Club',
            address: '0x...',
            image: '/images/bayc.png',
            floorPrice: '15.5',
            volume24h: '1200'
        },
        {
            name: 'CryptoPunks',
            address: '0x...',
            image: '/images/punks.png',
            floorPrice: '65.0',
            volume24h: '3500'
        }
    ]);

    const [paymentTokens, setPaymentTokens] = useState([
        { symbol: 'ETH', name: 'Ethereum', address: '0x...' },
        { symbol: 'WETH', name: 'Wrapped Ether', address: '0x...' },
        { symbol: 'USDC', name: 'USD Coin', address: '0x...' }
    ]);

    const [userListings, setUserListings] = useState([
        {
            id: '1',
            collection: 'Bored Ape Yacht Club',
            tokenId: '1234',
            price: '15.5',
            paymentToken: 'ETH',
            expiresAt: '2024-03-20T00:00:00Z'
        }
    ]);

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleAddToBatch = () => {
        if (formData.collection && formData.tokenId && formData.price) {
            setBatchOrders(prev => [...prev, { ...formData }]);
            setFormData(prev => ({
                ...prev,
                tokenId: '',
                price: ''
            }));
        }
    };

    const handleRemoveFromBatch = (index) => {
        setBatchOrders(prev => prev.filter((_, i) => i !== index));
    };

    const getEstimatedGas = async (data) => {
        // TODO: Implement actual gas estimation
        return data.isBatch ? '0.015' : '0.008';
    };

    const getTokenBalance = async (data) => {
        // TODO: Implement actual balance checking
        return {
            [data.paymentToken]: '1.5'
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

    const renderBatchOrders = () => (
        <div className="bg-gray-50 p-4 rounded-lg space-y-4">
            <h4 className="font-medium">Batch Orders</h4>
            {batchOrders.map((order, index) => (
                <div key={index} className="flex justify-between items-center bg-white p-3 rounded">
                    <div>
                        <p className="font-medium">{order.collection}</p>
                        <p className="text-sm text-gray-500">Token ID: {order.tokenId}</p>
                        <p className="text-sm text-gray-500">
                            Price: {order.price} {order.paymentToken}
                        </p>
                    </div>
                    <button
                        onClick={() => handleRemoveFromBatch(index)}
                        className="text-red-500 hover:text-red-600"
                    >
                        Remove
                    </button>
                </div>
            ))}
            {batchOrders.length > 0 && (
                <button
                    onClick={() => setFormData(prev => ({ ...prev, isBatch: true }))}
                    className="w-full bg-blue-500 text-white py-2 px-4 rounded hover:bg-blue-600"
                >
                    Process Batch ({batchOrders.length} items)
                </button>
            )}
        </div>
    );

    const renderMyListings = () => (
        <div className="space-y-4">
            {userListings.map(listing => (
                <div key={listing.id} className="bg-white p-4 rounded-lg shadow">
                    <div className="flex justify-between items-start">
                        <div>
                            <h4 className="font-medium">{listing.collection}</h4>
                            <p className="text-sm text-gray-500">Token ID: {listing.tokenId}</p>
                            <p className="text-sm text-gray-500">
                                Price: {listing.price} {listing.paymentToken}
                            </p>
                            <p className="text-sm text-gray-500">
                                Expires: {new Date(listing.expiresAt).toLocaleDateString()}
                            </p>
                        </div>
                        <div className="space-x-2">
                            <button className="text-blue-500 hover:text-blue-600">
                                Edit
                            </button>
                            <button className="text-red-500 hover:text-red-600">
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            ))}
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
                    onClick={() => setActiveTab('buy')}
                    className={`pb-2 px-4 ${activeTab === 'buy' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-gray-500'}`}
                >
                    Buy
                </button>
                <button
                    type="button"
                    onClick={() => setActiveTab('sell')}
                    className={`pb-2 px-4 ${activeTab === 'sell' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-gray-500'}`}
                >
                    Sell
                </button>
                <button
                    type="button"
                    onClick={() => setActiveTab('my-listings')}
                    className={`pb-2 px-4 ${activeTab === 'my-listings' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-gray-500'}`}
                >
                    My Listings
                </button>
            </div>

            {activeTab === 'my-listings' ? (
                renderMyListings()
            ) : (
                <>
                    {/* Collection Selection */}
                    <div className="space-y-2">
                        <label className="block text-sm font-medium text-gray-700">
                            Collection
                        </label>
                        <select
                            name="collection"
                            value={formData.collection}
                            onChange={handleInputChange}
                            className="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                            disabled={loading}
                        >
                            <option value="">Select a collection</option>
                            {collections.map(collection => (
                                <option key={collection.address} value={collection.name}>
                                    {collection.name}
                                </option>
                            ))}
                        </select>
                    </div>

                    {/* Token ID */}
                    <div className="space-y-2">
                        <label className="block text-sm font-medium text-gray-700">
                            Token ID
                        </label>
                        <input
                            type="text"
                            name="tokenId"
                            value={formData.tokenId}
                            onChange={handleInputChange}
                            placeholder="Enter token ID"
                            className="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                            disabled={loading}
                        />
                    </div>

                    {/* Price Input */}
                    <div className="space-y-2">
                        <label className="block text-sm font-medium text-gray-700">
                            Price
                        </label>
                        <div className="flex space-x-2">
                            <input
                                type="number"
                                name="price"
                                value={formData.price}
                                onChange={handleInputChange}
                                placeholder="0.0"
                                className="flex-1 rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                                disabled={loading}
                            />
                            <select
                                name="paymentToken"
                                value={formData.paymentToken}
                                onChange={handleInputChange}
                                className="w-32 rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                                disabled={loading}
                            >
                                {paymentTokens.map(token => (
                                    <option key={token.symbol} value={token.symbol}>
                                        {token.symbol}
                                    </option>
                                ))}
                            </select>
                        </div>
                        {tokenBalances[formData.paymentToken] && (
                            <p className="text-sm text-gray-500">
                                Balance: {tokenBalances[formData.paymentToken]} {formData.paymentToken}
                            </p>
                        )}
                    </div>

                    {/* Duration (for selling) */}
                    {activeTab === 'sell' && (
                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-gray-700">
                                Listing Duration (days)
                            </label>
                            <select
                                name="duration"
                                value={formData.duration}
                                onChange={handleInputChange}
                                className="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                                disabled={loading}
                            >
                                <option value="1">1 day</option>
                                <option value="3">3 days</option>
                                <option value="7">7 days</option>
                                <option value="30">30 days</option>
                            </select>
                        </div>
                    )}

                    {/* Batch Operations */}
                    {activeTab === 'buy' && (
                        <div className="space-y-4">
                            <div className="flex items-center">
                                <input
                                    type="checkbox"
                                    name="isBatch"
                                    checked={formData.isBatch}
                                    onChange={handleInputChange}
                                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                                    disabled={loading}
                                />
                                <label className="ml-2 block text-sm text-gray-900">
                                    Batch Operation
                                </label>
                            </div>
                            {formData.isBatch && (
                                <div className="space-y-2">
                                    <button
                                        type="button"
                                        onClick={handleAddToBatch}
                                        className="w-full bg-gray-100 text-gray-700 py-2 px-4 rounded hover:bg-gray-200"
                                        disabled={loading || !formData.collection || !formData.tokenId || !formData.price}
                                    >
                                        Add to Batch
                                    </button>
                                    {renderBatchOrders()}
                                </div>
                            )}
                        </div>
                    )}

                    {/* Collection Info */}
                    {formData.collection && collections.find(c => c.name === formData.collection) && (
                        <div className="bg-gray-50 p-4 rounded-lg space-y-2">
                            <h4 className="font-medium">Collection Information</h4>
                            <div className="grid grid-cols-2 gap-4 text-sm">
                                <div>
                                    <p className="text-gray-500">Floor Price</p>
                                    <p className="font-semibold">
                                        {collections.find(c => c.name === formData.collection).floorPrice} ETH
                                    </p>
                                </div>
                                <div>
                                    <p className="text-gray-500">24h Volume</p>
                                    <p className="font-semibold">
                                        {collections.find(c => c.name === formData.collection).volume24h} ETH
                                    </p>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Submit Button */}
                    <button
                        type="submit"
                        disabled={loading || !formData.collection || !formData.tokenId || !formData.price}
                        className="w-full bg-blue-500 text-white py-2 px-4 rounded hover:bg-blue-600 disabled:bg-blue-300"
                    >
                        {loading ? 'Processing...' : activeTab === 'buy' ? 'Buy NFT' : 'List NFT'}
                    </button>
                </>
            )}
        </form>
    );

    return (
        <BaseDefiInterface
            protocolName="OpenSea"
            protocolIcon="/images/opensea-logo.png"
            onTransactionSubmit={onTransactionSubmit}
            getEstimatedGas={getEstimatedGas}
            getTokenBalance={getTokenBalance}
        >
            {renderForm}
        </BaseDefiInterface>
    );
};

export default OpenSeaInterface;