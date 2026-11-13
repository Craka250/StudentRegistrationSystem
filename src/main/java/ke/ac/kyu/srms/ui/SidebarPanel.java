
package ke.ac.kyu.srms.ui;

import ke.ac.kyu.srms.config.ThemeManager;

import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SidebarPanel extends JPanel {

    private final MainFrame mainFrame;

    public SidebarPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.SIDEBAR);
        setPreferredSize(new Dimension(245, 0));

        add(createBrandPanel(), BorderLayout.NORTH);
        add(createNavigationPanel(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createBrandPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(ThemeManager.SIDEBAR);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(27, 22, 30, 15));

        JLabel brand = new JLabel("SRMS");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 25));
        brand.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("STUDENT MANAGEMENT");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        subtitle.setForeground(new Color(148, 163, 184));

        panel.add(brand);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitle);

        return panel;
    }

    private JPanel createNavigationPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(ThemeManager.SIDEBAR);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(5, 12, 10, 12));

        addSectionLabel(panel, "OVERVIEW");

        addNavigationButton(
                panel, "Dashboard", FontAwesomeSolid.HOME, "dashboard"
        );

        addSectionLabel(panel, "STUDENT RECORDS");

        addNavigationButton(
                panel, "Students", FontAwesomeSolid.USERS, "students"
        );

        addNavigationButton(
                panel, "Register Student", FontAwesomeSolid.USER_PLUS,
                "registration"
        );

        addSectionLabel(panel, "ACADEMIC STRUCTURE");

        addNavigationButton(
                panel, "Programmes", FontAwesomeSolid.BOOK_OPEN,
                "programmes"
        );

        addNavigationButton(
                panel, "Departments", FontAwesomeSolid.BUILDING,
                "departments"
        );

        addSectionLabel(panel, "SYSTEM");

        addNavigationButton(
                panel, "Reports", FontAwesomeSolid.CHART_LINE, "reports"
        );

        addNavigationButton(
                panel, "Settings", FontAwesomeSolid.COG, "settings"
        );

        return panel;
    }

    private void addSectionLabel(JPanel panel, String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setForeground(new Color(148, 163, 184));
        label.setBorder(new EmptyBorder(18, 10, 9, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
    }

    private void addNavigationButton(
            JPanel panel,
            String text,
            FontAwesomeSolid iconCode,
            String page
    ) {
        JButton button = new JButton(
                text,
                FontIcon.of(iconCode, 16, new Color(203, 213, 225))
        );

        button.setFont(ThemeManager.FONT_MEDIUM);
        button.setForeground(new Color(203, 213, 225));
        button.setBackground(ThemeManager.SIDEBAR);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(14);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 13, 12, 10));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> mainFrame.showPage(page));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(ThemeManager.SIDEBAR_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(ThemeManager.SIDEBAR);
            }
        });

        panel.add(button);
        panel.add(Box.createVerticalStrut(3));
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel();
        footer.setBackground(ThemeManager.SIDEBAR);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(new EmptyBorder(15, 20, 22, 20));

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(51, 65, 85));

        JLabel application = new JLabel("Student Registration System");
        application.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        application.setForeground(new Color(148, 163, 184));

        JLabel version = new JLabel("Version 1.0");
        version.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        version.setForeground(new Color(100, 116, 139));

        footer.add(separator);
        footer.add(Box.createVerticalStrut(15));
        footer.add(application);
        footer.add(Box.createVerticalStrut(5));
        footer.add(version);

        return footer;
    }
}
