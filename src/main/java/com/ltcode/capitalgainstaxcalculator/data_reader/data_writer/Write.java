package com.ltcode.capitalgainstaxcalculator.data_reader.data_writer;

import com.ltcode.capitalgainstaxcalculator.calculator.LeftStockInfo;
import com.ltcode.capitalgainstaxcalculator.calculator.StockGainsInfo;
import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyExchanger;
import com.ltcode.capitalgainstaxcalculator.settings.Settings;
import com.ltcode.capitalgainstaxcalculator.transaction.DividendTransaction;
import com.ltcode.capitalgainstaxcalculator.transaction.Transaction;
import com.ltcode.capitalgainstaxcalculator.transaction.TransactionData;
import com.ltcode.capitalgainstaxcalculator.transaction.joined.JoinedTransactions;

import static com.ltcode.capitalgainstaxcalculator.settings.Settings.CSV_SEPARATOR;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.Period;
import java.util.*;

public class Write {

    public static void generateTransactionsCsvFile(List<? extends Transaction> transactions,
                                                   String fileName) {
        generateTransactionsCsvFile(transactions, Settings.CSV_TRANSACTION_WRITE_ORDER, fileName);
    }

    public static void generateTransactionsCsvFile(List<? extends Transaction> transactions,
                                                   TransactionData[] order,
                                                   String fileName) {
        fileName = fileName + Settings.CSV_FILE_TYPE;
        Writer w = null;
        try {
            w = Files.newBufferedWriter(Settings.GENERATED_DATA_PATH.resolve(fileName));

            // header
            StringBuilder header = new StringBuilder();
            for (var data : order) {
                header.append(CSV_SEPARATOR)
                        .append(data);
            }
            w.append(header.substring(1))
                    .append("\n");

            // transaction
            for (Transaction t : transactions) {
                w.append(t.generateCsvLine(order));
                w.append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (w != null) {
                    w.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void generateJoinedTransactionsCsvFile(List<JoinedTransactions> joinedTransactionsList,
                                                         CurrencyExchanger exchanger,
                                                         String fileName) {
        generateJoinedTransactionsCsvFile(
                joinedTransactionsList,
                Settings.CSV_TRANSACTION_WRITE_ORDER,
                exchanger,
                Settings.DATA_SHIFT,
                Settings.PRECISION,
                Settings.ROUNDING_MODE,
                fileName);
    }

    public static void generateJoinedTransactionsCsvFile(List<JoinedTransactions> joinedTransactionsList,
                                                         TransactionData[] order,
                                                         CurrencyExchanger exchanger,
                                                         Period dateShift,
                                                         int precision,
                                                         RoundingMode roundingMode,
                                                         String fileName) {
        /*
                String[] extendedData = new String[] {
                "Sell Value",
                "Buy Value",
                "Total Buy Value",
                "Profit",
                "Currency",
                "Exchange Rate",
                "Sell Value",
                "Buy Value",
                "Total Buy Value",
                "Profit",
                "Currency"
        };
         */
        fileName = fileName + Settings.CSV_FILE_TYPE;
        String[] extendedData = new String[]{
                "Exchange Rate",
                "Sell Date",
                "Sell Value",
                "Buy Value",
                "Total Buy Value",
                "Commission",
                "Total Commission",
                "Profit",
                "Currency"
        };

        Writer w = null;
        try {
            w = Files.newBufferedWriter(Settings.GENERATED_DATA_PATH.resolve(fileName));
            StringBuilder header = new StringBuilder();
            // header
            for (var data : order) {
                header.append(CSV_SEPARATOR)
                        .append(data);
            }
            for (var data : extendedData) {
                header.append(CSV_SEPARATOR)
                        .append(data);
            }
            w.append(header.substring(1))
                    .append("\n");

            // transactions
            for (JoinedTransactions jt : joinedTransactionsList) {
                w.append(jt.generateCsvLine(order, exchanger, dateShift, precision, roundingMode));
                // check if joined transaction has not matching times
                if (jt.isSellTimeInvalid()) {
                    w.append(CSV_SEPARATOR)
                            .append("Sell time INVALID");
                }
                w.append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (w != null) {
                    w.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void generateDividendTransactionsCsvFile(List<DividendTransaction> dividendList,
                                                           CurrencyExchanger exchanger,
                                                           String fileName) {
        generateDividendTransactionsCsvFile(
                dividendList,
                Settings.CSV_DIVIDEND_WRITE_ORDER,
                exchanger,
                Settings.DATA_SHIFT,
                Settings.PRECISION,
                Settings.ROUNDING_MODE,
                fileName);
    }
    public static void generateDividendTransactionsCsvFile(List<DividendTransaction> dividendList,
                                                           TransactionData[] order,
                                                           CurrencyExchanger exchanger,
                                                           Period dateShift,
                                                           int precision,
                                                           RoundingMode roundingMode,
                                                           String fileName) {
        fileName = fileName + Settings.CSV_FILE_TYPE;
        Writer w = null;
        try {
            w = Files.newBufferedWriter(Settings.GENERATED_DATA_PATH.resolve(fileName));
            StringBuilder header = new StringBuilder();
            for (var data : order) {
                header.append(CSV_SEPARATOR)
                        .append(data);
            }
            // header
            header.append(CSV_SEPARATOR)
                    .append("Exchange Rate")
                    .append(CSV_SEPARATOR)
                    .append("Value")
                    .append(CSV_SEPARATOR)
                    .append("Paid Tax")
                    .append(CSV_SEPARATOR)
                    .append("Currency")
                    .append(CSV_SEPARATOR)
                    .append("% paid")
                    .append(CSV_SEPARATOR);
            w.append(header.substring(1));
            w.append('\n');

            // transaction csv representation
            for (DividendTransaction t : dividendList) {
                String sb = t.generateCsvLine(order) +
                        CSV_SEPARATOR +
                        exchanger.getRateUpTo7DaysPrevious(t.getCurrency(), t.getDateTime().toLocalDate().plus(dateShift)) +
                        CSV_SEPARATOR +
                        t.getValue(exchanger, dateShift, precision, roundingMode) +
                        CSV_SEPARATOR +
                        t.getPaidTaxes(exchanger, dateShift, precision, roundingMode) +
                        CSV_SEPARATOR +
                        exchanger.getToCurrency() +
                        CSV_SEPARATOR +
                        t.getPercentOfPaidTaxes();
                w.append(sb);
                w.append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (w != null) {
                    w.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }


    public static void generateYearStockGainsMapCsvFile(Map<Integer, Map<String, StockGainsInfo>> yearStockGainsMap,
                                                        CurrencyExchanger exchanger,
                                                        String fileName) {
        fileName = fileName + Settings.CSV_FILE_TYPE;
        Writer w = null;
        try {
            w = Files.newBufferedWriter(Settings.GENERATED_DATA_PATH.resolve(fileName));
            StringBuilder header = new StringBuilder();

            // header
            w.append(StockGainsInfo.CSV_HEADER);
            w.append('\n');

            int[] years = yearStockGainsMap.keySet().stream().mapToInt(Integer::intValue).sorted().toArray();
            for (int year : years) {
                List<String> tickers = yearStockGainsMap.get(year)
                        .keySet()
                        .stream()
                        .toList();
                StockGainsInfo[] gainsInfo = new StockGainsInfo[tickers.size()];
                for (int i = 0; i < gainsInfo.length; i++) {
                    gainsInfo[i] = yearStockGainsMap.get(year).get(tickers.get(i));
                }
                // sort by product name
                Arrays.sort(gainsInfo, Comparator.comparing(StockGainsInfo::getProduct));
                // write
                for (var info : gainsInfo) {
                    w.append(info.generateCsvLine());
                    w.append('\n');
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (w != null) {
                    w.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public static void generateCalculationSummaryTxtFile(Map<Integer, StockGainsInfo> yearAllStocksGainsMap,
                                                         Map<Integer, Map<String, StockGainsInfo>> yearStockGainsMap,
                                                         List<LeftStockInfo> leftStocksList,
                                                         String info,
                                                         String fileName) {
        fileName = fileName + ".txt";
        Writer w = null;
        try {
            w = Files.newBufferedWriter(Settings.GENERATED_DATA_PATH.resolve(fileName));
            DecimalFormat formatter = new DecimalFormat("#,##0.00");

            w.append("\tTOTAL GAINS BY YEAR\n\n");
            w.append(String.format("%7s %15s %15s %15s %15s\n",
                    "YEAR",
                    "SELL VALUE",
                    "BUY VALUE",
                    "COMMISSION",
                    "PROFIT",
                    "CURRENCY"
                    )
            );
            for (int year : yearAllStocksGainsMap.keySet()) {
                var gainsInfo = yearAllStocksGainsMap.get(year);
                w.append(String.format("%7s %15s %15s %15s %15s\n",
                        gainsInfo.getYear(),
                        formatter.format(gainsInfo.getTotalSellValue().doubleValue()),
                        formatter.format(gainsInfo.getTotalBuyValue()),
                        formatter.format(gainsInfo.getTotalCommission()),
                        formatter.format(gainsInfo.getTotalProfitValue()),
                        gainsInfo.getCurrency())
                );
            }

            w.append("\n\tSTOCKS IN PORTFOLIO\n\n");
            w.append(String.format("%15s %20s %15s \n",
                    "QUANTITY",
                    "TICKER",
                    "PRODUCT")
            );
            for (var stockInfo : leftStocksList) {
                w.append(String.format("%15s %20s   %s \n",
                        stockInfo.getQuantity(),
                        stockInfo.getTicker(),
                        stockInfo.getProduct())
                );
            }

            w.append("\n\tADDITIONAL INFO\n\n");
            w.append(info);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                w.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }
}
