package com.ltcode.capitalgainstaxcalculator.data_reader;

import com.ltcode.capitalgainstaxcalculator.broker.Broker;
import com.ltcode.capitalgainstaxcalculator.exception.*;
import com.ltcode.capitalgainstaxcalculator.settings.Settings;
import com.ltcode.capitalgainstaxcalculator.transaction.*;
import com.ltcode.capitalgainstaxcalculator.transaction.Currency;
import com.ltcode.capitalgainstaxcalculator.transaction.BuySellTransactionBuilder;
import com.ltcode.capitalgainstaxcalculator.transaction.type.TransactionType;
import com.ltcode.capitalgainstaxcalculator.utils.Utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TransactionReader {

    private static abstract class BrokerReader {

        /**
         * Columns / Info in files with transactions and accounts
         */
        enum FileData {
            DATE,
            TIME,
            PRODUCT,
            ISIN,
            TICKER,
            DESCRIPTION,
            EXCHANGE_RATE,
            DIVIDEND,
            CURRENCY

        }

        enum Language {
            POLISH ("Zakup",
                    "Sprzeda",
                    "Konwersja funduszu go",
                    "Dividende",
                    ""),
            GERMAN ("Kauf",
                    "Verkauf",
                    "Geldmarktfonds Umwandlung:",
                    "Dividende",
                    "Dividendensteuer");
            private final String buyKeyWord;
            private final String sellKeyWord;
            private final String buySellFundDescriptionStart;
            private final String dividendDescription;
            private final String dividendTaxDescription;
            Language(String buyKeyWord, String sellKeyWord, String buySellFundDescriptionStart, String dividendDescription,
                     String dividendTaxDescription) {
                this.buyKeyWord = buyKeyWord;
                this.sellKeyWord = sellKeyWord;
                this.buySellFundDescriptionStart = buySellFundDescriptionStart;
                this.dividendDescription = dividendDescription;
                this.dividendTaxDescription = dividendTaxDescription;
            }

            /**
             * Get Language depending on the first line in csv file
             */
            public static Language getLanguage(String[] fileHeader) {
                if (fileHeader[0].equals("Datum") && fileHeader[1].startsWith("Uhrze")) {
                    return GERMAN;
                } else if (fileHeader[0].equals("Data") && fileHeader[1].startsWith("Czas")) {
                    return POLISH;
                } else {
                    throw new LanguageException("No language has been found.");
                }
            }

            public String getBuyKeyWord() {
                return buyKeyWord;
            }

            public String getSellKeyWord() {
                return sellKeyWord;
            }

            public String getBuySellFundDescriptionStart() {
                return buySellFundDescriptionStart;
            }


            public String getDividendDescription() {
                return dividendDescription;
            }

            public String getDividendTaxDescription() {
                return dividendTaxDescription;
            }
        }

        /**
         * File provides information about dividends, fees, and / or buy sell stocks transactions
         */
        abstract List<Transaction> readAccountFile(Path path);

        /**
         * File provides information about buy sell stocks transactions
         */
        abstract List<Transaction> readTransactionsFile(Path path);

    }


    /**
     * Split -> must be calculated and not sold transactions updated in the GainCalculator class
     */
    private static class RevolutTransactionReader extends BrokerReader {

        @Override
        List<Transaction> readAccountFile(Path path) {
            return read(path);
        }

        /**
         * DividendList will be read together with buy / sell operations from account file
         */
        @Override
        List<Transaction> readTransactionsFile(Path path) {
            throw new RuntimeException("Not in use.");
        }

        private static class Split {
            String ticker;
            LocalDateTime date;
            BigDecimal oldQuantity;
            BigDecimal newQuantity;
        }

        /**
         * Dividend is only the amount you received at account. To get the source tax paid and the total amount,
         * check manually the investment account in revolut app
         */
        private static List<Transaction> read(Path path) {
            List<Transaction> transactionsList = new ArrayList<>();
            List<String> lines;

            try {
                lines = Files.readAllLines(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (int i = 0; i < lines.size(); i++) {
                // System.out.println("\tReading line: " + (i + 1));

                // update line
                // revolut is putting values bigger than "$1,000" in "" and is using commas
                // update it getting rid of the commas and ""
                String updatedLine = editLine(lines.get(i));
                String[] arr = updatedLine.split(",");
                if (arr.length != 8) {
                    throw new TransactionInfoException("Array length after splitting line should be always 8 but is: " + arr.length);
                }

                // create transaction type
                final String TRANSACTION_TYPE = arr[2].startsWith("TRANSFER FROM")
                        ? "TRANSFER FROM"
                        : arr[2];
                TransactionType type = switch (TRANSACTION_TYPE) {
                    case "BUY - MARKET", "BUY - STOP", "BUY - LIMIT" -> TransactionType.BUY;
                    case "SELL - MARKET", "SELL - STOP", "SELL - LIMIT" -> TransactionType.SELL;
                    case "DIVIDEND" -> TransactionType.DIVIDEND;
                    case "STOCK SPLIT" -> TransactionType.STOCK_SPLIT;
                    case "CUSTODY FEE" -> TransactionType.CUSTODY_FEE;
                    case "CASH TOP-UP" -> TransactionType.CASH_TOP_UP;
                    case "CASH WITHDRAWAL" -> TransactionType.CASH_WITHDRAWAL;
                    case "TRANSFER FROM" -> TransactionType.TRANSFER_TO_DIFF_LOCATION;
                    case "Type" -> null;
                    default -> throw new RevolutReaderException("No Type of type: " + arr[2]);
                };

                if (type == null && arr[2].equals("Type")) {            // first line -> header
                    continue;
                }

                // System.out.println(lines.get(i));
                // System.out.println(updatedLine);
                Transaction t;
                if (type == TransactionType.DIVIDEND) {
                    DividendData lideData = new DividendData(
                            i + 1,
                            getDateTime(arr[0]),
                            arr[1],
                            arr[1],             // product name and ticker the same - revolut does not give us more info in csv file
                            type,
                            null,       // no value in file
                            null,                   // no value in file
                            arr[5].length() == 0 ? null : new BigDecimal(getNumberFromCurrency(arr[5])),    // amount after taxes
                            Currency.valueOf(arr[6])
                    );
                    t = TransactionBuilder.build(lideData);
                } else {
                    TransactionData lineData = new TransactionData(
                            i + 1,
                            getDateTime(arr[0]),
                            arr[1],
                            arr[1],             // product name and ticker the same - revolut does not give us more info in csv file
                            type,
                            arr[3].length() == 0 ? null : new BigDecimal(getNumberFromCurrency(arr[3])),
                            arr[4].length() == 0 ? null : new BigDecimal(getNumberFromCurrency(arr[4])),
                            arr[5].length() == 0 ? null : new BigDecimal(getNumberFromCurrency(arr[5])),
                            BigDecimal.ZERO,
                            Currency.valueOf(arr[6]));

                    t = TransactionBuilder.build(lineData);
                }
                transactionsList.add(t);
            }

            return transactionsList;
        }

        private static String getNumberFromCurrency(String currency) {
            StringBuilder sb = new StringBuilder(currency.length());
            for (char ch : currency.toCharArray()) {
                if (ch == '-' || ch == '.' || Character.isDigit(ch)) {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }

        /**
         * Gets rid of "" and ',' in columns with values    -> 23-02-2023,00:00,ABC,"-1,234.00",
         */
        private static String editLine(String s) {
            List<Integer> quotationMarksIdx = new ArrayList<>();
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (ch == '"') {
                    quotationMarksIdx.add(i);
                }
            }
            if (quotationMarksIdx.size() == 0) {
                return s;
            }
            if ((quotationMarksIdx.size() & 1) == 1) {
                throw new TransactionInfoException("Number of quotations marks must be even. Found: " + quotationMarksIdx.size());
            }

            StringBuilder sb = new StringBuilder(s.length());
            sb.append(s, 0, quotationMarksIdx.get(0));
            for (int i = 0; i < quotationMarksIdx.size(); i += 2) {
                int startIdx = quotationMarksIdx.get(i);
                int endIdx = quotationMarksIdx.get(i + 1);
                String update = getNumberFromCurrency(s.substring(startIdx, endIdx));
                sb.append(update);
                int nextStartIdx = i + 2 < quotationMarksIdx.size()
                        ? quotationMarksIdx.get(i + 2)
                        : s.length();
                sb.append(s, endIdx + 1, nextStartIdx);
            }
            return sb.toString();
        }

        private static LocalDateTime getDateTime(String s) {
            try {
                return LocalDateTime.parse(s.substring(0, 21));     // two nano seconds places
            } catch (RuntimeException e) {

            }
            return null;
        }

        private static Split fillSplitData(Split split, TransactionData data) {
            if (split == null) {                                                                // data with old quantity value
                split = new Split();
                split.ticker = data.ticker();
                split.date = data.dateTime();
                split.oldQuantity = data.quantity().multiply(new BigDecimal("-1"));
            } else {                                                                                // data with new quantity value after split
                if (!split.ticker.equals(data.ticker())) {
                    throw new TransactionInfoException("Different split tickers: " + split.ticker + " / " + data.ticker());
                }
                split.newQuantity = data.quantity();
            }
            return split;
        }

        private static void updateTransactionsAfterSplit(Split split, List<Transaction> transactionsList) {
            BigDecimal splitAmount = split.oldQuantity.divide(split.newQuantity);

            for (int i = 0; i < transactionsList.size(); i++) {
                Transaction transaction = transactionsList.get(i);
                if (transaction.getType() != TransactionType.BUY && transaction.getType() == TransactionType.SELL) {
                    continue;
                }
                BuySellTransaction t = (BuySellTransaction) transactionsList.get(i);
                if (!t.getTicker().equals(split.ticker) || t.getDateTime().compareTo(split.date) >= 0) {
                    continue;
                }
                BuySellTransaction updated = new BuySellTransactionBuilder()
                        .setDateTime(t.getDateTime())
                        .setTicker(t.getTicker())
                        .setType(t.getType())
                        .setQuantity(t.getQuantity().divide(splitAmount))
                        .setPricePerShare(t.getPricePerShare().multiply(splitAmount))
                        .setValue(t.getValue())
                        .setCurrency(t.getCurrency())
                        .build();
                transactionsList.set(i, updated);
            }
        }
    }

    /**
     * HEADER REPRESENTATION OF ACCOUNT FILE -> [Data	Czas	Data	Produkt	ISIN	Opis	Kurs	Zmiana		Saldo		Identyfikator zlecenia]
     * Split -> stocks are sold and bought again in the new quantity
     */
    private static class DegiroTransactionReader extends BrokerReader {

        @Override
        List<Transaction> readAccountFile(Path path) {
            List<String> lines;
            try {
                lines = Files.readAllLines(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            List<Transaction> allLists = new ArrayList<>();
            allLists.addAll(getAutomaticFundTransactions(lines));
            allLists.addAll(getDividendList(lines));
            allLists.sort(Comparator.comparing(Transaction::getDateTime));
            return allLists;
        }

        @Override
        List<Transaction> readTransactionsFile(Path path) {
            return readBuySellTransactions(path);
        }

        /**
         * Columns indexes of Degiro Account File
         */
        private final Map<FileData, Integer> ACCOUNT_INDEX_MAP = new HashMap<>() {
            {
                put(FileData.DATE, 2);
                put(FileData.TIME, 1);
                put(FileData.PRODUCT, 3);
                put(FileData.ISIN, 4);
                put(FileData.DESCRIPTION, 5);
                put(FileData.DIVIDEND, 8);
                put(FileData.CURRENCY, 9);
            }
        };

        /**
         * Reads dividends from file "account"
         * Looks for the key word 'dywidend' in description column
         */
        List<DividendTransaction> getDividendList(List<String> lines) {
            Language LANGUAGE;
            String DIVIDEND_KEY_WORD;
            String DIVIDEND_TAX_KEY_WORD;
            List<DividendTransaction> dividendList = new ArrayList<>();
            List<DividendData> dividendDataList = new ArrayList<>();
            List<DividendData> taxPaidDataList = new ArrayList<>();

            // get language
            LANGUAGE = Language.getLanguage(getSplit(lines.get(0)));
            DIVIDEND_KEY_WORD = LANGUAGE.getDividendDescription();
            DIVIDEND_TAX_KEY_WORD = LANGUAGE.getDividendTaxDescription();

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] arr = getSplit(line);

                if (arr.length != 11 && arr.length != 12) {
                    throw new TransactionInfoException("Array length after splitting line should be always 18 or 19 but is: " + arr.length);
                }

                String DESCRIPTION = arr[ACCOUNT_INDEX_MAP.get(FileData.DESCRIPTION)];
                boolean isDividend = DESCRIPTION.equals(DIVIDEND_KEY_WORD);
                boolean isTaxPaid = DESCRIPTION.equals(DIVIDEND_TAX_KEY_WORD);

                if (!isDividend && !isTaxPaid) {
                    continue;
                }

                LocalDateTime dateTime = LocalDateTime.of(
                        LocalDate.parse(arr[ACCOUNT_INDEX_MAP.get(FileData.DATE)], DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                        LocalTime.parse(arr[ACCOUNT_INDEX_MAP.get(FileData.TIME)])
                );
                String ticker = arr[ACCOUNT_INDEX_MAP.get(FileData.ISIN)];
                String product = arr[ACCOUNT_INDEX_MAP.get(FileData.PRODUCT)];
                TransactionType type = TransactionType.DIVIDEND;
                Currency currency= Currency.valueOf(arr[ACCOUNT_INDEX_MAP.get(FileData.CURRENCY)]);
                BigDecimal value = new BigDecimal(arr[ACCOUNT_INDEX_MAP.get(FileData.DIVIDEND)]);
                BigDecimal taxPaid = null;

                if (isTaxPaid) {
                    taxPaid = value.multiply(new BigDecimal(-1));
                    value = null;
                }

                DividendData data = new DividendData(
                        i,
                        dateTime,
                        ticker,
                        product,
                        type,
                        value,
                        taxPaid,
                        null,
                        currency
                );

                // Invalid values can happen in Degiro
                if ((isDividend && Utils.isNegative(data.dividendBeforeTaxes()))
                        || (isTaxPaid && Utils.isNegative(data.taxPaid()))) {
                    System.out.println("Problem on line: " + i);
                    System.out.println("Invalid values!");
                    continue;
                }

                if (isDividend) {
                    dividendDataList.add(data);
                } else {
                    taxPaidDataList.add(data);
                }
            }

            // connect tax paid to dividend
            for (var dividend :dividendDataList) {
                int i;
                BigDecimal taxPaid = BigDecimal.ZERO;
                for (i = 0; i < taxPaidDataList.size(); i++) {
                    var taxPaidData = taxPaidDataList.get(i);
                    boolean isMatching = taxPaidData.ticker().equals(dividend.ticker())
                            && taxPaidData.dateTime().toLocalDate().equals(dividend.dateTime().toLocalDate());
                    if (isMatching) {
                        taxPaid = taxPaidData.taxPaid();
                        taxPaidDataList.remove(i);
                        break;
                    }
                }

                BigDecimal afterTaxes = dividend.dividendBeforeTaxes().subtract(taxPaid);
                dividend = DividendData.update(dividend, dividend.dividendBeforeTaxes(), taxPaid, afterTaxes);
                dividendList.add(TransactionBuilder.build(dividend));
            }

            return dividendList;
        }

        /**
         * DO NOT USE
         * WORKS ONLY FOR AUTOMATIC FUND TICKER
         */
        List<Transaction> getTransactions(Path path, String tickerOrISIN) {
            Language LANGUAGE;

            List<Transaction> transactions = new ArrayList<>();
            List<String> lines;
            try {
                lines = Files.readAllLines(path);

                // get language
                LANGUAGE = Language.getLanguage(getSplit(lines.get(0)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (int i = 1; i < lines.size(); i++) {    // skip header
                String line = lines.get(i);
                String[] arr = getSplit(line);

                if (arr.length != 11 && arr.length != 12) {
                    throw new TransactionInfoException("Array length after splitting line should be always 18 or 19 but is: " + arr.length);
                }

                String ISIN = arr[ACCOUNT_INDEX_MAP.get(FileData.ISIN)];
                String DESCRIPTION = arr[ACCOUNT_INDEX_MAP.get(FileData.DESCRIPTION)];

                if (!ISIN.equals(tickerOrISIN)
                    || !DESCRIPTION.startsWith(LANGUAGE.getBuySellFundDescriptionStart())) {
                    continue;
                }


                // read type of transaction - buy / sell, quantity and course from the description column
                Object[] descriptionArr;
                try {
                    //System.out.println("Line: " + i + " " + line);
                    descriptionArr = getDataFromDescription(DESCRIPTION, LANGUAGE);
                } catch (TransactionInfoException e) {
                    System.out.printf("Problem with ticker 's' on line: %s. Message: %s",
                            tickerOrISIN,
                            i,
                            e.getMessage());
                    continue;
                }

                var transactionType = (TransactionType) descriptionArr[0];
                var quantity = (BigDecimal) descriptionArr[1];
                var pricePerShare = (BigDecimal) descriptionArr[2];
                var currency = (Currency) descriptionArr[3];

                if (! Utils.isPositive(pricePerShare) || ! Utils.isPositive(quantity)) {
                    throw new TransactionInfoException("Must be positive: " + i +  " " + line);
                }

                TransactionData data = new TransactionData(
                        i,
                        LocalDateTime.of(
                                LocalDate.parse(arr[ACCOUNT_INDEX_MAP.get(FileData.DATE)], DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                                LocalTime.parse(arr[ACCOUNT_INDEX_MAP.get(FileData.TIME)])
                        ),
                        ISIN,
                        arr[ACCOUNT_INDEX_MAP.get(FileData.PRODUCT)],
                        transactionType,
                        quantity,
                        pricePerShare,
                        quantity.multiply(pricePerShare),
                        null,
                        currency
                );
                transactions.add(TransactionBuilder.build(data));
            }
            return transactions;
        }

        List<Transaction> getAutomaticFundTransactions(List<String> lines) {
            Language LANGUAGE;
            List<Transaction> transactions = new ArrayList<>();

            // get language
            LANGUAGE = Language.getLanguage(getSplit(lines.get(0)));

            for (int i = 1; i < lines.size(); i++) {    // skip header
                String line = lines.get(i);
                String[] arr = getSplit(line);

                if (arr.length != 11 && arr.length != 12) {
                    throw new TransactionInfoException("Array length after splitting line should be always 11 or 12 but is: " + arr.length);
                }

                String ISIN = arr[ACCOUNT_INDEX_MAP.get(FileData.ISIN)];
                String DESCRIPTION = arr[ACCOUNT_INDEX_MAP.get(FileData.DESCRIPTION)];

                if (!DESCRIPTION.startsWith(LANGUAGE.getBuySellFundDescriptionStart())) {
                    continue;
                }

                // read type of transaction - buy / sell, quantity and course from the description column
                Object[] descriptionArr;
                try {
                    descriptionArr = getDataFromDescription(DESCRIPTION, LANGUAGE);
                } catch (TransactionInfoException e) {
                    System.out.printf("Problem with ticker 's' on line: %s. Message: %s",
                            ISIN,
                            i,
                            e.getMessage());
                    continue;
                } catch (NotAllDataInDescriptionException e) {
                    // Sometimes in degiro files description might be extended to two lines instead of one
                    // concatenate 2 lines and try again
                    String[] NEXT_LINE_ARR = getSplit(lines.get(i + 1));
                    DESCRIPTION += NEXT_LINE_ARR[ACCOUNT_INDEX_MAP.get(FileData.DESCRIPTION)];
                    descriptionArr = getDataFromDescription(DESCRIPTION, LANGUAGE);
                }

                var transactionType = (TransactionType) descriptionArr[0];
                var quantity = (BigDecimal) descriptionArr[1];
                var pricePerShare = (BigDecimal) descriptionArr[2];
                var currency = (Currency) descriptionArr[3];

                if (! Utils.isPositiveOrZero(pricePerShare) || ! Utils.isPositiveOrZero(quantity)) {
                    throw new TransactionInfoException("Must be not negative: " + i +  " " + line);
                }

                TransactionData data = new TransactionData(
                        i,
                        LocalDateTime.of(
                                LocalDate.parse(arr[ACCOUNT_INDEX_MAP.get(FileData.DATE)], DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                                LocalTime.parse(arr[ACCOUNT_INDEX_MAP.get(FileData.TIME)])
                        ),
                        ISIN,
                        arr[ACCOUNT_INDEX_MAP.get(FileData.PRODUCT)],
                        transactionType,
                        quantity,
                        pricePerShare,
                        quantity.multiply(pricePerShare),
                        BigDecimal.ZERO,
                        currency
                );
                transactions.add(TransactionBuilder.build(data));

                // must be sorted by date . sometimes there is a difference by few days
                transactions.sort(Comparator.comparing(Transaction::getDateTime));
            }
            return transactions;
        }

        /**
         *
         * @param s - value of description column
         * @return Object[] {type, quantity, pricePerShare, currency}
         */
        private static Object[] getDataFromDescription(String s, Language country)
                throws TransactionInfoException,
                NotAllDataInDescriptionException{
            String BUY_KEY_WORD = country.getBuyKeyWord();
            String SELL_KEY_WORD = country.getSellKeyWord();

            TransactionType type;
            BigDecimal quantity;
            BigDecimal pricePerShare;
            Currency currency;
            int typeIdx = s.indexOf(BUY_KEY_WORD);
            if (typeIdx >= 0) {
                type = TransactionType.BUY;
            } else {
                typeIdx = s.indexOf(SELL_KEY_WORD);
                if (typeIdx == -1) {
                    throw new TransactionInfoException(String.format("No '%s' / '%s' in description '%s'.", BUY_KEY_WORD, SELL_KEY_WORD, s));
                }
                type = TransactionType.SELL;
            }

            // currency
            int j = s.length();
            int i = s.lastIndexOf(" ");
            try {
                currency = Currency.valueOf(s.substring(i + 1, j));
            } catch (RuntimeException e) {
                throw new NotAllDataInDescriptionException("Currency not found in description!");
            }

            // price per share
            j = i;
            i = s.lastIndexOf(" ", i - 1);
            pricePerShare = new BigDecimal(getNumberFromString(s.substring(i, j)));

            // quantity
            while (!Character.isDigit(s.charAt(i))) { // find last digit in quantity
                i--;
            }
            j = i + 1;  // first space after last digit in

            i = typeIdx;
            while (!Character.isDigit(s.charAt(i))) {   // find first digit in quantity
                i++;
            }
            quantity = new BigDecimal(getNumberFromString(s.substring(i, j)));

            return new Object[]{type, quantity, pricePerShare, currency};
        }

        private static List<Transaction> readBuySellTransactions(Path path) {
            List<Transaction> transactionsList = new ArrayList<>();
            List<String> lines;

            try {
                lines = Files.readAllLines(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // object to index in a String[]
            Map<String, Integer> indexMap = new HashMap<>();

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] arr = getSplit(line);

                if (arr.length != 18 && arr.length != 19) {
                    throw new TransactionInfoException("Array length after splitting line should be always 18 or 19 but is: " + arr.length
                    + " LINE: " + i);
                }

                // header, find indexes
                if (i == 0) {
                    for (int j = 0; j < arr.length; j++) {
                        String columnName = arr[j];
                        columnName = columnName.toLowerCase();
                        switch (columnName) {
                            case "datum" :
                            case "data" :
                                indexMap.put("date", j);
                                break;
                            case "uhrzeit" :
                            case "czas":
                                indexMap.put("time", j);
                                break;
                            case "isin" :
                                indexMap.put("ticker", j);
                                break;
                            case "produkt":
                                indexMap.put("product", j);
                                break;
                            case "wert" :
                            case "wartość" :
                                indexMap.put("valueWithoutCommission", j);
                                break;
                            case "anzahl" :
                            case "liczba" :
                                indexMap.put("quantity", j);
                                break;
                            case "kurs":
                                indexMap.put("pricePerShare", j);
                                break;
                            case "opłata transakcyjna":
                            case "transaktionsgebühren":
                                indexMap.put("commission", j);
                                break;
                            case "kurs wymian": // exchange curse from local currency to euro
                            case "wechselkurs":
                                indexMap.put("exchangeRate", j);        // can be empty
                                break;
                        }
                    }
                    // price per share is only given in local currency
                    // type depends if the quantity is negative or positive
                    indexMap.put("currency", 17);
                    continue;
                }

                // is given in local currency not in euro, but the whole value is always in euro
                // that's why we change it to euro
                BigDecimal pricePerShare = new BigDecimal(arr[indexMap.get("pricePerShare")]);
                if (! arr[indexMap.get("exchangeRate")].isEmpty()) {
                    BigDecimal exchangeRate = new BigDecimal(arr[indexMap.get("exchangeRate")]);
                    pricePerShare = pricePerShare.divide(exchangeRate, 2, RoundingMode.HALF_UP);
                }

                // System.out.println(line);

                BigDecimal quantity = new BigDecimal(arr[indexMap.get("quantity")]);
                // total value with commission
                BigDecimal valueWithoutCommission = new BigDecimal(arr[indexMap.get("valueWithoutCommission")]);
                BigDecimal commission = arr[indexMap.get("commission")].isEmpty()
                        ? BigDecimal.ZERO
                        : new BigDecimal(arr[indexMap.get("commission")]);
                TransactionType type;


                // quantity negative -> value positive
                // quantity positive -> value negative
                if (Utils.isPositive(quantity) && Utils.isNegative(valueWithoutCommission)) {
                    type = TransactionType.BUY;
                    valueWithoutCommission = valueWithoutCommission.abs();
                } else if (Utils.isNegative(quantity) && Utils.isPositiveOrZero(valueWithoutCommission)){
                    type = TransactionType.SELL;
                    quantity = quantity.abs();
                } else {
                    throw new InvalidQuantityException("Invalid quantity / value " + line);
                }

                if (Utils.isPositive(commission)) {
                    throw new InvalidQuantityException("Commission can not be positive: " + line);
                }
                commission = commission.abs();

                TransactionData lineData = new TransactionData(
                        i + 1,
                        LocalDateTime.of(
                                LocalDate.parse(arr[indexMap.get("date")], DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                                LocalTime.parse(arr[indexMap.get("time")])
                        ),
                        arr[indexMap.get("ticker")],
                        arr[indexMap.get("product")],
                        type,
                        quantity,
                        pricePerShare,
                        valueWithoutCommission,
                        commission,
                        Currency.valueOf(arr[indexMap.get("currency")]));

                Transaction t = TransactionBuilder.build(lineData);
                transactionsList.add(t);
            }
            return transactionsList;
        }

        /**
         * Don't split when ',' is between quotes    -> 23-02-2023,00:00,ABC,"-1,234.00",
         */
        private static String[] getSplit(String line) {
            final char QUOTE = '"';
            List<String> list = new ArrayList<>(19);
            for (int l = 0; l < line.length();) {

                int r = line.indexOf(Settings.CSV_SEPARATOR, l);
                if (r == -1) {
                    r = line.length();
                }
                // check for quotes
                if (r == 0 || line.charAt(l) != QUOTE && line.charAt(r - 1) != QUOTE) {
                    list.add(line.substring(l, r));
                    l = r + 1;
                } else if (line.charAt(l) == QUOTE && line.charAt(r - 1) == QUOTE) {
                    list.add(line.substring(l + 1, r - 1));
                    l = r + 1;
                } else {    // find ending quote
                    r = line.indexOf(QUOTE, r + 1);
                    list.add(line.substring(l + 1, r));
                    l = r + 2;
                }
            }
            return list.toArray(new String[0]);
        }

        /**
         * Remove spaces, change ',' to '.'
         * Ignore other characters
         */
        private static String getNumberFromString(String currency) {
            StringBuilder sb = new StringBuilder(currency.length());
            for (char ch : currency.toCharArray()) {
                if (ch == ',') {
                    sb.append('.');
                } else if (Character.isDigit(ch)) {
                    sb.append(ch - '0');
                }
            }
            return sb.toString();
        }
    }

    // == MAIN CLASS STATIC METHODS ==

    public static List<Transaction> readAccountFile(Broker broker, Path path) {
        BrokerReader reader = getBrokerReader(broker);
        return reader.readAccountFile(path);
    }

    public static List<Transaction> readTransactionsFile(Broker broker, Path path) {
        BrokerReader reader = getBrokerReader(broker);
        return reader.readTransactionsFile(path);
    }

    // == PRIVATE HELPER METHODS ==

    private static BrokerReader getBrokerReader(Broker broker) {
        return switch (broker) {
            case REVOLUT -> new RevolutTransactionReader();
            case DEGIRO -> new DegiroTransactionReader();
        };
    }
}
