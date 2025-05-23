package com.ltcode.capitalgainstaxcalculator.calculator;

import com.ltcode.capitalgainstaxcalculator.exception.CapitalGainsTaxCalculatorException;
import com.ltcode.capitalgainstaxcalculator.transaction.Currency;
import com.ltcode.capitalgainstaxcalculator.transaction.Transaction;
import com.ltcode.capitalgainstaxcalculator.transaction_converter.TransactionValuesConverter;

import java.math.BigDecimal;

public final class YearSellBuyInfo {

    private final int year;
    private final TransactionValuesConverter valuesConverter;
    private final Currency currency;
    private BigDecimal totalBuyValue;
    private BigDecimal totalSellValue;
    private BigDecimal totalBuyCommissionValue;
    private BigDecimal totalSellCommissionValue;

    public YearSellBuyInfo(int year, TransactionValuesConverter valuesConverter) {
        this.year = year;
        this.valuesConverter = valuesConverter;
        this.currency = valuesConverter.getToCurrency();
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

    // == PACKAGE PRIVATE ==

    void add(Transaction transaction) {
        if (year != transaction.getDateTime().getYear()) {
            throw new CapitalGainsTaxCalculatorException("Invalid year");
        }

        BigDecimal convertedValue = valuesConverter.getValue(transaction);
        BigDecimal convertedCommission = valuesConverter.getCommission(transaction);

        switch (transaction.getType()) {
            case SELL:
                addToTotalSellValue(convertedValue);
                addToTotalSellCommission(convertedCommission);
                break;
            case BUY:
                addToTotalBuyValue(convertedValue);
                addToTotalBuyCommission(convertedCommission);
                break;
            default:
                throw new CapitalGainsTaxCalculatorException("Invalid transaction type");
        }
    }

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
