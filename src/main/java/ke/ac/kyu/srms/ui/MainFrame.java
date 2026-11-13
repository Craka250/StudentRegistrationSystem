
package ke.ac.kyu.srms.ui;

import ke.ac.kyu.srms.config.ThemeManager;
import ke.ac.kyu.srms.model.Student;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainFrame extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel contentPanel;

    // Central source of truth for all registered students.
    private final List<Student> students = new ArrayList<>();

    private DashboardPanel dashboardPanel;
    private StudentManagementPanel studentManagementPanel;

    public MainFrame() {

        setTitle("SRMS | Student Registration and Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));
        setSize(1250, 780);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(ThemeManager.BACKGROUND);

        initializePanels();

        SidebarPanel sidebar = new SidebarPanel(this);

        setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        showPage("dashboard");
    }

    private void initializePanels() {

        // Create and retain the dashboard reference.
        dashboardPanel = new DashboardPanel(this);

        contentPanel.add(
                dashboardPanel,
                "dashboard"
        );

        // Create and retain the student management reference.
        studentManagementPanel = new StudentManagementPanel(this);

        contentPanel.add(
                studentManagementPanel,
                "students"
        );

        contentPanel.add(
                new StudentRegistrationPanel(this),
                "registration"
        );

        contentPanel.add(
                createPlaceholder(
                        "Reports and Analytics",
                        "View student statistics and academic reports."
                ),
                "reports"
        );

        contentPanel.add(
                createPlaceholder(
                        "System Settings",
                        "Configure application preferences."
                ),
                "settings"
        );

        // Initialize dashboard statistics from the central list.
        dashboardPanel.updateStatistics(students);
    }

    /**
     * Receives a student from the review dialog.
     *
     * @param student student record to validate and add
     * @return true if accepted; false if invalid or a duplicate
     */
    public boolean addStudent(Student student) {

        if (student == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "No student information was received.",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        // Check for duplicates before changing any data.
        String duplicateReason = findDuplicateReason(student);

        if (duplicateReason != null) {

            JOptionPane.showMessageDialog(
                    this,
                    "This student cannot be registered because a possible "
                    + "duplicate record was found.\n\n"
                    + duplicateReason
                    + "\n\nPlease return to the registration form and "
                    + "verify the student's details.",
                    "Duplicate Student Registration",
                    JOptionPane.WARNING_MESSAGE
            );

            return false;
        }

        // Add the student to the central list only after validation.
        students.add(student);

        // Synchronize the Students management table.
        studentManagementPanel.addStudent(student);

        // Refresh all dashboard statistics.
        dashboardPanel.updateStatistics(students);

        // Display the Students page after successful registration.
        showPage("students");

        return true;
    }

    /**
     * Checks whether a student already exists.
     * The first matching condition determines the warning message.
     */
    private String findDuplicateReason(Student candidate) {

        String registrationNumber = normalizeText(
                candidate.getRegistrationNumber()
        );

        String email = normalizeText(candidate.getEmail());

        String phone = normalizePhoneNumber(
                candidate.getPhoneNumber()
        );

        String fullName = normalizeText(candidate.getFullName());

        for (Student existing : students) {

            // Registration number check.
            if (!registrationNumber.isEmpty()
                    && registrationNumber.equals(
                            normalizeText(existing.getRegistrationNumber())
                    )) {

                return "Registration number '"
                        + candidate.getRegistrationNumber()
                        + "' is already registered.";
            }

            // Email address check.
            if (!email.isEmpty()
                    && email.equals(
                            normalizeText(existing.getEmail())
                    )) {

                return "Email address '"
                        + candidate.getEmail()
                        + "' is already associated with another student record.";
            }

            // Phone number check.
            String existingPhone = normalizePhoneNumber(
                    existing.getPhoneNumber()
            );

            if (!phone.isEmpty()
                    && phone.equals(existingPhone)) {

                return "Phone number '"
                        + candidate.getPhoneNumber()
                        + "' is already associated with another student record.";
            }

            // Full name and date of birth identity check.
            String existingFullName = normalizeText(
                    existing.getFullName()
            );

            if (!fullName.isEmpty()
                    && fullName.equals(existingFullName)
                    && candidate.getDateOfBirth() != null
                    && candidate.getDateOfBirth().equals(
                            existing.getDateOfBirth()
                    )) {

                return "A student with the same full name and date of birth "
                        + "already exists.";
            }
        }

        return null;
    }

    /**
     * Normalizes text for case-insensitive comparison.
     */
    private String normalizeText(String value) {

        if (value == null) {
            return "";
        }

        return value.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes common Kenyan phone number formats.
     *
     * Examples:
     * +254712345678 -> 712345678
     * 254712345678  -> 712345678
     * 0712345678    -> 712345678
     */
    private String normalizePhoneNumber(String phone) {

        if (phone == null || phone.isBlank()) {
            return "";
        }

        String digits = phone.replaceAll("\\D", "");

        if (digits.startsWith("254") && digits.length() == 12) {
            return digits.substring(3);
        }

        if (digits.startsWith("0") && digits.length() == 10) {
            return digits.substring(1);
        }

        return digits;
    }

    public void showPage(String page) {
        cardLayout.show(contentPanel, page);
    }

    private JPanel createPlaceholder(
            String title,
            String description
    ) {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.BACKGROUND);

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        40, 40, 40, 40
                )
        );

        JLabel heading = new JLabel(title);
        heading.setFont(ThemeManager.FONT_HEADING);
        heading.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel information = new JLabel(description);
        information.setFont(ThemeManager.FONT_REGULAR);
        information.setForeground(ThemeManager.TEXT_SECONDARY);

        JPanel textPanel = new JPanel();

        textPanel.setOpaque(false);

        textPanel.setLayout(
                new javax.swing.BoxLayout(
                        textPanel,
                        javax.swing.BoxLayout.Y_AXIS
                )
        );

        textPanel.add(heading);
        textPanel.add(javax.swing.Box.createVerticalStrut(12));
        textPanel.add(information);

        panel.add(textPanel, BorderLayout.NORTH);

        return panel;
    }
}
