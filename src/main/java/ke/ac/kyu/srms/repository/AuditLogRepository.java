
package ke.ac.kyu.srms.repository;

import ke.ac.kyu.srms.config.DatabaseConnection;
import ke.ac.kyu.srms.model.Admin;
import ke.ac.kyu.srms.model.AuditLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AuditLogRepository {

    /**
     * Records an administrator action.
     *
     * detailsJson must be valid JSON, or null.
     */
    public void logActivity(
            Admin admin,
            String action,
            String entityType,
            String entityId,
            String detailsJson
    ) throws SQLException {

        String sql = """
                INSERT INTO audit_logs (
                    admin_user_id,
                    action,
                    entity_type,
                    entity_id,
                    details
                )
                VALUES (?, ?, ?, ?, CAST(? AS jsonb))
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (admin == null) {
                statement.setNull(1, Types.SMALLINT);
            } else {
                statement.setInt(1, admin.getId());
            }

            statement.setString(2, action);
            statement.setString(3, entityType);
            statement.setString(4, entityId);

            if (detailsJson == null || detailsJson.isBlank()) {
                statement.setNull(5, Types.VARCHAR);
            } else {
                statement.setString(5, detailsJson);
            }

            statement.executeUpdate();
        }
    }

    /**
     * Retrieves the most recent audit activities.
     */
    public List<AuditLog> findRecent(int limit) throws SQLException {

        String sql = """
                SELECT
                    a.id,
                    a.admin_user_id,
                    u.username AS admin_username,
                    a.action,
                    a.entity_type,
                    a.entity_id,
                    a.details::text AS details,
                    a.created_at
                FROM audit_logs a
                LEFT JOIN admin_users u
                    ON u.id = a.admin_user_id
                ORDER BY a.created_at DESC, a.id DESC
                LIMIT ?
                """;

        List<AuditLog> activities = new ArrayList<>();

        int safeLimit = Math.max(1, Math.min(limit, 100));

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, safeLimit);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {

                    Timestamp timestamp = result.getTimestamp("created_at");

                    int adminId = result.getInt("admin_user_id");
                    Integer adminUserId = result.wasNull()
                            ? null : adminId;

                    activities.add(new AuditLog(
                            result.getLong("id"),
                            adminUserId,
                            result.getString("admin_username"),
                            result.getString("action"),
                            result.getString("entity_type"),
                            result.getString("entity_id"),
                            result.getString("details"),
                            timestamp != null
                                    ? timestamp.toLocalDateTime()
                                    : null
                    ));
                }
            }
        }

        return activities;
    }

    /**
     * Returns the number of activities recorded.
     */
    public int countActivities() throws SQLException {

        String sql = "SELECT COUNT(*) FROM audit_logs";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            if (result.next()) {
                return result.getInt(1);
            }
        }

        return 0;
    }
}
