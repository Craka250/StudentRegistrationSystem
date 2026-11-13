
package ke.ac.kyu.srms.ui;

import ke.ac.kyu.srms.config.ThemeManager;
import ke.ac.kyu.srms.model.Department;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DepartmentManagementPanel extends JPanel {

    private final MainFrame mainFrame;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField;

    private JButton editButton;
    private JButton deleteButton;

    public DepartmentManagementPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BACKGROUND);

        tableModel = new DefaultTableModel(
                new String[]{"Department", "Description", "Programmes"},
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
                "Search departments..."
        );

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        refreshData();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        refreshData();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        refreshData();
                    }
                }
        );

        refreshData();
        updateActionButtons();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(22, 30, 22, 30));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Department Management");
        title.setFont(ThemeManager.FONT_SUBHEADING);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subtitle = new JLabel(
                "Create, update and manage academic departments."
        );
        subtitle.setFont(ThemeManager.FONT_REGULAR);
        subtitle.setForeground(ThemeManager.TEXT_SECONDARY);

        titles.add(title);
        titles.add(Box.createVerticalStrut(5));
        titles.add(subtitle);

        JButton addButton = new JButton("Add Department");
        stylePrimaryButton(addButton);
        addButton.addActionListener(e -> showDepartmentDialog(null));

        header.add(titles, BorderLayout.WEST);
        header.add(addButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(ThemeManager.BACKGROUND);
        content.setBorder(new EmptyBorder(25, 30, 30, 30));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(new EmptyBorder(15, 18, 15, 18));

        JLabel label = new JLabel("Academic Departments");
        label.setFont(ThemeManager.FONT_MEDIUM);
        label.setForeground(ThemeManager.TEXT_PRIMARY);

        toolbar.add(label, BorderLayout.WEST);
        toolbar.add(searchField, BorderLayout.EAST);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240))
        );

        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 5)
        );
        actions.setBackground(Color.WHITE);
        actions.setBorder(new EmptyBorder(10, 10, 10, 10));

        editButton = new JButton("Edit Selected");
        deleteButton = new JButton("Delete Selected");

        stylePrimaryButton(editButton);
        styleDangerButton(deleteButton);

        editButton.addActionListener(e -> editSelected());
        deleteButton.addActionListener(e -> deleteSelected());

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

        actions.add(editButton);
        actions.add(deleteButton);

        tablePanel.add(actions, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateActionButtons();
            }
        });

        content.add(toolbar, BorderLayout.NORTH);
        content.add(tablePanel, BorderLayout.CENTER);

        return content;
    }

    private void styleTable() {
        table.setRowHeight(42);
        table.setFont(ThemeManager.FONT_REGULAR);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);

        table.getTableHeader().setFont(ThemeManager.FONT_MEDIUM);
        table.getTableHeader().setPreferredSize(
                new Dimension(100, 42)
        );
    }

    private void updateActionButtons() {
        boolean hasSelection = table.getSelectedRow() >= 0;

        if (editButton != null) {
            editButton.setEnabled(hasSelection);
        }

        if (deleteButton != null) {
            deleteButton.setEnabled(hasSelection);
        }
    }

    public void refreshData() {
        tableModel.setRowCount(0);

        String query = searchField.getText()
                .trim()
                .toLowerCase();

        List<Department> departments = mainFrame.getDepartments();

        for (Department department : departments) {

            String name = department.getName() == null
                    ? ""
                    : department.getName();

            String description = department.getDescription() == null
                    ? ""
                    : department.getDescription();

            long programmeCount = mainFrame.getProgrammes().stream()
                    .filter(programme ->
                            programme.getDepartmentId() != null
                            && programme.getDepartmentId().equals(
                                    department.getId()
                            )
                    )
                    .count();

            if (!name.toLowerCase().contains(query)
                    && !description.toLowerCase().contains(query)) {
                continue;
            }

            tableModel.addRow(new Object[]{
                name,
                description,
                programmeCount
            });
        }

        updateActionButtons();
    }

    private Department getSelectedDepartment() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a department first.",
                    "No Department Selected",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return null;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);

        String selectedName = (String) tableModel.getValueAt(
                modelRow, 0
        );

        return mainFrame.getDepartments().stream()
                .filter(department ->
                        department.getName() != null
                        && department.getName().equals(selectedName)
                )
                .findFirst()
                .orElse(null);
    }

    private void editSelected() {
        Department department = getSelectedDepartment();

        if (department != null) {
            showDepartmentDialog(department);
        }
    }

    private void deleteSelected() {
        Department department = getSelectedDepartment();

        if (department == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete department \"" + department.getName() + "\"?",
                "Confirm Department Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION
                && mainFrame.deleteDepartment(department.getId())) {
            refreshData();
        }
    }

    private void showDepartmentDialog(Department department) {
        boolean editing = department != null;

        JTextField nameField = new JTextField(
                editing ? department.getName() : "",
                25
        );

        JTextArea descriptionArea = new JTextArea(
                editing && department.getDescription() != null
                        ? department.getDescription()
                        : "",
                4,
                25
        );

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));

        form.add(new JLabel("Department Name"));
        form.add(nameField);
        form.add(new JLabel("Description"));
        form.add(new JScrollPane(descriptionArea));

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                editing ? "Edit Department" : "Add Department",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        boolean saved = editing
                ? mainFrame.updateDepartment(
                        department.getId(),
                        nameField.getText(),
                        descriptionArea.getText()
                )
                : mainFrame.addDepartment(
                        nameField.getText(),
                        descriptionArea.getText()
                );

        if (saved) {
            refreshData();
        }
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(ThemeManager.FONT_MEDIUM);
        button.setBackground(ThemeManager.PRIMARY);
        button.setForeground(Color.WHITE);

        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);

        button.putClientProperty("Button.arc", 0);
        button.putClientProperty("Component.arc", 0);

        button.setBorder(new EmptyBorder(11, 16, 11, 16));

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
    }

    private void styleDangerButton(JButton button) {
        button.setFont(ThemeManager.FONT_MEDIUM);
        button.setBackground(ThemeManager.DANGER);
        button.setForeground(Color.WHITE);

        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);

        button.putClientProperty("Button.arc", 0);
        button.putClientProperty("Component.arc", 0);

        button.setBorder(new EmptyBorder(11, 16, 11, 16));

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
    }
}
