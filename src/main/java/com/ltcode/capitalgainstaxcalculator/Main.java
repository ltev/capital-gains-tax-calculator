package com.ltcode.capitalgainstaxcalculator;

import com.ltcode.capitalgainstaxcalculator.broker.Broker;
import com.ltcode.capitalgainstaxcalculator.broker.FileInfo;
import com.ltcode.capitalgainstaxcalculator.broker.FileType;
import com.ltcode.capitalgainstaxcalculator.calculator.GainsCalculator;
import com.ltcode.capitalgainstaxcalculator.calculator.GainsCalculatorImpl;
import com.ltcode.capitalgainstaxcalculator.country_info.Country;
import com.ltcode.capitalgainstaxcalculator.country_info.CountryTaxCalculationInfo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {
        Path writeDirectory = Paths.get("D:\\workspace\\java\\CapitalGainsTaxCalculator", "end_data");

        String basePath = "F:\\Podatki\\transactions";

        FileInfo degiroTransactions = new FileInfo(
                Broker.DEGIRO,
                FileType.STOCK_TRANSACTIONS,
                Paths.get(basePath, "degiro_transactions.csv")
        );

        FileInfo degiroAccount = new FileInfo(
                Broker.DEGIRO,
                FileType.STOCK_ACCOUNT,
                Paths.get(basePath, "degiro_account.csv")
        );

        FileInfo revolutTransactions = new FileInfo(
                Broker.REVOLUT,
                FileType.STOCK_ACCOUNT,
                Paths.get(basePath, "revolut_transactions.csv")
        );

        /*
         * STOCK CALCULATION
         */
        CountryTaxCalculationInfo countryInfo = CountryTaxCalculationInfo.getInstance(Country.POLAND);
        LocalDate lastCalculationDate = LocalDate.of(2021, 06, 31);

        GainsCalculator calculator = new GainsCalculatorImpl(countryInfo, lastCalculationDate);
        calculator.calculate(
//                degiroTransactions,
//                degiroAccount
                revolutTransactions
        );

        calculator.generateTransactionsCsvFile(writeDirectory
                .resolve("rt")
                .resolve(countryInfo.getCountry().name().toLowerCase())
                .resolve("stocks"));

        /*
         * CRYPTO CALCULATION
         */
//        FileInfo revolutCryptoTransactions = new FileInfo(
//                Broker.REVOLUT,
//                FileType.CRYPTO_TRANSACTIONS,
//                Paths.get("F:\\Podatki\\transactions", "revolut_crypto.csv")
//        );
//
//        calculator.calculate(
//                revolutCryptoTransactions
//        );
//        calculator.generateTransactionsCsvFile(writeDirectory
//                .resolve("lt")
//                .resolve(countryInfo.getCountry().name().toLowerCase())
//                .resolve("crypto"));
    }
}
