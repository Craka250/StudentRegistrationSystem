
package ke.ac.kyu.srms.ui;

import ke.ac.kyu.srms.config.ThemeManager;
import ke.ac.kyu.srms.model.Student;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.github.lgooddatepicker.components.DatePicker;

import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

import java.util.TreeSet;
import java.util.Set;

public class StudentManagementPanel extends JPanel {

    private final MainFrame mainFrame;
    private final JTable studentTable;
    private final DefaultTableModel tableModel;
    private final CardLayout tableCardLayout;
    private final JPanel tableContentPanel;
    private final JLabel recordCountLabel;

    private JButton editButton;
    private JButton deleteButton;
    
    // Search and filtering controls.
    private final JTextField searchField;

    private final JComboBox<String> departmentFilter;
    private final JComboBox<String> programmeFilter;
    private final JComboBox<String> genderFilter;
    private final JComboBox<String> yearFilter;

    private final DatePicker fromDatePicker;
    private final DatePicker toDatePicker;

    // Handles filtering and sorting without rebuilding the table.
    private final TableRowSorter<DefaultTableModel> rowSorter;

    private static final Color BORDER_COLOR =
            new Color(226, 232, 240);

    private static final Color DELETE_COLOR =
            new Color(220, 53, 69);

    private static final String[] COLUMN_NAMES = {
        "Registration Number",
        "Full Name",
        "Gender",
        "Email Address",
        "Phone Number",
        "Programme",
        "Department",
        "Year",
        "Admission Date"
    };

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    public StudentManagementPanel(MainFrame mainFrame) {

        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BACKGROUND);

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentTable = createStudentTable();

        // Enable sorting and filtering.
        rowSorter = new TableRowSorter<>(tableModel);
        studentTable.setRowSorter(rowSorter);

        // Search field.
        searchField = new JTextField(24);
        searchField.putClientProperty(
                "JTextField.placeholderText",
                "Search by name, registration number, email..."
        );

        // Filter dropdowns.
        departmentFilter = new JComboBox<>();
        programmeFilter = new JComboBox<>();

        genderFilter = new JComboBox<>(
                new String[]{"All Genders", "Male", "Female"}
        );

        yearFilter = new JComboBox<>(
                new String[]{
                    "All Years",
                    "Year 1",
                    "Year 2",
                    "Year 3",
                    "Year 4",
                    "Year 5"
                }
        );

        // Admission date range.
        fromDatePicker = new DatePicker();
        toDatePicker = new DatePicker();

        tableCardLayout = new CardLayout();

        tableContentPanel = new JPanel(tableCardLayout);
        tableContentPanel.setBackground(Color.WHITE);

        recordCountLabel = new JLabel("0 student records");

        initializeTableContent();

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        
        initializeFilterOptions();
        initializeFilterListeners();

