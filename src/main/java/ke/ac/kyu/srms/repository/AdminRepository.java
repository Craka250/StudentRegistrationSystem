
package ke.ac.kyu.srms.repository;

import ke.ac.kyu.srms.config.DatabaseConnection;
import ke.ac.kyu.srms.model.Admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminRepository {

    /**
     * Contains the administrator and password hash for authentication.
     * The hash is deliberately kept out of the Admin model.
     */
    public record AdminAccount(Admin admin, String passwordHash) {
    }

    /**
     * Creates a new administrator.
     */
    public Admin createAdmin(String username, String passwordHash)
            throws SQLException {

        String sql = """
                INSERT INTO admin_users (username, password_hash)
                VALUES (?, ?)
                RETURNING id, username, is_active, created_at, last_login_at
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizeUsername(username));
            statement.setString(2, passwordHash);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return mapAdmin(result);
                }
            }
        }

        throw new SQLException("Unable to create administrator account.");
    }

    /**
     * Finds an administrator by username for authentication.
     */
    public Optional<AdminAccount> findByUsername(String username)
            throws SQLException {

        String sql = """
                SELECT id, username, password_hash,
                       is_active, created_at, last_login_at
                FROM admin_users
                WHERE LOWER(username) = LOWER(?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizeUsername(username));

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    Admin admin = mapAdmin(result);

                    return Optional.of(
                            new AdminAccount(
                                    admin,
                                    result.getString("password_hash")
                            )
                    );
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Retrieves all administrator accounts.
     */
    public List<Admin> findAll() throws SQLException {

        String sql = """
                SELECT id, username, is_active, created_at, last_login_at
                FROM admin_users
                ORDER BY id
                """;

        List<Admin> admins = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                admins.add(mapAdmin(result));
            }
        }

        return admins;
    }

    /**
     * Activates or deactivates an administrator account.
     */
    public boolean setActive(int adminId, boolean active)
            throws SQLException {

        String sql = """
                UPDATE admin_users
                SET is_active = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBoolean(1, active);
            statement.setInt(2, adminId);

            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Updates the administrator's last successful login time.
     */
    public boolean updateLastLogin(int adminId) throws SQLException {

        String sql = """
                UPDATE admin_users
                SET last_login_at = CURRENT_TIMESTAMP
                WHERE id = ? AND is_active = TRUE
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, adminId);

            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Returns the number of registered administrator accounts.
     */
    public int countAdmins() throws SQLException {

        String sql = "SELECT COUNT(*) FROM admin_users";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            if (result.next()) {
                return result.getInt(1);
            }
        }

        return 0;
    }

    private Admin mapAdmin(ResultSet result) throws SQLException {

        Timestamp createdTimestamp = result.getTimestamp("created_at");
        Timestamp loginTimestamp = result.getTimestamp("last_login_at");

        return new Admin(
                result.getInt("id"),
                result.getString("username"),
                result.getBoolean("is_active"),
                createdTimestamp != null
                        ? createdTimestamp.toLocalDateTime() : null,
                loginTimestamp != null
                        ? loginTimestamp.toLocalDateTime() : null
        );
    }

    private String normalizeUsername(String username) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Username cannot be empty."
            );
        }

        return username.trim().toLowerCase();
    }
}
