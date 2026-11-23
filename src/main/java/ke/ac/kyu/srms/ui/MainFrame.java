
package ke.ac.kyu.srms.ui;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ke.ac.kyu.srms.repository.DepartmentRepository;
import ke.ac.kyu.srms.repository.ProgrammeRepository;
import ke.ac.kyu.srms.repository.StudentRepository;

import java.sql.SQLException;

import ke.ac.kyu.srms.config.ThemeManager;
import ke.ac.kyu.srms.model.Department;
import ke.ac.kyu.srms.model.Programme;
import ke.ac.kyu.srms.model.Student;

import ke.ac.kyu.srms.model.Admin;
import ke.ac.kyu.srms.model.AuditLog;
import ke.ac.kyu.srms.repository.AuditLogRepository;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import ke.ac.kyu.srms.model.StudentPhoto;
import ke.ac.kyu.srms.storage.StudentPhotoStorage;

import java.io.File;
import java.io.IOException;

public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final List<Student> students = new ArrayList<>();
    private final List<Department> departments = new ArrayList<>();
    private final List<Programme> programmes = new ArrayList<>();

    private final StudentRepository studentRepository =
            new StudentRepository();

    private final StudentPhotoStorage studentPhotoStorage =
        new StudentPhotoStorage();

    private final DepartmentRepository departmentRepository =
            new DepartmentRepository();

    private final ProgrammeRepository programmeRepository =
            new ProgrammeRepository();
    
    private final Admin currentAdmin;
    private final AuditLogRepository auditLogRepository =
            new AuditLogRepository();

    private DashboardPanel dashboardPanel;
    private StudentManagementPanel studentManagementPanel;
    private StudentRegistrationPanel registrationPanel;
    private DepartmentManagementPanel departmentManagementPanel;
    private ProgrammeManagementPanel programmeManagementPanel;
    private ReportsPanel reportsPanel;
    private SidebarPanel sidebarPanel;

    public MainFrame() {
        this(null);
    }

    public MainFrame(Admin admin) {

        this.currentAdmin = admin;
        setTitle("Student Registration and Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));
        setSize(1250, 780);
        setLocationRelativeTo(null);

        contentPanel.setBackground(ThemeManager.BACKGROUND);

        // Load permanent records before creating the UI panels.
        try {
            reloadDataFromDatabase();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "The application could not connect to its database.\n\n"
                    + "Please ensure PostgreSQL is running and the database "
                    + "configuration is correct.\n\n"
                    + "Technical details: " + ex.getMessage(),
                    "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );

            dispose();

            throw new IllegalStateException(
                    "Unable to initialize SRMS because the database is unavailable.",
                    ex
            );
        }

        initializePanels();
        sidebarPanel = new SidebarPanel(this);

        setLayout(new BorderLayout());
        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        showPage("dashboard");
    }

    private void reloadDataFromDatabase() throws SQLException {

        List<Department> loadedDepartments =
                departmentRepository.findAll();

        List<Programme> loadedProgrammes =
                programmeRepository.findAll();

        List<Student> loadedStudents =
                studentRepository.findAll();

        // Replace cached data only after all database reads succeed.
        departments.clear();
        departments.addAll(loadedDepartments);

        programmes.clear();
        programmes.addAll(loadedProgrammes);

        students.clear();
        students.addAll(loadedStudents);
    }

    private void initializePanels() {
        dashboardPanel = new DashboardPanel(this);
        studentManagementPanel = new StudentManagementPanel(this);
        registrationPanel = new StudentRegistrationPanel(this);
        departmentManagementPanel = new DepartmentManagementPanel(this);
        programmeManagementPanel = new ProgrammeManagementPanel(this);
        reportsPanel = new ReportsPanel(this);

        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(studentManagementPanel, "students");
        contentPanel.add(registrationPanel, "registration");
        contentPanel.add(programmeManagementPanel, "programmes");
        contentPanel.add(departmentManagementPanel, "departments");

        /*
        contentPanel.add(
                createPlaceholder("Reports", "Reports and analytics will be implemented here."),
                "reports"
        ); */

        contentPanel.add(reportsPanel, "reports");

        contentPanel.add(
                createPlaceholder("Settings", "Application settings will be implemented here."),
                "settings"
        );

        refreshAllAcademicViews();
    }

    public void showPage(String page) {
        cardLayout.show(contentPanel, page);

        if (sidebarPanel != null) {
            sidebarPanel.setActivePage(page);
        }
    }

    /**
    * Opens the student registration form in edit mode
    * and loads the selected student's existing details.
    */
   public void openStudentForEditing(Student student) {
       if (student == null) {
           JOptionPane.showMessageDialog(
                   this,
                   "Please select a valid student record.",
                   "Student Not Found",
                   JOptionPane.WARNING_MESSAGE
           );
           return;
       }

       registrationPanel.loadStudentForEditing(student);
       showPage("registration");
   }

    public List<Student> getStudents() {
        return Collections.unmodifiableList(students);
    }

    public List<Department> getDepartments() {
        return Collections.unmodifiableList(departments);
    }

    public List<Programme> getProgrammes() {
        return Collections.unmodifiableList(programmes);
    }

    public Department getDepartmentById(String id) {
        if (id == null) {
            return null;
        }

        return departments.stream()
                .filter(department -> department.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Programme getProgrammeById(String id) {
        if (id == null) {
            return null;
        }

        return programmes.stream()
                .filter(programme -> programme.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Programme getProgrammeByName(String name) {
        if (name == null) {
            return null;
        }

        return programmes.stream()
                .filter(programme -> normalize(programme.getName())
                        .equals(normalize(name)))
                .findFirst()
                .orElse(null);
    }

    public Department getDepartmentForProgramme(String programmeName) {
        Programme programme = getProgrammeByName(programmeName);

        if (programme == null) {
            return null;
        }

        return getDepartmentById(programme.getDepartmentId());
    }

    public String getDepartmentNameForProgramme(String programmeName) {
        Department department = getDepartmentForProgramme(programmeName);
        return department == null ? "" : department.getName();
    }

    public List<String> getProgrammeNames() {
        return programmes.stream()
                .map(Programme::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }


    public boolean addStudent(Student student) {
        return addStudent(student, null);
    }

    /**
     * Registers a student and stores an optional photograph.
     * The photograph is copied only after confirmation by the user.
     */
    public boolean addStudent(Student student, File photoFile) {

        if (student == null) {
            return false;
        }

        String duplicateReason = findDuplicateReason(student, null);

        if (duplicateReason != null) {
            return showValidationError(duplicateReason);
        }

        Programme selectedProgramme =
                getProgrammeByName(student.getProgramme());

        if (selectedProgramme == null) {
            return showValidationError(
                    "The selected programme is no longer available. "
                    + "Refresh the form and select an existing programme."
            );
        }

        String currentDepartment =
                getDepartmentNameForProgramme(student.getProgramme());

        if (currentDepartment.isBlank()
                || !normalize(currentDepartment).equals(
                        normalize(student.getDepartment()))) {

            return showValidationError(
                    "The selected programme and department do not match. "
                    + "Please refresh the form and try again."
            );
        }

        StudentPhoto storedPhoto = null;

        try {

            // Store a photograph only when the user selected one.
            if (photoFile != null) {
                storedPhoto = studentPhotoStorage.store(photoFile);
            }

            // Student and photo metadata are saved in one transaction.
            studentRepository.saveWithPhoto(student, storedPhoto);

            // Record the successful registration.
            logActivity("CREATE", "STUDENT", null);

            students.add(student);

            refreshStudentViews();
            showPage("students");

            return true;

        } catch (IOException ex) {

            if (storedPhoto != null) {
                deleteStoredPhotoSafely(
                        storedPhoto.getFilePath().toString()
                );
            }

            return showPhotoStorageError(ex);

        } catch (SQLException ex) {

            if (storedPhoto != null) {
                deleteStoredPhotoSafely(
                        storedPhoto.getFilePath().toString()
                );
            }

            return showDatabaseError(ex);
        }
    }

    private String findDuplicateReason(
            Student candidate,
            String excludedRegistrationNumber
    ) {
        for (Student existing : students) {

            // Ignore the original record when editing that same student.
            if (excludedRegistrationNumber != null
                    && normalize(existing.getRegistrationNumber())
                    .equals(normalize(excludedRegistrationNumber))) {
                continue;
            }

            if (normalize(existing.getRegistrationNumber())
                    .equals(normalize(candidate.getRegistrationNumber()))) {

                return "A student with this registration number already exists.";
            }

            if (normalize(existing.getEmail())
                    .equals(normalize(candidate.getEmail()))) {

                return "A student with this email address already exists.";
            }

            if (normalizePhoneNumber(existing.getPhoneNumber())
                    .equals(normalizePhoneNumber(candidate.getPhoneNumber()))) {

                return "A student with this phone number already exists.";
            }

            String existingName = normalize(existing.getFullName());
            String candidateName = normalize(candidate.getFullName());

            if (existingName.equals(candidateName)
                    && existing.getDateOfBirth() != null
                    && existing.getDateOfBirth().equals(candidate.getDateOfBirth())) {

                return "A student with the same full name and date of birth already exists.";
            }
        }

        return null;
    }

    public boolean updateStudent(
            String originalRegistrationNumber,
            Student updatedStudent
    ) {
        return updateStudent(
                originalRegistrationNumber,
                updatedStudent,
                null,
                false
        );
    }

    /**
     * Updates a student while preserving, replacing or removing
     * the existing photograph as requested.
     */
    public boolean updateStudent(
            String originalRegistrationNumber,
            Student updatedStudent,
            File photoFile,
            boolean removeExistingPhoto
    ) {

        if (originalRegistrationNumber == null || updatedStudent == null) {
            return showValidationError(
                    "Invalid student information supplied."
            );
        }

        int studentIndex = -1;

        for (int i = 0; i < students.size(); i++) {

            if (normalize(students.get(i).getRegistrationNumber())
                    .equals(normalize(originalRegistrationNumber))) {

                studentIndex = i;
                break;
            }
        }

        if (studentIndex == -1) {
            return showValidationError(
                    "The student record could not be found. "
                    + "Refresh the table and try again."
            );
        }

        String duplicateReason = findDuplicateReason(
                updatedStudent,
                originalRegistrationNumber
        );

        if (duplicateReason != null) {
            return showValidationError(duplicateReason);
        }

        Programme selectedProgramme =
                getProgrammeByName(updatedStudent.getProgramme());

        if (selectedProgramme == null) {
            return showValidationError(
                    "The selected programme is no longer available."
            );
        }

        String currentDepartment =
                getDepartmentNameForProgramme(updatedStudent.getProgramme());

        if (currentDepartment.isBlank()
                || !normalize(currentDepartment).equals(
                        normalize(updatedStudent.getDepartment()))) {

            return showValidationError(
                    "The selected programme and department do not match."
            );
        }

        StudentPhoto storedPhoto = null;

        try {

            // Copy the replacement photograph only when one was selected.
            if (photoFile != null) {
                storedPhoto = studentPhotoStorage.store(photoFile);
            }

            // Update the student and photo metadata transactionally.
            StudentRepository.PhotoUpdateResult result =
                    studentRepository.updateWithPhoto(
                            originalRegistrationNumber,
                            updatedStudent,
                            storedPhoto,
                            removeExistingPhoto
                    );

            if (!result.isUpdated()) {

                if (storedPhoto != null) {
                    deleteStoredPhotoSafely(
                            storedPhoto.getFilePath().toString()
                    );
                }

                return showValidationError(
                        "The student record could not be updated. "
                        + "The original record may no longer exist, "
                        + "or the selected academic details are invalid."
                );
            }

            // The database has committed. It is now safe to delete
            // the previous photograph if it was replaced or removed.
            if (result.getOldPhotoPath() != null) {
                deleteStoredPhotoSafely(result.getOldPhotoPath());
            }

            // Record the successful update without storing student details.
            logActivity("UPDATE", "STUDENT", null);

            students.set(studentIndex, updatedStudent);

            refreshStudentViews();
            showPage("students");

            JOptionPane.showMessageDialog(
                    this,
                    "The student record has been updated successfully.",
                    "Update Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return true;

        } catch (IOException ex) {

            if (storedPhoto != null) {
                deleteStoredPhotoSafely(
                        storedPhoto.getFilePath().toString()
                );
            }

            return showPhotoStorageError(ex);

        } catch (SQLException ex) {

            if (storedPhoto != null) {
                deleteStoredPhotoSafely(
                        storedPhoto.getFilePath().toString()
                );
            }

            return showDatabaseError(ex);
        }
    }

    public boolean deleteStudent(String registrationNumber) {

        if (registrationNumber == null || registrationNumber.isBlank()) {
            return false;
        }

        Student studentToDelete = students.stream()
                .filter(student ->
                        normalize(student.getRegistrationNumber())
                        .equals(normalize(registrationNumber)))
                .findFirst()
                .orElse(null);

        if (studentToDelete == null) {
            return showValidationError(
                    "The selected student record could not be found."
            );
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this student?\n\n"
                + "Name: " + studentToDelete.getFullName() + "\n"
                + "Registration Number: "
                + studentToDelete.getRegistrationNumber()
                + "\n\nThis action cannot be undone.",
                "Confirm Student Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return false;
        }

        try {

            StudentRepository.PhotoDeleteResult result =
                    studentRepository.deleteWithPhoto(
                            studentToDelete.getRegistrationNumber()
                    );

            if (!result.isDeleted()) {
                return showValidationError(
                        "The student record could not be deleted. "
                        + "It may already have been removed."
                );
            }

            // Delete the physical photograph after the database commits.
            if (result.getOldPhotoPath() != null) {
                deleteStoredPhotoSafely(result.getOldPhotoPath());
            }

            // Record the successful deletion.
            logActivity("DELETE", "STUDENT", null);

            students.remove(studentToDelete);

            refreshStudentViews();
            showPage("students");

            JOptionPane.showMessageDialog(
                    this,
                    "The student record has been deleted successfully.",
                    "Deletion Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return true;

        } catch (SQLException ex) {
            return showDatabaseError(ex);
        }
    }

    public boolean addDepartment(String name, String description) {

        if (isDepartmentNameTaken(name, null)) {
            return showValidationError(
                    "A department with this name already exists."
            );
        }

        try {

            Department department = new Department(name, description);

            departmentRepository.save(department);

            logActivity("CREATE", "DEPARTMENT", department.getId());

            refreshFromDatabase();

            return true;

        } catch (IllegalArgumentException ex) {

            return showValidationError(ex.getMessage());

        } catch (SQLException ex) {

            return showDatabaseError(ex);
        }
    }

    public boolean updateDepartment(
            String departmentId,
            String name,
            String description
    ) {

        Department existingDepartment = getDepartmentById(departmentId);

        if (existingDepartment == null) {
            return showValidationError(
                    "The selected department could not be found."
            );
        }

        if (isDepartmentNameTaken(name, departmentId)) {
            return showValidationError(
                    "Another department already uses this name."
            );
        }

        try {

            Department updatedDepartment = new Department(
                    departmentId,
                    name,
                    description
            );

            boolean updated = departmentRepository.update(updatedDepartment);

            if (!updated) {
                return showValidationError(
                        "The department could not be updated."
                );
            }

            logActivity("UPDATE", "DEPARTMENT", departmentId);

            refreshFromDatabase();

            return true;

        } catch (IllegalArgumentException ex) {

            return showValidationError(ex.getMessage());

        } catch (SQLException ex) {

            return showDatabaseError(ex);
        }
    }

    public boolean deleteDepartment(String departmentId) {

        Department department = getDepartmentById(departmentId);

        if (department == null) {
            return showValidationError(
                    "The selected department could not be found."
            );
        }

        boolean hasProgrammes = programmes.stream()
                .anyMatch(programme ->
                        programme.getDepartmentId().equals(departmentId));

        if (hasProgrammes) {
            return showValidationError(
                    "This department cannot be deleted because it still "
                    + "has programmes. Move or delete its programmes first."
            );
        }

        try {

            boolean deleted = departmentRepository.delete(departmentId);

            if (!deleted) {
                return showValidationError(
                        "The department could not be deleted."
                );
            }

            logActivity("DELETE", "DEPARTMENT", departmentId);

            refreshFromDatabase();

            return true;

        } catch (SQLException ex) {

            return showDatabaseError(ex);
        }
    }

    public boolean addProgramme(
            String name,
            String departmentId,
            String description
    ) {

        if (getDepartmentById(departmentId) == null) {
            return showValidationError(
                    "Select an existing department for this programme."
            );
        }

        if (isProgrammeNameTaken(name, null)) {
            return showValidationError(
                    "A programme with this name already exists."
            );
        }

        try {

            Programme programme = new Programme(
                    name,
                    departmentId,
                    description
            );

            programmeRepository.save(programme);

            logActivity("CREATE", "PROGRAMME", programme.getId());

            refreshFromDatabase();

            return true;

        } catch (IllegalArgumentException ex) {

            return showValidationError(ex.getMessage());

        } catch (SQLException ex) {

            return showDatabaseError(ex);
        }
    }

    public boolean updateProgramme(
            String programmeId,
            String name,
            String departmentId,
            String description
    ) {

        Programme existingProgramme = getProgrammeById(programmeId);

        if (existingProgramme == null) {
            return showValidationError(
                    "The selected programme could not be found."
            );
        }

        if (getDepartmentById(departmentId) == null) {
            return showValidationError(
                    "Select an existing department for this programme."
            );
        }

        if (isProgrammeNameTaken(name, programmeId)) {
            return showValidationError(
                    "Another programme already uses this name."
            );
        }

        boolean hasRegisteredStudents =
                hasStudentsInProgramme(existingProgramme.getName());

        if (hasRegisteredStudents
                && !normalize(existingProgramme.getName()).equals(
                        normalize(name))) {

            return showValidationError(
                    "The programme name cannot be changed while students "
                    + "are registered under it. You may still change its "
                    + "department or description."
            );
        }

        try {

            Programme updatedProgramme = new Programme(
                    programmeId,
                    name,
                    departmentId,
                    description
            );

            boolean updated = programmeRepository.update(updatedProgramme);

            if (!updated) {
                return showValidationError(
                        "The programme could not be updated."
                );
            }

            logActivity("UPDATE", "PROGRAMME", programmeId);

            // Reloading also refreshes student department names if
            // the programme has been moved to another department.
            refreshFromDatabase();

            return true;

        } catch (IllegalArgumentException ex) {

            return showValidationError(ex.getMessage());

        } catch (SQLException ex) {

            return showDatabaseError(ex);
        }
    }

    public boolean deleteProgramme(String programmeId) {

        Programme programme = getProgrammeById(programmeId);

        if (programme == null) {
            return showValidationError(
                    "The selected programme could not be found."
            );
        }

        if (hasStudentsInProgramme(programme.getName())) {
            return showValidationError(
                    "This programme cannot be deleted because students "
                    + "are registered under it."
            );
        }

        try {

            boolean deleted = programmeRepository.delete(programmeId);

            if (!deleted) {
                return showValidationError(
                        "The programme could not be deleted."
                );
            }

            logActivity("DELETE", "PROGRAMME", programmeId);

            refreshFromDatabase();

            return true;

        } catch (SQLException ex) {

            return showDatabaseError(ex);
        }
    }

    public String getStudentPhotoPath(String registrationNumber) {
        try {
            return studentRepository.findPhotoPathByRegistrationNumber(
                    registrationNumber
            );

        } catch (SQLException ex) {
            showDatabaseError(ex);
            return null;
        }
    }

    private boolean hasStudentsInProgramme(String programmeName) {
        return students.stream().anyMatch(student ->
                normalize(student.getProgramme()).equals(normalize(programmeName)));
    }

    private boolean isDepartmentNameTaken(String name, String excludedId) {
        if (name == null || name.isBlank()) {
            return true;
        }

        return departments.stream().anyMatch(department ->
                !department.getId().equals(excludedId)
                && normalize(department.getName()).equals(normalize(name)));
    }

    private boolean isProgrammeNameTaken(String name, String excludedId) {
        if (name == null || name.isBlank()) {
            return true;
        }

        return programmes.stream().anyMatch(programme ->
                !programme.getId().equals(excludedId)
                && normalize(programme.getName()).equals(normalize(name)));
    }

    private boolean showValidationError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Academic Data Validation",
                JOptionPane.WARNING_MESSAGE
        );
        return false;
    }

    /**
     * Safely deletes a photograph from the application's
     * managed storage directory.
     *
     * Database changes are already committed before this method
     * is called.
     */
    private void deleteStoredPhotoSafely(String storedPath) {

        try {
            studentPhotoStorage.deleteStoredPhoto(storedPath);

        } catch (IOException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "The database operation succeeded, but the old "
                    + "photograph could not be deleted automatically.\n\n"
                    + "Details: " + ex.getMessage(),
                    "Photograph Cleanup Warning",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    /**
     * Displays photograph storage errors.
     */
    private boolean showPhotoStorageError(IOException ex) {

        JOptionPane.showMessageDialog(
                this,
                "The photograph could not be stored.\n\n"
                + "Details: " + ex.getMessage(),
                "Photograph Storage Error",
                JOptionPane.ERROR_MESSAGE
        );

        return false;
    }

    private boolean showDatabaseError(SQLException ex) {

        String message;

        if ("23505".equals(ex.getSQLState())) {

            message = "This operation could not be completed because "
                    + "a record with the same unique information already exists.\n\n"
                    + "Please check the registration number, email address, "
                    + "phone number, department name or programme name.";

        } else if ("23503".equals(ex.getSQLState())) {

            message = "This operation could not be completed because "
                    + "the selected record is still being referenced by "
                    + "another database record.";

        } else {

            message = "A database operation failed.\n\n"
                    + "Details: " + ex.getMessage();
        }

        JOptionPane.showMessageDialog(
                this,
                message,
                "Database Operation Failed",
                JOptionPane.ERROR_MESSAGE
        );

        return false;
    }

    private void refreshFromDatabase() throws SQLException {

        reloadDataFromDatabase();

        refreshAllAcademicViews();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    private String normalizePhoneNumber(String phone) {
        if (phone == null) {
            return "";
        }

        String digits = phone.replaceAll("\\D", "");

        if (digits.startsWith("254")) {
            return digits.substring(3);
        }

        if (digits.startsWith("0")) {
            return digits.substring(1);
        }

        return digits;
    }

    private void refreshStudentViews() {

        if (studentManagementPanel != null) {
            studentManagementPanel.refreshStudents(
                    students,
                    student -> getDepartmentNameForProgramme(
                            student.getProgramme()
                    )
            );
        }

        // Refresh programme student counts.
        if (programmeManagementPanel != null) {
            programmeManagementPanel.refreshData();
        }

        refreshDashboard();
    }

    private void refreshAllAcademicViews() {

        if (departmentManagementPanel != null) {
            departmentManagementPanel.refreshData();
        }

        if (programmeManagementPanel != null) {
            programmeManagementPanel.refreshData();
        }

        if (registrationPanel != null) {
            registrationPanel.refreshAcademicOptions();
        }

        if (studentManagementPanel != null) {
            studentManagementPanel.refreshStudents(
                    students,
                    student -> getDepartmentNameForProgramme(
                            student.getProgramme()
                    )
            );
        }

        refreshDashboard();
    }
    
    /**
    * Records an administrative action without interrupting the
    * main database operation if audit logging fails.
    */
   private void logActivity(
           String action,
           String entityType,
           String entityId
   ) {
       try {
           auditLogRepository.logActivity(
                   currentAdmin,
                   action,
                   entityType,
                   entityId,
                   null
           );

       } catch (SQLException ex) {
           System.err.println(
                   "Audit logging failed: " + ex.getMessage()
           );
       }
   }

   /**
    * Returns the most recent audit records for the dashboard
    * and notifications window.
    */
   public List<AuditLog> getRecentActivities(int limit) {

       try {
           return auditLogRepository.findRecent(limit);

       } catch (SQLException ex) {

           System.err.println(
                   "Could not load recent activities: "
                   + ex.getMessage()
           );

           return Collections.emptyList();
       }
   }

   /**
    * Opens the existing student management page and applies
    * the dashboard search through its existing search field.
    */
   public void searchStudents(String query) {

       if (studentManagementPanel == null) {
           return;
       }

       studentManagementPanel.searchFromDashboard(query);
       showPage("students");
   }

    private void refreshDashboard() {

        if (dashboardPanel != null) {

            dashboardPanel.updateStatistics(
                    students,
                    programmes,
                    departments
            );

            // Refresh the recent activity feed from PostgreSQL.
            dashboardPanel.updateRecentActivities(
                    getRecentActivities(8)
            );
        }

        // Keep reports and analytics synchronized with database records.
        if (reportsPanel != null) {
            reportsPanel.refreshData();
        }
    }

    private JPanel createPlaceholder(String title, String description) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeManager.BACKGROUND);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel(title);
        heading.setFont(ThemeManager.FONT_HEADING);
        heading.setForeground(ThemeManager.TEXT_PRIMARY);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel message = new JLabel(description);
        message.setFont(ThemeManager.FONT_REGULAR);
        message.setForeground(ThemeManager.TEXT_SECONDARY);
        message.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(heading);
        content.add(Box.createVerticalStrut(10));
        content.add(message);

        panel.add(content);
        return panel;
    }

    /**
     * Safely logs out the currently authenticated administrator.
     *
     * Displays a confirmation dialog before closing the dashboard
     * and returning to the administrator login screen.
     */
    public void logout() {

        Object[] options = {
            "Log Out",
            "Stay Logged In"
        };

        int choice = JOptionPane.showOptionDialog(
                this,
                "Are you sure you want to log out?\n\n"
                + "You will need to sign in again to access the system.",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]
        );

        // Cancel or stay logged in.
        if (choice != 0) {
            return;
        }

        // Close the current dashboard.
        dispose();

        // Return to the administrator login screen.
        SwingUtilities.invokeLater(() -> {

            LoginFrame loginFrame = new LoginFrame(admin -> {

                MainFrame newMainFrame = new MainFrame(admin);

                newMainFrame.setVisible(true);

            });

            loginFrame.setVisible(true);
        });
    }
}
