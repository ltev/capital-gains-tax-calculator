package com.ltcode.capitalgainstaxcalculator.transaction;

import com.ltcode.capitalgainstaxcalculator.exception.InvalidQuantityException;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;
import com.ltcode.capitalgainstaxcalculator.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class DividendTransaction extends Transaction {

    private final String ticker;
    private final String product;
    private final BigDecimal taxesPaid;
    private final BigDecimal dividendBeforeTaxes;

    DividendTransaction(LocalDateTime dateTime, String ticker, String product, BigDecimal dividendBeforeTaxes,
                        BigDecimal taxesPaid, BigDecimal dividendAfterTaxes, Currency currency) {
        super(dateTime, TransactionType.DIVIDEND, dividendAfterTaxes, currency);
        this.ticker = ticker;
        this.product = product;
        this.dividendBeforeTaxes = dividendBeforeTaxes;
        this.taxesPaid = taxesPaid;
        checkValidity();
    }

    private void checkValidity() {
        Utils.checkForNull(ticker, "ticker");

        if (Utils.isNegative(value) || (taxesPaid != null && Utils.isNegative(taxesPaid))) {
            throw new InvalidQuantityException("Invalid data. Dividend / tax paid can not be smaller than ZERO.");
        }
    }

    public String getTicker() {
        return ticker;
    }

    public BigDecimal getTaxesPaid() {
        return taxesPaid;
    }

    public BigDecimal getPercentOfPaidTaxes() {
        if (taxesPaid == null) {
            return null;
        }
        return new BigDecimal("100").multiply(taxesPaid.divide(value, 4, RoundingMode.HALF_UP));
    }

    public BigDecimal getDividendBeforeTaxes() {
        return dividendBeforeTaxes;
    }

    public BigDecimal getDividendAfterTaxes() {
        return value;
    }
}
