package com.ltcode.capitalgainstaxcalculator.csv_creator;

import com.ltcode.capitalgainstaxcalculator.exception.OperationNotSupportedException;
import com.ltcode.capitalgainstaxcalculator.settings.Settings;
import com.ltcode.capitalgainstaxcalculator.transaction.Transaction;
import com.ltcode.capitalgainstaxcalculator.transaction.TransactionData;
import com.ltcode.capitalgainstaxcalculator.transaction.TransactionUtils;
import com.ltcode.capitalgainstaxcalculator.transaction.joined.JoinedTransaction;
import com.ltcode.capitalgainstaxcalculator.transaction_converter.TransactionValuesConverter;

public class CsvCreator {

    public static String get(Transaction transaction,
                             TransactionData[] order) {
        StringBuilder sb = new StringBuilder();
        for (TransactionData data : order) {
            sb.append(Settings.CSV_SEPARATOR);
            Object output;
            try {
                output = TransactionUtils.get(transaction, data);
            } catch (OperationNotSupportedException e) {
                output = "";
                // ignore
            }
            if (data == TransactionData.PRODUCT) {              // ',' might be in product name
                sb.append("\"");
                sb.append(output);
                sb.append("\"");
            } else {
                sb.append(output);
            }
        }
        return sb.substring(1);
    }

    public static String get(JoinedTransaction joinedTransaction,
                             TransactionData[] order,
                             TransactionValuesConverter valuesConverter) {
        final char SEPARATOR = ',';
        StringBuilder sb = new StringBuilder();

        /* Extended Data
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
        for (int i = 0; i < joinedTransaction.getNumOfBuyTransactions(); i++) {
            var buy = joinedTransaction.getBuyTransaction(i);
            extendedData[0] = valuesConverter.getRateAfterShiftUpTo7DaysPrevious(buy);
            extendedData[1] = "";
            extendedData[2] = "";
            extendedData[3] = valuesConverter.getValue(buy);
            extendedData[4] = "";
            extendedData[5] = valuesConverter.getCommission(buy);
            extendedData[6] = "";
            extendedData[7] = "";
            extendedData[8] = valuesConverter.getToCurrency();

            // append simple transaction csv
            sb.append(get(buy, order));

            // append extended data
            for (Object data : extendedData) {
                sb.append(SEPARATOR)
                        .append(data);
            }
            sb.append('\n');
        }

        // add sell transaction with total costs and profit
        var sell = joinedTransaction.getSellTransaction();
        extendedData[0] = valuesConverter.getRateAfterShiftUpTo7DaysPrevious(sell);
        extendedData[1] = sell.getDateTime().toLocalDate();
        extendedData[2] = valuesConverter.getValue(sell);
        extendedData[3] = "";
        extendedData[4] = valuesConverter.getTotalBuyValue(joinedTransaction);
        extendedData[5] = valuesConverter.getCommission(sell);
        extendedData[6] = valuesConverter.getTotalBuySellCommission(joinedTransaction);
        extendedData[7] = valuesConverter.getProfit(joinedTransaction);
        extendedData[8] = valuesConverter.getToCurrency();

        sb.append(get(sell, order));
        for (Object data : extendedData) {
            sb.append(SEPARATOR)
                    .append(data);
        }

        if (joinedTransaction.isSellTimeInvalid()) {
            sb.append(SEPARATOR)
                    .append("Invalid time");
        }

        return sb.toString();
    }
}
