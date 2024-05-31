package com.ltcode.capitalgainstaxcalculator.calculator;

import com.ltcode.capitalgainstaxcalculator.exception.InvalidQuantityException;
import com.ltcode.capitalgainstaxcalculator.exception.TransactionInfoException;
import com.ltcode.capitalgainstaxcalculator.transaction.*;
import com.ltcode.capitalgainstaxcalculator.transaction.joined.JoinedTransaction;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;
import com.ltcode.capitalgainstaxcalculator.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;

public class SellBuyJoiner {

    /**
     * Report with all problems
     */
    private String REPORT;
    private final int PRECISION;
    private final RoundingMode ROUNDING_MODE;
    /**
     * original transaction list
     */
    private final List<Transaction> transactionList;
    private List<BuySellTransaction> sellTransactionList;
    private Map<String, List<SplitTransaction>> splitTransactionsMap;
    private List<Transaction> transactionsThatCouldNotBeSold;

    private int numberOfSplits;

    /**
     * ticker to list with buy transaction
     */
    private Map<String, List<BuySellTransaction>> buyTransactionMap;

    public SellBuyJoiner(List<? extends Transaction> transactionsList,
                         int precision,
                         RoundingMode roundingMode) {
        this.transactionList = new ArrayList<>(transactionsList);
        this.PRECISION = precision;
        this.ROUNDING_MODE = roundingMode;
        TransactionUtils.checkTransactionsValidity(this.transactionList);
    }

    public List<JoinedTransaction> join() {
        StringBuilder infoBuilder = new StringBuilder();
        this.sellTransactionList = new ArrayList<>();
        this.buyTransactionMap = new HashMap<>();
        this.splitTransactionsMap = new HashMap<>();
        this.transactionsThatCouldNotBeSold = new ArrayList<>();
        fillTickerMapData();

        List<JoinedTransaction> result = new ArrayList<>(sellTransactionList.size());

        BigDecimal ZERO = new BigDecimal("0.0");
        for (int i = 0; i < sellTransactionList.size(); i++) {
            try {
                BuySellTransaction sellTransaction = sellTransactionList.get(i);
                List<BuySellTransaction> buyList = buyTransactionMap.get(sellTransaction.getTicker());
                List<BuySellTransaction> matchingBuyList = new ArrayList<>();

                // CHECK FOR SPLIT
                // in Revolut: (in Degiro no split exists - stock are sold and bought instead)
                // split transaction nr 1 gives us the old quantity that we had before the split
                // split transaction nr 2 give us the new quantity after split that we have now
                List<SplitTransaction> splitTransactionList = splitTransactionsMap.get(sellTransaction.getTicker());
                if (splitTransactionList != null && splitTransactionList.size() > 0 && splitTransactionList.get(0).getTicker().equals(sellTransaction.getTicker())) {
                    numberOfSplits++;
                    updateAffectedSplitTransactions(splitTransactionList.get(0), splitTransactionList.get(1));
                    splitTransactionList.remove(0);
                    splitTransactionList.remove(0);
                }

                boolean isSellWithBuyMatching = true;
                boolean isSellBuyTimeMatching = true;

                BigDecimal quantityLeft = sellTransaction.getQuantity();
                while (quantityLeft.compareTo(ZERO) > 0) {
                    if (buyList == null || buyList.size() == 0) {
                        transactionsThatCouldNotBeSold.add(sellTransaction);
                        isSellWithBuyMatching = false;
                        throw new InvalidQuantityException("Quantity of sold stock is greater than quantity of all buy stocks. "
                                + "\nLeft quantity: " + quantityLeft
                                + "\nSell stock: " + sellTransaction
                                + "\nBuy stocks: " + matchingBuyList);

                    }
                    BuySellTransaction buyTransaction = buyList.get(0);

                    Duration durationDiff = Duration.between(buyTransaction.getDateTime(), sellTransaction.getDateTime());
                    if (durationDiff.isZero() || durationDiff.isNegative()) {
                        // in Degiro broker this can happen
                        // throw new InvalidDateOrderException("Sell transaction can not happen before buy transaction." + sellTransaction);
                        // thats why allow it to happen when the buy / sell difference is less than one day
                        if (durationDiff.getSeconds() < Duration.ofDays(1).getSeconds()) {
                            isSellBuyTimeMatching = false;
                        }
                    }

                    int compare = quantityLeft.compareTo(buyTransaction.getQuantity());
                    if (compare >= 0) {                         // quantityLeft is bigger or equal than transaction's one
                        quantityLeft = quantityLeft.subtract(buyTransaction.getQuantity());
                        buyList.remove(0);
                        matchingBuyList.add(buyTransaction);
                    } else {
                        // quantityLeft is smaller
                        BigDecimal diff = buyTransaction.getQuantity().subtract(quantityLeft);
                        BuySellTransaction toAdd = TransactionUtils.newPartitionedTransaction(buyTransaction, quantityLeft, PRECISION, ROUNDING_MODE);
                        BuySellTransaction toLeave = TransactionUtils.newPartitionedTransaction(buyTransaction, diff, PRECISION, ROUNDING_MODE);
                        quantityLeft = quantityLeft.subtract(toAdd.getQuantity());
                        if (quantityLeft.compareTo(ZERO) != 0) {
                            throw new TransactionInfoException("Should be zero. Is: " + quantityLeft);
                        }
                        buyList.set(0, toLeave);
                        matchingBuyList.add(toAdd);
                        break;
                    }
                }
                if (isSellWithBuyMatching) {
                    result.add(new JoinedTransaction(sellTransaction, matchingBuyList, isSellBuyTimeMatching));
                }
            } catch (InvalidQuantityException e) {
                infoBuilder.append("PROBLEM: ")
                        .append(e.getMessage())
                        .append("\n");
            }
        }

        // clear the map from empty transactions
        for (String ticker : buyTransactionMap.keySet().toArray(new String[0])) {
            if (buyTransactionMap.get(ticker).size() == 0) {
                buyTransactionMap.remove(ticker);
            }
        }

        REPORT = infoBuilder.toString();
        return result;
    }

