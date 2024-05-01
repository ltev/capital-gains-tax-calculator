package com.ltcode.capitalgainstaxcalculator.transaction;

import com.ltcode.capitalgainstaxcalculator.currency_exchange.CurrencyRateExchanger;
import com.ltcode.capitalgainstaxcalculator.exception.InvalidQuantityException;
import com.ltcode.capitalgainstaxcalculator.exception.TransactionInfoException;
import com.ltcode.capitalgainstaxcalculator.settings.Settings;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;
import com.ltcode.capitalgainstaxcalculator.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashMap;
import java.util.Objects;

public final class BuySellTransaction extends Transaction {

    /**
     * symbol or isin
     */
    private final String ticker;
    /**
     * Stock name
     */
    private final String product;
    public final BigDecimal pricePerShare;
    /**
     * quantity and commission that non yet have been sold
     */
    public final BigDecimal quantity;
    private final BigDecimal commission;
    /**
     * Original after only part of the transaction has been sold
     */
    private final BigDecimal originalQuantity;
    /*
    Should not be used yet
    */
    private final BigDecimal originalCommission;


    BuySellTransaction(TransactionType type, LocalDateTime dateTime, String ticker, String product, BigDecimal quantity, BigDecimal pricePerShare, BigDecimal value, Currency currency) {
        this(type, dateTime, ticker, product, quantity, pricePerShare, value, BigDecimal.ZERO, currency, quantity);
    }



    public BuySellTransaction(TransactionType type, LocalDateTime dateTime, String ticker, String product,
                              BigDecimal quantity, BigDecimal pricePerShare, BigDecimal value,
                              BigDecimal commission, Currency currency) {
        super(dateTime, type, value, currency);
        this.ticker = ticker;
        this.product = product;
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
        this.commission = commission;
        this.originalCommission = commission;
        this.originalQuantity = quantity;
        checkValidity();
    }

    public BuySellTransaction(TransactionType type, LocalDateTime dateTime, String ticker, String product, BigDecimal quantity,
                              BigDecimal pricePerShare, BigDecimal value, BigDecimal commission,
                              Currency currency, BigDecimal originalQuantity) {
        super(dateTime, type, value, currency);
        this.ticker = ticker;
        this.product = product;
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
        this.originalQuantity = originalQuantity;
        this.commission = commission;
        this.originalCommission = null;
        checkValidity();
    }


    private void checkValidity() {
        Utils.checkForNull(ticker, "ticker");
        Utils.checkForNull(quantity, "quantity");
        Utils.checkForNull(pricePerShare, "pricePerShare");
        Utils.checkForNull(currency, "currency");
        Utils.checkForNull(originalQuantity, "originalQuantity");

        if (type != TransactionType.BUY && type != TransactionType.SELL) {
            throw new TransactionInfoException("SellBuyTransaction must be of type SELL or BUY.");
        }

        BigDecimal ZERO = BigDecimal.ZERO;
        if (quantity.compareTo(ZERO) <= 0
                || Utils.isNegative(pricePerShare)
                || Utils.isNegativeOrZero(originalQuantity)) {
            throw new InvalidQuantityException("Invalid data. Quantities can not be smaller / equal and smaller than ZERO.");
        }
    }

    public String getTicker() {
        return ticker;
    }

    public String getProduct() {
        return product;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public BigDecimal getCommission(CurrencyRateExchanger exchanger, Period periodShift, int precision, RoundingMode roundingMode) {
        return currency == exchanger.getToCurrency()
                ? commission
                : commission.multiply(
                        exchanger.getRateUpTo7DaysPrevious(currency, getDateTime().toLocalDate().plus(periodShift)))
                .setScale(precision, roundingMode);     // RATE FROM THE PREVIOUS DAY
    }

    public BigDecimal getPricePerShare() {
        return pricePerShare;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getOriginalQuantity() {
        return originalQuantity;
    }

    @Override
    public String generateCsvLine() {
        return generateCsvLine(Settings.CSV_TRANSACTION_WRITE_ORDER);
    }

    public String generateCsvLine(TransactionData[] order) {
        return generateCsvLine(new HashMap<>() {
            {
                put(TransactionData.TICKER, ticker);
                put(TransactionData.PRODUCT, product);
                put(TransactionData.PRICE_PER_SHARE, pricePerShare);
                put(TransactionData.QUANTITY, quantity);
                put(TransactionData.COMMISSION, commission);
            }
        }, order);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BuySellTransaction that = (BuySellTransaction) o;

        if (type != that.type) return false;
        if (!pricePerShare.equals(that.pricePerShare)) return false;
        if (!quantity.equals(that.quantity)) return false;
        if (!Objects.equals(commission, that.commission)) return false;
        if (!Objects.equals(originalQuantity, that.originalQuantity))
            return false;
        return Objects.equals(originalCommission, that.originalCommission);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + pricePerShare.hashCode();
        result = 31 * result + quantity.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BuySellTransaction{" +
                "type=" + type +
                ", pricePerShare=" + pricePerShare +
                ", quantity=" + quantity +
                ", commission=" + commission +
                ", originalQuantity=" + originalQuantity +
                ", originalCommission=" + originalCommission +
                ", dateTime=" + dateTime +
                ", ticker='" + ticker + '\'' +
                ", value=" + value +
                ", currency=" + currency +
                "} " + super.toString();
    }


}
