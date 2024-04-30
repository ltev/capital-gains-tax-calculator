package com.ltcode.capitalgainstaxcalculator.data_reader;

import com.ltcode.capitalgainstaxcalculator.TestSettings;
import com.ltcode.capitalgainstaxcalculator.broker.Broker;
import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyExchanger;
import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyExchangerImp;
import com.ltcode.capitalgainstaxcalculator.data_reader.data_writer.Write;
import com.ltcode.capitalgainstaxcalculator.settings.Settings;
import com.ltcode.capitalgainstaxcalculator.transaction.Currency;
import com.ltcode.capitalgainstaxcalculator.transaction.DividendTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;


class DegiroTransactionReaderTest {

    List<DividendTransaction> list;
    CurrencyExchanger exchanger;

    @BeforeEach
    void setUp() {
        this.list = TransactionReader.readDividendList(Broker.DEGIRO, TestSettings.DEGIRO_ACOUNT_DIVIDEND_2021_TEST_PATH);
        this.exchanger = new CurrencyExchangerImp(
                Currency.PLN,
                Settings.EXCHANGE_RATES_DATA_PATH);
    }

    @Test
    void generateTest() {
        Write.generateDividendTransactionsCsvFile(list, exchanger, "aaa");
    }
}