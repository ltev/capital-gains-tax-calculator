package com.ltcode.capitalgainstaxcalculator.transaction.joined;

import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyRateExchanger;
import com.ltcode.capitalgainstaxcalculator.exception.*;
import com.ltcode.capitalgainstaxcalculator.transaction.BuySellTransaction;
import com.ltcode.capitalgainstaxcalculator.transaction.TransactionData;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;
import com.ltcode.capitalgainstaxcalculator.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Period;
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

    public BigDecimal getTotalBuyValue(CurrencyRateExchanger exchanger, Period dateShift, int precision, RoundingMode roundingMode) {
        BigDecimal value = BigDecimal.ZERO;
        for (BuySellTransaction buy : buyTransactionList) {
            value = value.add(buy.getValue(exchanger, dateShift, precision, roundingMode));
        }
        return value;
    }

    public BigDecimal getTotalBuySellCommission(CurrencyRateExchanger exchanger, Period dateShift, int precision, RoundingMode roundingMode) {
        BigDecimal commission = sell.getCommission(exchanger, dateShift, precision, roundingMode);
        for (BuySellTransaction buy : buyTransactionList) {
            commission = commission.add(buy.getCommission(exchanger, dateShift, precision, roundingMode));
        }
        return commission;
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

    public BigDecimal getProfit(CurrencyRateExchanger exchanger, Period dataShift, int precistion, RoundingMode roundingMode) {
        return sell.getValue(exchanger, dataShift, precistion, roundingMode)
                .subtract(getTotalBuyValue(exchanger, dataShift, precistion, roundingMode))
                .subtract(getTotalBuySellCommission(exchanger, dataShift, precistion, roundingMode));
    }

    public boolean isSellTimeInvalid() {
        return Duration.between(
                buyTransactionList.get(buyTransactionList.size() - 1).getDateTime(),
                sell.getDateTime()
        ).isNegative();
    }

    public String generateCsvLine() {
        final char SEPARATOR = ',';
        StringBuilder sb = new StringBuilder();
        for (var buy : buyTransactionList) {
            sb.append(buy.generateCsvLine())
                    .append("\n");
        }
        sb.append(sell.generateCsvLine());
        sb.append(SEPARATOR);

        // append total costs and profit
        sb.append(getTotalBuyValue())
                .append(SEPARATOR)
                .append(getProfit());
        return sb.toString();
    }

    /**
     * simpleTransactionOrder will be written first
     */
    public String generateCsvLine(TransactionData[] order,
                                  CurrencyRateExchanger exchanger,
                                  Period dataShift,
                                  int precision,
                                  RoundingMode roundingMode) {
        final char SEPARATOR = ',';
        StringBuilder sb = new StringBuilder();

        /*
                String[] extendedData = new String[] {
                0 "Exchange rate"
                1 "Sell Data"
                2 "Sell Value",
                3 "Buy Value",
                4 "Total Buy Value",
                5 "Commission"
                6 "Total commission"
                7 "Profit",
                8 "Currency"
        };
         */

        Object[] extendedData = new Object[9];

        // add buy transactions
        for (var buy : buyTransactionList) {
            extendedData[0] = exchanger.getRateUpTo7DaysPrevious(buy.getCurrency(), buy.getDateTime().toLocalDate().plus(dataShift));
            extendedData[1] = "";
            extendedData[2] = "";
            extendedData[3] = buy.getValue(exchanger, dataShift, precision, roundingMode);
            extendedData[4] = "";
            extendedData[5] = buy.getCommission(exchanger, dataShift, precision, roundingMode);
            extendedData[6] = "";
            extendedData[7] = "";
            extendedData[8] = exchanger.getToCurrency();

            sb.append(buy.generateCsvLine(order));
            for (Object data : extendedData) {
                sb.append(SEPARATOR)
                        .append(data);
            }
            sb.append('\n');
        }

        // add sell transaction with total costs and profit
        extendedData[0] = exchanger.getRateUpTo7DaysPrevious(sell.getCurrency(), sell.getDateTime().toLocalDate().plus(dataShift));
        extendedData[1] = sell.getDateTime().toLocalDate();
        extendedData[2] = sell.getValue(exchanger, dataShift, precision, roundingMode);
        extendedData[3] = "";
        extendedData[4] = getTotalBuyValue(exchanger, dataShift, precision, roundingMode);
        extendedData[5] = sell.getCommission(exchanger, dataShift, precision, roundingMode);
        extendedData[6] = getTotalBuySellCommission(exchanger, dataShift, precision, roundingMode);
        extendedData[7] = getProfit(exchanger, dataShift, precision, roundingMode);
        extendedData[8] = exchanger.getToCurrency();

        sb.append(sell.generateCsvLine(order));
        for (Object data : extendedData) {
            sb.append(SEPARATOR)
                    .append(data);
        }

        return sb.toString();
    }

    /**
     * Invalid - profit calculates in original values and they might be different
     */
    /*
    private String generateCsvLine2(TransactionData order[], CurrencyExchanger exchanger) {
        final char SEPARATOR = ',';
        StringBuilder sb = new StringBuilder();


        String[] extendedData = new String[] {
                "Sell Value",
                "Buy Value",
                "Total Buy Value",
                "Profit",
                "Currency",
                // THE VALUES UP should not be added cause they might not be in the same currency
                "Exchange Rate",
                "Sell Value",
                "Buy Value",
                "Total Buy Value",
                "Profit",
                "Currency"
        };
        Object[] extendedData = new Object[11];

        // add buy transactions
        for (var buy : buyTransactionList) {
            extendedData[0] = "";
            extendedData[1] = buy.getValue();
            extendedData[2] = "";
            extendedData[3] = "";
            extendedData[4] = buy.getCurrency();
            extendedData[5] = exchanger.getPrevUpTo7DaysRate(buy.getCurrency(), buy.getDateTime().toLocalDate());
            extendedData[6] = "";
            extendedData[7] = buy.getValue(exchanger);
            extendedData[8] = "";
            extendedData[9] = "";
            extendedData[10] = exchanger.getToCurrency();

            sb.append(buy.generateCsvLine(order));
            for (Object data : extendedData) {
                sb.append(SEPARATOR)
                        .append(data);
            }
            sb.append('\n');
        }

        // add sell transaction with total costs and profit
        extendedData[0] = sell.getValue();
        extendedData[1] = "";
        extendedData[2] = getTotalBuyValue();
        extendedData[3] = getProfit();
        extendedData[4] = sell.getCurrency();
        extendedData[5] = exchanger.getPrevUpTo7DaysRate(sell.getCurrency(), sell.getDateTime().toLocalDate());
        extendedData[6] = sell.getValue(exchanger);
        extendedData[7] = "";
        extendedData[8] = getTotalBuyValueFromTheDayBefore(exchanger);
        extendedData[9] = getProfit(exchanger);
        extendedData[10] = exchanger.getToCurrency();

        sb.append(sell.generateCsvLine(order));
        for (Object data : extendedData) {
            sb.append(SEPARATOR)
                    .append(data);
        }

        return sb.toString();
    }
*/
    public static void main(String[] args) {
        Object[] o = new Object[1];
        o[0] = new BigDecimal(2);
    }

    @Override
    public String toString() {
        return "TransactionGainsInfo{" +
                "sellTransaction=" + sell +
                ", buyTransactionList=" + buyTransactionList +
                '}';
    }


}
