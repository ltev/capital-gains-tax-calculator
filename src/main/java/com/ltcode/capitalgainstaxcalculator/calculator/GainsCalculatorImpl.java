package com.ltcode.capitalgainstaxcalculator.calculator;

import com.ltcode.capitalgainstaxcalculator.country_info.CountryTaxCalculationInfo;
import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyRateExchanger;
import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyRateExchangerImp;
import com.ltcode.capitalgainstaxcalculator.data_reader.data_writer.Write;
import com.ltcode.capitalgainstaxcalculator.exception.InvalidDateOrderException;
import com.ltcode.capitalgainstaxcalculator.settings.Settings;
import com.ltcode.capitalgainstaxcalculator.transaction.*;
import com.ltcode.capitalgainstaxcalculator.transaction.Currency;
import com.ltcode.capitalgainstaxcalculator.transaction.joined.JoinedTransaction;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;
import com.ltcode.capitalgainstaxcalculator.transaction_converter.TransactionValuesConverter;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Period;
import java.util.*;

public class GainsCalculatorImpl implements GainsCalculator {


    private final CountryTaxCalculationInfo COUNTRY_INFO;

    private final TransactionValuesConverter valuesConverter;

    /**
     * double places
     */
    private final int PRECISION;
    private final RoundingMode ROUNDING_MODE;
    /**
     *
     */
    private final Period EXCHANGE_RATE_DATA_SHIFT;
    private final List<? extends Transaction> TRANSACTIONS;
    private SellBuyJoiner joiner;
    private final CurrencyRateExchanger EXCHANGER;
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
    private final Currency TO_CURRENCY;

    public GainsCalculatorImpl(CountryTaxCalculationInfo countryInfo, List<? extends Transaction> transactions) {
        this.COUNTRY_INFO = countryInfo;
        this.TRANSACTIONS = new ArrayList<>(transactions);
        this.valuesConverter = new TransactionValuesConverter(countryInfo);

        this.EXCHANGER = new CurrencyRateExchangerImp(COUNTRY_INFO.getCurrency(), Settings.EXCHANGE_RATES_DATA_PATH);;
        this.TO_CURRENCY = EXCHANGER.getToCurrency();
        this.PRECISION = COUNTRY_INFO.getPrecision();
        this.ROUNDING_MODE = COUNTRY_INFO.getRoundingMode();
        this.EXCHANGE_RATE_DATA_SHIFT = COUNTRY_INFO.getDateShift();
    }

    public void calculate() {
        try {
            TransactionUtils.checkTransactionsValidity(TRANSACTIONS);
        } catch (InvalidDateOrderException e) {
            // reverse transactions if not in chronological order
            Collections.reverse(TRANSACTIONS);
        }
        TransactionUtils.checkTransactionsValidity(TRANSACTIONS);
        this.joiner = new SellBuyJoiner(this.TRANSACTIONS, PRECISION, ROUNDING_MODE);
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
    public void generateTransactionsCsvFile() {
        // in original currency
        Write.generateTransactionsCsvFile(TRANSACTIONS);

        Write.generateJoinedTransactionsCsvFile(
                joinedTransactionList,
                EXCHANGER,
                COUNTRY_INFO
        );

        Write.generateYearStockGainsMapCsvFile(yearStockGainsMap);

        Write.generateDividendTransactionsCsvFile(
                dividendTransactionsList,
                valuesConverter
        );

        Write.generateCalculationSummaryTxtFile(
            yearAllStocksGainsMap,
            getLeftStocksList(),
            joiner.getReport()
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
                yearAllStocksGainsMap.put(year, new StockGainsInfo(year, ALL_STOCKS_TICKER, ALL_STOCKS_TICKER, TO_CURRENCY));
                yearStockGainsMap.put(year, new HashMap<>());
            }
            yearStockGainsMap.get(year).putIfAbsent(
                    sell.getTicker(), new StockGainsInfo(year, sell.getTicker(), sell.getProduct(), TO_CURRENCY));

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
        for (Transaction t : TRANSACTIONS) {
            if (t.getType() == TransactionType.DIVIDEND) {
                dividendTransactionsList.add((DividendTransaction) t);
            }
        }
        for (Transaction t : dividendTransactionsList) {
            int year = t.getDateTime().getYear();
            yearDividendGainsMap.putIfAbsent(year, new DividendGainsInfo(year, TO_CURRENCY));
            yearDividendGainsMap.get(year).addToTotalValue(t.getValue(EXCHANGER, EXCHANGE_RATE_DATA_SHIFT, PRECISION, ROUNDING_MODE));
        }
    }
}
