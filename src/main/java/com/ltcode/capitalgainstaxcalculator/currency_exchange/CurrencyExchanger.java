package com.ltcode.capitalgainstaxcalculator.currency_exchange;

import com.ltcode.capitalgainstaxcalculator.transaction.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CurrencyExchanger {

    Currency getToCurrency();

    BigDecimal getRate(Currency from, LocalDate date);

    BigDecimal getRateUpTo7DaysPrevious(Currency from, LocalDate date);
}
