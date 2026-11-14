
package ke.ac.kyu.srms.util;

import ke.ac.kyu.srms.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseConnectionTest {

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println(" STUDENT REGISTRATION SYSTEM");
        System.out.println(" DATABASE CONNECTION TEST");
        System.out.println("======================================");

        String query = """
                SELECT
                    current_database(),
                    current_user,
                    version()
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query)
        ) {

            if (resultSet.next()) {

                System.out.println("\nDatabase connection successful!");

                System.out.println(
                        "Database: " + resultSet.getString(1)
                );

                System.out.println(
                        "User: " + resultSet.getString(2)
                );

                System.out.println(
                        "PostgreSQL: " + resultSet.getString(3)
                );

                System.out.println(
                        "\nSRMS is ready to communicate with PostgreSQL."
                );
            }

        } catch (Exception exception) {

            System.err.println("\nDATABASE CONNECTION FAILED!");
            System.err.println("Reason: " + exception.getMessage());

            exception.printStackTrace();
        }

        System.out.println("\n======================================");
    }
}
