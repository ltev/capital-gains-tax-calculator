package com.ltcode.capitalgainstaxcalculator.data_reader;

import com.ltcode.capitalgainstaxcalculator.transaction.Currency;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record DividendData(int lineNumber,
                           LocalDateTime dateTime,
                           String ticker,
                           String product,
                           TransactionType type,
                           BigDecimal dividendBeforeTaxes,
                           BigDecimal taxPaid,
                           BigDecimal dividendAfterTaxes,
                           Currency currency) {

    public static DividendData update(DividendData data, BigDecimal dividendBeforeTaxes, BigDecimal taxPaid, BigDecimal dividendAfterTaxes) {
        return new DividendData(
                data.lineNumber,
                data.dateTime,
                data.ticker,
                data.product,
                data.type,
                dividendBeforeTaxes,
                taxPaid,
                dividendAfterTaxes,
                data.currency
        );


    }
}
