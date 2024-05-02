package com.ltcode.capitalgainstaxcalculator.calculator;

import com.ltcode.capitalgainstaxcalculator.broker.FileInfo;
import com.ltcode.capitalgainstaxcalculator.country_info.CountryTaxCalculationInfo;
import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyRateExchanger;
import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyRateExchangerImp;
import com.ltcode.capitalgainstaxcalculator.data_reader.TransactionReader;
import com.ltcode.capitalgainstaxcalculator.data_reader.data_writer.Write;
import com.ltcode.capitalgainstaxcalculator.exception.InvalidDateOrderException;
import com.ltcode.capitalgainstaxcalculator.settings.Settings;
import com.ltcode.capitalgainstaxcalculator.transaction.*;
import com.ltcode.capitalgainstaxcalculator.transaction.Currency;
import com.ltcode.capitalgainstaxcalculator.transaction.joined.JoinedTransaction;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;
import com.ltcode.capitalgainstaxcalculator.transaction_converter.TransactionValuesConverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BaseGainsCalculatorImpl implements BaseGainsCalculator {

    private final CountryTaxCalculationInfo countryInfo;
    private FileInfo fileInfo;
    private final TransactionValuesConverter valuesConverter;
    private List<? extends Transaction> transactions;
    private SellBuyJoiner joiner;
    private final CurrencyRateExchanger exchanger;
    private List<JoinedTransaction> joinedTransactionList;
    private List<DividendTransaction> dividendTransactionsList;
    /**
     * year -> Map<Ticker, StockGainsInfo>
     */
    private Map<Integer, Map<String, StockGainsInfo>> yearStockGainsMap;
    /**
     * year -> TotalGainsInfo
     */
    private Map<Integer, StockGainsInfo> yearAllStocksGainsMap;

    private Map<Integer, DividendGainsInfo> yearDividendGainsMap;
    private final Currency toCurrency;

    public BaseGainsCalculatorImpl(CountryTaxCalculationInfo countryInfo) {
        this.countryInfo = countryInfo;
        this.valuesConverter = new TransactionValuesConverter(countryInfo);
        this.exchanger = new CurrencyRateExchangerImp(this.countryInfo.getCurrency(), Settings.EXCHANGE_RATES_DATA_PATH);;
        this.toCurrency = exchanger.getToCurrency();
    }

    @Override
    public void calculate(FileInfo fileInfo) {
        this.fileInfo = fileInfo;

        // read transactions from file
        List<? extends Transaction> transactions = switch (fileInfo.getFileType()) {
            case TRANSACTIONS -> TransactionReader.read(
                    fileInfo.getBroker(),
                    fileInfo.getPath()
            );
            case ACCOUNT -> TransactionReader.readAutomaticFundTransactions(
                    fileInfo.getBroker(),
                    fileInfo.getPath()
            );
        };
        calculate(transactions);
    }

    @Override
    public void calculate(List<? extends Transaction> transactions) {
        this.transactions = transactions;

        try {
            TransactionUtils.checkTransactionsValidity(transactions);
        } catch (InvalidDateOrderException e) {
            // reverse transactions if not in chronological order
            Collections.reverse(transactions);
        }
        TransactionUtils.checkTransactionsValidity(transactions);

        this.joiner = new SellBuyJoiner(this.transactions, countryInfo.getPrecision(), countryInfo.getRoundingMode());
        this.joinedTransactionList = joiner.join();
        this.dividendTransactionsList = new ArrayList<>();
        this.yearStockGainsMap = new LinkedHashMap<>();
        this.yearAllStocksGainsMap = new LinkedHashMap<>();
        this.yearDividendGainsMap = new HashMap<>();
        calculateTotalStockGainsByYear();
        calculateTotalDividendGainsByYear();
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
    public StockGainsInfo getTotalGains(int year) {
        if (! yearAllStocksGainsMap.containsKey(year)) {
            return null;
        }
        return yearAllStocksGainsMap.get(year);
    }

    @Override
    public StockGainsInfo getTotalGains(int year, String ticker) {
        return yearStockGainsMap.get(year).get(ticker);
    }

    @Override
    public DividendGainsInfo getTotalDividends(int year) {
        if (! yearDividendGainsMap.containsKey(year)) {
            return null;
        }
        return yearDividendGainsMap.get(year);
    }

    // == GENERATE FILES ==

    @Override
    public void generateTransactionsCsvFile(Path directory) {
        directory = directory.resolve(
                fileInfo.getBroker().name().toLowerCase()
                        + "_"
                        + fileInfo.getFileType().name().toLowerCase()
        );
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // in original currency
        if (transactions.size() > 0) {
            Write.generateTransactionsCsvFile(
                    transactions,
                    directory.resolve(Settings.TRANSACTIONS_FILE_NAME)
            );
        }

        if (joinedTransactionList.size() > 0) {
            Write.generateJoinedTransactionsCsvFile(
                    joinedTransactionList,
                    valuesConverter,
                    directory.resolve(Settings.JOINED_TRANSACTIONS_FILE_NAME)
            );
        }

        if (! yearStockGainsMap.isEmpty()) {
            Write.generateYearStockGainsMapCsvFile(
                    yearStockGainsMap,
                    directory.resolve(Settings.GAINS_FILE_NAME)
            );
        }

        if (dividendTransactionsList.size() > 0) {
            Write.generateDividendTransactionsCsvFile(
                    dividendTransactionsList,
                    valuesConverter,
                    directory.resolve(Settings.DIVIDEND_TRANSACTIONS_FILE_NAME)
            );
        }

        Write.generateCalculationSummaryTxtFile(
                yearAllStocksGainsMap,
                getLeftStocksList(),
                joiner.getReport(),
                directory.resolve(Settings.SUMMARY_FILE_NAME)
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
        String ALL_STOCKS_TICKER = "ALL STOCKS TOGETHER";

        for (var jt : joinedTransactionList) {
            var sell = jt.getSellTransaction();
            int year = sell.getDateTime().getYear();
            StockGainsInfo allStocks;
            StockGainsInfo currStock;

            if (yearAllStocksGainsMap.get(year) == null) {
                yearAllStocksGainsMap.put(year, new StockGainsInfo(year, ALL_STOCKS_TICKER, ALL_STOCKS_TICKER, toCurrency));
                yearStockGainsMap.put(year, new HashMap<>());
            }
            yearStockGainsMap.get(year).putIfAbsent(
                    sell.getTicker(), new StockGainsInfo(year, sell.getTicker(), sell.getProduct(), toCurrency));

            allStocks = yearAllStocksGainsMap.get(year);
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
}
