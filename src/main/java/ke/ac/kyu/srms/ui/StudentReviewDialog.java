
package ke.ac.kyu.srms.ui;

import ke.ac.kyu.srms.config.ThemeManager;
import ke.ac.kyu.srms.model.Student;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Image;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

public class StudentReviewDialog extends JDialog {

    private final Student student;
    private final File photoFile;

    // Predicate returns true when registration succeeds.
    private final Predicate<Student> onConfirm;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    public StudentReviewDialog(
            java.awt.Window owner,
            Student student,
            File photoFile,
            Predicate<Student> onConfirm
    ) {

        super(
                owner,
                "Review Student Registration",
                ModalityType.APPLICATION_MODAL
        );

        this.student = student;
        this.photoFile = photoFile;
        this.onConfirm = onConfirm;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(760, 720);
        setMinimumSize(new Dimension(680, 600));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        add(createActions(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {

        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(ThemeManager.BACKGROUND);
        panel.setBorder(new EmptyBorder(22, 26, 16, 26));

        JLabel title = new JLabel("Review Student Details");
        title.setFont(ThemeManager.FONT_SUBHEADING);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subtitle = new JLabel(
                "Please verify that all information is accurate before confirming."
        );
        subtitle.setFont(ThemeManager.FONT_REGULAR);
        subtitle.setForeground(ThemeManager.TEXT_SECONDARY);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(
                new BoxLayout(textPanel, BoxLayout.Y_AXIS)
        );

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(subtitle);

        panel.add(textPanel, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane createContent() {

        JPanel content = new JPanel();
        content.setBackground(ThemeManager.BACKGROUND);
        content.setBorder(new EmptyBorder(5, 26, 20, 26));
        content.setLayout(
                new BoxLayout(content, BoxLayout.Y_AXIS)
        );

        content.add(createPhotoPanel());
        content.add(Box.createVerticalStrut(18));

        content.add(createDetailsSection(
                "Personal Information",
                new String[][]{
                    {"Registration Number", student.getRegistrationNumber()},
                    {"First Name", student.getFirstName()},
                    {"Middle Name", display(student.getMiddleName())},
                    {"Last Name", student.getLastName()},
                    {"Gender", student.getGender()},
                    {"Date of Birth", formatDate(student.getDateOfBirth())}
                }
        ));

        content.add(Box.createVerticalStrut(14));

        content.add(createDetailsSection(
                "Contact Information",
                new String[][]{
                    {"Email Address", student.getEmail()},
                    {"Phone Number", student.getPhoneNumber()}
                }
        ));

        content.add(Box.createVerticalStrut(14));

        content.add(createDetailsSection(
                "Academic Information",
                new String[][]{
                    {"Programme", student.getProgramme()},
                    {"Department", student.getDepartment()},
                    {"Year of Study", String.valueOf(student.getYearOfStudy())},
                    {"Admission Date", formatDate(student.getAdmissionDate())}
                }
        ));

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(
                ThemeManager.BACKGROUND
        );

        return scrollPane;
    }

    private JPanel createPhotoPanel() {

        JPanel panel = new JPanel(new BorderLayout(18, 5));
        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 229, 235)
                        ),
                        new EmptyBorder(15, 16, 15, 16)
                )
        );

        panel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 155)
        );

        JLabel photo = new JLabel();
        photo.setHorizontalAlignment(SwingConstants.CENTER);
        photo.setVerticalAlignment(SwingConstants.CENTER);
        photo.setPreferredSize(new Dimension(115, 115));
        photo.setOpaque(true);
        photo.setBackground(new Color(245, 247, 250));

        if (photoFile != null && photoFile.exists()) {

            try {

                Image image = ImageIO.read(photoFile);

                if (image != null) {

                    photo.setIcon(
                            new ImageIcon(
                                    image.getScaledInstance(
                                            110,
                                            110,
                                            Image.SCALE_SMOOTH
                                    )
                            )
                    );

                } else {

                    setDefaultPhotoIcon(photo);
                }

            } catch (Exception ex) {

                setDefaultPhotoIcon(photo);
            }

        } else {

            setDefaultPhotoIcon(photo);
        }

        JPanel description = new JPanel();
        description.setOpaque(false);
        description.setLayout(
                new BoxLayout(description, BoxLayout.Y_AXIS)
        );

        JLabel heading = new JLabel("Student Photograph");
        heading.setFont(ThemeManager.FONT_MEDIUM);
        heading.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel status = new JLabel(
                photoFile == null
                        ? "No photograph uploaded"
                        : photoFile.getName()
        );

