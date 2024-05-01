package com.ltcode.capitalgainstaxcalculator;

import com.ltcode.capitalgainstaxcalculator.broker.Broker;
import com.ltcode.capitalgainstaxcalculator.data_reader.TransactionReader;
import com.ltcode.capitalgainstaxcalculator.transaction.*;
import com.ltcode.capitalgainstaxcalculator.transaction.BuySellTransactionBuilder;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestSettings {

    public enum SOURCE {
        CSV_FILE,
        HERE
    }
    public static final String SRC_TEST_DATA = "src\\test\\test_data";
    public static final Path USD_PLN_TEST_PATH = Path.of(SRC_TEST_DATA, "usd_pln.csv");
    public static final Path REVOLUT_STOCK_TEST_PATH = Path.of(SRC_TEST_DATA,"trading-account-statement_test.csv");

    public static final Path DEGIRO_ACOUNT_DIVIDEND_2021_TEST_PATH = Path.of(SRC_TEST_DATA, "dividend_2021.csv");

    public static final BuySellTransaction SAMPLE_BUY_SELL_TRANSACTION = new BuySellTransactionBuilder()
            .setType(TransactionType.BUY)
            .setDateTime(LocalDateTime.of(2020, 1, 7, 10, 10))
            .setTicker("XX")
            .setQuantity(new BigDecimal("5"))
            .setPricePerShare(new BigDecimal("2.2"))
            .setValue(new BigDecimal("11"))
            .setCurrency(Currency.USD)
            .build();

    public static final BuySellTransaction SAMPLE_BUY_SELL_WITH_COMMISSION_TRANSACTION = new BuySellTransactionBuilder()
            .setType(TransactionType.BUY)
            .setDateTime(LocalDateTime.of(2020, 1, 7, 10, 10))
            .setTicker("XX")
            .setQuantity(new BigDecimal("5"))
            .setPricePerShare(new BigDecimal("2.2"))
            .setCommission(new BigDecimal("1"))
            .setValue(new BigDecimal("11"))
            .setCurrency(Currency.USD)
            .build();

    public static List<? extends Transaction> getTransactionList(SOURCE source) {
        return switch (source) {
            case HERE -> getTransactionList();
            case CSV_FILE -> TransactionReader.read(Broker.REVOLUT, REVOLUT_STOCK_TEST_PATH);
        };
    }

    public static BuySellTransaction getDumbTransaction(LocalDateTime dateTime) {
        return getTransaction(TransactionType.BUY, dateTime, "A", BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, Currency.USD);
    }

    // == PRIVATE METHODS ==

    private static List<? extends Transaction> getTransactionList() {
        List<BuySellTransaction> list = new ArrayList<>();
            // TICKER A - sell all at once
            // total buy and sell is only counted for stocks that were sold
            // total buy 2020       -> 610
            // total sell 2020      -> 300
            // total profit 2020    -> -310
        var a1 = getTransaction(
                TransactionType.BUY,
                LocalDateTime.of(2020, 1, 1, 10, 10),
                "A",
                new BigDecimal("10"),
                new BigDecimal("1"),
                new BigDecimal("10"),
                Currency.USD);
        var a2 = getTransaction(
                TransactionType.BUY,
                LocalDateTime.of(2020, 1, 2, 10, 10),
                "A",
                new BigDecimal("20"),
                new BigDecimal("30"),
                new BigDecimal("600"),
                Currency.USD);

        var a3 = getTransaction(
                TransactionType.SELL,
                LocalDateTime.of(2020, 1, 3, 10, 10),
                "A",
                new BigDecimal("30"),
                new BigDecimal("10"),
                new BigDecimal("300"),
                Currency.USD);

        // TICKER B - SELL 1.5 transaction, quantity left 18.5
        // total buy 2020       -> 275
        // total sell 2020      -> 200
        // total profit 2020    -> -70
        // quantity left        -> 18.5

        var b1 = getTransaction(
                TransactionType.BUY,
                LocalDateTime.of(2020, 1, 1, 8, 10),
                "B",
                new BigDecimal("2.5"),
                new BigDecimal("100"),
                new BigDecimal("250"),
                Currency.USD);
        var b2 = getTransaction(
                TransactionType.BUY,
                LocalDateTime.of(2020, 2, 2, 10, 10),
                "B",
                new BigDecimal("4"),
                new BigDecimal("50"),
                new BigDecimal("200"),
                Currency.USD);

        var b3 = getTransaction(
                TransactionType.SELL,
                LocalDateTime.of(2020, 2, 3, 10, 10),
                "B",
                new BigDecimal("3"),
                new BigDecimal("120"),
                new BigDecimal("360"),
                Currency.USD);

        var b4 = getTransaction(
                TransactionType.BUY,
                LocalDateTime.of(2020, 3, 2, 10, 10),
                "B",
                new BigDecimal("15"),
                new BigDecimal("20"),
                new BigDecimal("300"),
                Currency.USD);

        // TICKER C - SELLING TOO MUCH
        // total buy 2021       -> 300
        // total sell 2021      -> 300
        // total profit 2020    -> 0
        // quantity left        -> 2
        // total buy 2022       -> 200 + 5
        // total sell 2022      -> 50
        // total profit 2022    -> 0
        // quantity left        -> 0.5

        var c1 = getTransaction(
                TransactionType.BUY,
                LocalDateTime.of(2020, 12, 1, 8, 10),
                "C",
                new BigDecimal("2.5"),
                new BigDecimal("100"),
                new BigDecimal("250"),
                Currency.USD);

        var c2 = getTransaction(
                TransactionType.BUY,
                LocalDateTime.of(2021, 1, 1, 9, 10),
                "C",
                new BigDecimal("2.5"),
                new BigDecimal("100"),
                new BigDecimal("250"),
                Currency.USD);

        var c3 = getTransaction(
                TransactionType.SELL,
                LocalDateTime.of(2021, 1, 1, 10, 10),
                "C",
                new BigDecimal("3"),
                new BigDecimal("100"),
                new BigDecimal("300"),
                Currency.USD);

        var c4 = getTransaction(
                TransactionType.BUY,
                LocalDateTime.of(2022, 1, 1, 9, 10),
                "C",
                new BigDecimal("1"),
                new BigDecimal("10"),
                new BigDecimal("10"),
                Currency.USD);

        var c5 = getTransaction(
                TransactionType.SELL,
                LocalDateTime.of(2022, 1, 1, 10, 10),
                "C",
                new BigDecimal("2.5"),
                new BigDecimal("20"),
                new BigDecimal("50"),
                Currency.USD);

        // chronological order

        list.add(b1);
        list.add(a1);
        list.add(a2);
        list.add(a3);
        list.add(b2);
        list.add(b3);
        list.add(b4);
        list.add(c1);
        list.add(c2);
        list.add(c3);
        list.add(c4);
        list.add(c5);

        return list;

    }

    private static BuySellTransaction getTransaction(TransactionType type, LocalDateTime dateTime, String ticker,
                                                     BigDecimal quantity, BigDecimal pricePerShare, BigDecimal value,
                                                     Currency currency) {
        return new BuySellTransactionBuilder()
                .setType(type)
                .setDateTime(dateTime)
                .setTicker(ticker)
                .setQuantity(quantity)
                .setPricePerShare(pricePerShare)
                .setValue(value)
                .setCurrency(currency)
                .setCommission(BigDecimal.ZERO)
                .build();
    }


}
