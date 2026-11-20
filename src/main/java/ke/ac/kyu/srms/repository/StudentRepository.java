
package ke.ac.kyu.srms.repository;

import ke.ac.kyu.srms.config.DatabaseConnection;
import ke.ac.kyu.srms.model.Student;
import ke.ac.kyu.srms.model.StudentPhoto;

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

    private static final String INSERT_PHOTO_SQL = """
            INSERT INTO student_photos (
                registration_number,
                file_path,
                original_filename,
                content_type,
                file_size
            )
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String UPSERT_PHOTO_SQL = """
            INSERT INTO student_photos (
                registration_number,
                file_path,
                original_filename,
                content_type,
                file_size
            )
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (registration_number)
            DO UPDATE SET
                file_path = EXCLUDED.file_path,
                original_filename = EXCLUDED.original_filename,
                content_type = EXCLUDED.content_type,
                file_size = EXCLUDED.file_size,
                uploaded_at = CURRENT_TIMESTAMP
            """;

    public List<Student> findAll() throws SQLException {
        String sql = SELECT_BASE + """
                ORDER BY s.created_at DESC, s.last_name, s.first_name
                """;

        List<Student> students = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
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
                PreparedStatement statement =
                        connection.prepareStatement(sql)
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

    /**
     * Saves a student without changing photograph information.
     */
    public void save(Student student) throws SQLException {
        saveWithPhoto(student, null);
    }

    /**
     * Saves the student and optional photograph metadata
     * in one database transaction.
     */
    public void saveWithPhoto(
            Student student,
            StudentPhoto photo
    ) throws SQLException {

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                saveStudent(connection, student);

                // No photograph row is created when no photo was selected.
                if (photo != null) {
                    insertPhoto(connection, student.getRegistrationNumber(), photo);
                }

                connection.commit();

            } catch (SQLException | RuntimeException ex) {
                rollback(connection);
                throw ex;
            }
        }
    }

    public boolean update(Student student) throws SQLException {
        return update(
                student.getRegistrationNumber(),
                student
        );
    }

    public boolean update(
            String originalRegistrationNumber,
            Student updatedStudent
    ) throws SQLException {

        PhotoUpdateResult result = updateWithPhoto(
                originalRegistrationNumber,
                updatedStudent,
                null,
                false
        );

        return result.isUpdated();
    }

    /**
     * Updates a student and optionally replaces or removes
     * the student's photograph in the same database transaction.
     *
     * The returned old photo path must only be deleted from disk
     * after this method has successfully committed.
     */
    public PhotoUpdateResult updateWithPhoto(
            String originalRegistrationNumber,
            Student updatedStudent,
            StudentPhoto newPhoto,
            boolean removeExistingPhoto
    ) throws SQLException {

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                String oldPhotoPath = findPhotoPath(
                        connection,
                        originalRegistrationNumber
                );

                boolean updated = updateStudent(
                        connection,
                        originalRegistrationNumber,
                        updatedStudent
                );

                if (!updated) {
                    connection.rollback();
                    return new PhotoUpdateResult(false, null);
                }

                if (newPhoto != null) {
                    // Replace existing metadata with the new photograph.
                    upsertPhoto(
                            connection,
                            updatedStudent.getRegistrationNumber(),
                            newPhoto
                    );

                } else if (removeExistingPhoto) {
                    // Remove metadata only when removal was explicitly requested.
                    deletePhotoMetadata(
                            connection,
                            updatedStudent.getRegistrationNumber()
                    );
                }

                connection.commit();

                String pathToDelete =
                        (newPhoto != null || removeExistingPhoto)
                                ? oldPhotoPath
                                : null;

                return new PhotoUpdateResult(true, pathToDelete);

            } catch (SQLException | RuntimeException ex) {
                rollback(connection);
                throw ex;
            }
        }
    }

    public boolean delete(String registrationNumber) throws SQLException {
        return deleteWithPhoto(registrationNumber).isDeleted();
    }

    /**
     * Deletes the student and cascaded photograph metadata
     * in one transaction.
     *
     * The returned photograph path can be deleted from disk
     * after the transaction commits.
     */
    public PhotoDeleteResult deleteWithPhoto(
            String registrationNumber
    ) throws SQLException {

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                String oldPhotoPath = findPhotoPath(
                        connection,
                        registrationNumber
                );

                String sql = """
                        DELETE FROM students
                        WHERE registration_number = ?
                        """;

                int affectedRows;

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    statement.setString(1, registrationNumber);
                    affectedRows = statement.executeUpdate();
                }

                if (affectedRows != 1) {
                    connection.rollback();
                    return new PhotoDeleteResult(false, null);
                }

                // The student_photos row is removed by ON DELETE CASCADE.
                connection.commit();

                return new PhotoDeleteResult(true, oldPhotoPath);

            } catch (SQLException | RuntimeException ex) {
                rollback(connection);
                throw ex;
            }
        }
    }

    public String findPhotoPathByRegistrationNumber(
            String registrationNumber
    ) throws SQLException {

        try (Connection connection = DatabaseConnection.getConnection()) {
            return findPhotoPath(connection, registrationNumber);
        }
    }

    public boolean existsByRegistrationNumber(
            String registrationNumber
    ) throws SQLException {

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM students
                    WHERE registration_number = ?
                )
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, registrationNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getBoolean(1);
            }
        }
    }

    private void saveStudent(
            Connection connection,
            Student student
    ) throws SQLException {

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

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

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

    private boolean updateStudent(
            Connection connection,
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

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, updatedStudent.getRegistrationNumber());
            statement.setString(2, updatedStudent.getFirstName());
            statement.setString(3, updatedStudent.getMiddleName());
            statement.setString(4, updatedStudent.getLastName());
            statement.setString(5, updatedStudent.getGender());
            statement.setDate(
                    6,
                    Date.valueOf(updatedStudent.getDateOfBirth())
            );
            statement.setString(7, updatedStudent.getEmail());
            statement.setString(8, updatedStudent.getPhoneNumber());
            statement.setInt(9, updatedStudent.getYearOfStudy());
            statement.setDate(
                    10,
                    Date.valueOf(updatedStudent.getAdmissionDate())
            );
            statement.setString(11, originalRegistrationNumber);
            statement.setString(12, updatedStudent.getProgramme());
            statement.setString(13, updatedStudent.getDepartment());

            return statement.executeUpdate() == 1;
        }
    }

    private void insertPhoto(
            Connection connection,
            String registrationNumber,
            StudentPhoto photo
    ) throws SQLException {

        try (PreparedStatement statement =
                     connection.prepareStatement(INSERT_PHOTO_SQL)) {

            bindPhoto(statement, registrationNumber, photo);
            statement.executeUpdate();
        }
    }

    private void upsertPhoto(
            Connection connection,
            String registrationNumber,
            StudentPhoto photo
    ) throws SQLException {

        try (PreparedStatement statement =
                     connection.prepareStatement(UPSERT_PHOTO_SQL)) {

            bindPhoto(statement, registrationNumber, photo);
            statement.executeUpdate();
        }
    }

    private void bindPhoto(
            PreparedStatement statement,
            String registrationNumber,
            StudentPhoto photo
    ) throws SQLException {

        statement.setString(1, registrationNumber);
        statement.setString(2, photo.getFilePath().toString());
        statement.setString(3, photo.getOriginalFilename());
        statement.setString(4, photo.getContentType());
        statement.setLong(5, photo.getFileSize());
    }

    private String findPhotoPath(
            Connection connection,
            String registrationNumber
    ) throws SQLException {

        String sql = """
                SELECT file_path
                FROM student_photos
                WHERE registration_number = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, registrationNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("file_path");
                }
            }
        }

        return null;
    }

    private void deletePhotoMetadata(
            Connection connection,
            String registrationNumber
    ) throws SQLException {

        String sql = """
                DELETE FROM student_photos
                WHERE registration_number = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, registrationNumber);
            statement.executeUpdate();
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

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Preserve the original database exception.
        }
    }

    public static class PhotoUpdateResult {

        private final boolean updated;
        private final String oldPhotoPath;

        public PhotoUpdateResult(
                boolean updated,
                String oldPhotoPath
        ) {
            this.updated = updated;
            this.oldPhotoPath = oldPhotoPath;
        }

        public boolean isUpdated() {
            return updated;
        }

        public String getOldPhotoPath() {
            return oldPhotoPath;
        }
    }

    public static class PhotoDeleteResult {

        private final boolean deleted;
        private final String oldPhotoPath;

        public PhotoDeleteResult(
                boolean deleted,
                String oldPhotoPath
        ) {
            this.deleted = deleted;
            this.oldPhotoPath = oldPhotoPath;
        }

        public boolean isDeleted() {
            return deleted;
        }

        public String getOldPhotoPath() {
            return oldPhotoPath;
        }
    }
}
