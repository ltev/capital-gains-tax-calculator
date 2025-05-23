package com.ltcode.capitalgainstaxcalculator.data_reader.data_writer;

import com.ltcode.capitalgainstaxcalculator.calculator.LeftStockInfo;
import com.ltcode.capitalgainstaxcalculator.calculator.StockGainsInfo;
import com.ltcode.capitalgainstaxcalculator.calculator.YearGainsInfo;
import com.ltcode.capitalgainstaxcalculator.calculator.YearSellBuyInfo;
import com.ltcode.capitalgainstaxcalculator.csv_creator.CsvCreator;
import com.ltcode.capitalgainstaxcalculator.settings.Settings;
import com.ltcode.capitalgainstaxcalculator.transaction.DividendTransaction;
import com.ltcode.capitalgainstaxcalculator.transaction.Transaction;
import com.ltcode.capitalgainstaxcalculator.transaction.TransactionData;
import com.ltcode.capitalgainstaxcalculator.transaction.joined.JoinedTransaction;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;
import com.ltcode.capitalgainstaxcalculator.transaction_converter.TransactionValuesConverter;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.ltcode.capitalgainstaxcalculator.settings.Settings.CSV_SEPARATOR;
import static com.ltcode.capitalgainstaxcalculator.settings.Settings.GENERATED_DATA_PATH;


public class Write {

    public static void generateTransactionsCsvFile(List<? extends Transaction> transactions,
                                                   TransactionValuesConverter valuesConverter) {
        generateTransactionsCsvFile(
                transactions,
                valuesConverter,
                GENERATED_DATA_PATH.resolve(Settings.TRANSACTIONS_FILE_NAME)
        );
    }

    public static void generateTransactionsCsvFile(List<? extends Transaction> transactions,
                                                   TransactionValuesConverter valuesConverter,
                                                   Path path) {
        generateTransactionsCsvFile(
                transactions,
                Settings.CSV_TRANSACTION_WRITE_ORDER,
                valuesConverter,
                path
        );
    }

