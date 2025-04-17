package com.ltcode.capitalgainstaxcalculator.transaction;

import com.ltcode.capitalgainstaxcalculator.data_reader.DividendData;
import com.ltcode.capitalgainstaxcalculator.data_reader.TransactionData;
import com.ltcode.capitalgainstaxcalculator.exception.InvalidTypeException;
import com.ltcode.capitalgainstaxcalculator.exception.OperationNotSupportedException;

public class TransactionBuilder {

    public static Transaction build(TransactionData data) {
        return switch (data.type()) {
            case BUY, SELL ->
                    new BuySellTransaction(data.type(), data.dateTime(), data.ticker(), data.product(), data.quantity(),
                    data.pricePerShare(), data.value(), data.commission(), data.currency());
            case STOCK_SPLIT ->
                    new SplitTransaction(data.dateTime(), data.ticker(), data.value(), data.currency());
            case CASH_TOP_UP, CASH_WITHDRAWAL, CUSTODY_FEE, CUSTODY_FEE_REVERSAL, TRANSFER_TO_DIFF_LOCATION ->
                    new DifferentTransaction(data.dateTime(), data.type(), data.value(), data.currency());
            default -> throw new OperationNotSupportedException("Not supported for type " + data.type());
        };
    }

    public static DividendTransaction build(DividendData data) {
        return switch (data.type()) {
            case DIVIDEND ->
                    new DividendTransaction(data.dateTime(), data.ticker(), data.product(), data.dividendBeforeTaxes(),
                            data.taxPaid(), data.dividendAfterTaxes(), data.currency());
            default -> throw new InvalidTypeException("Must be of type dividend");
        };
    }
}
