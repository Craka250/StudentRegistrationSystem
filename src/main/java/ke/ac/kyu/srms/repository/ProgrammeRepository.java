
package ke.ac.kyu.srms.repository;

import ke.ac.kyu.srms.config.DatabaseConnection;
import ke.ac.kyu.srms.model.Programme;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProgrammeRepository {

    public List<Programme> findAll() throws SQLException {

        String sql = """
                SELECT id, name, department_id, description
                FROM programmes
                ORDER BY name
                """;

        List<Programme> programmes = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                programmes.add(mapRow(resultSet));
            }
        }

        return programmes;
    }

    public List<Programme> findByDepartmentId(String departmentId)
            throws SQLException {

        String sql = """
                SELECT id, name, department_id, description
                FROM programmes
                WHERE department_id = ?
                ORDER BY name
                """;

        List<Programme> programmes = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setObject(1, UUID.fromString(departmentId));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    programmes.add(mapRow(resultSet));
                }
            }
        }

        return programmes;
    }

    public Programme findById(String id) throws SQLException {

        String sql = """
                SELECT id, name, department_id, description
                FROM programmes
                WHERE id = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setObject(1, UUID.fromString(id));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    public void save(Programme programme) throws SQLException {

        String sql = """
                INSERT INTO programmes
                    (id, name, department_id, description)
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setObject(1, UUID.fromString(programme.getId()));
            statement.setString(2, programme.getName());
            statement.setObject(
                    3, UUID.fromString(programme.getDepartmentId())
            );
            statement.setString(4, programme.getDescription());

            statement.executeUpdate();
        }
    }

    public boolean update(Programme programme) throws SQLException {

        String sql = """
                UPDATE programmes
                SET name = ?,
                    department_id = ?,
                    description = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, programme.getName());
            statement.setObject(
                    2, UUID.fromString(programme.getDepartmentId())
            );
            statement.setString(3, programme.getDescription());
            statement.setObject(4, UUID.fromString(programme.getId()));

            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(String id) throws SQLException {

        String sql = """
                DELETE FROM programmes
                WHERE id = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setObject(1, UUID.fromString(id));

            return statement.executeUpdate() == 1;
        }
    }

    private Programme mapRow(ResultSet resultSet) throws SQLException {

        return new Programme(
                resultSet.getObject("id").toString(),
                resultSet.getString("name"),
                resultSet.getObject("department_id").toString(),
                resultSet.getString("description")
        );
    }
}
