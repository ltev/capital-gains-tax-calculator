package com.ltcode.capitalgainstaxcalculator.calculator;

import java.math.BigDecimal;

public interface SoldStockGainsInfo {

    int numOfBuyTransactions();

    BigDecimal sellValue();

    BigDecimal sellCommission();

    /*
     * Values for specific buy transaction
     */
    BigDecimal buyValue(int buyTransactionIdx);

    BigDecimal buyCommission(int buyTransactionIdx);

    BigDecimal profitWithoutCommission(int buyTransactionIdx);

    BigDecimal profitWithCommission(int buyTransactionIdx);

    /*
     * Total buy values
     */
    BigDecimal buyValue();

    BigDecimal buyCommission();

    BigDecimal profitWithoutCommission();

    BigDecimal profitWithCommission();
}
