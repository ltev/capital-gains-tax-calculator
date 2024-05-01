package com.ltcode.capitalgainstaxcalculator.transaction;

import com.ltcode.capitalgainstaxcalculator.exception.InvalidQuantityException;
import com.ltcode.capitalgainstaxcalculator.settings.Settings;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;
import com.ltcode.capitalgainstaxcalculator.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;

public class DividendTransaction extends Transaction {

    private final String ticker;
    private final String product;
    private final BigDecimal taxPaid;

    DividendTransaction(LocalDateTime dateTime, String ticker, String product, BigDecimal dividendBeforeTaxes, Currency currency) {
        this(dateTime, ticker, product, dividendBeforeTaxes, null, currency);
    }

    DividendTransaction(LocalDateTime dateTime, String ticker, String product, BigDecimal dividendBeforeTaxes, BigDecimal taxPaid, Currency currency) {
        super(dateTime, TransactionType.DIVIDEND, dividendBeforeTaxes, currency);
        this.ticker = ticker;
        this.product = product;
        this.taxPaid = taxPaid;
        checkValidity();
    }

    @Override
    public String generateCsvLine() {
        return generateCsvLine(Settings.CSV_TRANSACTION_WRITE_ORDER);
    }

    @Override
    public String generateCsvLine(TransactionData[] order) {
        return generateCsvLine(new HashMap<>() {
            {
                put(TransactionData.PRODUCT, product);
                put(TransactionData.TICKER, ticker);
                put(TransactionData.TAX_PAID, taxPaid);
            }
        }, order);
    }

    private void checkValidity() {
        Utils.checkForNull(ticker, "ticker");

        if (Utils.isNegative(value) || Utils.isNegative(taxPaid)) {
            throw new InvalidQuantityException("Invalid data. Dividend / tax paid can not be smaller than ZERO.");
        }
    }

    public String getTicker() {
        return ticker;
    }

    public BigDecimal getTaxPaid() {
        return taxPaid;
    }

    public BigDecimal getPercentOfPaidTaxes() {
        return new BigDecimal("100").multiply(taxPaid.divide(value, 4, RoundingMode.HALF_UP));
    }
}
