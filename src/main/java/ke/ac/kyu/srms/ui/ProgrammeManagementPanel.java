
package ke.ac.kyu.srms.ui;

import ke.ac.kyu.srms.config.ThemeManager;
import ke.ac.kyu.srms.model.Department;
import ke.ac.kyu.srms.model.Programme;
import ke.ac.kyu.srms.model.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionListener;

public class ProgrammeManagementPanel extends JPanel {

    private final MainFrame mainFrame;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField;

    private JButton editButton;
    private JButton deleteButton;

    private static final Color BORDER_COLOR =
            new Color(226, 232, 240);

    private static final Color DELETE_COLOR =
            new Color(220, 53, 69);

    public ProgrammeManagementPanel(MainFrame mainFrame) {

        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BACKGROUND);

        tableModel = new DefaultTableModel(
                new String[]{
                    "Programme",
                    "Department",
                    "Description",
                    "Students"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        styleTable();

        searchField = new JTextField(18);

        searchField.putClientProperty(
                "JTextField.placeholderText",
                "Search programmes..."
        );

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {

            @Override
            public void insertUpdate(
                    javax.swing.event.DocumentEvent e
            ) {
                refreshData();
            }

            @Override
            public void removeUpdate(
                    javax.swing.event.DocumentEvent e
            ) {
                refreshData();
            }

            @Override
            public void changedUpdate(
                    javax.swing.event.DocumentEvent e
            ) {
                refreshData();
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {
                updateActionButtons();
            }
        });

        refreshData();
    }

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(22, 30, 22, 30));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(
                new BoxLayout(titles, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("Programme Management");
        title.setFont(ThemeManager.FONT_SUBHEADING);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subtitle = new JLabel(
                "Manage academic programmes and their department assignments."
        );

        subtitle.setFont(ThemeManager.FONT_REGULAR);
        subtitle.setForeground(ThemeManager.TEXT_SECONDARY);

        titles.add(title);
        titles.add(Box.createVerticalStrut(5));
        titles.add(subtitle);

        JButton addButton = createActionButton(
                "Add Programme",
                ThemeManager.PRIMARY,
                Color.WHITE,
                e -> showProgrammeDialog(null)
        );

        header.add(titles, BorderLayout.WEST);
        header.add(addButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {

        JPanel content = new JPanel(
                new BorderLayout(0, 18)
        );

        content.setBackground(ThemeManager.BACKGROUND);
        content.setBorder(
                new EmptyBorder(25, 30, 30, 30)
        );

        // Search and section heading.
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(
                new EmptyBorder(15, 18, 15, 18)
        );

        JLabel label = new JLabel("Academic Programmes");
        label.setFont(ThemeManager.FONT_MEDIUM);
        label.setForeground(ThemeManager.TEXT_PRIMARY);

        toolbar.add(label, BorderLayout.WEST);
        toolbar.add(searchField, BorderLayout.EAST);

        // Table container.
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(
                BorderFactory.createLineBorder(BORDER_COLOR)
        );

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Action bar.
        JPanel actions = new JPanel(new BorderLayout());
        actions.setBackground(Color.WHITE);
        actions.setBorder(
                new EmptyBorder(12, 18, 12, 18)
        );

        JLabel actionInformation = new JLabel(
                "Select a programme to edit or delete."
        );

        actionInformation.setFont(ThemeManager.FONT_REGULAR);
        actionInformation.setForeground(
                ThemeManager.TEXT_SECONDARY
        );

        JPanel buttonsPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );

        buttonsPanel.setOpaque(false);

        editButton = createActionButton(
                "Edit Selected",
                ThemeManager.PRIMARY,
                Color.WHITE,
                e -> editSelected()
        );

        deleteButton = createActionButton(
                "Delete Selected",
                DELETE_COLOR,
                Color.WHITE,
                e -> deleteSelected()
        );

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);

        actions.add(actionInformation, BorderLayout.WEST);
        actions.add(buttonsPanel, BorderLayout.EAST);

        tablePanel.add(actions, BorderLayout.SOUTH);

        content.add(toolbar, BorderLayout.NORTH);
        content.add(tablePanel, BorderLayout.CENTER);

        return content;
    }

    /**
     * Standard button styling shared by all programme actions.
     * Buttons have square corners and consistent dimensions.
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

        // Explicitly disable rounded FlatLaf button corners.
        button.putClientProperty("Button.arc", 0);
        button.putClientProperty("Component.arc", 0);

        button.setBorder(
                new EmptyBorder(11, 16, 11, 16)
        );

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        if (action != null) {
            button.addActionListener(action);
        }

        return button;
    }

    /**
     * Enables Edit and Delete only when a programme is selected.
     */
    private void updateActionButtons() {

        if (editButton == null || deleteButton == null) {
            return;
        }

        boolean selected = table.getSelectedRow() >= 0;

        editButton.setEnabled(selected);
        deleteButton.setEnabled(selected);
    }

    private void styleTable() {

        table.setRowHeight(43);
        table.setFont(ThemeManager.FONT_REGULAR);
        table.setForeground(ThemeManager.TEXT_PRIMARY);
        table.setBackground(Color.WHITE);

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        table.setSelectionBackground(
                new Color(232, 242, 255)
        );

        table.setSelectionForeground(
                ThemeManager.TEXT_PRIMARY
        );

        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(238, 242, 246));

        JTableHeader header = table.getTableHeader();

        header.setFont(ThemeManager.FONT_MEDIUM);
        header.setForeground(ThemeManager.TEXT_SECONDARY);
        header.setBackground(new Color(248, 250, 252));
        header.setPreferredSize(new Dimension(100, 45));
        header.setReorderingAllowed(false);
    }

