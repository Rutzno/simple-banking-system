package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Mack_TB
 * @version 1.0.7
 * @since 12/02/2020
 */

public class Main {

    private static SQLiteDataSource dataSource;

    public static void main(String[] args) {
        if (args[1] != null) {
            String url = "jdbc:sqlite:" + args[1];

            dataSource = new SQLiteDataSource();
            dataSource.setUrl(url);

            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                //statement.executeUpdate("CREATE DATABASE banking");

                statement.executeUpdate("CREATE TABLE IF NOT EXISTS card (" +
                        "id INTEGER PRIMARY KEY," +
                        "number TEXT," +
                        "pin TEXT," +
                        "balance INTEGER DEFAULT 0)"
                );
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Bank bank = new Bank();
            bank.run();
        } else {
            System.out.println("please specify the argument in the program arguments (-fileName card.s3db)");
        }
    }

    public static SQLiteDataSource getDataSource() {
        return dataSource;
    }
}
