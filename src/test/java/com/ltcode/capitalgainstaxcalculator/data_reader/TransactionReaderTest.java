package com.ltcode.capitalgainstaxcalculator.data_reader;

import com.ltcode.capitalgainstaxcalculator.broker.Broker;
import com.ltcode.capitalgainstaxcalculator.TestSettings;
import com.ltcode.capitalgainstaxcalculator.transaction.Transaction;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TransactionReaderTest {

    @Test
    void readTest() {
        Path path = TestSettings.REVOLUT_STOCK_TEST_PATH;
        List<Transaction> list = TransactionReader.readAccountFile(Broker.REVOLUT, path);

        assertEquals(13, list.size());
        for (int i = 0; i < list.size(); i++) {
            assertNotNull(list.get(i), "Line nr: " + (i + 1));
        }
    }
}