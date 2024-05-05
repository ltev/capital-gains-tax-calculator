package com.ltcode.capitalgainstaxcalculator.exception;

public class OperationNotSupportedException extends CapitalGainsTaxCalculatorException {

    public OperationNotSupportedException() {
    }

    public OperationNotSupportedException(String message) {
        super(message);
    }
}
