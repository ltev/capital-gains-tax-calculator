package com.ltcode.capitalgainstaxcalculator;
import com.ltcode.capitalgainstaxcalculator.broker.Broker;
import com.ltcode.capitalgainstaxcalculator.broker.FileInfo;
import com.ltcode.capitalgainstaxcalculator.broker.FileType;
import com.ltcode.capitalgainstaxcalculator.calculator.BaseGainsCalculator;
import com.ltcode.capitalgainstaxcalculator.calculator.BaseGainsCalculatorImpl;
import com.ltcode.capitalgainstaxcalculator.calculator.GainsCalculator;
import com.ltcode.capitalgainstaxcalculator.calculator.GainsCalculatorImpl;
import com.ltcode.capitalgainstaxcalculator.country_info.Country;
import com.ltcode.capitalgainstaxcalculator.country_info.CountryTaxCalculationInfo;
import com.ltcode.capitalgainstaxcalculator.transaction.Currency;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Period;

public class Main {

    public static void main(String[] args) {
        final Period  EXCHANGER_RATE_DATE_SHIFT = Period.ofDays(-1);
        final Broker BROKER = Broker.DEGIRO;
        final Currency CURRENCY = Currency.PLN;

        // FILES
        String revolut_transactions_file = "transactions_revolut.csv";
        String degiro_transactions_file = "transactions_degiro.csv";
        String degiro_account_file = "account_degiro.csv";

        // all transactions
        // List<? extends Transaction> transactionList = TransactionReader.read(BROKER,Settings.TRANSACTIONS_DATA_PATH.resolve(degiro_transactions_file));

        Path writeDirectory = Paths.get("D:\\workspace\\java\\CapitalGainsTaxCalculator", "end_data");

        FileInfo degiroTransactions = new FileInfo(
                Broker.DEGIRO,
                FileType.TRANSACTIONS,
                Paths.get("D:\\workspace\\java\\CapitalGainsTaxCalculator\\data\\transactions", "transactions_degiro.csv")
        );

        FileInfo degiroAccount = new FileInfo(
                Broker.DEGIRO,
                FileType.ACCOUNT,
                Paths.get("D:\\workspace\\java\\CapitalGainsTaxCalculator\\data\\transactions", "account_degiro.csv")
        );

        FileInfo revolutTransactions = new FileInfo(
                Broker.REVOLUT,
                FileType.ACCOUNT,
                Paths.get("D:\\workspace\\java\\CapitalGainsTaxCalculator\\data\\transactions", "transactions_revolut.csv")
        );

        // all transactions of one specific automatic degiro fund
        /*
        List<? extends Transaction> transactionList = TransactionReader.readTransactions(BROKER,
                Settings.TRANSACTIONS_DATA_PATH.resolve(degiro_account_file),
               "NL0010661914");
        */

        // dividends

        //List<? extends Transaction> transactionList = TransactionReader.readDividendList(
        //                BROKER,
        //                Settings.TRANSACTIONS_DATA_PATH.resolve(degiro_account_file)
        //);

        //transactionList.sort(Comparator.comparing((t) -> t.getDateTime()));
        //System.out.println("size: " + transactionList.size());


        // all transactions of all automatic degiro fund
        // List<? extends Transaction> transactionList = TransactionReader.readAutomaticFundTransactions(
              //  BROKER,Settings.TRANSACTIONS_DATA_PATH.resolve("Account_degiro.csv"));


        CountryTaxCalculationInfo polandInfo = CountryTaxCalculationInfo.getInstance(Country.POLAND);

        GainsCalculator calculator = new GainsCalculatorImpl(polandInfo);

        calculator.calculate(
                degiroTransactions,
                degiroAccount,
                revolutTransactions
        );
        calculator.generateTransactionsCsvFile(writeDirectory);

        /*
        System.out.println("GAINS BY YEAR:");
        System.out.println(calculator.getTotalGains(2018));
        System.out.println(calculator.getTotalGains(2019));
        System.out.println(calculator.getTotalGains(2020));
        System.out.println(calculator.getTotalGains(2021));
        System.out.println(calculator.getTotalGains(2022));
        System.out.println(calculator.getTotalGains(2023));

        System.out.println("DIVIDENDS BY YEAR:");
        System.out.println(calculator.getTotalDividends(2020));
        System.out.println(calculator.getTotalDividends(2021));
        System.out.println(calculator.getTotalDividends(2022));
        System.out.println(calculator.getTotalDividends(2023));
*/
        // DIVIDEND
        /*
        Write.generateDividendTransactionsCsvFile(
                TransactionReader.readDividendList(
                        Broker.DEGIRO,
                        Settings.TRANSACTIONS_DATA_PATH.resolve("degiro_account.csv")
                ), exchanger,
                "dividends_all"
        );
        */
        //System.out.println("=== STOCKS LEFT");
        //calculator.getLeftStocksList().forEach(System.out::println);
    }
}