        // Enable or disable management buttons based on selection.
        studentTable.getSelectionModel().addListSelectionListener(
                this::handleStudentSelection
        );
    }

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout(15, 10));
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(22, 30, 22, 30));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("Student Management");
        title.setFont(ThemeManager.FONT_SUBHEADING);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subtitle = new JLabel(
                "View and manage registered student information."
        );
        subtitle.setFont(ThemeManager.FONT_REGULAR);
        subtitle.setForeground(ThemeManager.TEXT_SECONDARY);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JButton registerButton = createActionButton(
                "Register New Student",
                ThemeManager.PRIMARY,
                Color.WHITE,
                e -> mainFrame.showPage("registration")
        );

        header.add(titlePanel, BorderLayout.WEST);
        header.add(registerButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createMainContent() {

        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBackground(ThemeManager.BACKGROUND);
        content.setBorder(new EmptyBorder(25, 30, 30, 30));

        content.add(createSummaryPanel(), BorderLayout.NORTH);
        content.add(createTableSection(), BorderLayout.CENTER);

        return content;
    }

    private JPanel createSummaryPanel() {

        JPanel summary = new JPanel(new BorderLayout());
        summary.setBackground(Color.WHITE);

        summary.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR),
                        new EmptyBorder(17, 20, 17, 20)
                )
        );

        JPanel information = new JPanel();
        information.setOpaque(false);
        information.setLayout(
                new BoxLayout(information, BoxLayout.Y_AXIS)
        );

        JLabel heading = new JLabel("Student Records");
        heading.setFont(ThemeManager.FONT_MEDIUM);
        heading.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel description = new JLabel(
                "Overview of students currently available in the system."
        );
        description.setFont(ThemeManager.FONT_REGULAR);
        description.setForeground(ThemeManager.TEXT_SECONDARY);

        information.add(heading);
        information.add(Box.createVerticalStrut(5));
        information.add(description);

        recordCountLabel.setFont(ThemeManager.FONT_MEDIUM);
        recordCountLabel.setForeground(ThemeManager.TEXT_SECONDARY);

        summary.add(information, BorderLayout.WEST);
        summary.add(recordCountLabel, BorderLayout.EAST);

        return summary;
    }
    
    private JPanel createFilterPanel() {

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(
                new EmptyBorder(15, 20, 15, 20)
        );

        // Search row.
        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setBackground(Color.WHITE);

        JLabel searchLabel = new JLabel("Search Students");
        searchLabel.setFont(ThemeManager.FONT_MEDIUM);
        searchLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        searchRow.add(searchLabel, BorderLayout.WEST);
        searchRow.add(searchField, BorderLayout.CENTER);

        // Filter controls.
        JPanel filters = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 8)
        );
        filters.setBackground(Color.WHITE);

        filters.add(createFilterControl("Department", departmentFilter));
        filters.add(createFilterControl("Programme", programmeFilter));
        filters.add(createFilterControl("Gender", genderFilter));
        filters.add(createFilterControl("Year", yearFilter));

        filters.add(createFilterControl("Admitted From", fromDatePicker));
        filters.add(createFilterControl("Admitted To", toDatePicker));

        JButton clearButton = createActionButton(
                "Clear Filters",
                new Color(100, 116, 139),
                Color.WHITE,
                e -> clearFilters()
        );

        filters.add(clearButton);

        filterPanel.add(searchRow);
        filterPanel.add(Box.createVerticalStrut(10));
        filterPanel.add(filters);

        return filterPanel;
    }

    private JPanel createFilterControl(String label, Component control) {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel heading = new JLabel(label);
        heading.setFont(ThemeManager.FONT_REGULAR);
        heading.setForeground(ThemeManager.TEXT_SECONDARY);

        panel.add(heading);
        panel.add(Box.createVerticalStrut(5));
        panel.add(control);

        return panel;
    }
    
    private void initializeFilterOptions() {

        departmentFilter.addItem("All Departments");
        programmeFilter.addItem("All Programmes");

        yearFilter.setSelectedItem("All Years");
        genderFilter.setSelectedItem("All Genders");

        refreshFilterOptions(mainFrame.getStudents());
    }

    private void refreshFilterOptions(List<Student> students) {

        String selectedDepartment =
                (String) departmentFilter.getSelectedItem();

        String selectedProgramme =
                (String) programmeFilter.getSelectedItem();

        Set<String> departments = new TreeSet<>(
                String.CASE_INSENSITIVE_ORDER
        );

        Set<String> programmes = new TreeSet<>(
                String.CASE_INSENSITIVE_ORDER
        );

        if (students != null) {
            for (Student student : students) {

                if (student == null) {
                    continue;
                }

                if (student.getDepartment() != null
                        && !student.getDepartment().isBlank()) {
                    departments.add(student.getDepartment());
                }

                if (student.getProgramme() != null
                        && !student.getProgramme().isBlank()) {
                    programmes.add(student.getProgramme());
                }
            }
        }

        departmentFilter.removeAllItems();
        departmentFilter.addItem("All Departments");

        for (String department : departments) {
            departmentFilter.addItem(department);
        }

        programmeFilter.removeAllItems();
        programmeFilter.addItem("All Programmes");

        for (String programme : programmes) {
            programmeFilter.addItem(programme);
        }

        if (selectedDepartment != null) {
            departmentFilter.setSelectedItem(selectedDepartment);
        }

        if (selectedProgramme != null) {
            programmeFilter.setSelectedItem(selectedProgramme);
        }

        if (departmentFilter.getSelectedItem() == null) {
            departmentFilter.setSelectedIndex(0);
        }

        if (programmeFilter.getSelectedItem() == null) {
            programmeFilter.setSelectedIndex(0);
        }
    }

    private void initializeFilterListeners() {

        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {

            @Override
            public void insertUpdate(
                    javax.swing.event.DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void removeUpdate(
                    javax.swing.event.DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void changedUpdate(
                    javax.swing.event.DocumentEvent e) {
                applyFilters();
            }
        });

        departmentFilter.addActionListener(e -> applyFilters());
        programmeFilter.addActionListener(e -> applyFilters());
        genderFilter.addActionListener(e -> applyFilters());
        yearFilter.addActionListener(e -> applyFilters());

        fromDatePicker.addDateChangeListener(e -> applyFilters());
        toDatePicker.addDateChangeListener(e -> applyFilters());
    }
    
    private void applyFilters() {

        String search = searchField.getText()
                .trim()
                .toLowerCase();

        String department = (String) departmentFilter.getSelectedItem();
        String programme = (String) programmeFilter.getSelectedItem();
        String gender = (String) genderFilter.getSelectedItem();
        String year = (String) yearFilter.getSelectedItem();

        LocalDate fromDate = fromDatePicker.getDate();
        LocalDate toDate = toDatePicker.getDate();

        rowSorter.setRowFilter(
                new RowFilter<DefaultTableModel, Integer>() {

            @Override
            public boolean include(
                    Entry<? extends DefaultTableModel, ? extends Integer> entry) {

                // 1. Search across all visible student columns.
                boolean matchesSearch = search.isEmpty();

                if (!matchesSearch) {

                    for (int column = 0;
                         column < entry.getValueCount();
                         column++) {

                        String value = entry.getStringValue(column);

                        if (value != null
                                && value.toLowerCase().contains(search)) {

                            matchesSearch = true;
                            break;
                        }
                    }
                }

                if (!matchesSearch) {
                    return false;
                }

                // 2. Department filter.
                String studentDepartment = entry.getStringValue(6);

                if (department != null
                        && !"All Departments".equals(department)
                        && !department.equalsIgnoreCase(studentDepartment)) {
                    return false;
                }

                // 3. Programme filter.
                String studentProgramme = entry.getStringValue(5);

                if (programme != null
                        && !"All Programmes".equals(programme)
                        && !programme.equalsIgnoreCase(studentProgramme)) {
                    return false;
                }

                // 4. Gender filter.
                String studentGender = entry.getStringValue(2);

                if (gender != null
                        && !"All Genders".equals(gender)
                        && !gender.equalsIgnoreCase(studentGender)) {
                    return false;
                }

                // 5. Year of study filter.
                String studentYear = entry.getStringValue(7);

                if (year != null && !"All Years".equals(year)) {

                    String requiredYear = year.replace("Year ", "");

                    if (!requiredYear.equals(studentYear)) {
                        return false;
                    }
                }

                // 6. Admission date range.
                if (fromDate != null || toDate != null) {

                    String registrationNumber = entry.getStringValue(0);

                    Student student = mainFrame.getStudents()
                            .stream()
                            .filter(s -> s != null)
                            .filter(s -> registrationNumber.equalsIgnoreCase(
                                    s.getRegistrationNumber()
                            ))
                            .findFirst()
                            .orElse(null);

                    if (student == null || student.getAdmissionDate() == null) {
                        return false;
                    }

                    LocalDate admissionDate = student.getAdmissionDate();

                    if (fromDate != null
                            && admissionDate.isBefore(fromDate)) {
                        return false;
                    }

                    if (toDate != null
                            && admissionDate.isAfter(toDate)) {
                        return false;
                    }
                }

                return true;
            }
        });

        updateRecordCount();
    }

    private void clearFilters() {

        searchField.setText("");

        departmentFilter.setSelectedIndex(0);
        programmeFilter.setSelectedIndex(0);
        genderFilter.setSelectedIndex(0);
        yearFilter.setSelectedIndex(0);

        fromDatePicker.clear();
        toDatePicker.clear();

        applyFilters();
    }

    private void updateRecordCount() {

        int total = tableModel.getRowCount();
        int displayed = studentTable.getRowCount();

        recordCountLabel.setText(
                "Showing " + displayed + " of " + total + " students"
        );
    }

    private JPanel createTableSection() {

        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(
                BorderFactory.createLineBorder(BORDER_COLOR)
        );

        // Table heading.
        JPanel tableHeading = new JPanel(new BorderLayout());
        tableHeading.setBackground(Color.WHITE);
        tableHeading.setBorder(
                new EmptyBorder(17, 20, 17, 20)
        );

        JLabel title = new JLabel("All Students");
        title.setFont(ThemeManager.FONT_MEDIUM);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel information = new JLabel(
                "Student information and academic records"
        );
        information.setFont(ThemeManager.FONT_REGULAR);
        information.setForeground(ThemeManager.TEXT_SECONDARY);

        tableHeading.add(title, BorderLayout.WEST);
        tableHeading.add(information, BorderLayout.EAST);

        // Student management action buttons.
        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setBackground(Color.WHITE);
        actionBar.setBorder(
                new EmptyBorder(0, 20, 14, 20)
        );

        JLabel actionInformation = new JLabel(
                "Select a student record to edit or delete."
        );
        actionInformation.setFont(ThemeManager.FONT_REGULAR);
        actionInformation.setForeground(ThemeManager.TEXT_SECONDARY);

        JPanel buttonsPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );
        buttonsPanel.setOpaque(false);

        editButton = createActionButton(
                "Edit Selected",
                ThemeManager.PRIMARY,
                Color.WHITE,
                e -> editSelectedStudent()
        );

        deleteButton = createActionButton(
                "Delete Selected",
                DELETE_COLOR,
                Color.WHITE,
                e -> deleteSelectedStudent()
        );

        // No student is selected initially.
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);

        actionBar.add(actionInformation, BorderLayout.WEST);
        actionBar.add(buttonsPanel, BorderLayout.EAST);

        // Table scroll pane.
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Empty state and populated table.
        tableContentPanel.add(
                createEmptyState(),
                "empty"
        );

        tableContentPanel.add(
                tablePanel,
                "table"
        );

        // Arrange the complete section.
        JPanel topSection = new JPanel();
        topSection.setLayout(
                new BoxLayout(topSection, BoxLayout.Y_AXIS)
        );
        topSection.setBackground(Color.WHITE);

        topSection.add(tableHeading);
        topSection.add(createFilterPanel());
        topSection.add(actionBar);

        section.add(topSection, BorderLayout.NORTH);
        section.add(tableContentPanel, BorderLayout.CENTER);

        return section;
    }

    /**
     * Creates a consistently styled, square-cornered button.
     */
    private JButton createActionButton(
            String text,
            Color background,
            Color foreground,
            ActionListener action
    ) {

        JButton button = new JButton(text);

        button.setFont(ThemeManager.FONT_MEDIUM);
        button.setBackground(background);
        button.setForeground(foreground);

        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);

        // FlatLaf: explicitly remove rounded corners.
        button.putClientProperty("Button.arc", 0);
        button.putClientProperty("Component.arc", 0);

        button.setBorder(
                new EmptyBorder(11, 16, 11, 16)
        );

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.setEnabled(true);

        if (action != null) {
            button.addActionListener(action);
        }

        return button;
    }

    /**
     * Updates Edit and Delete button states when selection changes.
     */
    private void handleStudentSelection(ListSelectionEvent event) {

        if (event.getValueIsAdjusting()) {
            return;
        }

        boolean selected = studentTable.getSelectedRow() != -1;

        editButton.setEnabled(selected);
        deleteButton.setEnabled(selected);
    }

    /**
     * Returns the selected student's registration number.
     * Handles table sorting by converting the selected view row
     * into the corresponding model row.
     */
    private String getSelectedRegistrationNumber() {

        int selectedViewRow = studentTable.getSelectedRow();

        if (selectedViewRow < 0) {
            return null;
        }

        int selectedModelRow =
                studentTable.convertRowIndexToModel(selectedViewRow);

        Object value = tableModel.getValueAt(selectedModelRow, 0);

        return value == null ? null : value.toString();
    }

    /**
     * Finds the selected student in MainFrame's central collection.
     */
    private Student getSelectedStudent() {

        String registrationNumber =
                getSelectedRegistrationNumber();

        if (registrationNumber == null) {
            return null;
        }

        return mainFrame.getStudents()
                .stream()
                .filter(student -> student != null)
                .filter(student ->
                        registrationNumber.equalsIgnoreCase(
                                student.getRegistrationNumber()
                        )
                )
                .findFirst()
                .orElse(null);
    }

    /**
     * Opens the registration form with the selected student's details.
     */
    private void editSelectedStudent() {

        Student selectedStudent = getSelectedStudent();

        if (selectedStudent == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a valid student record.",
                    "Student Not Found",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        mainFrame.openStudentForEditing(selectedStudent);
    }

    /**
     * Deletes the selected student using MainFrame's existing
     * confirmation and deletion functionality.
     */
    private void deleteSelectedStudent() {

        String registrationNumber =
                getSelectedRegistrationNumber();

        if (registrationNumber == null) {
            return;
        }

        mainFrame.deleteStudent(registrationNumber);
    }

    private JTable createStudentTable() {

        JTable table = new JTable(tableModel);

        table.setFont(ThemeManager.FONT_REGULAR);
        table.setForeground(ThemeManager.TEXT_PRIMARY);
        table.setBackground(Color.WHITE);

        table.setRowHeight(43);
        table.setIntercellSpacing(new Dimension(0, 1));

        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(238, 242, 246));

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        table.setSelectionBackground(new Color(232, 242, 255));
        table.setSelectionForeground(ThemeManager.TEXT_PRIMARY);

        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader header = table.getTableHeader();

        header.setPreferredSize(new Dimension(100, 45));
        header.setFont(ThemeManager.FONT_MEDIUM);
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(ThemeManager.TEXT_SECONDARY);

        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);

        int[] columnWidths = {
            165, 180, 90, 210, 135, 220, 210, 75, 135
        };

        for (int i = 0; i < columnWidths.length; i++) {
            table.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(columnWidths[i]);
        }

        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column
            ) {

                Component component =
                        super.getTableCellRendererComponent(
                                table,
                                value,
                                isSelected,
                                hasFocus,
                                row,
                                column
                        );

                setBorder(new EmptyBorder(0, 10, 0, 10));

                if (isSelected) {

                    component.setBackground(
                            table.getSelectionBackground()
                    );

                    component.setForeground(
                            table.getSelectionForeground()
                    );

                } else {

                    component.setBackground(
                            row % 2 == 0
                                    ? Color.WHITE
                                    : new Color(250, 251, 253)
                    );

                    component.setForeground(
                            ThemeManager.TEXT_PRIMARY
                    );
                }

                return component;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {

            table.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(renderer);
        }

        return table;
    }

    private JPanel createEmptyState() {

        JPanel emptyState = new JPanel();

        emptyState.setBackground(Color.WHITE);
        emptyState.setLayout(
                new BoxLayout(emptyState, BoxLayout.Y_AXIS)
        );

        emptyState.setBorder(
                new EmptyBorder(45, 20, 45, 20)
        );

        JLabel icon = new JLabel(
                org.kordamp.ikonli.swing.FontIcon.of(
                        org.kordamp.ikonli.fontawesome6.FontAwesomeSolid.USER,
                        45,
                        ThemeManager.TEXT_SECONDARY
                )
        );

        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel heading = new JLabel("No Student Records Yet");

        heading.setFont(ThemeManager.FONT_MEDIUM);
        heading.setForeground(ThemeManager.TEXT_PRIMARY);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel description = new JLabel(
                "Student records will appear here once they are saved."
        );

        description.setFont(ThemeManager.FONT_REGULAR);
        description.setForeground(ThemeManager.TEXT_SECONDARY);
        description.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton registerButton = createActionButton(
                "Register a Student",
                ThemeManager.PRIMARY,
                Color.WHITE,
                e -> mainFrame.showPage("registration")
        );

        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyState.add(Box.createVerticalGlue());
        emptyState.add(icon);
        emptyState.add(Box.createVerticalStrut(15));
        emptyState.add(heading);
        emptyState.add(Box.createVerticalStrut(8));
        emptyState.add(description);
        emptyState.add(Box.createVerticalStrut(20));
        emptyState.add(registerButton);
        emptyState.add(Box.createVerticalGlue());

        return emptyState;
    }

    /**
     * Updates the empty state and student record count.
     */
    private void initializeTableContent() {

        if (tableModel.getRowCount() == 0) {
            tableCardLayout.show(tableContentPanel, "empty");
        } else {
            tableCardLayout.show(tableContentPanel, "table");
        }

        updateRecordCount();

        if (editButton != null && deleteButton != null) {

            boolean selected = studentTable.getSelectedRow() != -1;

            editButton.setEnabled(selected);
            deleteButton.setEnabled(selected);
        }
    }

    /**
     * Adds a newly confirmed student to the management table.
     */
    public void addStudent(Student student) {

        if (student == null) {
            return;
        }

        Object[] rowData = {
            student.getRegistrationNumber(),
            student.getFullName(),
            student.getGender(),
            student.getEmail(),
            student.getPhoneNumber(),
            student.getProgramme(),
            student.getDepartment(),
            student.getYearOfStudy(),
            formatAdmissionDate(student.getAdmissionDate())
        };

        tableModel.addRow(rowData);
        applyFilters();

        initializeTableContent();

        studentTable.revalidate();
        studentTable.repaint();

        tableContentPanel.revalidate();
        tableContentPanel.repaint();
    }

    /**
     * Formats the admission date for display.
     */
    private String formatAdmissionDate(LocalDate date) {

        if (date == null) {
            return "Not provided";
        }

        return date.format(DATE_FORMATTER);
    }

    /**
     * Rebuilds the student table from MainFrame's central collection.
     * Department names are resolved from the current programme relationship.
     */
    public void refreshStudents(
            List<Student> students,
            Function<Student, String> departmentResolver
    ) {

        tableModel.setRowCount(0);

        if (students != null) {

            for (Student student : students) {

                if (student == null) {
                    continue;
                }

                String currentDepartment =
                        departmentResolver == null
                                ? student.getDepartment()
                                : departmentResolver.apply(student);

                Object[] rowData = {
                    student.getRegistrationNumber(),
                    student.getFullName(),
                    student.getGender(),
                    student.getEmail(),
                    student.getPhoneNumber(),
                    student.getProgramme(),
                    currentDepartment,
                    student.getYearOfStudy(),
                    formatAdmissionDate(student.getAdmissionDate())
                };

                tableModel.addRow(rowData);
            }
        }

        // Refresh dropdown options when student records change.
        refreshFilterOptions(students);

        // Reapply active filters to the updated records.
        applyFilters();

        initializeTableContent();

        studentTable.clearSelection();

        studentTable.revalidate();
        studentTable.repaint();

        tableContentPanel.revalidate();
        tableContentPanel.repaint();
    }
}
