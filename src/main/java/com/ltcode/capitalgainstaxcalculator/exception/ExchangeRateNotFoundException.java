package com.ltcode.capitalgainstaxcalculator.exception;

public class ExchangeRateNotFoundException extends CurrencyExchangerException {

    public ExchangeRateNotFoundException () {
    }

    public ExchangeRateNotFoundException (String message) {
        super(message);
    }
}
