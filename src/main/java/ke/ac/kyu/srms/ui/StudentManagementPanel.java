
package ke.ac.kyu.srms.ui;

import ke.ac.kyu.srms.config.ThemeManager;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.border.EmptyBorder;

import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import ke.ac.kyu.srms.model.Student;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class StudentManagementPanel extends JPanel {

    private final MainFrame mainFrame;
    private final JTable studentTable;
    private final DefaultTableModel tableModel;
    private final CardLayout tableCardLayout;
    private final JPanel tableContentPanel;
    private final JLabel recordCountLabel;

    private static final Color BORDER_COLOR =
            new Color(226, 232, 240);

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

        tableCardLayout = new CardLayout();
        tableContentPanel = new JPanel(tableCardLayout);
        tableContentPanel.setBackground(Color.WHITE);

        recordCountLabel = new JLabel("0 student records");

        initializeTableContent();

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
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

        JButton registerButton = new JButton(
                "Register New Student",
                FontIcon.of(
                        FontAwesomeSolid.USER,
                        15,
                        Color.WHITE
                )
        );

        registerButton.setFont(ThemeManager.FONT_MEDIUM);
        registerButton.setBackground(ThemeManager.PRIMARY);
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setBorder(
                new EmptyBorder(12, 17, 12, 17)
        );
        registerButton.addActionListener(
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

    private JPanel createTableSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(
                BorderFactory.createLineBorder(BORDER_COLOR)
        );

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

        tableContentPanel.add(
                createEmptyState(),
                "empty"
        );

        tableContentPanel.add(
                tablePanel,
                "table"
        );

        section.add(tableHeading, BorderLayout.NORTH);
        section.add(tableContentPanel, BorderLayout.CENTER);

        return section;
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
                FontIcon.of(
                        FontAwesomeSolid.USER,
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

        JButton registerButton = new JButton("Register a Student");
        registerButton.setFont(ThemeManager.FONT_MEDIUM);
        registerButton.setBackground(ThemeManager.PRIMARY);
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setBorder(
                new EmptyBorder(10, 16, 10, 16)
        );
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.addActionListener(
                e -> mainFrame.showPage("registration")
        );

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

    private void initializeTableContent() {
        if (tableModel.getRowCount() == 0) {
            tableCardLayout.show(tableContentPanel, "empty");
        } else {
            tableCardLayout.show(tableContentPanel, "table");
        }

        recordCountLabel.setText(
                tableModel.getRowCount() + " student records"
        );
    }
    
    /**
    * Adds a newly confirmed student to the management table.
    *
    * @param student the confirmed student record
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

       // Insert the new student into the table model.
       tableModel.addRow(rowData);

       // Refresh the empty-state/table display and record count.
       initializeTableContent();

       // Refresh the table interface.
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
}
