package com.ltcode.capitalgainstaxcalculator.calculator;

import com.ltcode.capitalgainstaxcalculator.transaction.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StockGainsInfoTest {

    StockGainsInfo info;

    @BeforeEach
    void setUp() {
        this.info = new StockGainsInfo(2021, "XXX", null, Currency.USD);
    }

    @Test
    void addToTotalBuyValue() {
        info.addToTotalBuyValue(new BigDecimal("1"));
        assertEquals(new BigDecimal("1"), info.getTotalBuyValue());
        info.addToTotalBuyValue(new BigDecimal("1.789"));
        assertEquals(new BigDecimal("2.789"), info.getTotalBuyValue());
    }

    @Test
    void addToTotalSellValue() {
        info.addToTotalSellValue(new BigDecimal("1"));
        assertEquals(new BigDecimal("1"), info.getTotalSellValue());
        info.addToTotalSellValue(new BigDecimal("1.789"));
        assertEquals(new BigDecimal("2.789"), info.getTotalSellValue());
    }

    /*
    @Test
    void addToTotalBuyCommission() {
        info.addToTotalBuyCommission(new BigDecimal("1"));
        assertEquals(new BigDecimal("1"), info.getTotalBuyCommission());
        info.addToTotalBuyCommission(new BigDecimal("1.789"));
        assertEquals(new BigDecimal("2.789"), info.getTotalBuyCommission());
    }

    @Test
    void addToTotalSellCommission() {
        info.addToTotalSellCommission(new BigDecimal("1"));
        assertEquals(new BigDecimal("1"), info.getTotalSellCommission());
        info.addToTotalSellCommission(new BigDecimal("1.789"));
        assertEquals(new BigDecimal("2.789"), info.getTotalSellCommission());
    }
     */
}