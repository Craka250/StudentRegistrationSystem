
package ke.ac.kyu.srms.ui;

import ke.ac.kyu.srms.config.ThemeManager;
import ke.ac.kyu.srms.model.Admin;
import ke.ac.kyu.srms.service.AdminAuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.concurrent.ExecutionException;

public class LoginFrame extends JFrame {

    private final AdminAuthService authService;
    private final Consumer<Admin> onLoginSuccess;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private JCheckBox showPasswordCheckBox;

    private char defaultEchoChar;

    public LoginFrame(Consumer<Admin> onLoginSuccess) {

        this.authService = new AdminAuthService();
        this.onLoginSuccess = onLoginSuccess;

        initializeFrame();
        initializeComponents();
    }

    private void initializeFrame() {

        setTitle("Administrator Login | Student Registration System");
        AppIcon.apply(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setMinimumSize(new Dimension(850, 560));
        setSize(1000, 650);
        setLocationRelativeTo(null);

        setResizable(true);
    }

    private void initializeComponents() {

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ThemeManager.BACKGROUND);

        mainPanel.add(createBrandPanel(), BorderLayout.WEST);
        mainPanel.add(createLoginPanel(), BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createBrandPanel() {

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setPreferredSize(new Dimension(390, 650));
        panel.setBackground(ThemeManager.SIDEBAR);
        panel.setBorder(new EmptyBorder(45, 40, 45, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel logo = new JLabel("SRMS");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        logo.setForeground(Color.WHITE);

        gbc.gridy = 0;
        panel.add(logo, gbc);

        JLabel institution = new JLabel(
                "EXAMPLE UNIVERSITY"
        );

        institution.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        institution.setForeground(
                new Color(147, 197, 253)
        );

        gbc.gridy = 1;
        gbc.insets = new Insets(8, 0, 0, 0);
        panel.add(institution, gbc);

        JLabel heading = new JLabel(
                "<html>Student<br>Registration<br>Management<br>System</html>"
        );

        heading.setFont(
                new Font("Segoe UI", Font.BOLD, 29)
        );

        heading.setForeground(Color.WHITE);

        gbc.gridy = 2;
        gbc.insets = new Insets(65, 0, 0, 0);
        panel.add(heading, gbc);

        JLabel description = new JLabel(
                "<html><div style='width:270px;'>"
                + "A centralized platform for managing student "
                + "records, academic programmes and university "
                + "registration activities."
                + "</div></html>"
        );

        description.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        description.setForeground(
                new Color(203, 213, 225)
        );

        gbc.gridy = 3;
        gbc.insets = new Insets(18, 0, 0, 0);
        panel.add(description, gbc);

        JPanel featurePanel = new JPanel();
        featurePanel.setOpaque(false);
        featurePanel.setLayout(
                new BoxLayout(featurePanel, BoxLayout.Y_AXIS)
        );

        featurePanel.add(createFeatureLabel(
                "Secure administrator authentication"
        ));

        featurePanel.add(Box.createVerticalStrut(15));

        featurePanel.add(createFeatureLabel(
                "Centralized student management"
        ));

        featurePanel.add(Box.createVerticalStrut(15));

        featurePanel.add(createFeatureLabel(
                "Academic programme management"
        ));

        gbc.gridy = 4;
        gbc.insets = new Insets(45, 0, 0, 0);
        panel.add(featurePanel, gbc);

        JLabel footer = new JLabel(
                "© 2026 EXAMPLE University | SRMS"
        );

        footer.setFont(
                new Font("Segoe UI", Font.PLAIN, 11)
        );

        footer.setForeground(
                new Color(148, 163, 184)
        );

        gbc.gridy = 5;
        gbc.insets = new Insets(50, 0, 0, 0);
        panel.add(footer, gbc);

        return panel;
    }

    private JLabel createFeatureLabel(String text) {

        JLabel label = new JLabel("✓  " + text);

        label.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        label.setForeground(
                new Color(226, 232, 240)
        );

        return label;
    }

    private JPanel createLoginPanel() {

        JPanel container = new JPanel(new GridBagLayout());

        container.setBackground(ThemeManager.BACKGROUND);
        container.setBorder(
                new EmptyBorder(35, 35, 35, 35)
        );

        JPanel card = new JPanel();

        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS)
        );

        card.setBackground(ThemeManager.SURFACE);
        card.setBorder(
                new EmptyBorder(40, 42, 40, 42)
        );

        card.setPreferredSize(new Dimension(430, 440));

        JLabel welcomeLabel = new JLabel("Welcome back");

        welcomeLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 27)
        );

        welcomeLabel.setForeground(
                ThemeManager.TEXT_PRIMARY
        );

        JLabel subtitle = new JLabel(
                "Sign in to access the administration portal."
        );

        subtitle.setFont(ThemeManager.FONT_REGULAR);
        subtitle.setForeground(ThemeManager.TEXT_SECONDARY);

        card.add(welcomeLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(32));

        JLabel usernameLabel = createFieldLabel("USERNAME");

        usernameField = new JTextField();
        styleTextField(usernameField);

        card.add(usernameLabel);
        card.add(Box.createVerticalStrut(9));
        card.add(usernameField);

        card.add(Box.createVerticalStrut(23));

        JLabel passwordLabel = createFieldLabel("PASSWORD");

        passwordField = new JPasswordField();
        styleTextField(passwordField);

        defaultEchoChar = passwordField.getEchoChar();

        card.add(passwordLabel);
        card.add(Box.createVerticalStrut(9));
        card.add(passwordField);

        showPasswordCheckBox = new JCheckBox("Show password");
        showPasswordCheckBox.setOpaque(false);
        showPasswordCheckBox.setFocusable(false);
        showPasswordCheckBox.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        showPasswordCheckBox.setForeground(
                ThemeManager.TEXT_SECONDARY
        );

        showPasswordCheckBox.addActionListener(event -> {

            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar(defaultEchoChar);
            }
        });

