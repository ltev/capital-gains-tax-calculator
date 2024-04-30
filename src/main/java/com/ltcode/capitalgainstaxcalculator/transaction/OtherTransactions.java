package com.ltcode.capitalgainstaxcalculator.transaction;

import com.ltcode.capitalgainstaxcalculator.settings.Settings;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;

public class OtherTransactions extends Transaction {

    public OtherTransactions(LocalDateTime dateTime, TransactionType type, BigDecimal value, Currency currency) {
        super(dateTime, type, value, currency);
    }

    public String generateCsvLine() {
        return generateCsvLine(Settings.CSV_TRANSACTION_WRITE_ORDER);
    }

    @Override
    public String generateCsvLine(TransactionData[] order) {
        return generateCsvLine(new HashMap<>(), order);
    }
}
