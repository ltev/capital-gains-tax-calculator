package com.ltcode.capitalgainstaxcalculator.calculator;

import com.ltcode.capitalgainstaxcalculator.country_info.CountryTaxCalculationInfo;
import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyRateExchanger;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface GainsCalculator {

    void loadFileData();

    void calculate(CountryTaxCalculationInfo countryInfo, LocalDate lastCalculationDate);

    void generateTransactionsCsvFile(Path directory);

    List<BaseGainsCalculator> getBaseGainsCalculators();

    Map<Integer, YearGainsInfo> getTotalGains();

}