    public static void generateTransactionsCsvFile(List<? extends Transaction> transactions,
                                                   TransactionData[] order,
                                                   TransactionValuesConverter valuesConverter,
                                                   Path path) {
        String[] extendedOrder = {
                "Exchange Rate",
                //"Price per share",
                "Value",
                "Commission",
                "Currency"
        };

        Writer w = null;
        try {
            w = Files.newBufferedWriter(path);

            // header
            StringBuilder header = new StringBuilder();
            for (var data : order) {
                header.append(CSV_SEPARATOR)
                        .append(data);
            }
            for (var data : extendedOrder) {
                header.append(CSV_SEPARATOR)
                        .append(data);
            }
            w.append(header.substring(1))
                    .append("\n");

            // transaction
            for (Transaction t : transactions) {
                w.append(CsvCreator.get(t, order));

                // local currency data
                if (t.getType() == TransactionType.BUY || t.getType() == TransactionType.SELL) {
                    w.append(CSV_SEPARATOR)
                            .append(valuesConverter.getRateAfterShiftUpTo7DaysPrevious(t).toString())
                            .append(CSV_SEPARATOR)
                            .append(valuesConverter.getValue(t).toString())
                            .append(CSV_SEPARATOR)
                            .append(valuesConverter.getCommission(t).toString())
                            .append(CSV_SEPARATOR)
                            .append(valuesConverter.getToCurrency().toString());
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

    public static void generateJoinedTransactionsCsvFile(List<JoinedTransaction> joinedTransactionList,
                                                         TransactionValuesConverter valuesConverter) {
        generateJoinedTransactionsCsvFile(
                joinedTransactionList,
                valuesConverter,
                GENERATED_DATA_PATH.resolve(Settings.JOINED_TRANSACTIONS_FILE_NAME)
        );
    }

    public static void generateJoinedTransactionsCsvFile(List<JoinedTransaction> joinedTransactionList,
                                                         TransactionValuesConverter valuesConverter,
                                                         Path path) {
        generateJoinedTransactionsCsvFile(
                joinedTransactionList,
                Settings.CSV_JOINED_TRANSACTION_WRITE_ORDER,
                valuesConverter,
                path
        );
    }

    public static void generateJoinedTransactionsCsvFile(List<JoinedTransaction> joinedTransactionList,
                                                         TransactionData[] order,
                                                         TransactionValuesConverter valuesConverter,
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
            for (JoinedTransaction jt : joinedTransactionList) {
                w.append(CsvCreator.get(jt, order, valuesConverter));
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
                                                           TransactionValuesConverter valuesConverter) {
        generateDividendTransactionsCsvFile(
                dividendList,
                valuesConverter,
                GENERATED_DATA_PATH.resolve(Settings.DIVIDEND_TRANSACTIONS_FILE_NAME)
        );
    }

    public static void generateDividendTransactionsCsvFile(List<DividendTransaction> dividendList,
                                                           TransactionValuesConverter valuesConverter,
                                                           Path path) {
        generateDividendTransactionsCsvFile(
                dividendList,
                Settings.CSV_DIVIDEND_WRITE_ORDER,
                valuesConverter,
                path
        );
    }

    public static void generateDividendTransactionsCsvFile(List<DividendTransaction> dividendList,
                                                           TransactionData[] order,
                                                           TransactionValuesConverter valuesConverter,
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
                    .append("Dividend Before Taxes")
                    .append(CSV_SEPARATOR)
                    .append("Paid Taxes")
                    .append(CSV_SEPARATOR)
                    .append("Dividend After Taxes")
                    .append(CSV_SEPARATOR)
                    .append("Currency")
                    .append(CSV_SEPARATOR)
                    .append("% paid")
                    .append(CSV_SEPARATOR);
            w.append(header.substring(1));
            w.append('\n');

            // transaction csv representation
            for (DividendTransaction t : dividendList) {
                String sb = CsvCreator.get(t, order)
                        + CSV_SEPARATOR
                        + valuesConverter.getRateAfterShiftUpTo7DaysPrevious(t)
                        + CSV_SEPARATOR
                        + (t.getDividendBeforeTaxes() == null ? null : valuesConverter.getDividendBeforeTaxes(t))
                        + CSV_SEPARATOR
                        + (t.getTaxesPaid() == null ? null : valuesConverter.getTaxesPaid(t))
                        + CSV_SEPARATOR
                        + (t.getDividendAfterTaxes() == null ? null : valuesConverter.getDividendAfterTaxes(t))
                        + CSV_SEPARATOR
                        + valuesConverter.getToCurrency()
                        + CSV_SEPARATOR
                        + t.getPercentOfPaidTaxes();
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

    public static void generateCalculationGainsSummaryTxtFile(Map<Integer, YearGainsInfo> yearAllStocksGainsMap,
                                                              List<LeftStockInfo> leftStocksList,
                                                              String report) {
        generateCalculationGainsSummaryTxtFile(
                yearAllStocksGainsMap,
                leftStocksList,
                report,
                GENERATED_DATA_PATH.resolve(Settings.GAINS_SUMMARY_FILE_NAME)
        );
    }

    public static void generateCalculationGainsSummaryTxtFile(Map<Integer, YearGainsInfo> yearAllStocksGainsMap,
                                                              List<LeftStockInfo> leftStocksList,
                                                              String report,
                                                              Path path) {
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

            int[] sortedYears = yearAllStocksGainsMap.keySet().stream()
                    .mapToInt(x -> x)
                    .sorted()
                    .toArray();

            for (int year : sortedYears) {
                var gainsInfo = yearAllStocksGainsMap.get(year);
                w.append(String.format("%7s %15s %15s %15s %15s %15s%n",
                        gainsInfo.getYear(),
                        formatter.format(gainsInfo.getTotalSellValue()),
                        formatter.format(gainsInfo.getTotalBuyValue()),
                        formatter.format(gainsInfo.getTotalBuySellCommissionValue()),
                        formatter.format(gainsInfo.getTotalProfitValue()),
                        gainsInfo.getCurrency())
                );
            }

            // left stocks in portfolio

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

            // write report

            if (!report.isEmpty()) {
                w.append("\n\tADDITIONAL INFO\n\n");
                w.append(report);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                w.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void generateCalculationSellBuySummaryTxtFile(Map<Integer, YearSellBuyInfo> yearSellBuyInfoMap,
                                                                Path path) {
        try (Writer w = Files.newBufferedWriter(path)) {
            DecimalFormat formatter = new DecimalFormat("#,##0.00");

            w.append("\tTOTAL BUY / SELL TRANSACTIONS VALUE\n\n");
            w.append(String.format("%7s %15s %15s %15s %15s %15s %15s\n",
                            "YEAR",
                            "SELL VALUE",
                            "SELL COMMISSION",
                            "BUY VALUE",
                            "BUY COMMISSION",
                            "TOTAL COMMISSION",
                            "CURRENCY"
                    )
            );

            int[] sortedYears = yearSellBuyInfoMap.keySet().stream()
                    .mapToInt(x -> x)
                    .sorted()
                    .toArray();

            for (int year : sortedYears) {
                var gainsInfo = yearSellBuyInfoMap.get(year);
                w.append(String.format("%7s %15s %15s %15s %15s %15s %15s%n",
                        gainsInfo.getYear(),
                        formatter.format(gainsInfo.getTotalSellValue()),
                        formatter.format(gainsInfo.getTotalSellCommissionValue()),
                        formatter.format(gainsInfo.getTotalBuyValue()),
                        formatter.format(gainsInfo.getTotalBuyCommissionValue()),
                        formatter.format(gainsInfo.getTotalBuySellCommissionValue()),
                        gainsInfo.getCurrency())
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
