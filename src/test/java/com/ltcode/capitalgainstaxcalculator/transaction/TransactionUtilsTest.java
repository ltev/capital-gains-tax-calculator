package com.ltcode.capitalgainstaxcalculator.transaction;

import com.ltcode.capitalgainstaxcalculator.TestSettings;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class TransactionUtilsTest {

    int PRECISION = 2;
    RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Test
    void testNewPartitionedTransaction_Equality() {
       BuySellTransaction t1 = TestSettings.SAMPLE_BUY_SELL_TRANSACTION;

       BuySellTransaction equalPartition = TransactionUtils
               .newPartitionedTransaction(t1, t1.getQuantity().subtract(new BigDecimal("0")), PRECISION, ROUNDING_MODE);
       BuySellTransaction diffPartition = TransactionUtils
               .newPartitionedTransaction(t1, t1.getQuantity().subtract(new BigDecimal("0.01")), PRECISION, ROUNDING_MODE);

        assertEquals(t1, equalPartition);
        assertNotEquals(t1, diffPartition);
    }

    @Test
    void testNewPartitionedTransaction_ValueCheck() {
        // quantity 5, value 11
        BuySellTransaction t = TestSettings.SAMPLE_BUY_SELL_TRANSACTION;

        BuySellTransaction t2 = TransactionUtils.newPartitionedTransaction(t, new BigDecimal("2.5"), PRECISION, ROUNDING_MODE);
        assertEquals(new BigDecimal("5.50"), t2.getValue());
        assertEquals(new BigDecimal("2.5"), t2.getQuantity());
        assertEquals(new BigDecimal(5), t2.getOriginalQuantity());

        BuySellTransaction t3 = TransactionUtils.newPartitionedTransaction(t2, new BigDecimal("1.25"), PRECISION, ROUNDING_MODE);
        assertEquals(new BigDecimal("2.75"), t3.getValue());
        assertEquals(new BigDecimal("1.25"), t3.getQuantity());
        assertEquals(new BigDecimal(5), t3.getOriginalQuantity());
    }

    @Test
    void testNewPartitionedTransaction_CommissionCheck() {
        // quantity 5, value 11 commission
        BuySellTransaction t = TestSettings.SAMPLE_BUY_SELL_WITH_COMMISSION_TRANSACTION;
        assertEquals(new BigDecimal("1"), t.getCommission());

        BuySellTransaction t2 = TransactionUtils.newPartitionedTransaction(t, new BigDecimal("2.5"), PRECISION, ROUNDING_MODE);
        System.out.println(t2);
        assertEquals(new BigDecimal("0.50"), t2.getCommission());

        // partition t2

        BuySellTransaction one = TransactionUtils.newPartitionedTransaction(t2, new BigDecimal("0.79"), PRECISION, ROUNDING_MODE);
        assertEquals(new BigDecimal("0.16"), one.getCommission()); // 0.158

        BuySellTransaction two = TransactionUtils.newPartitionedTransaction(t2, new BigDecimal("1.71"), PRECISION, ROUNDING_MODE);
        assertEquals(new BigDecimal("0.34"), two.getCommission());   // 0.342
    }
}