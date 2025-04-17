package com.ltcode.capitalgainstaxcalculator.transaction_converter;

import com.ltcode.capitalgainstaxcalculator.country_info.CountryTaxCalculationInfo;
import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyRateExchanger;
import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyRateExchangerImp;
import com.ltcode.capitalgainstaxcalculator.settings.Settings;
import com.ltcode.capitalgainstaxcalculator.transaction.BuySellTransaction;
import com.ltcode.capitalgainstaxcalculator.transaction.Currency;
import com.ltcode.capitalgainstaxcalculator.transaction.Transaction;
import com.ltcode.capitalgainstaxcalculator.transaction.joined.JoinedTransaction;
import java.math.BigDecimal;

/**
 * Converts all numeric data from Transaction and JoinedTransaction class to appropriate values in given currency
 */
public class TransactionValuesConverter {

    private final CurrencyRateExchanger exchanger;
    private CountryTaxCalculationInfo countryInfo;

    public TransactionValuesConverter(CountryTaxCalculationInfo countryInfo) {
        this.countryInfo = countryInfo;
        this.exchanger = new CurrencyRateExchangerImp(countryInfo.getCurrency(), Settings.EXCHANGE_RATES_DATA_PATH.resolve(countryInfo.getCountry().name()));;
    }

    public BigDecimal getRateAfterShiftUpTo7DaysPrevious(Transaction transaction) {
        return exchanger.getRateUpTo7DaysPrevious(
                transaction.getCurrency(),
                transaction.getDateTime().toLocalDate().plus(countryInfo.getDateShift())
        );
    }

    public Currency getToCurrency() {
        return exchanger.getToCurrency();
    }

    // == CONVERT VALUES ==

    public BigDecimal getCommission(Transaction transaction) {
        return calculate(transaction, transaction.getCommission());
    }

    public BigDecimal getDividendBeforeTaxes(Transaction transaction) {
        return calculate(transaction, transaction.getDividendBeforeTaxes());
    }

    public BigDecimal getDividendAfterTaxes(Transaction transaction) {
        return calculate(transaction, transaction.getDividendAfterTaxes());
    }

    public BigDecimal getTaxesPaid(Transaction transaction) {
        return calculate(transaction, transaction.getTaxesPaid());
    }

    public BigDecimal getValue(Transaction transaction) {
        return calculate(transaction, transaction.getValue());
    }

    public BigDecimal getTotalBuyValue(JoinedTransaction joinedTransaction) {
        BigDecimal value = BigDecimal.ZERO;
        for (int i = 0; i < joinedTransaction.getNumOfBuyTransactions(); i++) {
            BuySellTransaction buy = joinedTransaction.getBuyTransaction(i);
            value = value.add(getValue(buy));
        }
        return value;
    }

    public BigDecimal getTotalBuyCommission(JoinedTransaction joinedTransaction) {
        BigDecimal commission = BigDecimal.ZERO;
        for (int i = 0; i < joinedTransaction.getNumOfBuyTransactions(); i++) {
            BuySellTransaction buy = joinedTransaction.getBuyTransaction(i);
            commission = commission.add(getCommission(buy));
        }
        return commission;
    }

    public BigDecimal getTotalBuySellCommission(JoinedTransaction joinedTransaction) {
        return getCommission(joinedTransaction.getSellTransaction())
                .add(getTotalBuyCommission(joinedTransaction));
    }

    public BigDecimal getProfit(JoinedTransaction joinedTransaction) {
        Transaction sell = joinedTransaction.getSellTransaction();
        return getValue(sell)
                .subtract(getTotalBuyValue(joinedTransaction))
                .subtract(getTotalBuySellCommission(joinedTransaction));
    }

    // == PRIVATE METHODS ==

    private BigDecimal calculate(Transaction transaction, BigDecimal fromValue) {
        return transaction.getCurrency() == exchanger.getToCurrency()
                ? fromValue
                : fromValue
                    .multiply(getRateAfterShiftUpTo7DaysPrevious(transaction))
                    .setScale(countryInfo.getPrecision(), countryInfo.getRoundingMode());
    }
}
