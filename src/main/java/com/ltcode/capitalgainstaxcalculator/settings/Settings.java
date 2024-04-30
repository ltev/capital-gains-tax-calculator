package com.ltcode.capitalgainstaxcalculator.settings;

import com.ltcode.capitalgainstaxcalculator.transaction.TransactionData;

import static com.ltcode.capitalgainstaxcalculator.transaction.TransactionData.*;

import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.Period;

public class Settings {

    // == READ FILES ==

    public static final Path EXCHANGE_RATES_DATA_PATH = Path.of("data", "currency_exchange");

    public static final Path TRANSACTIONS_DATA_PATH = Path.of("data", "transactions");

    // == WRITE FILES ==

    public static final Path GENERATED_DATA_PATH = Path.of("data_generated");
    public static final String TRANSACTIONS_FILE_NAME = "transactions";

    public static final String JOINED_TRANSACTIONS_FILE_NAME = "joined_transactions";

    public static final String DIVIDEND_TRANSACTIONS_FILE_NAME = "dividend";

    public static final Object GAINS_FILE_NAME = "stock_gains";

    public static final Object SUMMARY_FILE_NAME = "summary";

    public static final String CSV_FILE_TYPE = ".csv";

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
            VALUE,
            TAX_PAID,
            CURRENCY
    };

    public static final TransactionData[] CSV_JOINED_TRANSACTION_WRITE_ORDER = new TransactionData[] {
            SUM_OF_BUYS_VALUE,
            PROFIT,
            CURRENCY
    };


    public static final Period DATA_SHIFT = Period.ZERO;
    public static final int PRECISION = 0;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

}
