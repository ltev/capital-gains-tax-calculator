package com.ltcode.capitalgainstaxcalculator.calculator;

import java.math.BigDecimal;
import java.util.List;

public interface GainsCalculator {

    void calculate();

    StockGainsInfo getTotalGains(int year);

    StockGainsInfo getTotalGains(int year, String ticker);

    DividendGainsInfo getTotalDividends(int year);

    String[] getTickersLeft();

    BigDecimal getQuantityLeft(String ticker);

    List<LeftStockInfo> getLeftStocksList();

    void generateTransactionsCsvFile();
}
