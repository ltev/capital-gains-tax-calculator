package com.ltcode.capitalgainstaxcalculator.calculator;

import com.ltcode.capitalgainstaxcalculator.transaction.Currency;

import java.math.BigDecimal;

public final class YearGainsInfo {

    private int year;
    private Currency currency;
    private BigDecimal totalBuyValue;
    private BigDecimal totalSellValue;
    private BigDecimal totalBuyCommissionValue;
    private BigDecimal totalSellCommissionValue;

    public YearGainsInfo(int year, Currency currency) {
        this.year = year;
        this.currency = currency;
        this.totalBuyValue = BigDecimal.ZERO;
        this.totalSellValue = BigDecimal.ZERO;
        this.totalBuyCommissionValue = BigDecimal.ZERO;
        this.totalSellCommissionValue = BigDecimal.ZERO;
    }

    public int getYear() {
        return year;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getTotalBuyValue() {
        return totalBuyValue;
    }

    public BigDecimal getTotalSellValue() {
        return totalSellValue;
    }

    public BigDecimal getTotalBuyCommissionValue() {
        return totalBuyCommissionValue;
    }

    public BigDecimal getTotalSellCommissionValue() {
        return totalSellCommissionValue;
    }

    public BigDecimal getTotalBuySellCommissionValue() {
        return totalSellCommissionValue.add(totalBuyCommissionValue);
    }

    public BigDecimal getTotalProfitValue() {
        return totalSellValue
                .subtract(totalBuyValue)
                .subtract(getTotalBuySellCommissionValue());
    }


    // == PACKAGE PRIVATE ==
    void addToTotalBuyValue(BigDecimal buyValue) {
        this.totalBuyValue = this.totalBuyValue.add(buyValue);
    }

    void addToTotalSellValue(BigDecimal sellValue) {
        this.totalSellValue = this.totalSellValue.add(sellValue);
    }

    void addToTotalBuyCommission(BigDecimal commission) {
        this.totalBuyCommissionValue = this.totalBuyCommissionValue.add(commission);
    }

    void addToTotalSellCommission(BigDecimal commission) {
        this.totalSellCommissionValue = this.totalSellCommissionValue.add(commission);
    }

    @Override
    public String toString() {
        return "YearTotalGainsInfo{" +
                "year=" + year +
                ", allBuyValue=" + totalBuyValue +
                ", allSellValue=" + totalSellValue +
                ", allBuyCommissionsValue=" + totalBuyCommissionValue +
                ", allSellCommissionsValue=" + totalSellCommissionValue +
                '}';
    }
}
