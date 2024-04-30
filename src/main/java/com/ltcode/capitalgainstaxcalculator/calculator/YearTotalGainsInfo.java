package com.ltcode.capitalgainstaxcalculator.calculator;

import java.math.BigDecimal;

public final class YearTotalGainsInfo {

    private final int year;
    private final BigDecimal allBuyValue;
    private final BigDecimal allSellValue;
    private final BigDecimal allBuyCommissionsValue;
    private final BigDecimal allSellCommissionsValue;

    public YearTotalGainsInfo(int year, BigDecimal allBuyValue, BigDecimal allSellValue, BigDecimal allBuyCommissionsValue, BigDecimal allSellCommissionsValue) {
        this.year = year;
        this.allBuyValue = allBuyValue;
        this.allSellValue = allSellValue;
        this.allBuyCommissionsValue = allBuyCommissionsValue;
        this.allSellCommissionsValue = allSellCommissionsValue;
    }

    public int getYear() {
        return year;
    }

    public BigDecimal getAllBuyValue() {
        return allBuyValue;
    }

    public BigDecimal getAllSellValue() {
        return allSellValue;
    }

    public BigDecimal getAllBuyCommissionsValue() {
        return allBuyCommissionsValue;
    }

    public BigDecimal getAllSellCommissionsValue() {
        return allSellCommissionsValue;
    }

    @Override
    public String toString() {
        return "YearTotalGainsInfo{" +
                "year=" + year +
                ", allBuyValue=" + allBuyValue +
                ", allSellValue=" + allSellValue +
                ", allBuyCommissionsValue=" + allBuyCommissionsValue +
                ", allSellCommissionsValue=" + allSellCommissionsValue +
                '}';
    }
}
