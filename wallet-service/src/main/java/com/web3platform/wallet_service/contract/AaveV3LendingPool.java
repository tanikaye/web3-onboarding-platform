package com.web3platform.wallet_service.contract;

import com.web3platform.wallet_service.model.AaveUserAccountData;
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

public class AaveV3LendingPool extends Contract {
    public static final String BINARY = ""; // Add the contract binary here

    public static final String FUNC_SUPPLY = "supply";
    public static final String FUNC_WITHDRAW = "withdraw";
    public static final String FUNC_BORROW = "borrow";
    public static final String FUNC_REPAY = "repay";
    public static final String FUNC_GET_USER_ACCOUNT_DATA = "getUserAccountData";

    protected AaveV3LendingPool(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, credentials, gasProvider);
    }

    protected AaveV3LendingPool(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, gasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> supply(
            String asset,
            BigInteger amount,
            String onBehalfOf,
            BigInteger referralCode) {
        final Function function = new Function(FUNC_SUPPLY,
                Arrays.asList(new org.web3j.abi.datatypes.Address(160, asset),
                        new Uint256(amount),
                        new org.web3j.abi.datatypes.Address(160, onBehalfOf),
                        new Uint256(referralCode)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> withdraw(
            String asset,
            BigInteger amount,
            String to) {
        final Function function = new Function(FUNC_WITHDRAW,
                Arrays.asList(new org.web3j.abi.datatypes.Address(160, asset),
                        new Uint256(amount),
                        new org.web3j.abi.datatypes.Address(160, to)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> borrow(
            String asset,
            BigInteger amount,
            BigInteger interestRateMode,
            BigInteger referralCode,
            String onBehalfOf) {
        final Function function = new Function(FUNC_BORROW,
                Arrays.asList(new org.web3j.abi.datatypes.Address(160, asset),
                        new Uint256(amount),
                        new Uint256(interestRateMode),
                        new Uint256(referralCode),
                        new org.web3j.abi.datatypes.Address(160, onBehalfOf)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> repay(
            String asset,
            BigInteger amount,
            BigInteger interestRateMode,
            String onBehalfOf) {
        final Function function = new Function(FUNC_REPAY,
                Arrays.asList(new org.web3j.abi.datatypes.Address(160, asset),
                        new Uint256(amount),
                        new Uint256(interestRateMode),
                        new org.web3j.abi.datatypes.Address(160, onBehalfOf)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<AaveUserAccountData> getUserAccountData(String user) {
        final Function function = new Function(FUNC_GET_USER_ACCOUNT_DATA,
                Arrays.asList(new org.web3j.abi.datatypes.Address(160, user)),
                Collections.singletonList(new TypeReference<AaveUserAccountData>() {}));
        return executeRemoteCallSingleValueReturn(function, AaveUserAccountData.class);
    }

    public static AaveV3LendingPool load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return new AaveV3LendingPool(contractAddress, web3j, credentials, gasProvider);
    }
}