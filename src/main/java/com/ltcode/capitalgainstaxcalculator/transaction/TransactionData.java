package com.ltcode.capitalgainstaxcalculator.transaction;

public enum TransactionData {

    DATE_TIME(true),
    TICKER(true),   // or ISIN
    PRODUCT(true),
    TYPE(true),
    QUANTITY(true),
    PRICE_PER_SHARE(true),
    VALUE(true),
    CURRENCY(true),

    SUM_OF_BUYS_VALUE(false),

    PROFIT(false),

    EXCHANGE_RATE(true),
    TAX_PAID(true),
    COMMISSION(true);

    private boolean isSimpleTransactionData;
    TransactionData(boolean simpleTransactionData) {
    }
}
