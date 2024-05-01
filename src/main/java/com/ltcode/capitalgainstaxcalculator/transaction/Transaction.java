package com.ltcode.capitalgainstaxcalculator.transaction;


import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyRateExchanger;
import com.ltcode.capitalgainstaxcalculator.exception.OperationNotSupportedException;
import com.ltcode.capitalgainstaxcalculator.settings.Settings;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;
import com.ltcode.capitalgainstaxcalculator.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Map;

public abstract class Transaction {

    protected final TransactionType type;
    protected final LocalDateTime dateTime;
    /**
     * quantity * pricePerShare (no commission)
     * Can differ from that equation when pricePerShare was rounded when read
     */
    protected final BigDecimal value;
    protected final Currency currency;

    public Transaction(LocalDateTime dateTime, TransactionType type, BigDecimal value, Currency currency) {
        this.dateTime = dateTime;
        this.type = type;
        this.value = value;
        this.currency = currency;
        checkValidity();
    }

    private void checkValidity() {
        Utils.checkForNull(type, "type");
        Utils.checkForNull(dateTime, "dateTime");
        Utils.checkForNull(value, "value");
        Utils.checkForNull(currency, "currency");
    }

    public TransactionType getType() {
        return type;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public BigDecimal getValue() {
        return value;
    }

    public Currency getCurrency() {
        return currency;
    }


    // OperationNotSupportedException - as default, using inheritance

    public BigDecimal getCommission() {
        throw new OperationNotSupportedException();
    }
    public String getProduct() {
        throw new OperationNotSupportedException();
    }

    public BigDecimal getPricePerShare() {
        throw new OperationNotSupportedException();
    }

    public BigDecimal getTaxPaid() {
        throw new OperationNotSupportedException();
    }

    public String getTicker() {
        throw new OperationNotSupportedException();
    }


    // EXCHANGING VALUES FOR MATCHING CURRENCY

    /**
     * @return value in exchange currency from THE DAY BEFORE, or the first working day before
     * precision - decimal places
     */
    public BigDecimal getValue(CurrencyRateExchanger exchanger, Period periodShift, int precision, RoundingMode roundingMode) {
        return currency == exchanger.getToCurrency()
                ? value
                : value.multiply(
                        exchanger.getRateUpTo7DaysPrevious(currency, getDateTime().toLocalDate().plus(periodShift)))
                .setScale(precision, roundingMode);     // RATE FROM THE PREVIOUS DAY
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "type=" + type +
                ", dateTime=" + dateTime +
                ", value=" + value +
                ", currency=" + currency +
                '}';
    }

    public abstract String generateCsvLine();

    public abstract String generateCsvLine(TransactionData[] order);

    // == PACKAGE PRIVATE ==

    String generateCsvLine(Map<TransactionData, Object> transactionDataMap) {
        return generateCsvLine(transactionDataMap, Settings.CSV_TRANSACTION_WRITE_ORDER);
    }

    String generateCsvLine(Map<TransactionData, Object> transactionDataMap, TransactionData[] order) {
        final char SEPARATOR = ',';
        transactionDataMap.put(TransactionData.DATE_TIME, dateTime);
        transactionDataMap.put(TransactionData.TYPE, type);
        transactionDataMap.put(TransactionData.VALUE, value);
        transactionDataMap.put(TransactionData.CURRENCY, currency);

        StringBuilder sb = new StringBuilder();
        for (TransactionData data : order) {
            sb.append(SEPARATOR);
            if (data == TransactionData.PRODUCT) {  // ',' might be in product name
                sb.append("\"");
                sb.append(transactionDataMap.getOrDefault(data, ""));
                sb.append("\"");
            } else {
                sb.append(transactionDataMap.getOrDefault(data, ""));
            }
        }
        return sb.substring(1);
    }
}
