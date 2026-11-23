
package ke.ac.kyu.srms.ui;

import ke.ac.kyu.srms.config.ThemeManager;
import ke.ac.kyu.srms.model.Department;
import ke.ac.kyu.srms.model.Programme;
import ke.ac.kyu.srms.model.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReportsPanel extends JPanel {

    private final MainFrame mainFrame;

    private final JPanel content = new JPanel();
    private final JPanel statisticsPanel = new JPanel(new GridLayout(1, 4, 14, 14));
    private final JPanel chartsPanel = new JPanel(new GridBagLayout());
    private final List<JPanel> chartCards = new ArrayList<>();
    private JScrollPane reportsScrollPane;

    private List<Student> students = new ArrayList<>();
    private List<Department> departments = new ArrayList<>();
    private List<Programme> programmes = new ArrayList<>();

    private JLabel totalStudentsValue;
    private JLabel totalDepartmentsValue;
    private JLabel totalProgrammesValue;
    private JLabel averageAgeValue;

    private JLabel reportStatus;

    private Map<String, Integer> departmentCounts = new LinkedHashMap<>();
    private Map<String, Integer> programmeCounts = new LinkedHashMap<>();
    private Map<String, Integer> academicYearCounts = new LinkedHashMap<>();
    private Map<String, Integer> genderCounts = new LinkedHashMap<>();
    private Map<String, Integer> ageGroupCounts = new LinkedHashMap<>();
    private Map<String, Integer> admissionCounts = new TreeMap<>();

    private int validAgeCount;
    private int youngestAge;
    private int oldestAge;
    private double averageAge;

    public ReportsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BACKGROUND);

        buildInterface();
        refreshData();
    }

    private void buildInterface() {

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(24, 28, 18, 28));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Reports & Analytics");
        title.setFont(ThemeManager.FONT_HEADING);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subtitle = new JLabel(
                "Insights into student enrolment and academic distribution"
        );
        subtitle.setFont(ThemeManager.FONT_REGULAR);
        subtitle.setForeground(ThemeManager.TEXT_SECONDARY);

        heading.add(title);
        heading.add(Box.createVerticalStrut(6));
        heading.add(subtitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton refreshButton = new JButton("Refresh");
        JButton exportButton = new JButton("Export CSV");

        styleButton(refreshButton, false);
        styleButton(exportButton, true);

        refreshButton.addActionListener(e -> refreshData());
        exportButton.addActionListener(e -> exportReport());

        actions.add(refreshButton);
        actions.add(exportButton);

        header.add(heading, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        statisticsPanel.setOpaque(false);

        chartsPanel.setOpaque(false);

        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ThemeManager.BACKGROUND);
        content.setBorder(new EmptyBorder(12, 28, 28, 28));

        content.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Centre the analytics content and prevent it from growing excessively wide.
        JPanel centeredContent = new JPanel(new GridBagLayout());
        centeredContent.setBackground(ThemeManager.BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(0, 0, 0, 0);

        centeredContent.add(content, gbc);

        reportsScrollPane = new JScrollPane(centeredContent);
        reportsScrollPane.setBorder(null);
        reportsScrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        reportsScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        reportsScrollPane.getViewport().setBackground(
                ThemeManager.BACKGROUND
        );

        reportsScrollPane.getVerticalScrollBar().setUnitIncrement(18);

        // Automatically adjust the number of columns when the window is resized.
        reportsScrollPane.getViewport().addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        updateResponsiveLayout();
                    }
                }
        );

        reportStatus = new JLabel("Analytics are based on registered student records.");
        reportStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reportStatus.setForeground(ThemeManager.TEXT_SECONDARY);
        reportStatus.setBorder(new EmptyBorder(0, 28, 14, 28));

        add(header, BorderLayout.NORTH);
        add(reportsScrollPane, BorderLayout.CENTER);
        add(reportStatus, BorderLayout.SOUTH);
    }

    private void updateResponsiveLayout() {

        if (reportsScrollPane == null) {
            return;
        }

        int availableWidth = reportsScrollPane.getViewport().getWidth();

        if (availableWidth <= 0) {
            return;
        }

        // Statistics: four columns on wide screens, two on smaller screens.
        int statisticColumns = availableWidth < 850 ? 2 : 4;

        statisticsPanel.setLayout(
                new GridLayout(0, statisticColumns, 14, 14)
        );

        // Charts: two independent columns on wide screens.
        // Narrow screens display all charts in one column.
        int chartColumns = availableWidth < 760 ? 1 : 2;

        chartsPanel.removeAll();
        chartsPanel.setLayout(new GridBagLayout());

        List<JPanel> columns = new ArrayList<>();

        for (int i = 0; i < chartColumns; i++) {

            JPanel column = new JPanel();

            column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
            column.setOpaque(false);

            columns.add(column);
        }

        // Distribute cards between columns without forcing equal heights.
        for (int i = 0; i < chartCards.size(); i++) {

            int columnIndex = chartColumns == 1 ? 0 : i % chartColumns;

            JPanel card = chartCards.get(i);

            card.setAlignmentX(Component.LEFT_ALIGNMENT);

            columns.get(columnIndex).add(card);

            if (i < chartCards.size() - chartColumns) {
                columns.get(columnIndex).add(Box.createVerticalStrut(14));
            }
        }

        // Place each independent column in the dashboard.
        for (int i = 0; i < columns.size(); i++) {

            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = i;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTH;
            gbc.insets = new Insets(0, 0, 0, 8);

            chartsPanel.add(columns.get(i), gbc);
        }

        statisticsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        chartsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.revalidate();
        content.repaint();

        chartsPanel.revalidate();
        chartsPanel.repaint();
    }

    private void styleButton(JButton button, boolean primary) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 17, 10, 17));

        if (primary) {
            button.setBackground(ThemeManager.PRIMARY);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(ThemeManager.TEXT_PRIMARY);
        }

        button.setOpaque(true);
    }

    /**
     * Reloads the latest data held by MainFrame and rebuilds the analytics.
     */
    public void refreshData() {

        students = new ArrayList<>(mainFrame.getStudents());
        departments = new ArrayList<>(mainFrame.getDepartments());
        programmes = new ArrayList<>(mainFrame.getProgrammes());

        calculateAnalytics();
        rebuildDashboard();

        reportStatus.setText(
                "Showing analytics for " + students.size()
                + " registered student record(s)."
        );
    }

    private void calculateAnalytics() {

        departmentCounts = new LinkedHashMap<>();
        programmeCounts = new LinkedHashMap<>();
        academicYearCounts = new LinkedHashMap<>();
        genderCounts = new LinkedHashMap<>();
        ageGroupCounts = new LinkedHashMap<>();
        admissionCounts = new TreeMap<>();

        // Include departments and programmes even when they have no students.
        for (Department department : departments) {
            departmentCounts.put(department.getName(), 0);
        }

        for (Programme programme : programmes) {
            programmeCounts.put(programme.getName(), 0);
        }

        for (int year = 1; year <= 6; year++) {
            academicYearCounts.put("Year " + year, 0);
        }

        ageGroupCounts.put("Below 18", 0);
        ageGroupCounts.put("18 - 20", 0);
        ageGroupCounts.put("21 - 24", 0);
        ageGroupCounts.put("25 - 29", 0);
        ageGroupCounts.put("30 and above", 0);
        ageGroupCounts.put("Unknown", 0);

        LocalDate today = LocalDate.now();

        int totalAge = 0;
        validAgeCount = 0;
        youngestAge = Integer.MAX_VALUE;
        oldestAge = Integer.MIN_VALUE;

        for (Student student : students) {

            // Department enrolment.
            incrementMatching(departmentCounts, student.getDepartment());

            // Programme enrolment.
            incrementMatching(programmeCounts, student.getProgramme());

            // Academic year distribution.
            int year = student.getYearOfStudy();

            if (year >= 1 && year <= 6) {
                String key = "Year " + year;
                academicYearCounts.put(
                        key,
                        academicYearCounts.get(key) + 1
                );
            } else {
                increment(academicYearCounts, "Other");
            }

            // Gender distribution.
            String gender = student.getGender();

            if (gender == null || gender.isBlank()) {
                gender = "Unspecified";
            } else {
                gender = gender.trim();
            }

            increment(genderCounts, gender);

            // Age distribution.
            LocalDate dob = student.getDateOfBirth();

            if (dob == null || dob.isAfter(today)) {
                increment(ageGroupCounts, "Unknown");
            } else {
                int age = Period.between(dob, today).getYears();

                totalAge += age;
                validAgeCount++;

                youngestAge = Math.min(youngestAge, age);
                oldestAge = Math.max(oldestAge, age);

                if (age < 18) {
                    increment(ageGroupCounts, "Below 18");
                } else if (age <= 20) {
                    increment(ageGroupCounts, "18 - 20");
                } else if (age <= 24) {
                    increment(ageGroupCounts, "21 - 24");
                } else if (age <= 29) {
                    increment(ageGroupCounts, "25 - 29");
                } else {
                    increment(ageGroupCounts, "30 and above");
                }
            }

            // Admissions grouped by calendar year.
            LocalDate admissionDate = student.getAdmissionDate();

            if (admissionDate != null) {
                String admissionYear = String.valueOf(
                        admissionDate.getYear()
                );
                increment(admissionCounts, admissionYear);
            }
        }

        averageAge = validAgeCount == 0
                ? 0
                : (double) totalAge / validAgeCount;
    }

    private void increment(Map<String, Integer> map, String key) {
        map.put(key, map.getOrDefault(key, 0) + 1);
    }

    private void incrementMatching(
            Map<String, Integer> map,
            String suppliedName
    ) {
        if (suppliedName == null || suppliedName.isBlank()) {
            increment(map, "Unassigned");
            return;
        }

        for (String existing : map.keySet()) {
            if (existing.equalsIgnoreCase(suppliedName.trim())) {
                increment(map, existing);
                return;
            }
        }

        increment(map, suppliedName.trim());
    }

    private void rebuildDashboard() {

        content.removeAll();

        statisticsPanel.removeAll();
        chartsPanel.removeAll();

        totalStudentsValue = createStatisticValue();
        totalDepartmentsValue = createStatisticValue();
        totalProgrammesValue = createStatisticValue();
        averageAgeValue = createStatisticValue();

        statisticsPanel.add(createStatisticCard(
                "Total Students",
                String.valueOf(students.size()),
                "Registered student records",
                new Color(37, 99, 235)
        ));

        statisticsPanel.add(createStatisticCard(
                "Departments",
                String.valueOf(departments.size()),
                "Academic departments",
                new Color(14, 165, 233)
        ));

        statisticsPanel.add(createStatisticCard(
                "Programmes",
                String.valueOf(programmes.size()),
                "Available academic programmes",
                new Color(139, 92, 246)
        ));

        statisticsPanel.add(createStatisticCard(
                "Average Age",
                validAgeCount == 0
                        ? "N/A"
                        : String.format("%.1f years", averageAge),
                "Based on valid dates of birth",
                new Color(22, 163, 74)
        ));

        statisticsPanel.setMaximumSize(
                new Dimension(1180, 240)
        );

        statisticsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(statisticsPanel);
        content.add(Box.createVerticalStrut(20));

        chartCards.clear();
        chartsPanel.removeAll();

        chartCards.add(createChartCard(
                "Students by Department",
                "Enrolment distribution across departments",
                departmentCounts
        ));

        chartCards.add(createChartCard(
                "Students by Programme",
                "Enrolment distribution across programmes",
                programmeCounts
        ));

        chartCards.add(createChartCard(
                "Academic Year Distribution",
                "Number of students in each year of study",
                academicYearCounts
        ));

        chartCards.add(createChartCard(
                "Gender Distribution",
                "Student records grouped by gender",
                genderCounts
        ));

        chartCards.add(createChartCard(
                "Age Distribution",
                "Students grouped according to their current age",
                ageGroupCounts
        ));

        chartCards.add(createChartCard(
                "Admission Trends",
                "Student admissions by calendar year",
                admissionCounts
        ));

        chartsPanel.setMaximumSize(
                new Dimension(1180, Integer.MAX_VALUE)
        );

        chartsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(chartsPanel);

        chartsPanel.setMaximumSize(
                new Dimension(1180, Integer.MAX_VALUE)
        );

        chartsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(chartsPanel);

        content.add(Box.createVerticalStrut(18));

        JPanel ageSummary = createAgeSummary();
        ageSummary.setAlignmentX(Component.CENTER_ALIGNMENT);
        ageSummary.setMaximumSize(new Dimension(1180, 105));

        content.add(ageSummary);

        // Apply the responsive layout after rebuilding the cards.
        updateResponsiveLayout();

        content.revalidate();
        content.repaint();
    }

    private JLabel createStatisticValue() {
        JLabel label = new JLabel("0");
        label.setFont(new Font("Segoe UI", Font.BOLD, 25));
        label.setForeground(ThemeManager.TEXT_PRIMARY);
        return label;
    }

    private JPanel createStatisticCard(
            String title,
            String value,
            String description,
            Color accent
    ) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(14, 16, 14, 14)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(ThemeManager.TEXT_SECONDARY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 25));
        valueLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descriptionLabel.setForeground(ThemeManager.TEXT_SECONDARY);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(descriptionLabel);

        return card;
    }

    private JPanel createChartCard(
            String title,
            String description,
            Map<String, Integer> data
    ) {

        // Display only categories with records, up to five categories.
        Map<String, Integer> displayData = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry : data.entrySet()) {

            if (entry.getValue() > 0 && displayData.size() < 5) {
                displayData.put(entry.getKey(), entry.getValue());
            }
        }

        JPanel card = new JPanel(new BorderLayout(0, 8));

        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(13, 15, 10, 15));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descriptionLabel.setForeground(ThemeManager.TEXT_SECONDARY);

        heading.add(titleLabel);
        heading.add(Box.createVerticalStrut(4));
        heading.add(descriptionLabel);

        ChartPanel chart = new ChartPanel(displayData);

        card.add(heading, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);

        int cardHeight = chart.getPreferredSize().height + 65;

        card.setPreferredSize(new Dimension(420, cardHeight));
        card.setMinimumSize(new Dimension(250, cardHeight));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, cardHeight));

        return card;
    }

    private JPanel createAgeSummary() {

        JPanel panel = new JPanel(new GridLayout(1, 3, 12, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String youngest = validAgeCount == 0
                ? "N/A"
                : youngestAge + " years";

        String oldest = validAgeCount == 0
                ? "N/A"
                : oldestAge + " years";

        panel.add(createSmallMetric("Youngest Student", youngest));
        panel.add(createSmallMetric("Oldest Student", oldest));
        panel.add(createSmallMetric(
                "Valid Age Records",
                String.valueOf(validAgeCount)
        ));

        return panel;
    }

    private JPanel createSmallMetric(String title, String value) {

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        heading.setForeground(ThemeManager.TEXT_SECONDARY);

        JLabel result = new JLabel(value);
        result.setFont(new Font("Segoe UI", Font.BOLD, 19));
        result.setForeground(ThemeManager.TEXT_PRIMARY);

        panel.add(heading);
        panel.add(Box.createVerticalStrut(7));
        panel.add(result);

        return panel;
    }

    /**
     * Exports the aggregated analytics to a CSV file.
     */
    private void exportReport() {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export SRMS Analytics");
        chooser.setSelectedFile(
                new File("SRMS_Analytics_Report.csv")
        );

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        List<String> rows = new ArrayList<>();

        rows.add("SRMS STUDENT ANALYTICS REPORT");
        rows.add("Generated On," + LocalDate.now());
        rows.add("");
        rows.add("OVERVIEW");
        rows.add("Metric,Value");
        rows.add("Total Students," + students.size());
        rows.add("Total Departments," + departments.size());
        rows.add("Total Programmes," + programmes.size());
        rows.add("Average Age," + (
                validAgeCount == 0
                        ? "N/A"
                        : String.format("%.1f", averageAge)
        ));
        rows.add("Youngest Age," + (
                validAgeCount == 0 ? "N/A" : youngestAge
        ));
        rows.add("Oldest Age," + (
                validAgeCount == 0 ? "N/A" : oldestAge
        ));

        addCsvSection(rows, "DEPARTMENT ENROLMENT", departmentCounts);
        addCsvSection(rows, "PROGRAMME ENROLMENT", programmeCounts);
        addCsvSection(rows, "ACADEMIC YEAR DISTRIBUTION", academicYearCounts);
        addCsvSection(rows, "GENDER DISTRIBUTION", genderCounts);
        addCsvSection(rows, "AGE DISTRIBUTION", ageGroupCounts);
        addCsvSection(rows, "ADMISSION TRENDS", admissionCounts);

        try {
            Files.write(
                    file.toPath(),
                    rows,
                    StandardCharsets.UTF_8
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Analytics exported successfully.\n\n"
                    + file.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IOException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "The report could not be exported.\n\n"
                    + ex.getMessage(),
                    "Export Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void addCsvSection(
            List<String> rows,
            String heading,
            Map<String, Integer> data
    ) {

        rows.add("");
        rows.add(heading);
        rows.add("Category,Students,Percentage");

        int total = data.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        for (Map.Entry<String, Integer> entry : data.entrySet()) {

            double percentage = total == 0
                    ? 0
                    : (entry.getValue() * 100.0) / total;

            rows.add(
                    csv(entry.getKey()) + ","
                    + entry.getValue() + ","
                    + String.format("%.2f%%", percentage)
            );
        }
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    /**
     * Lightweight Java2D horizontal bar chart.
     */

    private static class ChartPanel extends JPanel {

        private final Map<String, Integer> data;

        private final Color[] colors = {
            new Color(37, 99, 235),
            new Color(14, 165, 233),
            new Color(139, 92, 246),
            new Color(22, 163, 74),
            new Color(217, 119, 6),
            new Color(219, 39, 119)
        };

    ChartPanel(Map<String, Integer> data) {

        this.data = new LinkedHashMap<>(data);

        setOpaque(false);

        int chartHeight = this.data.isEmpty()
                ? 45
                : this.data.size() * 42 + 8;

        setPreferredSize(new Dimension(350, chartHeight));
        setMinimumSize(new Dimension(200, chartHeight));
    }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g = (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int width = getWidth();
            int height = getHeight();

            int left = Math.min(190, Math.max(125, width / 3));
            int right = 40;
            int top = 12;

            if (data.isEmpty()) {
                drawEmpty(g, width, height);
                g.dispose();
                return;
            }

            int maxValue = data.values().stream()
                    .mapToInt(Integer::intValue)
                    .max()
                    .orElse(0);

            if (maxValue == 0) {
                drawEmpty(g, width, height);
                g.dispose();
                return;
            }

            Font labelFont = new Font("Segoe UI", Font.PLAIN, 11);
            Font valueFont = new Font("Segoe UI", Font.BOLD, 11);

            FontMetrics labelMetrics = g.getFontMetrics(labelFont);

            int labelWidth = left - 12;
            int barWidth = Math.max(20, width - left - right);

            int index = 0;
            int currentY = top;

            for (Map.Entry<String, Integer> entry : data.entrySet()) {

                String label = entry.getKey();
                int value = entry.getValue();

                // Wrap long department/programme names instead of truncating them.
                List<String> lines = wrapText(
                        label,
                        labelMetrics,
                        labelWidth
                );

                int lineHeight = labelMetrics.getHeight();

                int rowHeight = Math.max(
                        30,
                        lines.size() * lineHeight + 7
                );

                if (currentY + rowHeight > height) {
                    break;
                }

                // Draw the full label over multiple lines.
                g.setFont(labelFont);
                g.setColor(ThemeManager.TEXT_SECONDARY);

                int textY = currentY + 12;

                for (String line : lines) {
                    g.drawString(line, 0, textY);
                    textY += lineHeight;
                }

                // Draw the background bar.
                int barY = currentY + Math.max(
                        5,
                        (rowHeight - 13) / 2
                );

                g.setColor(new Color(241, 245, 249));

                g.fillRoundRect(
                        left,
                        barY,
                        barWidth,
                        13,
                        7,
                        7
                );

                // Draw the actual enrolment bar.
                int filledWidth = (int) (
                        (value / (double) maxValue) * barWidth
                );

                if (value > 0) {

                    g.setColor(colors[index % colors.length]);

                    g.fillRoundRect(
                            left,
                            barY,
                            Math.max(5, filledWidth),
                            13,
                            7,
                            7
                    );
                }

                // Draw the count at the end of the bar.
                g.setFont(valueFont);
                g.setColor(ThemeManager.TEXT_PRIMARY);

                FontMetrics valueMetrics = g.getFontMetrics();

                int valueY = barY
                        + (13 - valueMetrics.getHeight()) / 2
                        + valueMetrics.getAscent();

                g.drawString(
                        String.valueOf(value),
                        left + barWidth + 7,
                        valueY
                );

                currentY += rowHeight + 2;
                index++;
            }

            g.dispose();
        }

        /**
         * Splits long labels into readable lines.
         */
        private List<String> wrapText(
                String text,
                FontMetrics metrics,
                int maxWidth
        ) {

            List<String> lines = new ArrayList<>();

            if (text == null || text.isBlank()) {
                lines.add("Unspecified");
                return lines;
            }

            StringBuilder currentLine = new StringBuilder();

            for (String word : text.trim().split("\\s+")) {

                String candidate = currentLine.length() == 0
                        ? word
                        : currentLine + " " + word;

                if (metrics.stringWidth(candidate) <= maxWidth) {

                    if (currentLine.length() > 0) {
                        currentLine.append(" ");
                    }

                    currentLine.append(word);

                } else {

                    if (currentLine.length() > 0) {
                        lines.add(currentLine.toString());
                        currentLine.setLength(0);
                    }

                    // Handle a single unusually long word.
                    if (metrics.stringWidth(word) > maxWidth) {

                        StringBuilder part = new StringBuilder();

                        for (char character : word.toCharArray()) {

                            String candidatePart =
                                    part.toString() + character;

                            if (metrics.stringWidth(candidatePart) > maxWidth
                                    && part.length() > 0) {

                                lines.add(part.toString());
                                part.setLength(0);
                            }

                            part.append(character);
                        }

                        currentLine.append(part);

                    } else {
                        currentLine.append(word);
                    }
                }
            }

            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }

            return lines;
        }

        private void drawEmpty(
                Graphics2D g,
                int width,
                int height
        ) {

            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.setColor(ThemeManager.TEXT_SECONDARY);

            String message = "No records available";

            FontMetrics metrics = g.getFontMetrics();

            g.drawString(
                    message,
                    Math.max(
                            0,
                            (width - metrics.stringWidth(message)) / 2
                    ),
                    height / 2
            );
        }
    }
}
