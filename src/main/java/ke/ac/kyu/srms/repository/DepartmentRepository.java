
package ke.ac.kyu.srms.repository;

import ke.ac.kyu.srms.config.DatabaseConnection;
import ke.ac.kyu.srms.model.Department;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DepartmentRepository {

    public List<Department> findAll() throws SQLException {

        String sql = """
                SELECT id, name, description
                FROM departments
                ORDER BY name
                """;

        List<Department> departments = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                departments.add(mapRow(resultSet));
            }
        }

        return departments;
    }

    public Department findById(String id) throws SQLException {

        String sql = """
                SELECT id, name, description
                FROM departments
                WHERE id = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setObject(1, java.util.UUID.fromString(id));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    public void save(Department department) throws SQLException {

        String sql = """
                INSERT INTO departments (id, name, description)
                VALUES (?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setObject(
                    1, java.util.UUID.fromString(department.getId())
            );
            statement.setString(2, department.getName());
            statement.setString(3, department.getDescription());

            statement.executeUpdate();
        }
    }

    public boolean update(Department department) throws SQLException {

        String sql = """
                UPDATE departments
                SET name = ?,
                    description = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, department.getName());
            statement.setString(2, department.getDescription());
            statement.setObject(
                    3, java.util.UUID.fromString(department.getId())
            );

            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(String id) throws SQLException {

        String sql = """
                DELETE FROM departments
                WHERE id = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setObject(1, java.util.UUID.fromString(id));

            return statement.executeUpdate() == 1;
        }
    }

    private Department mapRow(ResultSet resultSet) throws SQLException {

        return new Department(
                resultSet.getObject("id").toString(),
                resultSet.getString("name"),
                resultSet.getString("description")
        );
    }
}
