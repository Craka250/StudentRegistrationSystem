
package ke.ac.kyu.srms.ui;

import ke.ac.kyu.srms.config.ThemeManager;
import ke.ac.kyu.srms.model.Department;
import ke.ac.kyu.srms.model.Programme;
import ke.ac.kyu.srms.model.Student;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final List<Student> students = new ArrayList<>();
    private final List<Department> departments = new ArrayList<>();
    private final List<Programme> programmes = new ArrayList<>();

    private DashboardPanel dashboardPanel;
    private StudentManagementPanel studentManagementPanel;
    private StudentRegistrationPanel registrationPanel;
    private DepartmentManagementPanel departmentManagementPanel;
    private ProgrammeManagementPanel programmeManagementPanel;

    public MainFrame() {
        setTitle("Student Registration and Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));
        setSize(1250, 780);
        setLocationRelativeTo(null);

        contentPanel.setBackground(ThemeManager.BACKGROUND);

        initializeAcademicData();
        initializePanels();

        SidebarPanel sidebarPanel = new SidebarPanel(this);

        setLayout(new BorderLayout());
        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        showPage("dashboard");
    }

    private void initializeAcademicData() {
        Department computing = new Department(
                "Computing and Information Technology",
                "Academic department responsible for computing and information technology programmes."
        );

        Department business = new Department(
                "Business and Management",
                "Academic department responsible for business and management programmes."
        );

        departments.add(computing);
        departments.add(business);

        programmes.add(new Programme(
                "Bachelor of Software Engineering",
                computing.getId(),
                "Undergraduate programme in software engineering."
        ));

        programmes.add(new Programme(
                "Bachelor of Computer Science",
                computing.getId(),
                "Undergraduate programme in computer science."
        ));

        programmes.add(new Programme(
                "Bachelor of Information Technology",
                computing.getId(),
                "Undergraduate programme in information technology."
        ));

        programmes.add(new Programme(
                "Diploma in Information Technology",
                computing.getId(),
                "Diploma programme in information technology."
        ));

        programmes.add(new Programme(
                "Bachelor of Business Information Technology",
                business.getId(),
                "Undergraduate programme combining business and information technology."
        ));
    }

    private void initializePanels() {
        dashboardPanel = new DashboardPanel(this);
        studentManagementPanel = new StudentManagementPanel(this);
        registrationPanel = new StudentRegistrationPanel(this);
        departmentManagementPanel = new DepartmentManagementPanel(this);
        programmeManagementPanel = new ProgrammeManagementPanel(this);

        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(studentManagementPanel, "students");
        contentPanel.add(registrationPanel, "registration");
        contentPanel.add(programmeManagementPanel, "programmes");
        contentPanel.add(departmentManagementPanel, "departments");

        contentPanel.add(
                createPlaceholder("Reports", "Reports and analytics will be implemented here."),
                "reports"
        );

        contentPanel.add(
                createPlaceholder("Settings", "Application settings will be implemented here."),
                "settings"
        );

        refreshAllAcademicViews();
    }

    public void showPage(String page) {
        cardLayout.show(contentPanel, page);
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
        if (student == null) {
            return false;
        }

        String duplicateReason = findDuplicateReason(student, null);

        if (duplicateReason != null) {
            JOptionPane.showMessageDialog(
                    this,
                    duplicateReason,
                    "Duplicate Student Record",
                    JOptionPane.WARNING_MESSAGE
            );
            return false;
        }

        if (getProgrammeByName(student.getProgramme()) == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "The selected programme is no longer available. "
                    + "Refresh the form and select an existing programme.",
                    "Programme Unavailable",
                    JOptionPane.WARNING_MESSAGE
            );
            return false;
        }

        String currentDepartment =
                getDepartmentNameForProgramme(student.getProgramme());

        if (currentDepartment.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "The selected programme is not assigned to a valid department.",
                    "Invalid Academic Relationship",
                    JOptionPane.WARNING_MESSAGE
            );
            return false;
        }

        students.add(student);

        refreshStudentViews();
        showPage("students");

        return true;
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
        if (originalRegistrationNumber == null || updatedStudent == null) {
            return showValidationError("Invalid student information supplied.");
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
                    "The student record could not be found. Refresh the table and try again."
            );
        }

        String duplicateReason = findDuplicateReason(
                updatedStudent,
                originalRegistrationNumber
        );

        if (duplicateReason != null) {
            return showValidationError(duplicateReason);
        }

        if (getProgrammeByName(updatedStudent.getProgramme()) == null) {
            return showValidationError(
                    "The selected programme is no longer available."
            );
        }

        String currentDepartment =
                getDepartmentNameForProgramme(updatedStudent.getProgramme());

        if (currentDepartment.isBlank()) {
            return showValidationError(
                    "The selected programme is not assigned to a valid department."
            );
        }

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
    }

    public boolean addDepartment(String name, String description) {
        if (isDepartmentNameTaken(name, null)) {
            return showValidationError("A department with this name already exists.");
        }

        try {
            departments.add(new Department(name, description));
            refreshAllAcademicViews();
            return true;
        } catch (IllegalArgumentException ex) {
            return showValidationError(ex.getMessage());
        }
    }

    public boolean updateDepartment(
            String departmentId,
            String name,
            String description
    ) {
        Department department = getDepartmentById(departmentId);

        if (department == null) {
            return showValidationError("The selected department could not be found.");
        }

        if (isDepartmentNameTaken(name, departmentId)) {
            return showValidationError("Another department already uses this name.");
        }

        try {
            department.setName(name);
            department.setDescription(description);
            refreshAllAcademicViews();
            return true;
        } catch (IllegalArgumentException ex) {
            return showValidationError(ex.getMessage());
        }
    }

    public boolean deleteDepartment(String departmentId) {
        Department department = getDepartmentById(departmentId);

        if (department == null) {
            return showValidationError("The selected department could not be found.");
        }

        boolean hasProgrammes = programmes.stream()
                .anyMatch(programme ->
                        programme.getDepartmentId().equals(departmentId));

        if (hasProgrammes) {
            return showValidationError(
                    "This department cannot be deleted because it still has programmes. Move or delete its programmes first."
            );
        }

        departments.remove(department);
        refreshAllAcademicViews();
        return true;
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
            return showValidationError("A programme with this name already exists.");
        }

        try {
            programmes.add(new Programme(name, departmentId, description));
            refreshAllAcademicViews();
            return true;
        } catch (IllegalArgumentException ex) {
            return showValidationError(ex.getMessage());
        }
    }

    public boolean updateProgramme(
            String programmeId,
            String name,
            String departmentId,
            String description
    ) {
        Programme programme = getProgrammeById(programmeId);

        if (programme == null) {
            return showValidationError("The selected programme could not be found.");
        }

        if (getDepartmentById(departmentId) == null) {
            return showValidationError(
                    "Select an existing department for this programme."
            );
        }

        if (isProgrammeNameTaken(name, programmeId)) {
            return showValidationError("Another programme already uses this name.");
        }

        boolean hasRegisteredStudents = hasStudentsInProgramme(programme.getName());

        if (hasRegisteredStudents
                && !normalize(programme.getName()).equals(normalize(name))) {
            return showValidationError(
                    "The programme name cannot be changed while students are registered under it. You may still change its department or description."
            );
        }

        try {
            programme.setName(name);
            programme.setDepartmentId(departmentId);
            programme.setDescription(description);

            refreshAllAcademicViews();
            return true;
        } catch (IllegalArgumentException ex) {
            return showValidationError(ex.getMessage());
        }
    }

    public boolean deleteProgramme(String programmeId) {
        Programme programme = getProgrammeById(programmeId);

        if (programme == null) {
            return showValidationError("The selected programme could not be found.");
        }

        if (hasStudentsInProgramme(programme.getName())) {
            return showValidationError(
                    "This programme cannot be deleted because students are registered under it."
            );
        }

        programmes.remove(programme);
        refreshAllAcademicViews();
        return true;
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

    private void refreshDashboard() {
        if (dashboardPanel != null) {
            dashboardPanel.updateStatistics(
                    students,
                    programmes,
                    departments
            );
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
}
