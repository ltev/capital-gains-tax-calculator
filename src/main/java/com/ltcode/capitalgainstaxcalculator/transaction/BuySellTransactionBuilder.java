package com.ltcode.capitalgainstaxcalculator.transaction;

import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BuySellTransactionBuilder {

    private TransactionType type;
    private LocalDateTime dateTime;
    private String ticker;

    private String product;
    private BigDecimal quantity;
    private BigDecimal pricePerShare;

    private BigDecimal value;
    private BigDecimal commission;
    private Currency currency;

    private BigDecimal originalQuantity;

    //private BigDecimal originalCommission;

    public BuySellTransactionBuilder() {
    }

    public BuySellTransactionBuilder(BuySellTransaction t) {
        type = t.getType();
        dateTime = t.getDateTime();
        ticker = t.getTicker();
        product = t.getProduct();
        quantity = t.getQuantity();
        pricePerShare = t.getPricePerShare();
        value = t.getValue();
        commission = t.getCommission();
        currency = t.getCurrency();
        originalQuantity = t.getOriginalQuantity();
        // originalCommission = t.getOriginalCommission();
    }

    public BuySellTransactionBuilder setValue(BigDecimal value) {
        this.value = value;
        return this;
    }

    public BuySellTransactionBuilder setType(TransactionType type) {
        this.type = type;
        return this;
    }

    public BuySellTransactionBuilder setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
        return this;
    }

    public BuySellTransactionBuilder setTicker(String ticker) {
        this.ticker = ticker;
        return this;
    }

    public BuySellTransactionBuilder setProduct(String product) {
        this.product = product;
        return this;
    }

    public BuySellTransactionBuilder setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        return this;
    }

    public BuySellTransactionBuilder setPricePerShare(BigDecimal pricePerShare) {
        this.pricePerShare = pricePerShare;
        return this;
    }

    public BuySellTransactionBuilder setCommission(BigDecimal commission) {
        this.commission = commission;
        return this;
    }


    public BuySellTransactionBuilder setCurrency(Currency currency) {
        this.currency = currency;
        return this;
    }

    public BuySellTransactionBuilder setOriginalQuantity(BigDecimal originalQuantity) {
        this.originalQuantity = originalQuantity;
        return this;
    }


    public BuySellTransaction build() {
        if (originalQuantity == null) {
            return new BuySellTransaction(type, dateTime, ticker, product, quantity, pricePerShare, value, commission, currency);
        }
        else {
            return new BuySellTransaction(type, dateTime, ticker, product, quantity, pricePerShare, value, commission, currency, originalQuantity);
        }
    }
}
