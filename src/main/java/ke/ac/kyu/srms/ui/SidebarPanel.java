
package ke.ac.kyu.srms.ui;

import ke.ac.kyu.srms.config.ThemeManager;

import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SidebarPanel extends JPanel {

    private final MainFrame mainFrame;

    private JButton activeButton;

    public SidebarPanel(MainFrame mainFrame) {

        this.mainFrame = mainFrame;

        setPreferredSize(new Dimension(245, 0));

        setBackground(ThemeManager.SIDEBAR);

        setLayout(new BorderLayout());

        add(createBrandSection(), BorderLayout.NORTH);

        add(createNavigationSection(), BorderLayout.CENTER);

        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createBrandSection() {

        JPanel panel = new JPanel();

        panel.setOpaque(false);

        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        30, 22, 30, 22
                )
        );

        JLabel brand = new JLabel("SRMS");

        brand.setFont(new Font(
                "Segoe UI", Font.BOLD, 27
        ));

        brand.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "STUDENT MANAGEMENT"
        );

        subtitle.setFont(new Font(
                "Segoe UI", Font.BOLD, 10
        ));

        subtitle.setForeground(
                new Color(148, 163, 184)
        );

        panel.add(brand);

        panel.add(Box.createVerticalStrut(5));

        panel.add(subtitle);

        return panel;
    }

    private JPanel createNavigationSection() {

        JPanel panel = new JPanel();

        panel.setOpaque(false);

        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        5, 12, 10, 12
                )
        );

        JLabel sectionTitle = new JLabel("MAIN MENU");

        sectionTitle.setForeground(
                new Color(148, 163, 184)
        );

        sectionTitle.setFont(new Font(
                "Segoe UI", Font.BOLD, 10
        ));

        sectionTitle.setBorder(
                BorderFactory.createEmptyBorder(
                        10, 12, 15, 0
                )
        );

        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(sectionTitle);

        addNavigationButton(
                panel,
                "Dashboard",
                FontAwesomeSolid.HOME,
                "dashboard",
                true
        );

        addNavigationButton(
                panel,
                "Students",
                FontAwesomeSolid.USERS,
                "students",
                false
        );

        addNavigationButton(
                panel,
                "Register Student",
                FontAwesomeSolid.USER_PLUS,
                "registration",
                false
        );

        addNavigationButton(
                panel,
                "Reports",
                FontAwesomeSolid.CHART_LINE,
                "reports",
                false
        );

        addNavigationButton(
                panel,
                "Settings",
                FontAwesomeSolid.COG,
                "settings",
                false
        );

        return panel;
    }

    private void addNavigationButton(
            JPanel container,
            String text,
            FontAwesomeSolid iconCode,
            String page,
            boolean selected
    ) {

        JButton button = new JButton(text);

        button.setIcon(
                FontIcon.of(
                        iconCode,
                        18,
                        new Color(203, 213, 225)
                )
        );

        button.setHorizontalAlignment(SwingConstants.LEFT);

        button.setIconTextGap(15);

        button.setFont(ThemeManager.FONT_REGULAR);

        button.setForeground(Color.WHITE);

        button.setBackground(
                selected
                        ? ThemeManager.PRIMARY
                        : ThemeManager.SIDEBAR
        );

        button.setBorder(
                BorderFactory.createEmptyBorder(
                        13, 14, 13, 12
                )
        );

        button.setFocusPainted(false);

        button.setOpaque(true);

        button.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 48)
        );

        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.addActionListener(e -> {

            if (activeButton != null) {

                activeButton.setBackground(
                        ThemeManager.SIDEBAR
                );
            }

            activeButton = button;

            button.setBackground(ThemeManager.PRIMARY);

            mainFrame.showPage(page);
        });

        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {

                if (button != activeButton) {

                    button.setBackground(
                            ThemeManager.SIDEBAR_HOVER
                    );
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {

                if (button != activeButton) {

                    button.setBackground(
                            ThemeManager.SIDEBAR
                    );
                }
            }
        });

        container.add(button);

        container.add(Box.createVerticalStrut(7));

        if (selected) {
            activeButton = button;
        }
    }

    private JPanel createFooter() {

        JPanel panel = new JPanel();

        panel.setOpaque(false);

        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        15, 22, 25, 15
                )
        );

        JLabel application = new JLabel("SRMS v1.0");

        application.setForeground(
                new Color(203, 213, 225)
        );

        application.setFont(ThemeManager.FONT_MEDIUM);

        JLabel institution = new JLabel(
                "Student Administration"
        );

        institution.setForeground(
                new Color(148, 163, 184)
        );

        institution.setFont(new Font(
                "Segoe UI", Font.PLAIN, 11
        ));

        panel.add(application);

        panel.add(Box.createVerticalStrut(5));

        panel.add(institution);

        return panel;
    }
}
