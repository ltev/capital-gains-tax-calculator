package com.ltcode.capitalgainstaxcalculator.exception;

public class InvalidQuantityException extends TransactionInfoException {

    public InvalidQuantityException() {
    }

    public InvalidQuantityException(String message) {
        super(message);
    }
}
