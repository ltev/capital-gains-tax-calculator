package com.ltcode.capitalgainstaxcalculator.calculator;

import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyExchanger;
import com.ltcode.capitalgainstaxcalculator.data_reader.data_writer.Write;
import com.ltcode.capitalgainstaxcalculator.exception.InvalidDateOrderException;
import com.ltcode.capitalgainstaxcalculator.settings.Settings;
import com.ltcode.capitalgainstaxcalculator.transaction.*;
import com.ltcode.capitalgainstaxcalculator.transaction.Currency;
import com.ltcode.capitalgainstaxcalculator.transaction.joined.JoinedTransactions;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Period;
import java.util.*;

import static com.ltcode.capitalgainstaxcalculator.transaction.TransactionData.*;

public class GainsCalculatorImpl implements GainsCalculator {

    /**
     * double places
     */
    private final int PRECISION;
    private final RoundingMode ROUNDING_MODE;
    /**
     *
     */
    private final Period EXCHANGE_RATE_DATA_SHIFT;
    private final List<? extends Transaction> transactions;
    private SellBuyJoiner joiner;
    private final CurrencyExchanger EXCHANGER;
    private List<JoinedTransactions> joinedTransactionsList;
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


    public GainsCalculatorImpl(List<? extends Transaction> transactions,
                               CurrencyExchanger exchangeRateGenerator) {
        this(transactions, exchangeRateGenerator, Period.ofDays(0), 0, RoundingMode.HALF_UP);

        Write.generateTransactionsCsvFile(
                transactions,
                Settings.TRANSACTIONS_FILE_NAME);

    }

    public GainsCalculatorImpl(List<? extends Transaction> transactions,
                               CurrencyExchanger exchangeRateGenerator,
                               Period exchangeRateDataShift,
                               int precision,
                               RoundingMode ROUNDING_MODE) {
        this.transactions = new ArrayList<>(transactions);
        this.EXCHANGER = exchangeRateGenerator;
        this.TO_CURRENCY = EXCHANGER.getToCurrency();
        this.PRECISION = precision;
        this.ROUNDING_MODE = ROUNDING_MODE;
        this.EXCHANGE_RATE_DATA_SHIFT = exchangeRateDataShift;

        Write.generateTransactionsCsvFile(
                transactions,
                Settings.TRANSACTIONS_FILE_NAME);
    }

    public void calculate() {
        try {
            TransactionUtils.checkTransactionsValidity(transactions);
        } catch (InvalidDateOrderException e) {
            // reverse transactions if not in chronological order
            Collections.reverse(transactions);
        }
        TransactionUtils.checkTransactionsValidity(transactions);
        this.joiner = new SellBuyJoiner(this.transactions, PRECISION, ROUNDING_MODE);
        this.joinedTransactionsList = joiner.join();
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
        Write.generateTransactionsCsvFile(
                transactions,
                Settings.TRANSACTIONS_FILE_NAME);

        Write.generateJoinedTransactionsCsvFile(joinedTransactionsList,
                new TransactionData[]{
                        DATE_TIME,
                        TICKER,
                        PRODUCT,
                        TYPE,
                        QUANTITY,
                        PRICE_PER_SHARE,
                        VALUE,
                        COMMISSION,
                        TransactionData.CURRENCY
                },
                EXCHANGER,
                EXCHANGE_RATE_DATA_SHIFT,
                PRECISION,
                ROUNDING_MODE,
                Settings.JOINED_TRANSACTIONS_FILE_NAME + "_" + EXCHANGER.getToCurrency());

        Write.generateYearStockGainsMapCsvFile(yearStockGainsMap,
                EXCHANGER,
                Settings.GAINS_FILE_NAME + "_" + EXCHANGER.getToCurrency());

        Write.generateDividendTransactionsCsvFile(
                dividendTransactionsList,
                new TransactionData[] {
                        DATE_TIME,
                        TICKER,
                        PRODUCT,
                        TYPE,
                        VALUE,
                        TAX_PAID,
                        TransactionData.CURRENCY
                },
                EXCHANGER,
                EXCHANGE_RATE_DATA_SHIFT,
                PRECISION,
                ROUNDING_MODE,
                Settings.DIVIDEND_TRANSACTIONS_FILE_NAME + "_" + EXCHANGER.getToCurrency()
        );

        Write.generateCalculationSummaryTxtFile(
            yearAllStocksGainsMap,
            yearStockGainsMap,
            getLeftStocksList(),
            joiner.getReport(),
            Settings.SUMMARY_FILE_NAME + "_" + EXCHANGER.getToCurrency()
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

        for (var jt : joinedTransactionsList) {
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
            allStocks.addToTotalSellValue(sell.getValue(EXCHANGER, EXCHANGE_RATE_DATA_SHIFT, PRECISION, ROUNDING_MODE));
            allStocks.addToTotalBuyValue(jt.getTotalBuyValue(EXCHANGER, EXCHANGE_RATE_DATA_SHIFT, PRECISION, ROUNDING_MODE));
            allStocks.addToTotalBuySellCommission(jt.getTotalBuySellCommission(EXCHANGER, EXCHANGE_RATE_DATA_SHIFT, PRECISION, ROUNDING_MODE));

            currStock.addToTotalSellValue(sell.getValue(EXCHANGER, EXCHANGE_RATE_DATA_SHIFT, PRECISION, ROUNDING_MODE));
            currStock.addToTotalBuyValue(jt.getTotalBuyValue(EXCHANGER, EXCHANGE_RATE_DATA_SHIFT, PRECISION, ROUNDING_MODE));
            currStock.addToTotalBuySellCommission(jt.getTotalBuySellCommission(EXCHANGER, EXCHANGE_RATE_DATA_SHIFT, PRECISION, ROUNDING_MODE));
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
            yearDividendGainsMap.putIfAbsent(year, new DividendGainsInfo(year, TO_CURRENCY));
            yearDividendGainsMap.get(year).addToTotalValue(t.getValue(EXCHANGER, EXCHANGE_RATE_DATA_SHIFT, PRECISION, ROUNDING_MODE));
        }
    }
}
