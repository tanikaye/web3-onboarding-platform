import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const WalletCreation = () => {
    const navigate = useNavigate();
    const [step, setStep] = useState(1);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [formData, setFormData] = useState({
        password: '',
        confirmPassword: '',
        recoveryEmail: '',
        mfaEnabled: false
    });

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const validatePassword = (password) => {
        const minLength = 8;
        const hasUpperCase = /[A-Z]/.test(password);
        const hasLowerCase = /[a-z]/.test(password);
        const hasNumbers = /\d/.test(password);
        const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(password);

        return password.length >= minLength &&
               hasUpperCase &&
               hasLowerCase &&
               hasNumbers &&
               hasSpecialChar;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);

        if (formData.password !== formData.confirmPassword) {
            setError('Passwords do not match');
            return;
        }

        if (!validatePassword(formData.password)) {
            setError('Password must be at least 8 characters long and contain uppercase, lowercase, numbers, and special characters');
            return;
        }

        try {
            setLoading(true);
            const response = await fetch('/api/v1/wallets/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    password: formData.password,
                    recoveryEmail: formData.recoveryEmail,
                    mfaEnabled: formData.mfaEnabled
                })
            });

            if (!response.ok) {
                throw new Error('Failed to create wallet');
            }

            const data = await response.json();

            // Store wallet address and other necessary data
            localStorage.setItem('walletAddress', data.walletAddress);

            // Navigate to wallet dashboard
            navigate('/dashboard');
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const renderStep = () => {
        switch (step) {
            case 1:
                return (
                    <div className="space-y-4">
                        <h3 className="text-lg font-semibold">Create Your Wallet</h3>
                        <p className="text-gray-600">
                            This will create a new Ethereum wallet secured with your password.
                            Make sure to save your recovery phrase in a safe place.
                        </p>
                        <button
                            onClick={() => setStep(2)}
                            className="w-full bg-blue-500 text-white py-2 px-4 rounded hover:bg-blue-600"
                        >
                            Continue
                        </button>
                    </div>
                );
            case 2:
                return (
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700">
                                Password
                            </label>
                            <input
                                type="password"
                                name="password"
                                value={formData.password}
                                onChange={handleInputChange}
                                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">
                                Confirm Password
                            </label>
                            <input
                                type="password"
                                name="confirmPassword"
                                value={formData.confirmPassword}
                                onChange={handleInputChange}
                                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">
                                Recovery Email
                            </label>
                            <input
                                type="email"
                                name="recoveryEmail"
                                value={formData.recoveryEmail}
                                onChange={handleInputChange}
                                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                                required
                            />
                        </div>
                        <div className="flex items-center">
                            <input
                                type="checkbox"
                                name="mfaEnabled"
                                checked={formData.mfaEnabled}
                                onChange={handleInputChange}
                                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                            />
                            <label className="ml-2 block text-sm text-gray-900">
                                Enable Two-Factor Authentication
                            </label>
                        </div>
                        {error && (
                            <div className="text-red-500 text-sm">{error}</div>
                        )}
                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full bg-blue-500 text-white py-2 px-4 rounded hover:bg-blue-600 disabled:bg-blue-300"
                        >
                            {loading ? 'Creating Wallet...' : 'Create Wallet'}
                        </button>
                    </form>
                );
            default:
                return null;
        }
    };

    return (
        <div className="max-w-md mx-auto p-6 bg-white rounded-lg shadow-lg">
            <h2 className="text-2xl font-bold mb-6">Create New Wallet</h2>
            {renderStep()}
        </div>
    );
};

export default WalletCreation;