        status.setFont(ThemeManager.FONT_REGULAR);
        status.setForeground(ThemeManager.TEXT_SECONDARY);

        description.add(Box.createVerticalGlue());
        description.add(heading);
        description.add(Box.createVerticalStrut(8));
        description.add(status);
        description.add(Box.createVerticalGlue());

        panel.add(photo, BorderLayout.WEST);
        panel.add(description, BorderLayout.CENTER);

        return panel;
    }

    private void setDefaultPhotoIcon(JLabel label) {

        label.setIcon(
                FontIcon.of(
                        FontAwesomeSolid.USER,
                        55,
                        ThemeManager.TEXT_SECONDARY
                )
        );
    }

    private JPanel createDetailsSection(
            String heading,
            String[][] details
    ) {

        JPanel section = new JPanel(
                new BorderLayout(8, 12)
        );

        section.setBackground(Color.WHITE);

        section.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 229, 235)
                        ),
                        new EmptyBorder(15, 17, 15, 17)
                )
        );

        section.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = new JLabel(heading);
        title.setFont(ThemeManager.FONT_MEDIUM);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        section.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(7, 0, 7, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < details.length; i++) {

            gbc.gridy = i;

            gbc.gridx = 0;
            gbc.weightx = 0.35;

            JLabel label = new JLabel(details[i][0]);
            label.setFont(ThemeManager.FONT_REGULAR);
            label.setForeground(ThemeManager.TEXT_SECONDARY);

            grid.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.65;

            JLabel value = new JLabel(display(details[i][1]));
            value.setFont(ThemeManager.FONT_MEDIUM);
            value.setForeground(ThemeManager.TEXT_PRIMARY);

            grid.add(value, gbc);
        }

        section.add(grid, BorderLayout.CENTER);

        return section;
    }

    private JPanel createActions() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.BACKGROUND);
        panel.setBorder(new EmptyBorder(14, 26, 20, 26));

        JLabel notice = new JLabel(
                "Review carefully. You can return to the form to make corrections."
        );

        notice.setFont(ThemeManager.FONT_REGULAR);
        notice.setForeground(ThemeManager.TEXT_SECONDARY);

        JButton backButton = new JButton("Back to Edit");

        backButton.setFont(ThemeManager.FONT_MEDIUM);

        backButton.setIcon(
                FontIcon.of(
                        FontAwesomeSolid.ARROW_LEFT,
                        14,
                        ThemeManager.TEXT_PRIMARY
                )
        );

        styleSquareButton(backButton);

        backButton.setBackground(Color.WHITE);
        backButton.setForeground(ThemeManager.TEXT_PRIMARY);

        backButton.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(203, 213, 225)
                        ),
                        new EmptyBorder(11, 16, 11, 16)
                )
        );

        backButton.addActionListener(e -> dispose());

        JButton confirmButton = new JButton("Confirm Details");

        confirmButton.setFont(ThemeManager.FONT_MEDIUM);
        confirmButton.setBackground(ThemeManager.PRIMARY);
        confirmButton.setForeground(Color.BLACK);

        confirmButton.setIcon(
                FontIcon.of(
                        FontAwesomeSolid.CHECK,
                        14,
                        Color.BLACK
                )
        );

        styleSquareButton(confirmButton);

        confirmButton.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                ThemeManager.PRIMARY.darker()
                        ),
                        new EmptyBorder(11, 16, 11, 16)
                )
        );

        confirmButton.addActionListener(e -> confirmDetails());

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(
                new BoxLayout(buttons, BoxLayout.X_AXIS)
        );

        buttons.add(backButton);
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(confirmButton);

        panel.add(notice, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.EAST);

        return panel;
    }

    /**
     * Applies consistent sharp-corner styling and pointer cursor.
     */
    private void styleSquareButton(JButton button) {

        button.putClientProperty(
                "JButton.buttonType",
                "square"
        );

        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
    }

    /**
     * Confirms registration only when MainFrame accepts the student.
     */
    private void confirmDetails() {

        boolean accepted = onConfirm == null
                || onConfirm.test(student);

        // A duplicate or rejected student stays in the review dialog.
        if (!accepted) {
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Student registration has been confirmed successfully.\n\n"
                + "Registration Number: "
                + student.getRegistrationNumber()
                + "\n\nThe student has been added to Student Management.",
                "Registration Successful",
                JOptionPane.INFORMATION_MESSAGE
        );

        dispose();
    }

    private String formatDate(java.time.LocalDate date) {

        return date == null
                ? "Not provided"
                : date.format(dateFormatter);
    }

    private String display(String value) {

        return value == null || value.trim().isEmpty()
                ? "Not provided"
                : value;
    }
}
