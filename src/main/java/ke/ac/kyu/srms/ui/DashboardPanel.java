
package ke.ac.kyu.srms.ui;

import ke.ac.kyu.srms.config.ThemeManager;
import ke.ac.kyu.srms.model.Student;
import ke.ac.kyu.srms.model.Programme;
import ke.ac.kyu.srms.model.Department;
import ke.ac.kyu.srms.model.AuditLog;

import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final MainFrame mainFrame;

    private JLabel totalStudentsValue;
    private JLabel programmesValue;
    private JLabel newRegistrationsValue;
    private JLabel departmentsValue;
    private JLabel newRegistrationsDescription;

    private JPanel activityRows;

    private static final DateTimeFormatter ACTIVITY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    public DashboardPanel(MainFrame mainFrame) {

        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createDashboardContent(), BorderLayout.CENTER);

        updateStatistics(List.of());
        updateRecentActivities(List.of());
    }

    public void updateStatistics(List<Student> students) {
        updateStatistics(students, List.of(), List.of());
    }

    public void updateStatistics(
            List<Student> students,
            List<Programme> programmes,
            List<Department> departments
    ) {
        if (students == null) {
            students = List.of();
        }

        if (programmes == null) {
            programmes = List.of();
        }

        if (departments == null) {
            departments = List.of();
        }

        LocalDate today = LocalDate.now();

        // KyU academic year: 1 September to 31 August.
        int academicYearStart = today.getMonthValue() >= 9
                ? today.getYear()
                : today.getYear() - 1;

        LocalDate startDate = LocalDate.of(academicYearStart, 9, 1);
        LocalDate endDate = startDate.plusYears(1).minusDays(1);

        long newRegistrations = students.stream()
                .filter(student -> student != null)
                .filter(student -> student.getAdmissionDate() != null)
                .filter(student ->
                        !student.getAdmissionDate().isBefore(startDate)
                        && !student.getAdmissionDate().isAfter(endDate)
                )
                .count();

        totalStudentsValue.setText(String.valueOf(students.size()));
        programmesValue.setText(String.valueOf(programmes.size()));
        departmentsValue.setText(String.valueOf(departments.size()));
        newRegistrationsValue.setText(String.valueOf(newRegistrations));

        newRegistrationsDescription.setText(
                "Academic Year " + academicYearStart
                + "/" + (academicYearStart + 1)
        );

        revalidate();
        repaint();
    }

    /**
     * Refreshes the recent activity section using audit records.
     */
    public void updateRecentActivities(List<AuditLog> activities) {

        if (activityRows == null) {
            return;
        }

        activityRows.removeAll();

        if (activities == null || activities.isEmpty()) {

            JLabel emptyMessage = new JLabel(
                    "No recent activities to display."
            );

            emptyMessage.setFont(ThemeManager.FONT_REGULAR);
            emptyMessage.setForeground(ThemeManager.TEXT_SECONDARY);

            activityRows.add(emptyMessage);

        } else {

            for (AuditLog activity : activities) {
                if (activity != null) {
                    activityRows.add(createActivityRow(activity));
                }
            }
        }

        activityRows.revalidate();
        activityRows.repaint();
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

        searchField.addActionListener(e -> {
            String query = searchField.getText().trim();

            if (!query.isEmpty()) {
                mainFrame.searchStudents(query);
            }
        });

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

        notificationButton.addActionListener(e -> showNotifications());

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

        JPanel section = new JPanel(new BorderLayout(0, 14));
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
                new Dimension(Integer.MAX_VALUE, 220)
        );
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Recent Activity");
        title.setFont(ThemeManager.FONT_SUBHEADING);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        activityRows = new JPanel();
        activityRows.setOpaque(false);
        activityRows.setLayout(
                new BoxLayout(activityRows, BoxLayout.Y_AXIS)
        );

        JScrollPane scrollPane = new JScrollPane(activityRows);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        section.add(title, BorderLayout.NORTH);
        section.add(scrollPane, BorderLayout.CENTER);

        return section;
    }

    private JPanel createActivityRow(AuditLog activity) {

        JPanel row = new JPanel(new BorderLayout(12, 4));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(7, 0, 7, 0));
        row.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 55)
        );

        JLabel icon = new JLabel(
                FontIcon.of(
                        getActivityIcon(activity),
                        15,
                        ThemeManager.PRIMARY
                )
        );

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(
                new BoxLayout(text, BoxLayout.Y_AXIS)
        );

        JLabel description = new JLabel(
                describeActivity(activity)
        );
        description.setFont(ThemeManager.FONT_MEDIUM);
        description.setForeground(ThemeManager.TEXT_PRIMARY);

        String username = activity.adminUsername() == null
                || activity.adminUsername().isBlank()
                ? "Administrator"
                : activity.adminUsername();

        JLabel actor = new JLabel("By " + username);
        actor.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        actor.setForeground(ThemeManager.TEXT_SECONDARY);

        text.add(description);
        text.add(Box.createVerticalStrut(3));
        text.add(actor);

        JLabel timestamp = new JLabel(
                activity.createdAt() == null
                        ? ""
                        : activity.createdAt().format(ACTIVITY_DATE_FORMAT)
        );
        timestamp.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timestamp.setForeground(ThemeManager.TEXT_SECONDARY);

        row.add(icon, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.add(timestamp, BorderLayout.EAST);

        return row;
    }

    private FontAwesomeSolid getActivityIcon(AuditLog activity) {

        String entity = safeUpper(activity.entityType());
        String action = safeUpper(activity.action());

        if ("STUDENT".equals(entity)) {
            return "DELETE".equals(action)
                    ? FontAwesomeSolid.USER_MINUS
                    : FontAwesomeSolid.USER;
        }

        if ("DEPARTMENT".equals(entity)) {
            return FontAwesomeSolid.BUILDING;
        }

        if ("PROGRAMME".equals(entity)) {
            return FontAwesomeSolid.BOOK_OPEN;
        }

        return FontAwesomeSolid.CLIPBOARD_LIST;
    }

    private String describeActivity(AuditLog activity) {

        String action = safeUpper(activity.action());
        String entity = safeUpper(activity.entityType());

        String verb = switch (action) {
            case "CREATE", "INSERT" -> "created";
            case "UPDATE", "EDIT" -> "updated";
            case "DELETE", "REMOVE" -> "deleted";
            case "LOGIN" -> "signed in";
            case "LOGOUT" -> "signed out";
            default -> action.isBlank()
                    ? "performed an action on"
                    : action.toLowerCase();
        };

        if ("LOGIN".equals(action) || "LOGOUT".equals(action)) {
            return "Administrator " + verb;
        }

        String entityName = switch (entity) {
            case "STUDENT" -> "student record";
            case "DEPARTMENT" -> "department";
            case "PROGRAMME" -> "programme";
            default -> entity.isBlank()
                    ? "record"
                    : entity.toLowerCase() + " record";
        };

        return "Administrator " + verb + " " + entityName;
    }

    private String safeUpper(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private void showNotifications() {

        List<AuditLog> notifications =
                mainFrame.getRecentActivities(20);

        JPanel content = new JPanel();
        content.setLayout(
                new BoxLayout(content, BoxLayout.Y_AXIS)
        );
        content.setBorder(new EmptyBorder(8, 8, 8, 8));
        content.setBackground(Color.WHITE);

        if (notifications.isEmpty()) {

            JLabel empty = new JLabel(
                    "There are no notifications yet."
            );
            empty.setFont(ThemeManager.FONT_REGULAR);
            empty.setForeground(ThemeManager.TEXT_SECONDARY);
            content.add(empty);

        } else {

            for (AuditLog activity : notifications) {
                content.add(createNotificationRow(activity));
                content.add(Box.createVerticalStrut(5));
            }
        }

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setPreferredSize(new Dimension(530, 360));
        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(226, 232, 240)
                )
        );

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Recent Notifications",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private JPanel createNotificationRow(AuditLog activity) {

        JPanel row = new JPanel(new BorderLayout(10, 4));
        row.setBackground(Color.WHITE);
        row.setBorder(
                new EmptyBorder(10, 8, 10, 8)
        );

        JLabel description = new JLabel(
                describeActivity(activity)
        );
        description.setFont(ThemeManager.FONT_MEDIUM);
        description.setForeground(ThemeManager.TEXT_PRIMARY);

        String username = activity.adminUsername() == null
                || activity.adminUsername().isBlank()
                ? "Administrator"
                : activity.adminUsername();

        String timestamp = activity.createdAt() == null
                ? ""
                : activity.createdAt().format(ACTIVITY_DATE_FORMAT);

        JLabel details = new JLabel(
                "By " + username + (timestamp.isBlank() ? "" : "  •  " + timestamp)
        );
        details.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        details.setForeground(ThemeManager.TEXT_SECONDARY);

        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(
                new BoxLayout(labels, BoxLayout.Y_AXIS)
        );

        labels.add(description);
        labels.add(Box.createVerticalStrut(4));
        labels.add(details);

        row.add(
                new JLabel(FontIcon.of(
                        getActivityIcon(activity),
                        16,
                        ThemeManager.PRIMARY
                )),
                BorderLayout.WEST
        );
        row.add(labels, BorderLayout.CENTER);

        return row;
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
