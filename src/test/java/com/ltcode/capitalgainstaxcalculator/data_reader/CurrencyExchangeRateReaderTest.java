package com.ltcode.capitalgainstaxcalculator.data_reader;

import com.ltcode.capitalgainstaxcalculator.TestSettings;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyExchangeRateReaderTest {

    @Test
    void readTest() {
        Path path = TestSettings.USD_PLN_TEST_PATH;
        var map = CurrencyExchangeRateReader.read(path);
        assertEquals(255, map.size());
    }

}