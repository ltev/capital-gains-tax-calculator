package com.ltcode.capitalgainstaxcalculator.transaction;

import com.ltcode.capitalgainstaxcalculator.exception.InvalidDateOrderException;
import com.ltcode.capitalgainstaxcalculator.exception.InvalidTypeException;
import com.ltcode.capitalgainstaxcalculator.exception.InvalidQuantityException;
import com.ltcode.capitalgainstaxcalculator.transaction.joined.JoinedTransaction;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;


class JoinedTransactionTest {

    static BuySellTransaction sellTransaction;
    static BuySellTransaction buyTransaction1;
    static BuySellTransaction buyTransaction2;

    @BeforeAll
    static void setUp() {
        sellTransaction = new BuySellTransactionBuilder()
                .setDateTime(LocalDateTime.of(2020, 1, 1, 10, 10))
                .setType(TransactionType.SELL)
                .setTicker("A")
                .setQuantity(new BigDecimal(15))
                .setPricePerShare(new BigDecimal("1"))
                .setValue(new BigDecimal("15"))
                .setCurrency(Currency.USD)
                .build();
        buyTransaction1 = new BuySellTransactionBuilder()
                .setDateTime(LocalDateTime.of(2020, 1, 1, 10, 1))
                .setType(TransactionType.BUY)
                .setTicker("A")
                .setQuantity(new BigDecimal(3))
                .setPricePerShare(new BigDecimal("1"))
                .setValue(new BigDecimal("3"))
                .setCurrency(Currency.USD)
                .build();
        buyTransaction2 = new BuySellTransactionBuilder()
                .setDateTime(LocalDateTime.of(2020, 1, 1, 10, 2))
                .setType(TransactionType.BUY)
                .setTicker("A")
                .setQuantity(new BigDecimal(12))
                .setPricePerShare(new BigDecimal("1"))
                .setValue(new BigDecimal("12"))
                .setCurrency(Currency.USD)
                .build();
    }

    @Test
    void typeCheckTest() {
        System.out.println(sellTransaction + " ddd ");
        BuySellTransaction sell = new BuySellTransactionBuilder(sellTransaction).setType(TransactionType.BUY).build();
        BuySellTransaction buy = new BuySellTransactionBuilder(buyTransaction1).setType(TransactionType.SELL).build();

        assertThrows(InvalidTypeException.class, () -> new JoinedTransaction(sell, List.of(buyTransaction1, buyTransaction2)));
        assertThrows(InvalidTypeException.class, () -> new JoinedTransaction(sellTransaction, List.of(buy, buyTransaction2)));
    }

    @Test
    void quantityAndOrderCheckTest_Valid() {
        var info = new JoinedTransaction(sellTransaction, List.of(buyTransaction1, buyTransaction2));
    }

    @Test
    void quantityCheckTest_Invalid() {
        buyTransaction2 = new BuySellTransactionBuilder()
                .setDateTime(LocalDateTime.of(2020, 1, 1, 10, 2))
                .setType(TransactionType.BUY)
                .setTicker("A")
                .setQuantity(new BigDecimal(11))
                .setPricePerShare(new BigDecimal("1"))
                .setValue(new BigDecimal("11"))
                .setCurrency(Currency.USD)
                .build();
        assertThrows(InvalidQuantityException.class, () -> new JoinedTransaction(sellTransaction, List.of(buyTransaction1, buyTransaction2)));
    }

    @Test
    void chronologicalOrderTest_Invalid() {
        buyTransaction2 = new BuySellTransactionBuilder()
                .setDateTime(LocalDateTime.of(2020, 1, 1, 10, 2))
                .setType(TransactionType.BUY)
                .setTicker("A")
                .setQuantity(new BigDecimal(12))
                .setPricePerShare(new BigDecimal("1"))
                .setValue(new BigDecimal("12"))
                .setCurrency(Currency.USD)
                .build();
        assertThrows(InvalidDateOrderException.class, () -> new JoinedTransaction(sellTransaction, List.of(buyTransaction2, buyTransaction1)));
    }
}