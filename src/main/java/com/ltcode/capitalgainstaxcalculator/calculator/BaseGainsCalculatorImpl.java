package com.ltcode.capitalgainstaxcalculator.calculator;

import com.ltcode.capitalgainstaxcalculator.broker.FileInfo;
import com.ltcode.capitalgainstaxcalculator.country_info.CountryTaxCalculationInfo;
import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyRateExchanger;
import com.ltcode.capitalgainstaxcalculator.data_reader.TransactionReader;
import com.ltcode.capitalgainstaxcalculator.data_reader.data_writer.Write;
import com.ltcode.capitalgainstaxcalculator.exception.InvalidDateOrderException;
import com.ltcode.capitalgainstaxcalculator.settings.Settings;
import com.ltcode.capitalgainstaxcalculator.transaction.Currency;
import com.ltcode.capitalgainstaxcalculator.transaction.DividendTransaction;
import com.ltcode.capitalgainstaxcalculator.transaction.Transaction;
import com.ltcode.capitalgainstaxcalculator.transaction.TransactionUtils;
import com.ltcode.capitalgainstaxcalculator.transaction.joined.JoinedTransaction;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;
import com.ltcode.capitalgainstaxcalculator.transaction_converter.TransactionValuesConverter;
import lombok.Getter;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class BaseGainsCalculatorImpl implements BaseGainsCalculator {

    @Getter
    private final FileInfo fileInfo;
    private CountryTaxCalculationInfo countryInfo;
    private LocalDate lastCalculationDate;
    private TransactionValuesConverter valuesConverter;
    private CurrencyRateExchanger exchanger;
    private Currency toCurrency;
    private List<? extends Transaction> transactions;
    private SellBuyJoiner joiner;
    private List<JoinedTransaction> joinedTransactionList;
    private List<DividendTransaction> dividendTransactionsList;
    /**
     * year -> Map<Ticker, StockGainsInfo>
     */
    private Map<Integer, Map<String, StockGainsInfo>> yearStockGainsMap;
    /**
     * year -> TotalGainsInfo
     */
    private Map<Integer, YearGainsInfo> yearGainsInfo;
    private Map<Integer, DividendGainsInfo> yearDividendGainsMap;
    private Map<Integer, YearSellBuyInfo> yearSellBuyInfoMap;

    public BaseGainsCalculatorImpl(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    /**
     * Reads transactions from file
     */
    @Override
    public void loadFileData() {
        // read transactions from file
        transactions = switch (fileInfo.fileType()) {
            case STOCK_TRANSACTIONS -> TransactionReader.readTransactionsFile(
                    fileInfo.broker(),
                    fileInfo.path()
            );
            case STOCK_ACCOUNT -> TransactionReader.readAccountFile(
                    fileInfo.broker(),
                    fileInfo.path()
            );
            case CRYPTO_TRANSACTIONS -> TransactionReader.readCryptoTransactionsFile(
                    fileInfo.broker(),
                    fileInfo.path()
            );
        };
    }

    @Override
    public void calculate(CountryTaxCalculationInfo countryInfo, LocalDate lastCalculationDate, CurrencyRateExchanger exchanger) {
        this.countryInfo = countryInfo;
        this.lastCalculationDate = lastCalculationDate;
        this.valuesConverter = new TransactionValuesConverter(countryInfo);
        this.exchanger = exchanger;
        this.toCurrency = exchanger.getToCurrency();

        calculate();
    }

    //@Override
    void calculate() {
        try {
            TransactionUtils.checkTransactionsValidity(transactions);
        } catch (InvalidDateOrderException e) {
            // reverse transactions if not in chronological order
            Collections.reverse(transactions);
        }
        TransactionUtils.checkTransactionsValidity(transactions);

        this.joiner = new SellBuyJoiner(this.transactions, lastCalculationDate, countryInfo.getPrecision(), countryInfo.getRoundingMode());
        this.joinedTransactionList = joiner.join();
        this.dividendTransactionsList = new ArrayList<>();
        this.yearStockGainsMap = new LinkedHashMap<>();
        this.yearGainsInfo = new LinkedHashMap<>();
        this.yearDividendGainsMap = new HashMap<>();
        this.yearSellBuyInfoMap = new HashMap<>();

        calculateTotalStockGainsByYear();
        calculateTotalDividendGainsByYear();
        calculateTotalBuySellValuesByYear();
    }

    @Override
    public String[] getTickersLeft() {
        return joiner.getTickersLeft();
    }

    @Override
    public List<LeftStockInfo> getLeftStocksList() {
        return joiner.getLeftStocksList();
    }

    @Override
    public BigDecimal getQuantityLeft(String ticker) {
        return joiner.getQuantityLeft(ticker);
    }

    @Override
    public Map<Integer, YearGainsInfo> getTotalGains() {
        return new LinkedHashMap<>(yearGainsInfo);
    }

    @Override
    public YearGainsInfo getTotalGains(int year) {
        if (!yearGainsInfo.containsKey(year)) {
            return null;
        }
        return yearGainsInfo.get(year);
    }

    @Override
    public Map<Integer, YearSellBuyInfo> getTotalSellsBuys() {
        return yearSellBuyInfoMap;
    }

    @Override
    public YearSellBuyInfo getTotalSellsBuys(int year) {
        return yearSellBuyInfoMap.get(year);
    }

    @Override
    public StockGainsInfo getTotalGains(int year, String ticker) {
        return yearStockGainsMap.get(year).get(ticker);
    }

    @Override
    public DividendGainsInfo getTotalDividends(int year) {
        if (!yearDividendGainsMap.containsKey(year)) {
            return null;
        }
        return yearDividendGainsMap.get(year);
    }

    // == GENERATE FILES ==

    @Override
    public void generateTransactionsCsvFile(Path directory) {
        directory = directory.resolve(
                fileInfo.broker().name().toLowerCase()
                        + "_"
                        + fileInfo.fileType().name().toLowerCase()
        );
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // in original currency and local currency
        Write.generateTransactionsCsvFile(
                transactions,
                valuesConverter,
                directory.resolve(Settings.TRANSACTIONS_FILE_NAME)
        );

        // generate active buy transactions - ones that still hasn't been sold before 'lastCalculationDate'
        Write.generateTransactionsCsvFile(
                joiner.getLeftTransaction(),
                valuesConverter,
                directory.resolve(Settings.LEFT_BUY_TRANSACTIONS_FILE_NAME)
        );

        Map<TransactionType, List<Transaction>> buySellTransactions = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getType));

        // all buy transactions
        Write.generateTransactionsCsvFile(
                buySellTransactions.get(TransactionType.BUY),
                valuesConverter,
                directory.resolve(Settings.BUY_TRANSACTIONS_FILE_NAME)
        );

        // all sell transactions
        Write.generateTransactionsCsvFile(
                buySellTransactions.get(TransactionType.SELL),
                valuesConverter,
                directory.resolve(Settings.SELL_TRANSACTIONS_FILE_NAME)
        );

        Write.generateJoinedTransactionsCsvFile(
                joinedTransactionList,
                valuesConverter,
                directory.resolve(Settings.JOINED_TRANSACTIONS_FILE_NAME)
        );

        Write.generateYearStockGainsMapCsvFile(
                yearStockGainsMap,
                directory.resolve(Settings.GAINS_FILE_NAME)
        );

        Write.generateDividendTransactionsCsvFile(
                dividendTransactionsList,
                valuesConverter,
                directory.resolve(Settings.DIVIDEND_TRANSACTIONS_FILE_NAME)
        );

        Write.generateCalculationGainsSummaryTxtFile(
                yearGainsInfo,
                getLeftStocksList(),
                joiner.getReport(),
                directory.resolve(Settings.GAINS_SUMMARY_FILE_NAME)
        );

        Write.generateCalculationSellBuySummaryTxtFile(
                yearSellBuyInfoMap,
                directory.resolve(Settings.BUY_SELL_SUMMARY_FILE_NAME)
        );
    }

    // == PRIVATE HELPER METHODS ==

    /**
     * year -> total buy value
     * year -> total sell value
     * year -> total buy value by stock
     * year -> total sell value by stock
     */
    private void calculateTotalStockGainsByYear() {
        for (var jt : joinedTransactionList) {
            var sell = jt.getSellTransaction();
            int year = sell.getDateTime().getYear();
            YearGainsInfo allStocks;
            StockGainsInfo currStock;

            if (yearGainsInfo.get(year) == null) {
                yearGainsInfo.put(year, new YearGainsInfo(year, toCurrency));
                yearStockGainsMap.put(year, new HashMap<>());
            }
            yearStockGainsMap.get(year).putIfAbsent(
                    sell.getTicker(), new StockGainsInfo(year, sell.getTicker(), sell.getProduct(), toCurrency));

            allStocks = yearGainsInfo.get(year);
            currStock = yearStockGainsMap.get(year).get(sell.getTicker());

            // add to total value
            allStocks.addToTotalSellValue(valuesConverter.getValue(sell));
            allStocks.addToTotalSellCommission(valuesConverter.getCommission(sell));
            allStocks.addToTotalBuyValue(valuesConverter.getTotalBuyValue(jt));
            allStocks.addToTotalBuyCommission(valuesConverter.getTotalBuyCommission(jt));

            currStock.addToTotalSellValue(valuesConverter.getValue(sell));
            currStock.addToTotalSellCommission(valuesConverter.getCommission(sell));
            currStock.addToTotalBuyValue(valuesConverter.getTotalBuyValue(jt));
            currStock.addToTotalBuyCommission(valuesConverter.getTotalBuyCommission(jt));
            currStock.addToTotalSoldQuantity(sell.quantity);
        }
    }

    private void calculateTotalDividendGainsByYear() {
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.DIVIDEND) {
                dividendTransactionsList.add((DividendTransaction) t);
            }
        }
        for (Transaction t : dividendTransactionsList) {
            int year = t.getDateTime().getYear();
            yearDividendGainsMap.putIfAbsent(year, new DividendGainsInfo(year, toCurrency));
            yearDividendGainsMap.get(year).addToTotalValue(valuesConverter.getValue(t));
        }
    }

    private void calculateTotalBuySellValuesByYear() {
        transactions.stream()
                .filter(t -> t.getType() == TransactionType.SELL || t.getType() == TransactionType.BUY)
                .forEach(t -> {
                    int year = t.getDateTime().getYear();
                    yearSellBuyInfoMap.putIfAbsent(year, new YearSellBuyInfo(year, valuesConverter));
                    yearSellBuyInfoMap.get(year).add(t);
                });
    }
}