    /**
     * Refreshes the programme table using the central data collections.
     */
    public void refreshData() {

        tableModel.setRowCount(0);

        String query = searchField == null
                ? ""
                : searchField.getText().trim().toLowerCase();

        for (Programme programme : mainFrame.getProgrammes()) {

            Department department =
                    mainFrame.getDepartmentById(
                            programme.getDepartmentId()
                    );

            String departmentName = department == null
                    ? "Unassigned"
                    : department.getName();

            long studentCount = mainFrame.getStudents()
                    .stream()
                    .filter(student -> student != null)
                    .filter(student ->
                            student.getProgramme() != null
                            && student.getProgramme().equalsIgnoreCase(
                                    programme.getName()
                            )
                    )
                    .count();

            String programmeName = programme.getName() == null
                    ? ""
                    : programme.getName();

            String description = programme.getDescription() == null
                    ? ""
                    : programme.getDescription();

            if (!programmeName.toLowerCase().contains(query)
                    && !departmentName.toLowerCase().contains(query)
                    && !description.toLowerCase().contains(query)) {
                continue;
            }

            tableModel.addRow(new Object[]{
                programmeName,
                departmentName,
                description,
                studentCount
            });
        }

        table.clearSelection();
        updateActionButtons();

        table.revalidate();
        table.repaint();
    }

    /**
     * Retrieves the selected programme from the central collection.
     */
    private Programme getSelectedProgramme() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a programme first.",
                    "No Programme Selected",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return null;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);

        String selectedName =
                (String) tableModel.getValueAt(modelRow, 0);

        return mainFrame.getProgrammeByName(selectedName);
    }

    private void editSelected() {

        Programme programme = getSelectedProgramme();

        if (programme != null) {
            showProgrammeDialog(programme);
        }
    }

    private void deleteSelected() {

        Programme programme = getSelectedProgramme();

        if (programme == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete programme \"" + programme.getName() + "\"?",
                "Confirm Programme Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION
                && mainFrame.deleteProgramme(programme.getId())) {

            refreshData();
        }
    }

    private void showProgrammeDialog(Programme programme) {

        boolean editing = programme != null;

        JTextField nameField = new JTextField(
                editing ? programme.getName() : "",
                25
        );

        JTextArea descriptionArea = new JTextArea(
                editing ? programme.getDescription() : "",
                4,
                25
        );

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        JComboBox<Department> departmentBox = new JComboBox<>(
                mainFrame.getDepartments().toArray(Department[]::new)
        );

        if (editing) {

            for (int i = 0; i < departmentBox.getItemCount(); i++) {

                Department item = departmentBox.getItemAt(i);

                if (item.getId().equals(programme.getDepartmentId())) {
                    departmentBox.setSelectedIndex(i);
                    break;
                }
            }
        }

        JPanel form = new JPanel(
                new GridLayout(0, 1, 5, 5)
        );

        form.add(new JLabel("Programme Name"));
        form.add(nameField);

        form.add(new JLabel("Department"));
        form.add(departmentBox);

        form.add(new JLabel("Description"));
        form.add(new JScrollPane(descriptionArea));

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                editing ? "Edit Programme" : "Add Programme",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        Department selectedDepartment =
                (Department) departmentBox.getSelectedItem();

        if (selectedDepartment == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Create a department before adding a programme.",
                    "Department Required",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        boolean saved = editing
                ? mainFrame.updateProgramme(
                        programme.getId(),
                        nameField.getText(),
                        selectedDepartment.getId(),
                        descriptionArea.getText()
                )
                : mainFrame.addProgramme(
                        nameField.getText(),
                        selectedDepartment.getId(),
                        descriptionArea.getText()
                );

        if (saved) {
            refreshData();
        }
    }
}
