package org.diarpyy.banking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class Account {
    private Card card;

    public Account(Card card) {
        this.card = card;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }


    public static void createAccount() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        String accountIdentifier;
        int digit;
        do {
            digit = random.nextInt(10);
            sb.append(digit);
            accountIdentifier = sb.toString();
            for (Account account : Bank.getAccounts()) {
                String number = account.getCard().getNumber();
                String accountNumber = number.substring(6,14);
                if (accountNumber.equals(accountIdentifier)) {
                    sb = new StringBuilder();
                    break;
                }
            }
        } while (sb.length() != 9);

        int issuerIdentificationNumber = 400000;
        sb.insert(0, issuerIdentificationNumber);
        sb.append(getChecksum(sb));
        String cardNumber = sb.toString();

        sb = new StringBuilder();
        do {
            digit = random.nextInt(10);
            sb.append(digit);
        } while (sb.length() != 4);
        String pin = sb.toString();

        Card card = new Card(cardNumber, pin);
        createCard(card);
        System.out.println("\nYour card has been created");
        System.out.println("Your card number:");
        System.out.println(cardNumber);
        System.out.println("Your card PIN:");
        System.out.println(pin);
    }

    private static void createCard(Card card) {
        String insertSQL = "INSERT INTO card (number, pin) VALUES (?, ?)";
        try (Connection connection = Main.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

            preparedStatement.setString(1, card.getNumber());
            preparedStatement.setString(2, card.getPin());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getChecksum(StringBuilder numbers) {
        int controlNumber = getControlNumber(numbers);
        int mod = controlNumber % 10;

        return mod == 0 ? 0 : 10 - mod;
    }

    /**
     * Multiply even digits(step) by 2
     * Subtract 9 to numbers over 9
     * add all numbers
     * @param numbers (credit card numbers without checksum)
     * @return int
     */
    public static int getControlNumber(StringBuilder numbers) {
        int controlNumber = 0;
        StringBuilder numbers2 = new StringBuilder(numbers.toString());
        for (int i = 0; i < numbers2.length(); i += 2) {
            char ch = numbers2.charAt(i);
            int value = Character.getNumericValue(ch) * 2;
            ch = value > 9 ?
                    Character.forDigit(value - 9, 10) :
                    Character.forDigit(value, 10);
            numbers2.setCharAt(i, ch);
        }
        for (int i = 0; i < numbers2.length(); i++) {
            controlNumber += Character.getNumericValue(numbers2.charAt(i));
        }
        return controlNumber;
    }


    /***
     * Checking card numbers with Luhn algorithm
     * @param numbers (credit card numbers)
     * @return boolean
     */
    public static boolean isValidCard(StringBuilder numbers) {
        char ch = numbers.charAt(numbers.length() - 1);
        int checksum = Character.getNumericValue(ch);
        StringBuilder numbers2 = new StringBuilder(numbers.toString());
        numbers2.deleteCharAt(numbers2.length() - 1);
        int controlNumber = getControlNumber(numbers2);

        return (controlNumber + checksum) % 10 == 0;
    }
}