        card.add(Box.createVerticalStrut(9));
        card.add(showPasswordCheckBox);

        card.add(Box.createVerticalStrut(25));

        loginButton = new JButton("Sign In");

        loginButton.setFont(
                new Font("Segoe UI", Font.BOLD, 15)
        );

        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(ThemeManager.PRIMARY);

        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 48)
        );

        loginButton.setPreferredSize(
                new Dimension(340, 48)
        );

        loginButton.addActionListener(
                event -> authenticate()
        );

        card.add(loginButton);

        statusLabel = new JLabel(" ");

        statusLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        statusLabel.setForeground(ThemeManager.DANGER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(Box.createVerticalStrut(15));
        card.add(statusLabel);

        JLabel securityLabel = new JLabel(
                "Authorized administrators only"
        );

        securityLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 11)
        );

        securityLabel.setForeground(
                ThemeManager.TEXT_SECONDARY
        );

        securityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalStrut(15));
        card.add(securityLabel);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        container.add(card, gbc);

        getRootPane().setDefaultButton(loginButton);

        return container;
    }

    private JLabel createFieldLabel(String text) {

        JLabel label = new JLabel(text);

        label.setFont(
                new Font("Segoe UI", Font.BOLD, 11)
        );

        label.setForeground(
                ThemeManager.TEXT_PRIMARY
        );

        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    private void styleTextField(JTextField field) {

        field.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        field.setForeground(
                ThemeManager.TEXT_PRIMARY
        );

        field.setBackground(Color.WHITE);

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(203, 213, 225)
                        ),
                        new EmptyBorder(10, 12, 10, 12)
                )
        );

        field.setPreferredSize(
                new Dimension(340, 45)
        );

        field.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 45)
        );

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void authenticate() {

        String username = usernameField.getText().trim();

        char[] password = passwordField.getPassword();

        if (username.isBlank() || password.length == 0) {

            statusLabel.setForeground(ThemeManager.DANGER);
            statusLabel.setText(
                    "Please enter your username and password."
            );

            java.util.Arrays.fill(password, '\0');

            return;
        }

        setBusy(true);

        statusLabel.setForeground(
                ThemeManager.TEXT_SECONDARY
        );

        statusLabel.setText("Authenticating...");

        SwingWorker<Optional<Admin>, Void> worker =
                new SwingWorker<>() {

            @Override
            protected Optional<Admin> doInBackground()
                    throws SQLException {

                return authService.authenticate(
                        username,
                        password
                );
            }

            @Override
            protected void done() {

                java.util.Arrays.fill(password, '\0');
                passwordField.setText("");

                try {

                    Optional<Admin> authenticatedAdmin = get();

                    if (authenticatedAdmin.isPresent()) {

                        statusLabel.setForeground(
                                ThemeManager.SUCCESS
                        );

                        statusLabel.setText(
                                "Login successful. Opening dashboard..."
                        );

                        Admin admin = authenticatedAdmin.get();

                        dispose();

                        if (onLoginSuccess != null) {
                            onLoginSuccess.accept(admin);
                        }

                    } else {

                        statusLabel.setForeground(
                                ThemeManager.DANGER
                        );

                        statusLabel.setText(
                                "Invalid username or password."
                        );

                        setBusy(false);
                        passwordField.requestFocusInWindow();
                    }

                } catch (InterruptedException exception) {

                    Thread.currentThread().interrupt();

                    statusLabel.setForeground(
                            ThemeManager.DANGER
                    );

                    statusLabel.setText(
                            "Authentication was interrupted."
                    );

                    setBusy(false);

                } catch (ExecutionException exception) {

                    Throwable cause = exception.getCause();

                    statusLabel.setForeground(
                            ThemeManager.DANGER
                    );

                    if (cause instanceof SQLException) {

                        statusLabel.setText(
                                "Unable to connect to the database."
                        );

                        JOptionPane.showMessageDialog(
                                LoginFrame.this,
                                "Could not authenticate your account.\n"
                                + "Please check your database connection "
                                + "and try again.",
                                "Database Connection Error",
                                JOptionPane.ERROR_MESSAGE
                        );

                    } else {

                        statusLabel.setText(
                                "An unexpected error occurred."
                        );

                        JOptionPane.showMessageDialog(
                                LoginFrame.this,
                                "An unexpected error occurred during "
                                + "authentication.\nPlease try again.",
                                "Authentication Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }

                    setBusy(false);
                }
            }
        };

        worker.execute();
    }

    private void setBusy(boolean busy) {

        loginButton.setEnabled(!busy);
        usernameField.setEnabled(!busy);
        passwordField.setEnabled(!busy);
        showPasswordCheckBox.setEnabled(!busy);

        loginButton.setText(
                busy ? "Signing in..." : "Sign In"
        );

        setCursor(
                busy
                ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                : Cursor.getDefaultCursor()
        );
    }
}
