package com.ltcode.capitalgainstaxcalculator.utils;

import com.ltcode.capitalgainstaxcalculator.exception.InvalidTypeException;
import com.ltcode.capitalgainstaxcalculator.transaction.Transaction;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

public class Utils {


    /**
     * @return -1 if is in chronological order (dates must be equal or in asc order)
     * first list index that is invalid
     */
    public static int isInChronologicalOrder(List<? extends Transaction> transactionsList) {
        // check if dates are in chronological order
        for (int i = 1; i < transactionsList.size(); i++) {
            Duration duration = Duration.between(
                    transactionsList.get(i-1).getDateTime(),
                    transactionsList.get(i).getDateTime());
            if (duration.isNegative()) {
               return i;
            }
        }
        return -1;
    }

    public static void checkForNull(Object o, String parameterName) {
        if (o == null) {
            throw new InvalidTypeException(parameterName + " can not be null!");
        }
    }

    public static boolean isNegative(BigDecimal number) {
        return number.compareTo(BigDecimal.ZERO) == -1;
    }

    public static boolean isPositive(BigDecimal number) {
        return number.compareTo(BigDecimal.ZERO) == 1;
    }

    public static boolean isPositiveOrZero(BigDecimal number) {
        return number.compareTo(BigDecimal.ZERO) >= 0;
    }

    public static boolean isZero(BigDecimal number) {
        return number.compareTo(BigDecimal.ZERO) == 0;
    }

    public static boolean isNegativeOrZero(BigDecimal number) {
        return number.compareTo(BigDecimal.ZERO) <= 0;
    }
}
