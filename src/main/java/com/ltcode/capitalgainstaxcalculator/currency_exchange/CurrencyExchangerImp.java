package com.ltcode.capitalgainstaxcalculator.currency_exchange;

import com.ltcode.capitalgainstaxcalculator.data_reader.CurrencyExchangeRateReader;
import com.ltcode.capitalgainstaxcalculator.exception.CurrencyExchangeRateReaderException;
import com.ltcode.capitalgainstaxcalculator.exception.ExchangeRateNotFoundException;
import com.ltcode.capitalgainstaxcalculator.transaction.Currency;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class CurrencyExchangerImp implements CurrencyExchanger{

    private final Currency toCurrency;
    /**
     * Directory with .csv files with exchange rates. File pattern is 'fromCurrency_toCurrency.csv' like 'EUR_PLN.csv'
     */
    private final Path dirPath;
    /**
     * <from currency, localDate, exchange rate>
     */
    private final Map<Currency, Map<LocalDate, BigDecimal>> fromCurrencyMap;

    public CurrencyExchangerImp(Currency toCurrency, Path dirPath) {
        this.toCurrency = toCurrency;
        this.dirPath = dirPath;
        this.fromCurrencyMap = new HashMap<>();
    }

    @Override
    public Currency getToCurrency() {
        return toCurrency;
    }

    /**
     * Returns the exchange rate from the date or up to 7 days before
     */
    @Override
    public BigDecimal getRateUpTo7DaysPrevious(Currency from, LocalDate date) {
        if (from == getToCurrency()) {
            return BigDecimal.ONE;
        }
        Map<LocalDate, BigDecimal> exchangeRateMap = fromCurrencyMap.get(from);

        // read from file if no record present
        if (exchangeRateMap == null) {
            try {
                String fileName = String.format("%s_%s.csv", from, toCurrency).toLowerCase();
                exchangeRateMap = CurrencyExchangeRateReader.read(dirPath.resolve(fileName));
            } catch (RuntimeException e) {
                throw new CurrencyExchangeRateReaderException(e.getMessage() + " date: " + date + " from: " + from + " to: " + toCurrency);
            }
            fromCurrencyMap.put(from, exchangeRateMap);
        }

        // find date in map, if not found try to find the day before that until 3 days prior
        // weekends, holidays
        for (int i = 0; i < 6; i++) {
            if (exchangeRateMap.get(date) == null) {
                date = date.minusDays(1);
            } else {
                break;
            }
        }
        if (exchangeRateMap.get(date) == null) {
            throw new ExchangeRateNotFoundException(
                    String.format("Found no exchange rage from currency %s to currency %s on date %s.\n",
                            from, toCurrency, date));
        }
        return exchangeRateMap.get(date);
    }

    @Override
    public BigDecimal getRate(Currency from, LocalDate date) {
        if (from == getToCurrency()) {
            return BigDecimal.ONE;
        }
        Map<LocalDate, BigDecimal> exchangeRateMap = fromCurrencyMap.get(from);

        // read from file if no record present
        if (exchangeRateMap == null) {
            try {
                String fileName = String.format("%s_%s.csv", from, toCurrency).toLowerCase();
                exchangeRateMap = CurrencyExchangeRateReader.read(dirPath.resolve(fileName));
            } catch (RuntimeException e) {
                throw new CurrencyExchangeRateReaderException(e.getMessage());
            }
            fromCurrencyMap.put(from, exchangeRateMap);
        }

        if (exchangeRateMap.get(date) == null) {
            throw new ExchangeRateNotFoundException(
                    String.format("Found no exchange rage from currency %s to currency %s on date %s.\n",
                            from, toCurrency, date));
        }
        return exchangeRateMap.get(date);
    }


}
