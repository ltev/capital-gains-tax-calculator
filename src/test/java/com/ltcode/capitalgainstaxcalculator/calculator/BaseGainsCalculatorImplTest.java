package com.ltcode.capitalgainstaxcalculator.calculator;

import com.ltcode.capitalgainstaxcalculator.TestSettings;
import com.ltcode.capitalgainstaxcalculator.country_info.CountryTaxCalculationInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Period;

import static com.ltcode.capitalgainstaxcalculator.transaction.Currency.USD;
import static org.junit.jupiter.api.Assertions.*;

class
BaseGainsCalculatorImplTest {

    BaseGainsCalculatorImpl calculator;

    @BeforeEach
    void setUp() {
        calculator = new BaseGainsCalculatorImpl(
                new CountryTaxCalculationInfo(null, USD, 2, RoundingMode.HALF_UP, Period.ofDays(0))
        );
        calculator.calculate(TestSettings.getTransactionList(TestSettings.SOURCE.HERE));
    }

    @Test
    void print() {
         var tickers= calculator.getTickersLeft();
        for (String ticker : tickers) {
            System.out.println(ticker + " " + calculator.getQuantityLeft(ticker));
        }
    }

    @Test
    void generateTransactionsCsvFile() {
        // calculator.generateTransactionsCsvFile();
    }

    @Test
    void testGetTotalGains_OneStock() {
        var gainsA = calculator.getTotalGains(2020, "A");
        assertEquals(new BigDecimal(610), gainsA.getTotalBuyValue());
        assertEquals(new BigDecimal(300), gainsA.getTotalSellValue());
        assertEquals(new BigDecimal(-310), gainsA.getTotalProfitValue());

        var gainsB = calculator.getTotalGains(2020, "B");
        assertEquals(new BigDecimal("275.0000000000"), gainsB.getTotalBuyValue());
        assertEquals(new BigDecimal(360), gainsB.getTotalSellValue());
        assertEquals(new BigDecimal("85.0000000000"), gainsB.getTotalProfitValue());

        var gainsC = calculator.getTotalGains(2021, "C");
        assertEquals(new BigDecimal("300.0000000000"), gainsC.getTotalBuyValue());
        assertEquals(new BigDecimal(300), gainsC.getTotalSellValue());
        assertEquals(new BigDecimal("0.0000000000"), gainsC.getTotalProfitValue());

        gainsC = calculator.getTotalGains(2022, "C");
        assertEquals(new BigDecimal("205.0000000000"), gainsC.getTotalBuyValue());
        assertEquals(new BigDecimal(50), gainsC.getTotalSellValue());
        assertEquals(new BigDecimal("-155.0000000000"), gainsC.getTotalProfitValue());
    }

    @Test
    void testGetTotalGains_AllStocksTogether() {
        var gains2020 = calculator.getTotalGains(2020);
        assertEquals(new BigDecimal("885.0000000000"), gains2020.getTotalBuyValue());
        assertEquals(new BigDecimal(660), gains2020.getTotalSellValue());
        assertEquals(new BigDecimal("-225.0000000000"), gains2020.getTotalProfitValue());

        var gains2021 = calculator.getTotalGains(2021);
        assertEquals(new BigDecimal("300.0000000000"), gains2021.getTotalBuyValue());
        assertEquals(new BigDecimal(300), gains2021.getTotalSellValue());
        assertEquals(new BigDecimal("0.0000000000"), gains2021.getTotalProfitValue());

        var gains2022 = calculator.getTotalGains(2022);
        assertEquals(new BigDecimal("205.0000000000"), gains2022.getTotalBuyValue());
        assertEquals(new BigDecimal(50), gains2022.getTotalSellValue());
        assertEquals(new BigDecimal("-155.0000000000"), gains2022.getTotalProfitValue());
    }

    @Test
    void getTransactionsLeft() {
        assertArrayEquals(new String[] {"B", "C"}, calculator.getTickersLeft());
    }

    @Test
    void getQuantityLeft() {
        assertEquals(BigDecimal.ZERO, calculator.getQuantityLeft("A"));
        assertEquals(new BigDecimal("18.5"), calculator.getQuantityLeft("B"));
        assertEquals(new BigDecimal("0.5"), calculator.getQuantityLeft("C"));
    }

    @Test
    void testGenerateTransactionsCsvFile() {
    }
}