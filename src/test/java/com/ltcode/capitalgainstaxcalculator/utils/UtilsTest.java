package com.ltcode.capitalgainstaxcalculator.utils;

import com.ltcode.capitalgainstaxcalculator.TestSettings;
import com.ltcode.capitalgainstaxcalculator.transaction.BuySellTransaction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    static BuySellTransaction t1;
    static BuySellTransaction t2;
    static BuySellTransaction t3;
    static BuySellTransaction t4;

    @BeforeAll
    static void setUp() {
        t1 = TestSettings.getDumbTransaction(LocalDateTime.of(2020, 1, 1, 10, 10));
        t2 = TestSettings.getDumbTransaction(LocalDateTime.of(2020, 1, 1, 10, 15));
        t3 = TestSettings.getDumbTransaction(LocalDateTime.of(2020, 1, 1, 10, 15));
        t4 = TestSettings.getDumbTransaction(LocalDateTime.of(2020, 1, 1, 10, 20));
    }

    @Test
    void isInChronologicalOrder_Valid() {
        int result = Utils.isInChronologicalOrder(List.of(t1, t2, t4));
        assertEquals(-1, result);
    }

    @Test
    void isInChronologicalOrder_ValidTheSameTimeZero() {
        int result = Utils.isInChronologicalOrder(List.of(t1, t2, t3));
        assertEquals(-1, result);

        result = Utils.isInChronologicalOrder(List.of(t3, t2));
        assertEquals(-1, result);
    }

    @Test
    void isInChronologicalOrder_InvalidDesc() {
        int result = Utils.isInChronologicalOrder(List.of(t1, t4, t2));
        assertEquals(2, result);

        result = Utils.isInChronologicalOrder(List.of(t2, t1));
        assertEquals(1, result);
    }

}