package com.web3platform.wallet_service.contract;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

public class UniswapV3Quoter extends Contract {
    public static final String BINARY = ""; // Add the contract binary here

    public static final String FUNC_QUOTEEXACTINPUTSINGLE = "quoteExactInputSingle";

    protected UniswapV3Quoter(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, credentials, gasProvider);
    }

    protected UniswapV3Quoter(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, gasProvider);
    }

    public RemoteFunctionCall<BigInteger> quoteExactInputSingle(
            String tokenIn,
            String tokenOut,
            BigInteger fee,
            BigInteger amountIn,
            BigInteger sqrtPriceLimitX96) {
        final Function function = new Function(FUNC_QUOTEEXACTINPUTSINGLE,
                Arrays.asList(new org.web3j.abi.datatypes.Address(160, tokenIn),
                        new org.web3j.abi.datatypes.Address(160, tokenOut),
                        new Uint256(fee),
                        new Uint256(amountIn),
                        new Uint256(sqrtPriceLimitX96)),
                Collections.singletonList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public static UniswapV3Quoter load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return new UniswapV3Quoter(contractAddress, web3j, credentials, gasProvider);
    }
}