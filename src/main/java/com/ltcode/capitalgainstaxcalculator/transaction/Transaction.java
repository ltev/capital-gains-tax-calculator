package com.ltcode.capitalgainstaxcalculator.transaction;

import com.ltcode.capitalgainstaxcalculator.exception.OperationNotSupportedException;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;
import com.ltcode.capitalgainstaxcalculator.utils.Utils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    public BigDecimal getQuantity()  {
        throw new OperationNotSupportedException();
    }

    public BigDecimal getTaxPaid() {
        throw new OperationNotSupportedException();
    }

    public String getTicker() {
        throw new OperationNotSupportedException();
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
}
