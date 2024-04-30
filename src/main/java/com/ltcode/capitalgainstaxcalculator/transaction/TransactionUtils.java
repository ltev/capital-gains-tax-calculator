package com.ltcode.capitalgainstaxcalculator.transaction;

import com.ltcode.capitalgainstaxcalculator.exception.CalculatorException;
import com.ltcode.capitalgainstaxcalculator.exception.InvalidDateOrderException;
import com.ltcode.capitalgainstaxcalculator.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class TransactionUtils {


    /**
     * Create new partitioned transaction (less quantity) with keeping the variables originalQuantity and
     * originalCommission as it was original
     * Calculates new value
     */
    public static BuySellTransaction newPartitionedTransaction(BuySellTransaction originalTransaction,
                                                               BigDecimal newQuantity,
                                                               int precision,
                                                               RoundingMode roundingMode) {
        BigDecimal newValue = newQuantity
                .divide(originalTransaction.getQuantity(), 10, roundingMode)
                .multiply(originalTransaction.getValue())
                .setScale(precision, roundingMode);
        BigDecimal newCommission = newQuantity
                .divide(originalTransaction.getQuantity(), 10, roundingMode)
                .multiply(originalTransaction.getCommission())
                .setScale(precision, roundingMode);

        return new BuySellTransactionBuilder(originalTransaction)
                .setQuantity(newQuantity)
                .setValue(newValue)
                .setCommission(newCommission)
                .setOriginalQuantity(originalTransaction.getOriginalQuantity())
                .build();
    }


    /**
     * Check for chronological order
     */
    public static void checkTransactionsValidity(List<? extends Transaction> transactionList) {
        // check if dates are in chronological order
        int invalidIdx;
        if ((invalidIdx = Utils.isInChronologicalOrder(transactionList)) > 0) {
            throw new InvalidDateOrderException("Transactions are not in chronological order! transaction nr: " + invalidIdx + " " + transactionList.get(invalidIdx));
        }
    }
}
