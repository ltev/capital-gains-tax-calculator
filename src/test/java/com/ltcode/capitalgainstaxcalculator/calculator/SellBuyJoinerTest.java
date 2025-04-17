package com.ltcode.capitalgainstaxcalculator.calculator;

import com.ltcode.capitalgainstaxcalculator.TestSettings;
import com.ltcode.capitalgainstaxcalculator.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SellBuyJoinerTest {

    SellBuyJoiner joiner;

    @BeforeEach
    void setUp() {
        List<? extends Transaction> transactionList = TestSettings.getTransactionList(TestSettings.SOURCE.HERE);
        joiner = new SellBuyJoiner(transactionList, LocalDate.now(), 2, RoundingMode.HALF_UP);
        joiner.join();
    }

    @Test
    void joinTest_SoldStocks() {
        var list = joiner.join();
        assertEquals(4, list.size());
    }

    @Test
    void joinTest_LeftQuantities() {
        assertEquals(BigDecimal.ZERO, joiner.getQuantityLeft("A"));
        assertEquals(new BigDecimal("18.5"), joiner.getQuantityLeft("B"));
        assertEquals(new BigDecimal("0.5"), joiner.getQuantityLeft("C"));
    }

    @Test
    void joinTest_LeftStocks() {
        var list = joiner.join();

        var buyListA = joiner.getLeftTransaction("A");
        var buyListB = joiner.getLeftTransaction("B");
        var buyListC = joiner.getLeftTransaction("C");

        assertEquals(0, buyListA.size());
        assertEquals(2, buyListB.size());
        assertEquals(1, buyListC.size());

        assertEquals(3.5, buyListB.get(0).getQuantity().doubleValue());
        assertEquals(0.5, buyListC.get(0).getQuantity().doubleValue());
    }

    @Test
    void getTickersLeft() {
        var list = joiner.join();

        assertArrayEquals(new String[]{"B", "C"}, joiner.getTickersLeft());
    }

    @Test
    void getQuantityLeft() {
        assertEquals(BigDecimal.ZERO, joiner.getQuantityLeft("A"));
        assertEquals(new BigDecimal("18.5"), joiner.getQuantityLeft("B"));
        assertEquals(new BigDecimal("0.5"), joiner.getQuantityLeft("C"));
    }
}