package com.ltcode.capitalgainstaxcalculator.country_info;

import com.ltcode.capitalgainstaxcalculator.exception.CountryInfoException;
import com.ltcode.capitalgainstaxcalculator.transaction.Currency;

import java.math.RoundingMode;
import java.time.Period;

public final class CountryTaxCalculationInfo {

    private final Country country;
    private final Currency currency;
    private final int precision;
    private final RoundingMode roundingMode;
    private final Period dateShift;

    public CountryTaxCalculationInfo(Country country, Currency currency, int precision, RoundingMode roundingMode, Period dateShift) {
        this.country = country;
        this.currency = currency;
        this.precision = precision;
        this.roundingMode = roundingMode;
        this.dateShift = dateShift;
    }

    public static CountryTaxCalculationInfo getInstance(Country country) {
        final Currency currency;
        final int precision;
        final RoundingMode rounding;
        /*
         * difference in days between when the transaction happened and what day for exchange rate to take
         * dataShift of value minus one (-1) means that the exchange rate should be from the previous day
         */
        final Period dataShift;

        switch (country) {
            case POLAND:
                currency = Currency.PLN;
                precision = 2;
                rounding = RoundingMode.HALF_UP;
                dataShift = Period.ofDays(-1);
                break;
            case GERMANY:
                currency = Currency.EUR;
                precision = 2;
                rounding = RoundingMode.HALF_UP;
                dataShift = Period.ofDays(0);
                break;
            default:
                throw new CountryInfoException("No dataShift for country: " + country);
        }

        return new CountryTaxCalculationInfo(country, currency, precision, rounding, dataShift);
    }

    public Country getCountry() {
        return country;
    }

    public Currency getCurrency() {
        return currency;
    }

    public int getPrecision() {
        return precision;
    }

    public RoundingMode getRoundingMode() {
        return roundingMode;
    }

    public Period getDateShift() {
        return dateShift;
    }
}
