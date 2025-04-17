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

    public SplitTransaction(LocalDateTime dateTime, String ticker, BigDecimal value, Currency currency) {
        super(dateTime, TransactionType.STOCK_SPLIT, value, currency);
        this.ticker = ticker;
        checkValidity();
    }

    private void checkValidity() {
        Utils.checkForNull(ticker, "ticker");
        if (! Utils.isPositive(value)) {
            throw new InvalidQuantityException("Value must be positive.");
        }
    }

    public String getTicker() {
        return ticker;
    }
}
