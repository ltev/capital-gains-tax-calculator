package com.ltcode.capitalgainstaxcalculator.calculator;

import com.ltcode.capitalgainstaxcalculator.broker.FileInfo;
import com.ltcode.capitalgainstaxcalculator.country_info.CountryTaxCalculationInfo;
import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyRateExchanger;
import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyRateExchangerImp;
import com.ltcode.capitalgainstaxcalculator.data_reader.data_writer.Write;
import com.ltcode.capitalgainstaxcalculator.exception.CapitalGainsTaxCalculatorException;
import com.ltcode.capitalgainstaxcalculator.settings.Settings;
import com.ltcode.capitalgainstaxcalculator.transaction.Currency;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Makes calculations for many files
 */
public class GainsCalculatorImpl implements GainsCalculator {

    private final List<BaseGainsCalculator> baseCalculators;
    private final CurrencyRateExchanger exchanger;
    private CountryTaxCalculationInfo countryInfo;

    public GainsCalculatorImpl(FileInfo... fileInfos) {
        if (fileInfos == null || fileInfos.length == 0) {
            throw new CapitalGainsTaxCalculatorException("No files information supplied.");
        }

        List<BaseGainsCalculator> tempCalculators = new ArrayList<>();
        for (FileInfo fileInfo : fileInfos) {
            BaseGainsCalculator baseCalculator = new BaseGainsCalculatorImpl(fileInfo);
            tempCalculators.add(baseCalculator);
        }
        this.baseCalculators = tempCalculators;
        this.exchanger = new CurrencyRateExchangerImp(Currency.PLN, Settings.EXCHANGE_RATES_DATA_PATH);
    }


    @Override
    public void loadFileData() {
        baseCalculators.forEach(BaseGainsCalculator::loadFileData);
    }

    @Override
    public void calculate(CountryTaxCalculationInfo countryInfo, LocalDate lastCalculationDate) {
        this.countryInfo = countryInfo;
        baseCalculators.forEach(bc -> bc.calculate(countryInfo, lastCalculationDate, exchanger));
    }

    @Override
    public void generateTransactionsCsvFile(Path directory) {
        for (var baseCalculator : baseCalculators) {
            baseCalculator.generateTransactionsCsvFile(directory);
        }
        Write.generateCalculationGainsSummaryTxtFile(
                getTotalGains(),
                List.of(),
                "",
                directory.resolve("summary_all_brokers.txt")
        );
    }

    @Override
    public List<BaseGainsCalculator> getBaseGainsCalculators() {
        return baseCalculators;
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
        return totalGainsMap;
    }
}
