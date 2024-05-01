package com.ltcode.capitalgainstaxcalculator.transaction;

import com.ltcode.capitalgainstaxcalculator.exception.CalculatorException;
import com.ltcode.capitalgainstaxcalculator.exception.InvalidDateOrderException;
import com.ltcode.capitalgainstaxcalculator.exception.OperationNotSupportedException;
import com.ltcode.capitalgainstaxcalculator.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.ltcode.capitalgainstaxcalculator.transaction.TransactionData.*;

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

    public static Object get(Transaction t, TransactionData data) {
        return switch (data) {
            case DATE_TIME -> t.getDateTime();
            case TICKER -> t.getTicker();
            case PRODUCT -> t.getProduct();
            case TYPE -> t.getType();
            case QUANTITY -> t.getQuantity();
            case PRICE_PER_SHARE -> t.getPricePerShare();
            case VALUE -> t.getValue();
            case CURRENCY -> t.getCurrency();
            case TAX_PAID -> t.getTaxPaid();
            case COMMISSION -> t.getCommission();
            default -> throw new OperationNotSupportedException("No valid data type.");
        };
    }
}
