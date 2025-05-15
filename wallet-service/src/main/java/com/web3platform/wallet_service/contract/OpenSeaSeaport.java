package com.web3platform.wallet_service.contract;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

public class OpenSeaSeaport extends Contract {
    public static final String BINARY = ""; // Add the contract binary here

    public static final String FUNC_FULFILL_ORDER = "fulfillOrder";
    public static final String FUNC_CANCEL_ORDER = "cancelOrder";
    public static final String FUNC_VALIDATE_ORDER = "validateOrder";

    protected OpenSeaSeaport(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, credentials, gasProvider);
    }

    protected OpenSeaSeaport(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, gasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> fulfillOrder(
            String order,
            String fulfillerConduitKey,
            BigInteger value) {
        final Function function = new Function(FUNC_FULFILL_ORDER,
                Arrays.asList(
                    new org.web3j.abi.datatypes.DynamicBytes(order.getBytes()),
                    new org.web3j.abi.datatypes.DynamicBytes(fulfillerConduitKey.getBytes()),
                    new Uint256(value)
                ),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> cancelOrder(
            String order) {
        final Function function = new Function(FUNC_CANCEL_ORDER,
                Arrays.asList(
                    new org.web3j.abi.datatypes.DynamicBytes(order.getBytes())
                ),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> validateOrder(
            String order) {
        final Function function = new Function(FUNC_VALIDATE_ORDER,
                Arrays.asList(
                    new org.web3j.abi.datatypes.DynamicBytes(order.getBytes())
                ),
                Collections.singletonList(new TypeReference<org.web3j.abi.datatypes.Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public static OpenSeaSeaport load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return new OpenSeaSeaport(contractAddress, web3j, credentials, gasProvider);
    }
}