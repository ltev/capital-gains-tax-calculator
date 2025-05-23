package com.ltcode.capitalgainstaxcalculator.calculator;

import com.ltcode.capitalgainstaxcalculator.transaction.Currency;

import java.math.BigDecimal;

public class StockGainsInfo {

    public static final String CSV_HEADER = "year,ticker,product, sold quantity, sell value, buy value, commission, profit, currency";

    private final int year;
    private final String ticker;
    private final String product;
    private final Currency currency;
    private BigDecimal totalSoldQuantity;
    private BigDecimal totalBuyValue;
    private BigDecimal totalSellValue;
    private BigDecimal totalBuyCommission;
    private BigDecimal totalSellCommission;

    public StockGainsInfo(int year, String ticker, String product, Currency currency) {
        this.year = year;
        this.ticker = ticker;
        this.product = product;
        this.currency = currency;
        this.totalSoldQuantity = BigDecimal.ZERO;
        this.totalBuyValue = BigDecimal.ZERO;
        this.totalSellValue = BigDecimal.ZERO;
        this.totalBuyCommission = BigDecimal.ZERO;
        this.totalSellCommission = BigDecimal.ZERO;
    }

    public int getYear() {
        return year;
    }

    public String getTicker() {
        return ticker;
    }

    public String getProduct() {
        return product;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getTotalSoldQuantity() {return totalSoldQuantity;}

    public BigDecimal getTotalBuyValue() {
        return totalBuyValue;
    }

    public BigDecimal getTotalSellValue() {
        return totalSellValue;
    }

    public BigDecimal getTotalBuyCommission() {
        return totalBuyCommission;
    }

    public BigDecimal getTotalSellCommission() {
        return totalSellCommission;
    }

    public BigDecimal getTotalBuySellCommission() {
        return getTotalBuyCommission().add(getTotalSellCommission());
    }


    // == PACKAGE PRIVATE ==
    void addToTotalSoldQuantity(BigDecimal soldQuantity) {
        this.totalSoldQuantity = this.totalSoldQuantity.add(soldQuantity);
    }

    void addToTotalBuyValue(BigDecimal buyValue) {
        this.totalBuyValue = this.totalBuyValue.add(buyValue);
    }

    void addToTotalSellValue(BigDecimal sellValue) {
        this.totalSellValue = this.totalSellValue.add(sellValue);
    }

    void addToTotalBuyCommission(BigDecimal commission) {
        this.totalBuyCommission = this.totalBuyCommission.add(commission);
    }

    void addToTotalSellCommission(BigDecimal commission) {
        this.totalSellCommission = this.totalSellCommission.add(commission);
    }

    public BigDecimal getTotalProfitValue() {
        return totalSellValue.subtract(totalBuyValue).subtract(getTotalBuySellCommission());
    }

    public String generateCsvLine() {
        final char SEPARATOR = ',';
        StringBuilder sb = new StringBuilder();

        Object[] data = new Object[9];
        data[0] = year;
        data[1] = ticker;
        data[2] = "\"" + product + "\"";
        data[3] = totalSoldQuantity;
        data[4] = totalSellValue;
        data[5] = totalBuyValue;
        data[6] = getTotalBuySellCommission();
        data[7] = getTotalProfitValue();
        data[8] = currency;

        for (Object d : data) {
            sb.append(SEPARATOR)
                    .append(d);
        }
        return sb.substring(1);
    }

    @Override
    public String toString() {
        return "StockGainsInfo{" +
                "year=" + year +
                ", ticker='" + ticker + '\'' +
                ", totalBuyValue=" + totalBuyValue +
                ", totalSellValue=" + totalSellValue +
                ", profit=" + getTotalProfitValue() +
                '}';
    }


}
