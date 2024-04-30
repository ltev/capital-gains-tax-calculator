package com.ltcode.capitalgainstaxcalculator.currency_exchange;

import com.ltcode.capitalgainstaxcalculator.TestSettings;
import com.ltcode.capitalgainstaxcalculator.exception.CurrencyExchangeRateReaderException;
import com.ltcode.capitalgainstaxcalculator.exception.ExchangeRateNotFoundException;
import com.ltcode.capitalgainstaxcalculator.transaction.Currency;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Period;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CurrencyExchangerImpTest {

    CurrencyExchanger exchanger;

    @BeforeAll
    void setUp() {
        exchanger = new CurrencyExchangerImp(
                Currency.PLN,
                Path.of(TestSettings.SRC_TEST_DATA));
    }

    @Test
    void getExchangeRate_FileWithDataNotFound() {
        assertThrows(CurrencyExchangeRateReaderException.class, () -> exchanger.getRate(Currency.JPY, LocalDate.now()));
    }

    @Test
    void getExchangeRate_ValidDate() {
        Currency FROM_CURRENCY = Currency.USD;
        assertEquals(new BigDecimal("3.7584"), exchanger.getRate(FROM_CURRENCY, LocalDate.of(2020, 12,31)));
        assertEquals(new BigDecimal("3.663"), exchanger.getRate(FROM_CURRENCY, LocalDate.parse("2020-12-11")));
        assertEquals(new BigDecimal("3.9121"), exchanger.getRate(FROM_CURRENCY, LocalDate.parse("2020-06-03")));

        FROM_CURRENCY = Currency.EUR;
        assertEquals(new BigDecimal("4.4"), exchanger.getRate(FROM_CURRENCY, LocalDate.of(2021, 12,31)));
        assertEquals(new BigDecimal("4.5"), exchanger.getRate(FROM_CURRENCY, LocalDate.parse("2021-12-30")));
        assertEquals(new BigDecimal("4.51"), exchanger.getRate(FROM_CURRENCY, LocalDate.parse("2021-12-28")));
    }

    @Test
    void getExchangeRate_InvalidDate() {
        assertThrows(ExchangeRateNotFoundException.class, () -> exchanger.getRate(Currency.USD, LocalDate.of(2019, 12,31)));
        assertThrows(ExchangeRateNotFoundException.class, () -> exchanger.getRate(Currency.USD, LocalDate.of(2021, 1,11)));
        assertThrows(ExchangeRateNotFoundException.class, () -> exchanger.getRate(Currency.USD, LocalDate.of(2020, 12,25)));

        assertThrows(ExchangeRateNotFoundException.class, () -> exchanger.getRate(Currency.EUR, LocalDate.of(2020, 12,25)));
    }

    @Test
    void getPrevUpTo7DaysRate1() {
        Currency FROM_CURRENCY = Currency.USD;
        assertEquals(new BigDecimal("3.6901"), exchanger.getRateUpTo7DaysPrevious(FROM_CURRENCY, LocalDate.of(2020, 12,31).plus(Period.ofDays(-1))));
        assertEquals(new BigDecimal("3.6574"), exchanger.getRateUpTo7DaysPrevious(FROM_CURRENCY, LocalDate.parse("2020-12-11").plus(Period.ofDays(-1))));
        assertEquals(new BigDecimal("3.663"), exchanger.getRateUpTo7DaysPrevious(FROM_CURRENCY, LocalDate.parse("2020-12-14").plus(Period.ofDays(-1))));

        FROM_CURRENCY = Currency.EUR;
        assertEquals(new BigDecimal("4.5"), exchanger.getRateUpTo7DaysPrevious(FROM_CURRENCY, LocalDate.of(2021, 12,31).plus(Period.ofDays(-1))));
        assertEquals(new BigDecimal("4.55"), exchanger.getRateUpTo7DaysPrevious(FROM_CURRENCY, LocalDate.parse("2021-12-30").plus(Period.ofDays(-1))));
    }

    @Test
    void getPrevUpTo7DaysRate2() {
        assertThrows(ExchangeRateNotFoundException.class, () -> exchanger.getRateUpTo7DaysPrevious(Currency.USD, LocalDate.of(2019, 12,31)));
        assertThrows(ExchangeRateNotFoundException.class, () -> exchanger.getRateUpTo7DaysPrevious(Currency.USD, LocalDate.of(2021, 1,8)));
    }
}