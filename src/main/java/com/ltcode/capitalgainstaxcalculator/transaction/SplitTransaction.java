package com.ltcode.capitalgainstaxcalculator.transaction;

import com.ltcode.capitalgainstaxcalculator.exception.InvalidQuantityException;
import com.ltcode.capitalgainstaxcalculator.settings.Settings;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;
import com.ltcode.capitalgainstaxcalculator.utils.Utils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;

public class SplitTransaction extends Transaction {

    private final String ticker;
    private final BigDecimal quantity;

    public SplitTransaction(LocalDateTime dateTime, String ticker, BigDecimal quantity, BigDecimal value, Currency currency) {
        super(dateTime, TransactionType.STOCK_SPLIT, value, currency);
        this.ticker = ticker;
        this.quantity = quantity;
        checkValidity();
    }

    private void checkValidity() {
        Utils.checkForNull(ticker, "ticker");

        BigDecimal ZERO = BigDecimal.ZERO;
        if (! Utils.isZero(value)) {
            throw new InvalidQuantityException("Quantity must be ZERO.");
        }
        // Share quantity can be negative
    }

    public String getTicker() {
        return ticker;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }
}
