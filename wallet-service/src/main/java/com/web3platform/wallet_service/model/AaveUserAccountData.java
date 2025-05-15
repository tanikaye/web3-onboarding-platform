package com.web3platform.wallet_service.model;

import lombok.Data;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.generated.Uint256;
import java.math.BigInteger;

@Data
public class AaveUserAccountData extends StaticStruct {
    public final Uint256 totalCollateralBase;
    public final Uint256 totalDebtBase;
    public final Uint256 availableBorrowsBase;
    public final Uint256 currentLiquidationThreshold;
    public final Uint256 ltv;
    public final Uint256 healthFactor;

    public AaveUserAccountData(
            BigInteger totalCollateralBase,
            BigInteger totalDebtBase,
            BigInteger availableBorrowsBase,
            BigInteger currentLiquidationThreshold,
            BigInteger ltv,
            BigInteger healthFactor) {
        super(
            new Uint256(totalCollateralBase),
            new Uint256(totalDebtBase),
            new Uint256(availableBorrowsBase),
            new Uint256(currentLiquidationThreshold),
            new Uint256(ltv),
            new Uint256(healthFactor)
        );
        this.totalCollateralBase = new Uint256(totalCollateralBase);
        this.totalDebtBase = new Uint256(totalDebtBase);
        this.availableBorrowsBase = new Uint256(availableBorrowsBase);
        this.currentLiquidationThreshold = new Uint256(currentLiquidationThreshold);
        this.ltv = new Uint256(ltv);
        this.healthFactor = new Uint256(healthFactor);
    }
}