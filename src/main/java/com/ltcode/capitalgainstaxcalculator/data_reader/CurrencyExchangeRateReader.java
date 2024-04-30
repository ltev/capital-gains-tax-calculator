package com.ltcode.capitalgainstaxcalculator.data_reader;

import com.ltcode.capitalgainstaxcalculator.exception.CurrencyExchangeRateReaderException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CurrencyExchangeRateReader {

    public static Map<LocalDate, BigDecimal> read(Path path) {
        List<String> lines;
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        boolean isFromJPYtoPLN = path.endsWith("jpy_pln.csv");
        Map<LocalDate, BigDecimal> map = new HashMap<>();

        for (String line : lines) {
            String[] arr = line.split(",");
            if (arr.length == 0) {
                continue;
            }

            var date = getDate(arr[0]);
            if (arr.length == 0 || date == null) {
                continue;
            }
            if (map.containsKey(date)) {
                throw new CurrencyExchangeRateReaderException("Date: " + date + " is present multiple times in path: " + path);
            }

            var rate = getRate(arr[1]);
            if (rate == null) {
                throw new CurrencyExchangeRateReaderException("Invalid rate for date: " + date);
            }
            // from japan to pln rate should be divided by 100, file gives rate for 100 jpy not 1
            if (isFromJPYtoPLN) {
                rate = rate.divide(new BigDecimal("100"));
            }
            map.put(date, rate);
        }
        return map;
    }

    private static LocalDate getDate(String s) {
        try {
            return LocalDate.parse(s);
        } catch (RuntimeException e) {

        }
        return null;
    }

    private static BigDecimal getRate(String s) {
        return new BigDecimal(s);
    }
}
