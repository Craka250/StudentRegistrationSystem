
package ke.ac.kyu.srms.setup;

import ke.ac.kyu.srms.config.DatabaseConnection;
import ke.ac.kyu.srms.security.AdminPasswordUtil;

import java.io.Console;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;

public class AdminSetup {

    private static final long SETUP_LOCK_ID = 72190411L;

    public static void main(String[] args) {

        Console console = System.console();

        if (console == null) {
            System.err.println(
                    "Secure console input is unavailable. "
                    + "Run this setup from a real terminal."
            );
            return;
        }

        char[] password = null;
        char[] confirmation = null;

        try {
            console.printf("%n====================================%n");
            console.printf(" STUDENT REGISTRATION SYSTEM%n");
            console.printf(" INITIAL ADMINISTRATOR SETUP%n");
            console.printf("====================================%n%n");

            String username = console.readLine("Admin username: ");

            if (username == null) {
                return;
            }

            username = username.trim().toLowerCase(Locale.ROOT);

            if (!username.matches("[a-z0-9._-]{3,50}")) {
                console.printf(
                        "Username must be 3-50 characters "
                        + "and contain only letters, numbers, dots, "
                        + "underscores or hyphens.%n"
                );
                return;
            }

            password = console.readPassword("Create password: ");

            if (password == null || password.length < 12) {
                console.printf(
                        "Password must contain at least 12 characters.%n"
                );
                return;
            }

            confirmation = console.readPassword("Confirm password: ");

            if (!Arrays.equals(password, confirmation)) {
                console.printf("Passwords do not match.%n");
                return;
            }

            String passwordHash =
                    AdminPasswordUtil.hashPassword(password);

            createFirstAdministrator(username, passwordHash);

            console.printf(
                    "%nAdministrator account created successfully.%n"
            );

            console.printf(
                    "You can now launch the application and sign in.%n"
            );

        } catch (SQLException exception) {

            System.err.println(
                    "Unable to create the administrator account: "
                    + exception.getMessage()
            );

        } catch (IllegalArgumentException exception) {

            System.err.println(exception.getMessage());

        } finally {

            if (password != null) {
                Arrays.fill(password, '\0');
            }

            if (confirmation != null) {
                Arrays.fill(confirmation, '\0');
            }
        }
    }

    private static void createFirstAdministrator(
            String username,
            String passwordHash
    ) throws SQLException {

        String countSql = "SELECT COUNT(*) FROM admin_users";

        String insertSql = """
                INSERT INTO admin_users (username, password_hash)
                VALUES (?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                // Prevent two initial setup processes from running together.
                try (PreparedStatement lockStatement =
                             connection.prepareStatement(
                                     "SELECT pg_advisory_xact_lock(?)"
                             )) {

                    lockStatement.setLong(1, SETUP_LOCK_ID);
                    lockStatement.executeQuery().close();
                }

                // The bootstrap utility only works when no admin exists.
                try (PreparedStatement countStatement =
                             connection.prepareStatement(countSql);
                     ResultSet result = countStatement.executeQuery()) {

                    if (!result.next() || result.getInt(1) != 0) {
                        throw new SQLException(
                                "An administrator account already exists. "
                                + "Initial setup is no longer available."
                        );
                    }
                }

                try (PreparedStatement insertStatement =
                             connection.prepareStatement(insertSql)) {

                    insertStatement.setString(1, username);
                    insertStatement.setString(2, passwordHash);

                    insertStatement.executeUpdate();
                }

                connection.commit();

            } catch (SQLException | RuntimeException exception) {

                connection.rollback();
                throw exception;

            } finally {

                connection.setAutoCommit(true);
            }
        }
    }
}
