
package ke.ac.kyu.srms.repository;

import ke.ac.kyu.srms.config.DatabaseConnection;
import ke.ac.kyu.srms.model.Student;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class StudentRepository {

    private static final String SELECT_BASE = """
            SELECT
                s.registration_number,
                s.first_name,
                s.middle_name,
                s.last_name,
                s.gender,
                s.date_of_birth,
                s.email,
                s.phone_number,
                p.name AS programme_name,
                d.name AS department_name,
                s.year_of_study,
                s.admission_date
            FROM students s
            INNER JOIN programmes p
                ON s.programme_id = p.id
            INNER JOIN departments d
                ON p.department_id = d.id
            """;

    public List<Student> findAll() throws SQLException {

        String sql = SELECT_BASE + """
                ORDER BY s.created_at DESC, s.last_name, s.first_name
                """;

        List<Student> students = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                students.add(mapRow(resultSet));
            }
        }

        return students;
    }

    public Student findByRegistrationNumber(String registrationNumber)
            throws SQLException {

        String sql = SELECT_BASE + """
                WHERE s.registration_number = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, registrationNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    public void save(Student student) throws SQLException {

        String sql = """
                INSERT INTO students (
                    registration_number,
                    first_name,
                    middle_name,
                    last_name,
                    gender,
                    date_of_birth,
                    email,
                    phone_number,
                    programme_id,
                    year_of_study,
                    admission_date
                )
                SELECT
                    ?, ?, ?, ?, ?, ?, ?, ?, p.id, ?, ?
                FROM programmes p
                INNER JOIN departments d
                    ON p.department_id = d.id
                WHERE LOWER(TRIM(p.name)) = LOWER(TRIM(?))
                  AND LOWER(TRIM(d.name)) = LOWER(TRIM(?))
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            bindStudent(statement, student);

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new SQLException(
                        "Student was not saved. The selected programme "
                        + "and department could not be matched."
                );
            }
        }
    }

    
    public boolean update(Student student) throws SQLException {

        return update(student.getRegistrationNumber(), student);
    }

    public boolean update(
            String originalRegistrationNumber,
            Student updatedStudent
    ) throws SQLException {

        String sql = """
                UPDATE students s
                SET registration_number = ?,
                    first_name = ?,
                    middle_name = ?,
                    last_name = ?,
                    gender = ?,
                    date_of_birth = ?,
                    email = ?,
                    phone_number = ?,
                    programme_id = p.id,
                    year_of_study = ?,
                    admission_date = ?,
                    updated_at = CURRENT_TIMESTAMP
                FROM programmes p
                INNER JOIN departments d
                    ON p.department_id = d.id
                WHERE s.registration_number = ?
                  AND LOWER(TRIM(p.name)) = LOWER(TRIM(?))
                  AND LOWER(TRIM(d.name)) = LOWER(TRIM(?))
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, updatedStudent.getRegistrationNumber());
            statement.setString(2, updatedStudent.getFirstName());
            statement.setString(3, updatedStudent.getMiddleName());
            statement.setString(4, updatedStudent.getLastName());
            statement.setString(5, updatedStudent.getGender());
            statement.setDate(6, Date.valueOf(updatedStudent.getDateOfBirth()));
            statement.setString(7, updatedStudent.getEmail());
            statement.setString(8, updatedStudent.getPhoneNumber());
            statement.setInt(9, updatedStudent.getYearOfStudy());
            statement.setDate(10, Date.valueOf(updatedStudent.getAdmissionDate()));

            // Identify the existing database record.
            statement.setString(11, originalRegistrationNumber);

            // Resolve the selected programme and department.
            statement.setString(12, updatedStudent.getProgramme());
            statement.setString(13, updatedStudent.getDepartment());

            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(String registrationNumber) throws SQLException {

        String sql = """
                DELETE FROM students
                WHERE registration_number = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, registrationNumber);

            return statement.executeUpdate() == 1;
        }
    }

    public boolean existsByRegistrationNumber(String registrationNumber)
            throws SQLException {

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM students
                    WHERE registration_number = ?
                )
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, registrationNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getBoolean(1);
            }
        }
    }

    private void bindStudent(
            PreparedStatement statement,
            Student student
    ) throws SQLException {

        statement.setString(1, student.getRegistrationNumber());
        statement.setString(2, student.getFirstName());
        statement.setString(3, student.getMiddleName());
        statement.setString(4, student.getLastName());
        statement.setString(5, student.getGender());
        statement.setDate(6, Date.valueOf(student.getDateOfBirth()));
        statement.setString(7, student.getEmail());
        statement.setString(8, student.getPhoneNumber());
        statement.setInt(9, student.getYearOfStudy());
        statement.setDate(10, Date.valueOf(student.getAdmissionDate()));
        statement.setString(11, student.getProgramme());
        statement.setString(12, student.getDepartment());
    }

    private Student mapRow(ResultSet resultSet) throws SQLException {

        return new Student(
                resultSet.getString("registration_number"),
                resultSet.getString("first_name"),
                resultSet.getString("middle_name"),
                resultSet.getString("last_name"),
                resultSet.getString("gender"),
                resultSet.getDate("date_of_birth").toLocalDate(),
                resultSet.getString("email"),
                resultSet.getString("phone_number"),
                resultSet.getString("programme_name"),
                resultSet.getString("department_name"),
                resultSet.getInt("year_of_study"),
                resultSet.getDate("admission_date").toLocalDate()
        );
    }
}
