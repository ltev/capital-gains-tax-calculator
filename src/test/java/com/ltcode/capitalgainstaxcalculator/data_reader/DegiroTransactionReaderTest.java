package com.ltcode.capitalgainstaxcalculator.data_reader;

import com.ltcode.capitalgainstaxcalculator.TestSettings;
import com.ltcode.capitalgainstaxcalculator.broker.Broker;
import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyRateExchanger;
import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyRateExchangerImp;
import com.ltcode.capitalgainstaxcalculator.settings.Settings;
import com.ltcode.capitalgainstaxcalculator.transaction.Currency;
import com.ltcode.capitalgainstaxcalculator.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;


class DegiroTransactionReaderTest {

    List<Transaction> list;
    CurrencyRateExchanger exchanger;

    @BeforeEach
    void setUp() {
        this.list = TransactionReader.readAccountFile(Broker.DEGIRO, TestSettings.DEGIRO_ACOUNT_DIVIDEND_2021_TEST_PATH);
        this.exchanger = new CurrencyRateExchangerImp(
                Currency.PLN,
                Settings.EXCHANGE_RATES_DATA_PATH);
    }

    @Test
    void generateTest() {
        //Write.generateDividendTransactionsCsvFile(list, exchanger, "aaa");
    }
}