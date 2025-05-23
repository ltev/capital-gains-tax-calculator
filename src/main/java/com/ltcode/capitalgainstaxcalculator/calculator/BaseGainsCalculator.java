package com.ltcode.capitalgainstaxcalculator.calculator;

import com.ltcode.capitalgainstaxcalculator.broker.FileInfo;
import com.ltcode.capitalgainstaxcalculator.country_info.CountryTaxCalculationInfo;
import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyRateExchanger;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Makes calculations for one specific file
 */
public interface BaseGainsCalculator {

    FileInfo getFileInfo();

    void loadFileData();

    void calculate(CountryTaxCalculationInfo countryInfo, LocalDate lastCalculationDate, CurrencyRateExchanger exchanger);

    Map<Integer, YearGainsInfo> getTotalGains();

    YearGainsInfo getTotalGains(int year);

    Map<Integer, YearSellBuyInfo> getTotalSellsBuys();

    YearSellBuyInfo getTotalSellsBuys(int year);

    StockGainsInfo getTotalGains(int year, String ticker);

    DividendGainsInfo getTotalDividends(int year);

    String[] getTickersLeft();

    BigDecimal getQuantityLeft(String ticker);

    List<LeftStockInfo> getLeftStocksList();

    void generateTransactionsCsvFile(Path directory);
}
