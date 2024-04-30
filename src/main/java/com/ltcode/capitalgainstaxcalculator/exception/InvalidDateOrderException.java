package com.ltcode.capitalgainstaxcalculator.exception;

public class InvalidDateOrderException extends TransactionInfoException {

    public InvalidDateOrderException() {
    }

    public InvalidDateOrderException(String message) {
        super(message);
    }
}
