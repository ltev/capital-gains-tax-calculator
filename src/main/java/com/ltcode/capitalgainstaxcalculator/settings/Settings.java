package com.ltcode.capitalgainstaxcalculator.settings;

import com.ltcode.capitalgainstaxcalculator.transaction.TransactionData;
import static com.ltcode.capitalgainstaxcalculator.transaction.TransactionData.*;
import java.nio.file.Path;

public class Settings {

    // == READ FILES ==

    public static final Path EXCHANGE_RATES_DATA_PATH = Path.of("data", "currency_exchange");

    public static final Path TRANSACTIONS_DATA_PATH = Path.of("data", "transactions");

    // == WRITE FILES ==

    public static final String CSV_FILE_TYPE = ".csv";
    public static final Path GENERATED_DATA_PATH = Path.of("data_generated");
    public static final String TRANSACTIONS_FILE_NAME = "transactions" + CSV_FILE_TYPE;

    public static final String JOINED_TRANSACTIONS_FILE_NAME = "joined_transactions" + CSV_FILE_TYPE;;

    public static final String DIVIDEND_TRANSACTIONS_FILE_NAME = "dividend" + CSV_FILE_TYPE;;

    public static final String GAINS_FILE_NAME = "stock_gains" + CSV_FILE_TYPE;;

    public static final String SUMMARY_FILE_NAME = "summary.txt";



    public static final char CSV_SEPARATOR = ',';

    // == CSV DATA ORDER ==

    public static final TransactionData[] CSV_TRANSACTION_WRITE_ORDER = new TransactionData[] {
            DATE_TIME,
            TICKER,
            PRODUCT,
            TYPE,
            QUANTITY,
            PRICE_PER_SHARE,
            VALUE,
            COMMISSION,
            CURRENCY
    };

    public static final TransactionData[] CSV_DIVIDEND_WRITE_ORDER = new TransactionData[] {
            DATE_TIME,
            TICKER,
            PRODUCT,
            TYPE,
            DIVIDEND_BEFORE_TAXES,
            TAXES_PAID,
            DIVIDEND_AFTER_TAXES,
            CURRENCY
    };

    public static final TransactionData[] CSV_JOINED_TRANSACTION_WRITE_ORDER = new TransactionData[] {
            DATE_TIME,
            TICKER,
            PRODUCT,
            TYPE,
            QUANTITY,
            PRICE_PER_SHARE,
            VALUE,
            COMMISSION,
            CURRENCY
    };

}
