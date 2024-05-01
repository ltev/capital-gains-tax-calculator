package com.ltcode.capitalgainstaxcalculator.transaction.joined;

import com.ltcode.capitalgainstaxcalculator.exception.*;
import com.ltcode.capitalgainstaxcalculator.transaction.BuySellTransaction;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;
import com.ltcode.capitalgainstaxcalculator.utils.Utils;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


/**
 * For every sell transaction, matching buy transaction / transactions
 * For some brokers the sell time can happen before the buy time but at the same day - 'isTimeMatching'
 */
public class JoinedTransaction {

    private final BuySellTransaction sell;
    private final List<BuySellTransaction> buyTransactionList;

    public JoinedTransaction(BuySellTransaction sellTransaction, List<BuySellTransaction> buyTransactionList) {
        this(sellTransaction, buyTransactionList, true);
    }

    public JoinedTransaction(BuySellTransaction sellTransaction,
                             List<BuySellTransaction> buyTransactionList,
                             boolean isTimeMatching) {
        this.sell = sellTransaction;
        this.buyTransactionList = new ArrayList<>(buyTransactionList);
        checkValidity(isTimeMatching);
    }

    /**
     * Check for ticker
     * Check for buy / sell transactions
     * Checks for equality between quantity of sell transaction and buy transactions, and for dates validity - buy
     * transactions must be in chronological order and must be before sell transaction occur
     */
    private void checkValidity(boolean isTimeMatching) {
        // check type
        if (sell.getType() != TransactionType.SELL) {
            throw new InvalidTypeException("Sell transaction must be of type SELL.");
        }
        // check type and ticker
        for (BuySellTransaction t : buyTransactionList) {
            if (t.getType() != TransactionType.BUY) {
                throw new InvalidTypeException("Buy transaction must be of type BUY.");
            }
            if (! t.getTicker().equals(sell.getTicker())) {
                throw new TransactionInfoException(String.format("Buy transaction must have the same ticker as sell transaction. %s %s", sell, t));
            }
        }
        // check buy chronological order
        int invalidIdx;
        if ((invalidIdx = Utils.isInChronologicalOrder(buyTransactionList)) > 0) {
            throw new InvalidDateOrderException("Transactions are not in chronological order! transaction nr: " + invalidIdx + " " + buyTransactionList.get(invalidIdx));
        }

        // check sell chronological order
        if (isTimeMatching
                && Duration.between(
                        buyTransactionList.get(buyTransactionList.size() - 1).getDateTime(),
                        sell.getDateTime()
            ).isNegative()) {
            throw new InvalidDateOrderException("Sell transaction can not happen before buy transaction " + this);
        }

        // check quantities
        BigDecimal totalBuyQuantity = new BigDecimal(0);
        for (BuySellTransaction t : buyTransactionList) {
            totalBuyQuantity = totalBuyQuantity.add(t.getQuantity());
        }
        if (totalBuyQuantity.compareTo(sell.getQuantity()) != 0) {
            throw new InvalidQuantityException(String.format("Quantities are not equal: %s, total buy quantity: %s.\n", sell, totalBuyQuantity));
        }
    }

    public BuySellTransaction getSellTransaction() {
        return sell;
    }

    public BuySellTransaction getBuyTransaction(int buyIdx) {
        return buyTransactionList.get(buyIdx);
    }

    public int getNumOfBuyTransactions() {
        return buyTransactionList.size();
    }

    /**
     * throws exception if not the same currency
     */
    public BigDecimal getTotalBuyValue() {
        for (int i = 1; i < buyTransactionList.size(); i++) {
            if (buyTransactionList.get(i-1).getCurrency() != buyTransactionList.get(i).getCurrency()) {
                throw new CurrencyExchangerException("Different currencies!");
            }
        }
        BigDecimal value = BigDecimal.ZERO;
        for (BuySellTransaction t : buyTransactionList) {
            value = value.add(t.getValue());
        }
        return value;
    }

    /**
     * throws exception if not the same currency
     */
    public BigDecimal getProfit() {
        for (var buy : buyTransactionList) {
            if (buy.getCurrency() != sell.getCurrency()) {
                throw new CurrencyExchangerException("Different currencies!");
            }
        }
        return sell.getValue().subtract(getTotalBuyValue());
    }

    public boolean isSellTimeInvalid() {
        return Duration.between(
                buyTransactionList.get(buyTransactionList.size() - 1).getDateTime(),
                sell.getDateTime()
        ).isNegative();
    }

    @Override
    public String toString() {
        return "TransactionGainsInfo{" +
                "sellTransaction=" + sell +
                ", buyTransactionList=" + buyTransactionList +
                '}';
    }
}
