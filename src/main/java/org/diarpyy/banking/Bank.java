package org.diarpyy.banking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Bank {

    public Bank() { }

    private void printMenu() {
        System.out.println("\n1. Create an account" +
                "\n2. Log into account" +
                "\n0. Exit");
    }

    private void printSubMenu() {
        System.out.println("\n1. Balance" +
                "\n2. Add income" +
                "\n3. Do transfer" +
                "\n4. Close account" +
                "\n5. Log out" +
                "\n6. Withdraw" +
                "\n0. Exit");
    }

    public Account getAccount(Card card) {
        Account account = null;
        String selectSQL = "SELECT * FROM card WHERE number = ? AND pin = ?";
        try (Connection connection = Main.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {

            preparedStatement.setString(1, card.getNumber());
            preparedStatement.setString(2, card.getPin());
            ResultSet resultSet = preparedStatement.executeQuery();
            account = getAccount(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return account;
    }

    private Account getAccount(ResultSet resultSet) throws SQLException {
        Account account = null;
        if (resultSet.next()) {
            int id = resultSet.getInt(1);
            String number = resultSet.getString(2);
            String pin = resultSet.getString(3);
            int balance = resultSet.getInt(4);
            Card card1 = new Card(id, number, pin, balance);
            account = new Account(card1);
        }
        return account;
    }

    public Account getAccountByNumber(String cardNumber) {
        Account account = null;
        String selectSQL = "SELECT * FROM card WHERE number = ?";
        try (Connection connection = Main.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {

            preparedStatement.setString(1, cardNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            account = getAccount(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return account;
    }

    public static List<Account> getAccounts() {
        List<Account> list = new ArrayList<>();
        try (Connection connection = Main.getDataSource().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM card")) {
            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                String number = resultSet.getString(2);
                String pin = resultSet.getString(3);
                int balance = resultSet.getInt(4);
                Card card = new Card(id, number, pin, balance);
                Account account = new Account(card);
                list.add(account);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public boolean checkNumber(String number) {
        boolean result = false;
        String selectSQL = "SELECT * FROM card WHERE number = ?";
        try (Connection connection = Main.getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)){
                preparedStatement.setString(1, number);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                   result = true; 
                }
                connection.commit();
                resultSet.close();
            } catch (SQLException e) {
                connection.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean updateBalance(Card card) {
        String updateSQL = "UPDATE card SET balance = ? WHERE id = ?";
        try (Connection connection = Main.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {

                preparedStatement.setInt(1, card.getBalance());
                preparedStatement.setInt(2, card.getId());
                preparedStatement.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        while(true) {
            printMenu();
            String menu = scanner.next();

            switch (menu) {
                case "0":
                    System.out.println("\nBye!");
                    return;

                case "1":
                    Account.createAccount();
                    break;

                case "2":
                    System.out.println("\nEnter your card number:");
                    String cardNumber = scanner.next();
                    System.out.println("Enter your PIN:");
                    String pin = scanner.next();
                    Card card = new Card(cardNumber, pin);
                    Account account = getAccount(card);
                    if (account != null) {
                        System.out.println("\nYou have successfully logged in!");
                        label:
                        while (true) {
                            printSubMenu();
                            menu = scanner.next();
                            switch (menu) {
                                case "1":
                                    System.out.println("\nBalance: " + account.getCard().getBalance());
                                    break;

                                case "2":
                                    System.out.println("\nEnter income:");
                                    int amount = scanner.nextInt();
                                    addIncome(account, amount);
                                    break;

                                case "3":
                                    System.out.println("\nTransfer");
                                    String recipientNumber;
                                    System.out.println("Enter card number:");
                                    recipientNumber = scanner.next();

                                    if (account.getCard().getNumber().equals(recipientNumber)) {
                                        System.out.println("You can't transfer money to the same account!");
                                    } else if (!Account.isValidCard(new StringBuilder(recipientNumber))) {
                                        System.out.println("Probably you made mistake in the card number. Please try again!");
                                    } else if (!checkNumber(recipientNumber)) {
                                        System.out.println("Such a card does not exist.");
                                    } else {
                                        System.out.println("Enter how much money you want to transfer:");
                                        amount = scanner.nextInt();
                                        if (amount > account.getCard().getBalance()) {
                                            System.out.println("Not enough money!");
                                        } else {
                                            Account recipient = getAccountByNumber(recipientNumber);
                                            if (transfer(account, recipient, amount)) {
                                                System.out.println("Success!");
                                            } else System.out.println("Error when transferring. Please try again!");
                                        }
                                    }
                                    break;

                                case "4":
                                    if (delete(account)) {
                                        System.out.println("The account has been closed!");
                                    } else System.out.println("Error when deleting. Please try again!");
                                    break;

                                case "5":
                                    System.out.println("\nYou have successfully logged out!");
                                    break label;

                                case "6":
                                    System.out.println("\nEnter the amount to withdraw:");
                                    amount = scanner.nextInt();
                                    if (amount > account.getCard().getBalance()) {
                                        System.out.println("Not enough money!");
                                    } else {
                                        remove(account, amount);
                                    }
                                    break;

                                case "0":
                                    System.out.println("\nBye!");
                                    return;
                                default:
                                    System.out.println("Unknown menu");
                                    break;
                            }
                        }
                    } else {
                        System.out.println("\nWrong card number or PIN!");
                    }
                    break;

                default:
                    System.out.println("Unknown menu");
                    break;
            }
        }
    }

    private void addIncome(Account recipient, int amount) {
        int recipientBalance = recipient.getCard().getBalance() + amount;
        recipient.getCard().setBalance(recipientBalance);
        if (updateBalance(recipient.getCard())) {
            System.out.println("Income was added!");
        } else {
            System.out.println("Error when adding. Please try again!");
            recipient.getCard().setBalance(recipientBalance - amount);
        }
    }

    private void remove(Account account, int amount) {
        int balance = account.getCard().getBalance() - amount;
        account.getCard().setBalance(balance);
        if (updateBalance(account.getCard())) {
            System.out.println("Amount was removed!");
        } else {
            System.out.println("Error when removing. Please try again!");
            account.getCard().setBalance(balance + amount);
        }
    }

    private void updateBalance(PreparedStatement preparedStatement, Account account, int newBalance) throws SQLException {
        preparedStatement.setInt(1, newBalance);
        preparedStatement.setInt(2, account.getCard().getId());
        preparedStatement.executeUpdate();
    }

    private boolean delete(Account account) {
        String updateSQL = "DELETE FROM card WHERE id = ?";
        try (Connection connection = Main.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {

            preparedStatement.setInt(1, account.getCard().getId());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean transfer(Account sender, Account recipient, int amount) {
        String updateSQL = "UPDATE card SET balance = ? WHERE id = ?";
        try (Connection connection = Main.getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)){
                int senderBalance = sender.getCard().getBalance() - amount;
                updateBalance(preparedStatement, sender, senderBalance);

                int recipientBalance = recipient.getCard().getBalance() + amount;
                updateBalance(preparedStatement, recipient, recipientBalance);

                connection.commit();
                sender.getCard().setBalance(senderBalance);
                return true;
            } catch (SQLException e) {
                connection.rollback();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
