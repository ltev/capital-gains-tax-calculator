package com.ltcode.capitalgainstaxcalculator.calculator;

import com.ltcode.capitalgainstaxcalculator.broker.FileInfo;
import com.ltcode.capitalgainstaxcalculator.country_info.CountryTaxCalculationInfo;
import com.ltcode.capitalgainstaxcalculator.data_reader.data_writer.Write;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Makes calculations for many files
 */
public class GainsCalculatorImpl implements GainsCalculator {

    private final CountryTaxCalculationInfo countryInfo;
    private BaseGainsCalculator[] baseCalculators;

    public GainsCalculatorImpl(CountryTaxCalculationInfo countryInfo) {
        this.countryInfo = countryInfo;
    }

    @Override
    public void calculate(FileInfo... fileInfoArr) {
        this.baseCalculators = new BaseGainsCalculator[fileInfoArr.length];
        for (int i = 0; i < baseCalculators.length; i++) {
            baseCalculators[i] = new BaseGainsCalculatorImpl(countryInfo);
            baseCalculators[i].calculate(fileInfoArr[i]);
        }
    }

    @Override
    public void generateTransactionsCsvFile(Path directory) {
        for (var baseCalculator : baseCalculators) {
            baseCalculator.generateTransactionsCsvFile(directory);
        }
        Write.generateCalculationSummaryTxtFile(
                getTotalGains(),
                List.of(),
                "",
                directory.resolve("summary_all_brokers.txt")
        );
    }

    @Override
    public Map<Integer, YearGainsInfo> getTotalGains() {
        Map<Integer, YearGainsInfo> totalGainsMap = new LinkedHashMap<>();

        for (var baseCalculator : baseCalculators) {
            var brokerYearsGains = baseCalculator.getTotalGains();
            for (var currYear : brokerYearsGains.keySet()) {
                totalGainsMap.putIfAbsent(currYear, new YearGainsInfo(currYear, countryInfo.getCurrency()));
                var totalYearGains = totalGainsMap.get(currYear);

                var brokerYearGains = brokerYearsGains.get(currYear);
                totalYearGains.addToTotalBuyValue(brokerYearGains.getTotalBuyValue());
                totalYearGains.addToTotalSellValue(brokerYearGains.getTotalSellValue());
                totalYearGains.addToTotalBuyCommission(brokerYearGains.getTotalBuyCommissionValue());
                totalYearGains.addToTotalSellCommission(brokerYearGains.getTotalSellCommissionValue());
            }
        }
        return  totalGainsMap;
    }
}
