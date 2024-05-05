package com.ltcode.capitalgainstaxcalculator.calculator;

import com.ltcode.capitalgainstaxcalculator.broker.FileInfo;

import java.nio.file.Path;
import java.util.Map;

public interface GainsCalculator {

    void calculate(FileInfo... fileInfo);

    void generateTransactionsCsvFile(Path directory);

    Map<Integer, YearGainsInfo> getTotalGains();
}
