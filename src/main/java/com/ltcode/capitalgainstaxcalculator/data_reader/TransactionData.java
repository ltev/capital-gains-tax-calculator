package com.ltcode.capitalgainstaxcalculator.data_reader;

import com.ltcode.capitalgainstaxcalculator.transaction.Currency;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionData(int lineNumber, LocalDateTime dateTime, String ticker, String product, TransactionType type,
                              BigDecimal quantity, BigDecimal pricePerShare, BigDecimal value, BigDecimal commission,  Currency currency) {

    public static TransactionData copyWithNewValue(TransactionData transactionData, BigDecimal newValue) {
        return new TransactionData(
                transactionData.lineNumber,
                transactionData.dateTime,
                transactionData.ticker(),
                transactionData.product(),
                transactionData.type,
                transactionData.quantity,
                transactionData.pricePerShare,
                newValue,
                transactionData.commission,
                transactionData.currency);
    }
}