    public String getReport() {
        return REPORT;
    }
    public String[] getTickersLeft() {
        return buyTransactionMap.keySet().toArray(new String[0]);
    }
    public BigDecimal getQuantityLeft(String ticker) {
        List<BuySellTransaction> list = buyTransactionMap.get(ticker);
        if (list == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal left = BigDecimal.ZERO;
        for (BuySellTransaction t : list) {
            left = left.add(t.getQuantity());
        }
        return left;
    }

    public List<LeftStockInfo> getLeftStocksList() {
        List<LeftStockInfo> list = new ArrayList<>();
        for (var ticker : getTickersLeft()) {
            // quantity
            var quantity = getQuantityLeft(ticker);

            // product name
            String product = null;
            if (getLeftTransaction(ticker).size() > 0) {
                product = getLeftTransaction(ticker).get(0).getProduct();
            } else {
                for (var sell : sellTransactionList) {
                    if (sell.getTicker().equals(ticker)) {
                        product = sell.getProduct();
                        break;
                    }
                }
            }
            list.add(new LeftStockInfo(ticker, product, quantity));
        }
        list.sort(Comparator.comparing(LeftStockInfo::getProduct));
        return list;
    }

    /**
     * Transaction bought but still not sold
     */
    public List<BuySellTransaction> getLeftTransaction(String ticker) {
        var list = buyTransactionMap.get(ticker);
        return list == null ? new ArrayList<>() : list;
    }

    public int getNumberOfSplits() {
        return numberOfSplits;
    }

    // == PRIVATE HELPER METHODS ==

    private void fillTickerMapData() {
        for (Transaction t : transactionList) {
            if (t.getType() == TransactionType.BUY) {
                BuySellTransaction buyTransaction = (BuySellTransaction) t;
                buyTransactionMap.putIfAbsent(buyTransaction.getTicker(), new ArrayList<>());
                buyTransactionMap.get(buyTransaction.getTicker()).add(buyTransaction);
            } else if (t.getType() == TransactionType.SELL) {
                BuySellTransaction sellTransaction = (BuySellTransaction) t;
                sellTransactionList.add(sellTransaction);
            } else if (t.getType() == TransactionType.STOCK_SPLIT) {
                splitTransactionsMap.putIfAbsent(t.getTicker(), new ArrayList<>());
                splitTransactionsMap.get(t.getTicker()).add((SplitTransaction) t);
            }
        }
    }

    private void updateAffectedSplitTransactions(SplitTransaction oldData, SplitTransaction newData) {
        if (!oldData.getTicker().equals(newData.getTicker())) {
            throw new TransactionInfoException("Different split tickers: " + oldData.getTicker() + " / " + newData.getTicker());
        }
        if (! Utils.isNegative(oldData.getQuantity())
                || ! Utils.isPositive(newData.getQuantity())
                || Utils.isZero(oldData.getQuantity())
                || Utils.isZero(newData.getQuantity())) {
            throw new InvalidQuantityException(String.format(
                    "First transaction must have negative quantity and second positive. %s %s", oldData, newData));
        }
        BigDecimal fromQuantity = oldData.getQuantity().negate();
        BigDecimal toQuantity = newData.getQuantity();
        BigDecimal splitAmount = toQuantity.divide(fromQuantity);

        var buyList = buyTransactionMap.get(newData.getTicker());
        for (int i = 0; i < buyList.size(); i++) {
            BuySellTransaction buy = buyList.get(i);
            if (Duration.between(buy.getDateTime(), oldData.getDateTime()).isNegative()) {
                break;
            }
            var newQuantity = buy.getQuantity().multiply(splitAmount);
            BuySellTransaction updated = new BuySellTransactionBuilder(buy)
                    .setQuantity(newQuantity)
                    .setOriginalQuantity(newQuantity)
                    .setPricePerShare(buy.pricePerShare.divide(splitAmount))
                    .build();
            buyList.set(i, updated);
        }
    }

    @Override
    public String toString() {
        return "calculator_GainCalculator{" +
                "sellTransactionList=" + sellTransactionList +
                ", buyTransactionMap=" + buyTransactionMap +
                '}';
    }


}
