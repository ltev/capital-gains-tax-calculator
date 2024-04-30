package com.ltcode.capitalgainstaxcalculator.data_reader.data_writer;

import com.ltcode.capitalgainstaxcalculator.calculator.LeftStockInfo;
import com.ltcode.capitalgainstaxcalculator.calculator.StockGainsInfo;
import com.ltcode.capitalgainstaxcalculator.country_info.CountryTaxCalculationInfo;
import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyExchanger;
import com.ltcode.capitalgainstaxcalculator.settings.Settings;
import com.ltcode.capitalgainstaxcalculator.transaction.DividendTransaction;
import com.ltcode.capitalgainstaxcalculator.transaction.Transaction;
import com.ltcode.capitalgainstaxcalculator.transaction.TransactionData;
import com.ltcode.capitalgainstaxcalculator.transaction.joined.JoinedTransactions;
import static com.ltcode.capitalgainstaxcalculator.settings.Settings.GENERATED_DATA_PATH;
import static com.ltcode.capitalgainstaxcalculator.settings.Settings.CSV_SEPARATOR;
import java.io.IOException;
import java.io.Writer;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.Period;
import java.util.*;


public class Write {

    public static void generateTransactionsCsvFile(List<? extends Transaction> transactions) {
        generateTransactionsCsvFile(
                transactions,
                Settings.CSV_TRANSACTION_WRITE_ORDER,
                GENERATED_DATA_PATH.resolve(Settings.TRANSACTIONS_FILE_NAME)
        );
    }

    public static void generateTransactionsCsvFile(List<? extends Transaction> transactions,
                                                   TransactionData[] order,
                                                   Path path) {
        Writer w = null;
        try {
            w = Files.newBufferedWriter(path);

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
                                                         CountryTaxCalculationInfo countryInfo) {
        generateJoinedTransactionsCsvFile(
                joinedTransactionsList,
                Settings.CSV_JOINED_TRANSACTION_WRITE_ORDER,
                exchanger,
                countryInfo.getDateShift(),
                countryInfo.getPrecision(),
                countryInfo.getRoundingMode(),
                GENERATED_DATA_PATH.resolve(Settings.JOINED_TRANSACTIONS_FILE_NAME));
    }

    public static void generateJoinedTransactionsCsvFile(List<JoinedTransactions> joinedTransactionsList,
                                                         TransactionData[] order,
                                                         CurrencyExchanger exchanger,
                                                         Period dateShift,
                                                         int precision,
                                                         RoundingMode roundingMode,
                                                         Path path) {
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
            w = Files.newBufferedWriter(path);
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
                                                           CountryTaxCalculationInfo countryInfo) {
        generateDividendTransactionsCsvFile(
                dividendList,
                Settings.CSV_DIVIDEND_WRITE_ORDER,
                exchanger,
                countryInfo,
                GENERATED_DATA_PATH.resolve(Settings.DIVIDEND_TRANSACTIONS_FILE_NAME)
        );
    }

    public static void generateDividendTransactionsCsvFile(List<DividendTransaction> dividendList,
                                                           TransactionData[] order,
                                                           CurrencyExchanger exchanger,
                                                           CountryTaxCalculationInfo countryInfo,
                                                           Path path) {
        Writer w = null;
        try {
            w = Files.newBufferedWriter(path);
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
                        exchanger.getRateUpTo7DaysPrevious(t.getCurrency(), t.getDateTime().toLocalDate().plus(countryInfo.getDateShift())) +
                        CSV_SEPARATOR +
                        t.getValue(exchanger, countryInfo.getDateShift(),countryInfo.getPrecision(), countryInfo.getRoundingMode()) +
                        CSV_SEPARATOR +
                        t.getPaidTaxes(exchanger, countryInfo.getDateShift(), countryInfo.getPrecision(), countryInfo.getRoundingMode()) +
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

    public static void generateYearStockGainsMapCsvFile(Map<Integer, Map<String, StockGainsInfo>> yearStockGainsMap) {
        generateYearStockGainsMapCsvFile(
                yearStockGainsMap,
                GENERATED_DATA_PATH.resolve(Settings.GAINS_FILE_NAME)
        );
    }

    public static void generateYearStockGainsMapCsvFile(Map<Integer, Map<String, StockGainsInfo>> yearStockGainsMap,
                                                        Path path) {
        Writer w = null;
        try {
            w = Files.newBufferedWriter(path);
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
                                                         List<LeftStockInfo> leftStocksList,
                                                         String info) {
        generateCalculationSummaryTxtFile(
                yearAllStocksGainsMap,
                leftStocksList,
                info,
                GENERATED_DATA_PATH.resolve(Settings.SUMMARY_FILE_NAME)
        );
    }

    public static void generateCalculationSummaryTxtFile(Map<Integer, StockGainsInfo> yearAllStocksGainsMap,
                                                         List<LeftStockInfo> leftStocksList,
                                                         String info,
                                                         Path path) {
        Writer w = null;
        try {
            w = Files.newBufferedWriter(path);
            DecimalFormat formatter = new DecimalFormat("#,##0.00");

            w.append("\tTOTAL GAINS BY YEAR\n\n");
            w.append(String.format("%7s %15s %15s %15s %15s %15s\n",
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
                w.append(String.format("%7s %15s %15s %15s %15s %15s\n",
                        gainsInfo.getYear(),
                        formatter.format(gainsInfo.getTotalSellValue()),
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

            if (! info.isEmpty()) {
                w.append("\n\tADDITIONAL INFO\n\n");
                w.append(info);
            }
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
