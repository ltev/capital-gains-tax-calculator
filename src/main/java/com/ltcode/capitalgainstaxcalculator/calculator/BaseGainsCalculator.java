package com.ltcode.capitalgainstaxcalculator.calculator;

import com.ltcode.capitalgainstaxcalculator.broker.FileInfo;
import com.ltcode.capitalgainstaxcalculator.transaction.Transaction;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Makes calculations for one specific file
 */
public interface BaseGainsCalculator {

    void calculate(FileInfo fileInfo);

    void calculate(List<? extends Transaction> transactions);

    Map<Integer, YearGainsInfo> getTotalGains();

    YearGainsInfo getTotalGains(int year);

    StockGainsInfo getTotalGains(int year, String ticker);

    DividendGainsInfo getTotalDividends(int year);

    String[] getTickersLeft();

    BigDecimal getQuantityLeft(String ticker);

    List<LeftStockInfo> getLeftStocksList();

    void generateTransactionsCsvFile(Path directory);
}
