
package ke.ac.kyu.srms.ui;

import ke.ac.kyu.srms.config.ThemeManager;

import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class SidebarPanel extends JPanel {

    private final MainFrame mainFrame;

    // Stores each navigation button using its page identifier.
    private final Map<String, JButton> navigationButtons =
            new LinkedHashMap<>();

    // Active navigation colours.
    private final Color ACTIVE_BACKGROUND = new Color(37, 99, 235);
    private final Color ACTIVE_FOREGROUND = Color.WHITE;

    private final Color NORMAL_FOREGROUND =
            new Color(203, 213, 225);

    private final Color HOVER_FOREGROUND =
            new Color(255, 255, 255);

    private String activePage = "dashboard";

    public SidebarPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.SIDEBAR);
        setPreferredSize(new Dimension(245, 0));

        add(createBrandPanel(), BorderLayout.NORTH);
        add(createNavigationPanel(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        // Dashboard is the initial page.
        setActivePage("dashboard");
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
                FontIcon.of(iconCode, 16, NORMAL_FOREGROUND)
        );

        button.setFont(ThemeManager.FONT_MEDIUM);
        button.setForeground(NORMAL_FOREGROUND);
        button.setBackground(ThemeManager.SIDEBAR);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(14);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 13, 12, 10));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);

        navigationButtons.put(page, button);

        button.addActionListener(e -> {
            setActivePage(page);
            mainFrame.showPage(page);
        });

        button.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!page.equals(activePage)) {
                    button.setBackground(ThemeManager.SIDEBAR_HOVER);
                    button.setForeground(HOVER_FOREGROUND);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!page.equals(activePage)) {
                    button.setBackground(ThemeManager.SIDEBAR);
                    button.setForeground(NORMAL_FOREGROUND);
                }
            }
        });

        panel.add(button);
        panel.add(Box.createVerticalStrut(3));
    }

    /**
     * Updates the persistent active navigation item.
     */
    public void setActivePage(String page) {

        if (page == null || !navigationButtons.containsKey(page)) {
            return;
        }

        activePage = page;

        for (Map.Entry<String, JButton> entry
                : navigationButtons.entrySet()) {

            boolean isActive = entry.getKey().equals(activePage);
            JButton button = entry.getValue();

            if (isActive) {
                button.setBackground(ACTIVE_BACKGROUND);
                button.setForeground(ACTIVE_FOREGROUND);
                button.setIcon(FontIcon.of(
                        getIconForPage(entry.getKey()),
                        16,
                        ACTIVE_FOREGROUND
                ));
            } else {
                button.setBackground(ThemeManager.SIDEBAR);
                button.setForeground(NORMAL_FOREGROUND);
                button.setIcon(FontIcon.of(
                        getIconForPage(entry.getKey()),
                        16,
                        NORMAL_FOREGROUND
                ));
            }
        }
    }

    /**
     * Returns the icon associated with a navigation page.
     */
    private FontAwesomeSolid getIconForPage(String page) {

        return switch (page) {
            case "dashboard" -> FontAwesomeSolid.HOME;
            case "students" -> FontAwesomeSolid.USERS;
            case "registration" -> FontAwesomeSolid.USER_PLUS;
            case "programmes" -> FontAwesomeSolid.BOOK_OPEN;
            case "departments" -> FontAwesomeSolid.BUILDING;
            case "reports" -> FontAwesomeSolid.CHART_LINE;
            case "settings" -> FontAwesomeSolid.COG;
            default -> FontAwesomeSolid.CIRCLE;
        };
    }

    private JPanel createFooter() {

        JPanel footer = new JPanel();

        footer.setBackground(ThemeManager.SIDEBAR);

        footer.setLayout(
                new BoxLayout(footer, BoxLayout.Y_AXIS)
        );

        footer.setBorder(
                new EmptyBorder(15, 15, 20, 15)
        );

        JSeparator separator = new JSeparator();

        separator.setForeground(
                new Color(51, 65, 85)
        );

        JLabel application = new JLabel(
                "Student Registration System"
        );

        application.setFont(
                new Font("Segoe UI", Font.PLAIN, 11)
        );

        application.setForeground(
                new Color(148, 163, 184)
        );

        JLabel version = new JLabel("Version 1.0");

        version.setFont(
                new Font("Segoe UI", Font.PLAIN, 10)
        );

        version.setForeground(
                new Color(100, 116, 139)
        );

        JButton logoutButton = new JButton(
                "Log Out",
                FontIcon.of(
                        FontAwesomeSolid.POWER_OFF,
                        16,
                        new Color(248, 113, 113)
                )
        );

        logoutButton.setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        logoutButton.setForeground(
                new Color(248, 113, 113)
        );

        logoutButton.setBackground(ThemeManager.SIDEBAR);

        logoutButton.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        logoutButton.setIconTextGap(14);

        logoutButton.setBorder(
                new EmptyBorder(12, 13, 12, 10)
        );

        logoutButton.setFocusPainted(false);
        logoutButton.setOpaque(true);

        logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoutButton.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 45)
        );

        logoutButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        logoutButton.setToolTipText(
                "Safely log out of your administrator session"
        );

        logoutButton.getAccessibleContext().setAccessibleName(
                "Log out of Student Registration System"
        );

        logoutButton.addActionListener(e -> mainFrame.logout());

        logoutButton.addMouseListener(
                new java.awt.event.MouseAdapter() {

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {

                logoutButton.setBackground(
                        new Color(127, 29, 29)
                );

                logoutButton.setForeground(Color.WHITE);

                logoutButton.setIcon(
                        FontIcon.of(
                                FontAwesomeSolid.POWER_OFF,
                                16,
                                Color.WHITE
                        )
                );
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {

                logoutButton.setBackground(
                        ThemeManager.SIDEBAR
                );

                logoutButton.setForeground(
                        new Color(248, 113, 113)
                );

                logoutButton.setIcon(
                        FontIcon.of(
                                FontAwesomeSolid.POWER_OFF,
                                16,
                                new Color(248, 113, 113)
                        )
                );
            }
        });

        footer.add(separator);
        footer.add(Box.createVerticalStrut(13));
        footer.add(application);
        footer.add(Box.createVerticalStrut(5));
        footer.add(version);
        footer.add(Box.createVerticalStrut(15));
        footer.add(logoutButton);
        return footer;
    }
}
