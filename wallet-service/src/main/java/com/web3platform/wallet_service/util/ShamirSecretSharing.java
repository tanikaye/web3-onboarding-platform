package com.web3platform.wallet_service.util;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class ShamirSecretSharing {
    private static final SecureRandom random = new SecureRandom();
    private static final BigInteger PRIME = BigInteger.valueOf(2).pow(256).subtract(BigInteger.valueOf(189));

    public static class Share {
        private final int x;
        private final BigInteger y;

        public Share(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public BigInteger getY() {
            return y;
        }
    }

    public static List<Share> split(BigInteger secret, int totalShares, int requiredShares) {
        if (requiredShares > totalShares) {
            throw new IllegalArgumentException("Required shares cannot be greater than total shares");
        }

        // Generate random coefficients for the polynomial
        List<BigInteger> coefficients = new ArrayList<>();
        coefficients.add(secret); // a0 is the secret

        for (int i = 1; i < requiredShares; i++) {
            coefficients.add(generateRandomCoefficient());
        }

        // Generate shares
        List<Share> shares = new ArrayList<>();
        for (int i = 1; i <= totalShares; i++) {
            BigInteger y = evaluatePolynomial(coefficients, BigInteger.valueOf(i));
            shares.add(new Share(i, y));
        }

        return shares;
    }

    public static BigInteger recover(List<Share> shares) {
        if (shares.isEmpty()) {
            throw new IllegalArgumentException("No shares provided");
        }

        // Use Lagrange interpolation to recover the secret
        BigInteger secret = BigInteger.ZERO;
        for (Share share : shares) {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (Share otherShare : shares) {
                if (share.getX() != otherShare.getX()) {
                    numerator = numerator.multiply(BigInteger.valueOf(-otherShare.getX()));
                    denominator = denominator.multiply(BigInteger.valueOf(share.getX() - otherShare.getX()));
                }
            }

            BigInteger term = share.getY().multiply(numerator).multiply(denominator.modInverse(PRIME));
            secret = secret.add(term).mod(PRIME);
        }

        return secret;
    }

    private static BigInteger generateRandomCoefficient() {
        BigInteger coefficient;
        do {
            coefficient = new BigInteger(PRIME.bitLength(), random);
        } while (coefficient.compareTo(PRIME) >= 0);
        return coefficient;
    }

    private static BigInteger evaluatePolynomial(List<BigInteger> coefficients, BigInteger x) {
        BigInteger result = coefficients.get(0);
        for (int i = 1; i < coefficients.size(); i++) {
            result = result.add(coefficients.get(i).multiply(x.pow(i))).mod(PRIME);
        }
        return result;
    }
}