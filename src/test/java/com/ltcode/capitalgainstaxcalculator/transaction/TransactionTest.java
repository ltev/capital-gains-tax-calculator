package com.ltcode.capitalgainstaxcalculator.transaction;

import com.ltcode.capitalgainstaxcalculator.TestSettings;
import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyRateExchanger;
import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyRateExchangerImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Period;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    CurrencyRateExchanger toPlnExchanger;
    CurrencyRateExchanger toEuroExchanger;
    Period dateShift = Period.ofDays(0);
    int precision =4;
    RoundingMode roundingMode = RoundingMode.HALF_UP;

    @BeforeEach
    void setUp() {
        Path dirPath = Paths.get(TestSettings.SRC_TEST_DATA);
        toPlnExchanger = new CurrencyRateExchangerImp(Currency.PLN, dirPath);
        toEuroExchanger = new CurrencyRateExchangerImp(Currency.EUR, dirPath);
    }

    @Test
    void getValue_InPln() {
        var transaction = TestSettings.SAMPLE_BUY_SELL_TRANSACTION;  // 7-1-2020 rate from 3-1-2020 = 3.8213 value=11 * rate = 42.0343
        assertEquals(new BigDecimal("42.0343"), transaction.getValue(toPlnExchanger, dateShift, precision, roundingMode));
    }

    @Test
    void getValue_InEuro() {
        var transaction = TestSettings.SAMPLE_BUY_SELL_TRANSACTION; // 7-1-2020 rate from 3-1-2020 = 0.97 value=11 * rate = 42.0343
        assertEquals(new BigDecimal("10.67"), transaction.getValue(toEuroExchanger, dateShift, precision, roundingMode));
    }
}