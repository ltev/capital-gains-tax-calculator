package com.ltcode.capitalgainstaxcalculator.calculator;

import java.math.BigDecimal;

public final class LeftStockInfo {

    private final String ticker;
    private final String product;
    private final BigDecimal quantity;

    public LeftStockInfo(String ticker, String product, BigDecimal quantity) {
        this.ticker = ticker;
        this.product = product;
        this.quantity = quantity;
    }

    public String getTicker() {
        return ticker;
    }

    public String getProduct() {
        return product;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "LeftStockInfo{" +
                "ticker='" + ticker + '\'' +
                ", product='" + product + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
