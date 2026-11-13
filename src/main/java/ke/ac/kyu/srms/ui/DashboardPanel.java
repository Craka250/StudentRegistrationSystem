
package ke.ac.kyu.srms.ui;

import ke.ac.kyu.srms.config.ThemeManager;
import ke.ac.kyu.srms.model.Student;

import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DashboardPanel extends JPanel {

    private final MainFrame mainFrame;

    private JLabel totalStudentsValue;
    private JLabel programmesValue;
    private JLabel newRegistrationsValue;
    private JLabel departmentsValue;
    private JLabel newRegistrationsDescription;

    public DashboardPanel(MainFrame mainFrame) {

        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createDashboardContent(), BorderLayout.CENTER);

        // Display correct zero values until the student list is supplied.
        updateStatistics(List.of());
    }

    /**
     * Refreshes all dashboard statistics using the central student list.
     */
    public void updateStatistics(List<Student> students) {

        if (students == null) {
            students = List.of();
        }

        Set<String> programmes = new HashSet<>();
        Set<String> departments = new HashSet<>();

        for (Student student : students) {

            if (student == null) {
                continue;
            }

            if (student.getProgramme() != null
                    && !student.getProgramme().isBlank()) {

                programmes.add(normalize(student.getProgramme()));
            }

            if (student.getDepartment() != null
                    && !student.getDepartment().isBlank()) {

                departments.add(normalize(student.getDepartment()));
            }
        }

        LocalDate today = LocalDate.now();

        // KyU academic year: 1 September to 31 August.
        int academicYearStart = today.getMonthValue() >= 9
                ? today.getYear()
                : today.getYear() - 1;

        LocalDate startDate = LocalDate.of(
                academicYearStart, 9, 1
        );

        LocalDate endDate = startDate.plusYears(1).minusDays(1);

        long newRegistrations = students.stream()
                .filter(student -> student != null)
                .filter(student -> student.getAdmissionDate() != null)
                .filter(student ->
                        !student.getAdmissionDate().isBefore(startDate)
                        && !student.getAdmissionDate().isAfter(endDate)
                )
                .count();

        totalStudentsValue.setText(
                String.valueOf(students.size())
        );

        programmesValue.setText(
                String.valueOf(programmes.size())
        );

        newRegistrationsValue.setText(
                String.valueOf(newRegistrations)
        );

        departmentsValue.setText(
                String.valueOf(departments.size())
        );

        newRegistrationsDescription.setText(
                "Academic Year " + academicYearStart
                + "/" + (academicYearStart + 1)
        );

        revalidate();
        repaint();
    }

    private String normalize(String value) {

        return value.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(18, 30, 18, 30));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("Dashboard");
        title.setFont(ThemeManager.FONT_SUBHEADING);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subtitle = new JLabel(
                "Student Registration and Management System"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(ThemeManager.TEXT_SECONDARY);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 12, 5)
        );
        actions.setOpaque(false);

        JTextField searchField = new JTextField(16);
        searchField.putClientProperty(
                "JTextField.placeholderText",
                "Search students..."
        );
        searchField.setPreferredSize(new Dimension(210, 38));
        searchField.setFont(ThemeManager.FONT_REGULAR);

        JButton notificationButton = new JButton(
                FontIcon.of(
                        FontAwesomeSolid.BELL,
                        17,
                        ThemeManager.TEXT_SECONDARY
                )
        );

        notificationButton.setPreferredSize(
                new Dimension(40, 38)
        );
        notificationButton.setToolTipText("Notifications");
        notificationButton.setFocusPainted(false);
        notificationButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        actions.add(searchField);
        actions.add(notificationButton);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        return header;
    }

    private JPanel createDashboardContent() {

        JPanel content = new JPanel();
        content.setBackground(ThemeManager.BACKGROUND);
        content.setLayout(
                new BoxLayout(content, BoxLayout.Y_AXIS)
        );
        content.setBorder(
                new EmptyBorder(30, 30, 30, 30)
        );

        JLabel welcome = new JLabel("Welcome to SRMS");
        welcome.setFont(ThemeManager.FONT_HEADING);
        welcome.setForeground(ThemeManager.TEXT_PRIMARY);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "Here is an overview of student registration activities."
        );
        description.setFont(ThemeManager.FONT_REGULAR);
        description.setForeground(
                ThemeManager.TEXT_SECONDARY
        );
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(welcome);
        content.add(Box.createVerticalStrut(8));
        content.add(description);
        content.add(Box.createVerticalStrut(28));
        content.add(createStatistics());
        content.add(Box.createVerticalStrut(30));
        content.add(createActivitySection());
        content.add(Box.createVerticalStrut(25));
        content.add(createQuickActions());

        return content;
    }

    private JPanel createStatistics() {

        JPanel panel = new JPanel(
                new GridLayout(1, 4, 16, 16)
        );

        panel.setOpaque(false);
        panel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 135)
        );
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalStudentsValue = createMetricLabel();
        programmesValue = createMetricLabel();
        newRegistrationsValue = createMetricLabel();
        departmentsValue = createMetricLabel();

        newRegistrationsDescription = new JLabel(
                "Current academic year"
        );
        newRegistrationsDescription.setFont(
                new Font("Segoe UI", Font.PLAIN, 10)
        );
        newRegistrationsDescription.setForeground(
                ThemeManager.TEXT_SECONDARY
        );

        panel.add(createStatCard(
                "Total Students",
                totalStudentsValue,
                "Registered records",
                FontAwesomeSolid.USERS,
                ThemeManager.PRIMARY
        ));

        panel.add(createStatCard(
                "Programmes",
                programmesValue,
                "Distinct programmes",
                FontAwesomeSolid.BOOK_OPEN,
                new Color(124, 58, 237)
        ));

        panel.add(createStatCard(
                "New Registrations",
                newRegistrationsValue,
                newRegistrationsDescription,
                FontAwesomeSolid.USER_PLUS,
                ThemeManager.SUCCESS
        ));

        panel.add(createStatCard(
                "Departments",
                departmentsValue,
                "Distinct departments",
                FontAwesomeSolid.BUILDING,
                ThemeManager.WARNING
        ));

        return panel;
    }

    private JLabel createMetricLabel() {

        JLabel number = new JLabel("0");

        number.setFont(new Font(
                "Segoe UI", Font.BOLD, 25
        ));

        number.setForeground(ThemeManager.TEXT_PRIMARY);

        return number;
    }

    private JPanel createStatCard(
            String title,
            JLabel number,
            String description,
            FontAwesomeSolid iconCode,
            Color accent
    ) {

        JLabel information = new JLabel(description);

        information.setFont(new Font(
                "Segoe UI", Font.PLAIN, 10
        ));

        information.setForeground(
                ThemeManager.TEXT_SECONDARY
        );

        return createStatCard(
                title, number, information, iconCode, accent
        );
    }

    private JPanel createStatCard(
            String title,
            JLabel number,
            JLabel information,
            FontAwesomeSolid iconCode,
            Color accent
    ) {

        JPanel card = new JPanel(
                new BorderLayout(12, 8)
        );

        card.setBackground(Color.WHITE);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(226, 232, 240)
                        ),
                        new EmptyBorder(17, 16, 17, 16)
                )
        );

        JLabel heading = new JLabel(title);
        heading.setFont(new Font(
                "Segoe UI", Font.PLAIN, 12
        ));
        heading.setForeground(
                ThemeManager.TEXT_SECONDARY
        );

        JLabel icon = new JLabel(
                FontIcon.of(iconCode, 21, accent)
        );
        icon.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(
                new BoxLayout(textPanel, BoxLayout.Y_AXIS)
        );

        textPanel.add(heading);
        textPanel.add(Box.createVerticalStrut(7));
        textPanel.add(number);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(information);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(icon, BorderLayout.EAST);

        return card;
    }

    private JPanel createActivitySection() {

        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(226, 232, 240)
                        ),
                        new EmptyBorder(20, 20, 20, 20)
                )
        );

        section.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 165)
        );
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Recent Activity");
        title.setFont(ThemeManager.FONT_SUBHEADING);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel emptyMessage = new JLabel(
                "No recent student activities to display."
        );
        emptyMessage.setFont(ThemeManager.FONT_REGULAR);
        emptyMessage.setForeground(
                ThemeManager.TEXT_SECONDARY
        );
        emptyMessage.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        section.add(title, BorderLayout.NORTH);
        section.add(emptyMessage, BorderLayout.CENTER);

        return section;
    }

    private JPanel createQuickActions() {

        JPanel section = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 12, 8)
        );

        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton registerButton = new JButton(
                "Register New Student",
                FontIcon.of(
                        FontAwesomeSolid.USER_PLUS,
                        16,
                        Color.WHITE
                )
        );

        registerButton.setBackground(ThemeManager.PRIMARY);
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(ThemeManager.FONT_MEDIUM);
        registerButton.setFocusPainted(false);
        registerButton.setBorder(
                new EmptyBorder(12, 17, 12, 17)
        );
        registerButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        registerButton.addActionListener(e ->
                mainFrame.showPage("registration")
        );

        JButton studentsButton = new JButton("View Students");

        studentsButton.setFont(ThemeManager.FONT_MEDIUM);
        studentsButton.setBackground(Color.WHITE);
        studentsButton.setForeground(
                ThemeManager.TEXT_PRIMARY
        );
        studentsButton.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(203, 213, 225)
                        ),
                        new EmptyBorder(11, 17, 11, 17)
                )
        );
        studentsButton.setFocusPainted(false);
        studentsButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        studentsButton.addActionListener(e ->
                mainFrame.showPage("students")
        );

        section.add(registerButton);
        section.add(studentsButton);

        return section;
    }
}
