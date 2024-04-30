package com.ltcode.capitalgainstaxcalculator.calculator;

import com.ltcode.capitalgainstaxcalculator.transaction.Currency;

import java.math.BigDecimal;

public class DividendGainsInfo {

    private final int year;

    private final Currency currency;

    private BigDecimal totalValue;

    public DividendGainsInfo(int year, Currency currency) {
        this.year = year;
        this.currency = currency;
        this.totalValue = BigDecimal.ZERO;
    }

    public int getYear() {
        return year;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    // == PACKAGE PRIVATE ==
    void addToTotalValue(BigDecimal value) {
        this.totalValue = this.totalValue.add(value);
    }

    @Override
    public String toString() {
        return "DividendGainsInfo{" +
                "year=" + year +
                ", currency=" + currency +
                ", totalValue=" + totalValue +
                '}';
    }
}
