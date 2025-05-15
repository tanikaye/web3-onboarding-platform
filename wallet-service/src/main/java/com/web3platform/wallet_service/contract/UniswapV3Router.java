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

public class UniswapV3Router extends Contract {
    public static final String BINARY = ""; // Add the contract binary here

    public static final String FUNC_EXACTINPUTSINGLE = "exactInputSingle";
    public static final String FUNC_EXACTINPUT = "exactInput";

    protected UniswapV3Router(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, credentials, gasProvider);
    }

    protected UniswapV3Router(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, gasProvider);
    }

    public RemoteFunctionCall<BigInteger> exactInputSingle(
            String tokenIn,
            String tokenOut,
            BigInteger fee,
            String recipient,
            BigInteger deadline,
            BigInteger amountIn,
            BigInteger amountOutMinimum,
            BigInteger sqrtPriceLimitX96) {
        final Function function = new Function(FUNC_EXACTINPUTSINGLE,
                Arrays.asList(new org.web3j.abi.datatypes.Address(160, tokenIn),
                        new org.web3j.abi.datatypes.Address(160, tokenOut),
                        new Uint256(fee),
                        new org.web3j.abi.datatypes.Address(160, recipient),
                        new Uint256(deadline),
                        new Uint256(amountIn),
                        new Uint256(amountOutMinimum),
                        new Uint256(sqrtPriceLimitX96)),
                Collections.singletonList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<BigInteger> exactInput(
            byte[] path,
            String recipient,
            BigInteger deadline,
            BigInteger amountIn,
            BigInteger amountOutMinimum) {
        final Function function = new Function(FUNC_EXACTINPUT,
                Arrays.asList(new org.web3j.abi.datatypes.DynamicBytes(path),
                        new org.web3j.abi.datatypes.Address(160, recipient),
                        new Uint256(deadline),
                        new Uint256(amountIn),
                        new Uint256(amountOutMinimum)),
                Collections.singletonList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public static UniswapV3Router load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return new UniswapV3Router(contractAddress, web3j, credentials, gasProvider);
    }
}