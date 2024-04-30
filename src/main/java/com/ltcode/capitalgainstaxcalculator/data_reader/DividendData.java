package com.ltcode.capitalgainstaxcalculator.data_reader;

import com.ltcode.capitalgainstaxcalculator.transaction.Currency;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DividendData(int lineNumber,
                           LocalDateTime dateTime,
                           String ticker,
                           String product,
                           TransactionType type,
                           BigDecimal value,
                           BigDecimal taxPaid,
                           Currency currency) {
}
