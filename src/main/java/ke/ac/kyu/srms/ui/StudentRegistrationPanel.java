
package ke.ac.kyu.srms.ui;

import ke.ac.kyu.srms.config.ThemeManager;
import ke.ac.kyu.srms.model.Student;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

public class StudentRegistrationPanel extends JPanel {

    private final MainFrame mainFrame;
    
    // Student editing mode
    private Student editingStudent;
    private JLabel headerTitle;
    private JLabel headerSubtitle;
    private JButton clearButton;
    private JButton validateButton;

    // Personal information
    private JTextField registrationField;
    private JTextField firstNameField;
    private JTextField middleNameField;
    private JTextField lastNameField;
    private JComboBox<String> genderBox;
    private DatePicker dateOfBirthPicker;

    // Contact information
    private JTextField emailField;
    private JTextField phoneField;

    // Academic information
    private boolean refreshingAcademicOptions = false;
    
    private JComboBox<String> programmeBox;
    private JComboBox<String> departmentBox;
    private JComboBox<Integer> yearBox;

    // Admission information
    private DatePicker admissionDatePicker;

    // Student photograph
    private JLabel photoPreviewLabel;
    private JLabel photoStatusLabel;
    private JButton removePhotoButton;
    private File selectedPhotoFile;

    private static final long MAX_PHOTO_SIZE = 5 * 1024 * 1024;

    // Form feedback
    private JLabel statusLabel;
    private JLabel progressLabel;
    private JProgressBar progressBar;

    private final Map<JComponent, JLabel> errorLabels =
            new IdentityHashMap<>();

    private static final Color BORDER_COLOR =
            new Color(226, 232, 240);

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[\\p{L}]+(?:[ '\\-][\\p{L}]+)*$");

    private static final Pattern REG_PATTERN =
            Pattern.compile("^[A-Za-z0-9][A-Za-z0-9/\\-]{2,29}$");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public StudentRegistrationPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createFormContent(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(18, 30, 18, 30));

        JPanel headingPanel = new JPanel();
        headingPanel.setOpaque(false);
        headingPanel.setLayout(
                new BoxLayout(headingPanel, BoxLayout.Y_AXIS)
        );

        headerTitle = new JLabel("Student Registration");
        headerTitle.setFont(ThemeManager.FONT_SUBHEADING);
        headerTitle.setForeground(ThemeManager.TEXT_PRIMARY);

        headerSubtitle = new JLabel(
                "Register a new student and capture their academic details."
        );
        headerSubtitle.setFont(ThemeManager.FONT_REGULAR);
        headerSubtitle.setForeground(ThemeManager.TEXT_SECONDARY);

        headingPanel.add(headerTitle);
        headingPanel.add(Box.createVerticalStrut(5));
        headingPanel.add(headerSubtitle);

        JButton backButton = new JButton(
                FontIcon.of(
                        FontAwesomeSolid.ARROW_LEFT,
                        16,
                        ThemeManager.TEXT_SECONDARY
                )
        );

        backButton.setToolTipText("Return to dashboard");
        backButton.setPreferredSize(new Dimension(42, 38));
        backButton.setFocusPainted(false);
        backButton.addActionListener(
                e -> mainFrame.showPage("dashboard")
        );

        header.add(headingPanel, BorderLayout.WEST);
        header.add(backButton, BorderLayout.EAST);

        return header;
    }

    private JScrollPane createFormContent() {
        JPanel form = new JPanel();
        form.setBackground(ThemeManager.BACKGROUND);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(25, 30, 30, 30));

        form.add(createIntroduction());
        form.add(Box.createVerticalStrut(20));

        form.add(createPersonalSection());
        form.add(Box.createVerticalStrut(18));

        form.add(createPhotoSection());
        form.add(Box.createVerticalStrut(18));

        form.add(createContactSection());
        form.add(Box.createVerticalStrut(18));

        form.add(createAcademicSection());
        form.add(Box.createVerticalStrut(18));

        form.add(createAdmissionSection());
        form.add(Box.createVerticalStrut(20));

        form.add(createProgressSection());
        form.add(Box.createVerticalStrut(15));

        form.add(createActionSection());

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(ThemeManager.BACKGROUND);

        return scrollPane;
    }

    private JPanel createIntroduction() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel required = new JLabel(
                "<html>Fields marked "
                + "<font color='#DC2626'>*</font>"
                + " are required.</html>"
        );

        required.setFont(ThemeManager.FONT_REGULAR);
        required.setForeground(ThemeManager.TEXT_SECONDARY);
        panel.add(required, BorderLayout.WEST);

        return panel;
    }

    private JPanel createPersonalSection() {
        JPanel section = createSection(
                "Personal Information",
                "Basic student identification and personal details."
        );

        JPanel grid = createFieldGrid();

        registrationField = new JTextField();
        registrationField.setToolTipText(
                "Enter the unique university registration number."
        );

        firstNameField = new JTextField();
        middleNameField = new JTextField();
        lastNameField = new JTextField();

        genderBox = new JComboBox<>(
                new String[]{
                    "Select gender",
                    "Male",
                    "Female",
                    "Other",
                    "Prefer not to say"
                }
        );

        dateOfBirthPicker = createDatePicker(
                LocalDate.of(2000, 1, 1)
        );

        addField(grid, 0, 0, "Registration Number *", registrationField);
        addField(grid, 0, 1, "First Name *", firstNameField);
        addField(grid, 1, 0, "Middle Name", middleNameField);
        addField(grid, 1, 1, "Last Name *", lastNameField);
        addField(grid, 2, 0, "Gender *", genderBox);
        addField(grid, 2, 1, "Date of Birth *", dateOfBirthPicker);

        section.add(grid, BorderLayout.CENTER);

        return section;
    }

    private JPanel createPhotoSection() {
        JPanel section = createSection(
                "Student Photograph",
                "Upload a clear, recent student photograph. This is optional."
        );

        JPanel content = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 25, 5)
        );
        content.setOpaque(false);

        photoPreviewLabel = new JLabel();
        photoPreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        photoPreviewLabel.setVerticalAlignment(SwingConstants.CENTER);
        photoPreviewLabel.setPreferredSize(new Dimension(150, 150));
        photoPreviewLabel.setBorder(
                BorderFactory.createLineBorder(BORDER_COLOR)
        );
        photoPreviewLabel.setOpaque(true);
        photoPreviewLabel.setBackground(new Color(248, 250, 252));

        resetPhotoPreview();

        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(
                new BoxLayout(controls, BoxLayout.Y_AXIS)
        );

        JLabel heading = new JLabel("Upload student photo");
        heading.setFont(ThemeManager.FONT_MEDIUM);
        heading.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel instructions = new JLabel(
                "<html>Choose a clear photograph of the student.<br>"
                + "Supported formats: JPG, JPEG and PNG.<br>"
                + "Maximum file size: 5 MB.</html>"
        );
        instructions.setFont(ThemeManager.FONT_REGULAR);
        instructions.setForeground(ThemeManager.TEXT_SECONDARY);

        JButton uploadButton = new JButton(
                "Choose Photograph",
                FontIcon.of(
                        FontAwesomeSolid.UPLOAD,
                        15,
                        Color.WHITE
                )
        );

        uploadButton.setFont(ThemeManager.FONT_MEDIUM);
        uploadButton.setBackground(ThemeManager.PRIMARY);
        uploadButton.setForeground(Color.WHITE);
        uploadButton.setFocusPainted(false);
        uploadButton.setBorder(new EmptyBorder(11, 16, 11, 16));

        removePhotoButton = new JButton(
                "Remove Photo",
                FontIcon.of(
                        FontAwesomeSolid.TRASH,
                        14,
                        ThemeManager.TEXT_SECONDARY
                )
        );

        removePhotoButton.setFont(ThemeManager.FONT_REGULAR);
        removePhotoButton.setFocusPainted(false);
        removePhotoButton.setEnabled(false);

        photoStatusLabel = new JLabel("No photograph selected.");
        photoStatusLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 11)
        );
        photoStatusLabel.setForeground(ThemeManager.TEXT_SECONDARY);

        uploadButton.addActionListener(
                e -> chooseStudentPhoto()
        );

        removePhotoButton.addActionListener(
                e -> removeStudentPhoto()
        );

        controls.add(heading);
        controls.add(Box.createVerticalStrut(8));
        controls.add(instructions);
        controls.add(Box.createVerticalStrut(15));
        controls.add(uploadButton);
        controls.add(Box.createVerticalStrut(8));
        controls.add(removePhotoButton);
        controls.add(Box.createVerticalStrut(8));
        controls.add(photoStatusLabel);

        content.add(photoPreviewLabel);
        content.add(controls);

        section.add(content, BorderLayout.CENTER);

        return section;
    }

    private void chooseStudentPhoto() {
        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setDialogTitle("Select Student Photograph");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        FileNameExtensionFilter imageFilter =
                new FileNameExtensionFilter(
                        "Image Files (*.jpg, *.jpeg, *.png)",
                        "jpg", "jpeg", "png"
                );

        fileChooser.setFileFilter(imageFilter);

        int result = fileChooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fileChooser.getSelectedFile();

        if (file == null || !file.isFile()) {
            showPhotoError("Please select a valid image file.");
            return;
        }

        String fileName = file.getName().toLowerCase(Locale.ROOT);

        boolean validExtension =
                fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg")
                || fileName.endsWith(".png");

        if (!validExtension) {
            showPhotoError(
                    "Please select a JPG, JPEG or PNG image."
            );
            return;
        }

        if (file.length() > MAX_PHOTO_SIZE) {
            showPhotoError(
                    "The selected photograph exceeds the 5 MB limit."
            );
            return;
        }

        try {
            BufferedImage image = ImageIO.read(file);

            if (image == null) {
                showPhotoError(
                        "The selected file is not a valid or readable image."
                );
                return;
            }

            if (image.getWidth() > 10000
                    || image.getHeight() > 10000) {
                showPhotoError(
                        "The image dimensions are too large."
                );
                return;
            }

            selectedPhotoFile = file;
            displayStudentPhoto(image);

            photoStatusLabel.setText(
                    "<html>Selected: " + file.getName() + "</html>"
            );
            photoStatusLabel.setForeground(ThemeManager.SUCCESS);
            removePhotoButton.setEnabled(true);

        } catch (IOException ex) {
            showPhotoError(
                    "Unable to load the selected photograph."
            );
        }
    }

    private void displayStudentPhoto(BufferedImage image) {
        int previewWidth = 130;
        int previewHeight = 130;

        double scale = Math.min(
                (double) previewWidth / image.getWidth(),
                (double) previewHeight / image.getHeight()
        );

        int scaledWidth = Math.max(
                1,
                (int) (image.getWidth() * scale)
        );

        int scaledHeight = Math.max(
                1,
                (int) (image.getHeight() * scale)
        );

        Image scaledImage = image.getScaledInstance(
                scaledWidth,
                scaledHeight,
                Image.SCALE_SMOOTH
        );

        photoPreviewLabel.setIcon(new ImageIcon(scaledImage));
        photoPreviewLabel.setText("");
    }

    private void removeStudentPhoto() {
        selectedPhotoFile = null;
        resetPhotoPreview();

        photoStatusLabel.setText("No photograph selected.");
        photoStatusLabel.setForeground(ThemeManager.TEXT_SECONDARY);

        removePhotoButton.setEnabled(false);
    }

    private void resetPhotoPreview() {
        if (photoPreviewLabel == null) {
            return;
        }

        photoPreviewLabel.setIcon(
                FontIcon.of(
                        FontAwesomeSolid.USER,
                        65,
                        ThemeManager.TEXT_SECONDARY
                )
        );

        photoPreviewLabel.setText("");
    }

    private void showPhotoError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Photograph Error",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private JPanel createContactSection() {
        JPanel section = createSection(
                "Contact Information",
                "Student communication and contact details."
        );

        JPanel grid = createFieldGrid();

        emailField = new JTextField();
        phoneField = new JTextField();

        emailField.setToolTipText("Example: student@example.com");
        phoneField.setToolTipText("Example: 0712345678 or +254712345678");

        addField(grid, 0, 0, "Email Address *", emailField);
        addField(grid, 0, 1, "Phone Number *", phoneField);

        section.add(grid, BorderLayout.CENTER);

        return section;
    }

    private JPanel createAcademicSection() {
        JPanel section = createSection(
                "Academic Information",
                "Programme, department and current study level."
        );

        JPanel grid = createFieldGrid();

        programmeBox = new JComboBox<>();
        departmentBox = new JComboBox<>();

        yearBox = new JComboBox<>(
                new Integer[]{null, 1, 2, 3, 4, 5, 6}
        );

        programmeBox.addActionListener(e -> {
            if (!refreshingAcademicOptions) {
                filterDepartments();
            }
        });

        refreshAcademicOptions();

        addField(grid, 0, 0, "Academic Programme *", programmeBox);
        addField(grid, 0, 1, "Department *", departmentBox);
        addField(grid, 1, 0, "Year of Study *", yearBox);

        section.add(grid, BorderLayout.CENTER);

        return section;
    }

    private void resetDepartments() {
        if (departmentBox == null) {
            return;
        }

        departmentBox.removeAllItems();
        departmentBox.addItem("Select department");
        departmentBox.addItem("Select a programme first");
        departmentBox.setSelectedIndex(0);

        updateProgress();
    }

    /**
     * Loads programmes from MainFrame instead of hardcoding them.
     */
    public void refreshAcademicOptions() {
        if (programmeBox == null || departmentBox == null) {
            return;
        }

        String previousSelection =
                (String) programmeBox.getSelectedItem();

        refreshingAcademicOptions = true;

        try {
            programmeBox.removeAllItems();
            programmeBox.addItem("Select programme");

            for (String programmeName : mainFrame.getProgrammeNames()) {
                programmeBox.addItem(programmeName);
            }

            int selectedIndex = 0;

            if (previousSelection != null) {
                for (int i = 1; i < programmeBox.getItemCount(); i++) {
                    if (programmeBox.getItemAt(i).equals(previousSelection)) {
                        selectedIndex = i;
                        break;
                    }
                }
            }

            programmeBox.setSelectedIndex(selectedIndex);

        } finally {
            refreshingAcademicOptions = false;
        }

        filterDepartments();
    }

    /**
     * Displays the department currently assigned to the selected programme.
     */
    private void filterDepartments() {
        if (programmeBox == null || departmentBox == null) {
            return;
        }

        String programme = (String) programmeBox.getSelectedItem();

        departmentBox.removeAllItems();
        departmentBox.addItem("Select department");

        if (programme == null || programme.equals("Select programme")) {
            departmentBox.addItem("Select a programme first");

        } else {
            String department =
                    mainFrame.getDepartmentNameForProgramme(programme);

            if (department == null || department.isBlank()) {
                departmentBox.addItem("No department assigned");
            } else {
                departmentBox.addItem(department);
                departmentBox.setSelectedIndex(1);
            }
        }

        updateProgress();
    }

    private JPanel createAdmissionSection() {
        JPanel section = createSection(
                "Admission Information",
                "Official student admission details."
        );

        JPanel grid = createFieldGrid();

        admissionDatePicker = createDatePicker(LocalDate.now());

        addField(
                grid,
                0,
                0,
                "Admission Date *",
                admissionDatePicker
        );

        section.add(grid, BorderLayout.CENTER);

        return section;
    }

    private DatePicker createDatePicker(LocalDate initialDate) {
        DatePickerSettings settings = new DatePickerSettings();

        settings.setFormatForDatesCommonEra("dd MMM yyyy");
        settings.setAllowKeyboardEditing(true);
        settings.setVisibleClearButton(true);
        settings.setVisibleTodayButton(true);

        DatePicker datePicker = new DatePicker(settings);

        settings.setDateRangeLimits(null, LocalDate.now());

        datePicker.setDate(initialDate);

        datePicker.getComponentDateTextField()
                .setFont(ThemeManager.FONT_REGULAR);

        datePicker.getComponentDateTextField()
                .setPreferredSize(new Dimension(100, 42));

        return datePicker;
    }

    private JPanel createSection(String title, String description) {
        JPanel section = new JPanel(new BorderLayout(0, 18));
        section.setBackground(Color.WHITE);

        section.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR),
                        new EmptyBorder(22, 22, 22, 22)
                )
        );

        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(
                new BoxLayout(heading, BoxLayout.Y_AXIS)
        );

        JLabel headingLabel = new JLabel(title);
        headingLabel.setFont(ThemeManager.FONT_SUBHEADING);
        headingLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        descriptionLabel.setForeground(ThemeManager.TEXT_SECONDARY);

        heading.add(headingLabel);
        heading.add(Box.createVerticalStrut(5));
        heading.add(descriptionLabel);

        section.add(heading, BorderLayout.NORTH);

        return section;
    }

    private JPanel createFieldGrid() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        return grid;
    }

    private void addField(
            JPanel grid,
            int row,
            int column,
            String labelText,
            JComponent component
    ) {
        JPanel field = new JPanel();
        field.setOpaque(false);
        field.setLayout(
                new BoxLayout(field, BoxLayout.Y_AXIS)
        );

        JLabel label = new JLabel(labelText);
        label.setFont(ThemeManager.FONT_MEDIUM);
        label.setForeground(ThemeManager.TEXT_PRIMARY);
        label.setLabelFor(component);

        field.add(label);
        field.add(Box.createVerticalStrut(8));

        component.setFont(ThemeManager.FONT_REGULAR);
        component.setPreferredSize(new Dimension(100, 42));
        component.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 42)
        );

        field.add(component);

        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 11)
        );
        errorLabel.setForeground(ThemeManager.DANGER);

        errorLabels.put(component, errorLabel);

        field.add(Box.createVerticalStrut(3));
        field.add(errorLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = column;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(0, 0, 15, 18);

        grid.add(field, gbc);

        if (component instanceof JTextField textField) {
            textField.getDocument().addDocumentListener(
                    new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    handleTextChange(component);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    handleTextChange(component);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    handleTextChange(component);
                }
            });

            textField.addFocusListener(
                    new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    validateComponent(component);
                }
            });
        }

        if (component instanceof JComboBox<?> comboBox) {
            comboBox.addActionListener(
                    e -> {
                        validateComponent(component);
                        updateProgress();
                    }
            );
        }

        if (component instanceof DatePicker datePicker) {
            datePicker.getComponentDateTextField()
                    .addFocusListener(
                            new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    validateComponent(component);
                    updateProgress();
                }
            });
        }
    }

    private JPanel createProgressSection() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR),
                        new EmptyBorder(15, 18, 15, 18)
                )
        );

        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);

        JLabel title = new JLabel("Registration Progress");
        title.setFont(ThemeManager.FONT_MEDIUM);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        progressLabel = new JLabel("0% completed");
        progressLabel.setFont(ThemeManager.FONT_REGULAR);
        progressLabel.setForeground(ThemeManager.TEXT_SECONDARY);

        heading.add(title, BorderLayout.WEST);
        heading.add(progressLabel, BorderLayout.EAST);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(100, 9));
        progressBar.setForeground(ThemeManager.PRIMARY);
        progressBar.setBackground(new Color(241, 245, 249));

        panel.add(heading);
        panel.add(Box.createVerticalStrut(12));
        panel.add(progressBar);

        updateProgress();

        return panel;
    }

    private void updateProgress() {
        if (progressBar == null || progressLabel == null) {
            return;
        }

        int completed = 0;
        int total = 11;

        if (registrationField != null
                && !registrationField.getText().trim().isEmpty()) {
            completed++;
        }

        if (firstNameField != null
                && !firstNameField.getText().trim().isEmpty()) {
            completed++;
        }

        if (lastNameField != null
                && !lastNameField.getText().trim().isEmpty()) {
            completed++;
        }

        if (genderBox != null && genderBox.getSelectedIndex() > 0) {
            completed++;
        }

        if (dateOfBirthPicker != null
                && dateOfBirthPicker.getDate() != null) {
            completed++;
        }

        if (emailField != null
                && !emailField.getText().trim().isEmpty()) {
            completed++;
        }

        if (phoneField != null
                && !phoneField.getText().trim().isEmpty()) {
            completed++;
        }

        if (programmeBox != null
                && programmeBox.getSelectedIndex() > 0) {
            completed++;
        }

        if (departmentBox != null
                && departmentBox.getSelectedIndex() > 0
                && departmentBox.getSelectedItem() != null
                && !departmentBox.getSelectedItem()
                        .equals("Select a programme first")) {
            completed++;
        }

        if (yearBox != null && yearBox.getSelectedItem() != null) {
            completed++;
        }

        if (admissionDatePicker != null
                && admissionDatePicker.getDate() != null) {
            completed++;
        }

        int percentage = (int) Math.round(
                (completed * 100.0) / total
        );

        progressBar.setValue(percentage);
        progressLabel.setText(percentage + "% completed");
    }

    private void handleTextChange(JComponent component) {
        updateProgress();

        JLabel errorLabel = errorLabels.get(component);

        if (errorLabel != null
                && !errorLabel.getText().trim().isEmpty()) {
            validateComponent(component);
        }
    }
    
        /**
        * Loads an existing student into the registration form
        * and switches the panel into edit mode.
        */
       public void loadStudentForEditing(Student student) {

           if (student == null) {
               JOptionPane.showMessageDialog(
                       this,
                       "No student was selected for editing.",
                       "Edit Student",
                       JOptionPane.WARNING_MESSAGE
               );
               return;
           }

           editingStudent = student;

           // Personal information
           registrationField.setText(student.getRegistrationNumber());
           firstNameField.setText(student.getFirstName());
           middleNameField.setText(student.getMiddleName());
           lastNameField.setText(student.getLastName());

           genderBox.setSelectedItem(student.getGender());

           dateOfBirthPicker.setDate(student.getDateOfBirth());

           // Contact information
           emailField.setText(student.getEmail());
           phoneField.setText(student.getPhoneNumber());

           // Academic information
           refreshAcademicOptions();

           programmeBox.setSelectedItem(student.getProgramme());

           filterDepartments();

           departmentBox.setSelectedItem(student.getDepartment());

           yearBox.setSelectedItem(student.getYearOfStudy());

           // Admission information
           admissionDatePicker.setDate(student.getAdmissionDate());

           // Student photos are not currently stored in the Student model.
           // Therefore, the existing photo cannot be preloaded here.
           removeStudentPhoto();

           // Clear previous validation messages.
           for (JLabel label : errorLabels.values()) {
               label.setText(" ");
           }

           // Change the form heading.
           headerTitle.setText("Edit Student Information");

           headerSubtitle.setText(
                   "Update the student's personal, contact and academic details."
           );

           // Change action button labels.
           clearButton.setText("Cancel Editing");

           validateButton.setText("Review Changes");

           statusLabel.setText(
                   "Editing student: " + student.getRegistrationNumber()
           );

           statusLabel.setForeground(ThemeManager.PRIMARY);

           updateProgress();

           // Bring the registration number into view.
           registrationField.requestFocusInWindow();
       }
       
    /**
    * Cancels editing and returns the form to new-registration mode.
    */
   private void cancelEditing() {

       int choice = JOptionPane.showConfirmDialog(
               this,
               "Are you sure you want to cancel editing?\n"
               + "Any unsaved changes will be discarded.",
               "Cancel Student Editing",
               JOptionPane.YES_NO_OPTION,
               JOptionPane.QUESTION_MESSAGE
       );

       if (choice != JOptionPane.YES_OPTION) {
           return;
       }

       editingStudent = null;

       clearFormFields();

       headerTitle.setText("Student Registration");

       headerSubtitle.setText(
               "Register a new student and capture their academic details."
       );

       clearButton.setText("Clear Form");

       validateButton.setText("Validate Details");

       statusLabel.setText("Ready for registration.");

       statusLabel.setForeground(ThemeManager.TEXT_SECONDARY);

       mainFrame.showPage("students");
   }
   
   /**
    * Resets all form fields without displaying a confirmation dialog.
    */
   private void clearFormFields() {

       registrationField.setText("");
       firstNameField.setText("");
       middleNameField.setText("");
       lastNameField.setText("");

       genderBox.setSelectedIndex(0);

       dateOfBirthPicker.setDate(LocalDate.of(2000, 1, 1));

       emailField.setText("");
       phoneField.setText("");

       refreshAcademicOptions();

       programmeBox.setSelectedIndex(0);

       resetDepartments();

       yearBox.setSelectedIndex(0);

       admissionDatePicker.setDate(LocalDate.now());

       removeStudentPhoto();

       for (JLabel label : errorLabels.values()) {
           label.setText(" ");
       }

       updateProgress();
   }
   
    private JPanel createActionSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 12, 5)
        );
        buttons.setOpaque(false);

        clearButton = new JButton(
                "Clear Form",
                FontIcon.of(
                        FontAwesomeSolid.ERASER,
                        15,
                        ThemeManager.TEXT_SECONDARY
                )
        );

        validateButton = new JButton(
                "Validate Details",
                FontIcon.of(
                        FontAwesomeSolid.CHECK_CIRCLE,
                        15,
                        Color.WHITE
                )
        );

        clearButton.setFont(ThemeManager.FONT_MEDIUM);
        validateButton.setFont(ThemeManager.FONT_MEDIUM);

        clearButton.setToolTipText("Clear all registration fields.");
        validateButton.setToolTipText("Validate the registration information.");

        clearButton.addActionListener(e -> {
            if (editingStudent != null) {
                cancelEditing();
            } else {
                confirmClear();
            }
        });

        validateButton.addActionListener(e -> validateAndPreview());

        validateButton.setBackground(ThemeManager.PRIMARY);
        validateButton.setForeground(Color.WHITE);

        validateButton.setFocusPainted(false);
        clearButton.setFocusPainted(false);

        clearButton.setBorder(new EmptyBorder(12, 18, 12, 18));
        validateButton.setBorder(new EmptyBorder(12, 18, 12, 18));

        buttons.add(clearButton);
        buttons.add(validateButton);

        statusLabel = new JLabel("Ready for registration.");
        statusLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        statusLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(buttons, BorderLayout.EAST);

        return panel;
    }

    private String getValidationMessage(JComponent component) {
        if (component == registrationField) {
            String value = registrationField.getText().trim();

            if (value.isEmpty()) {
                return "Registration number is required.";
            }

            if (!REG_PATTERN.matcher(value).matches()) {
                return "Use 3–30 letters, numbers, hyphens or slashes.";
            }
        }

        if (component == firstNameField) {
            if (!validName(firstNameField.getText().trim())) {
                return "Enter a valid first name.";
            }
        }

        if (component == middleNameField) {
            String value = middleNameField.getText().trim();

            if (!value.isEmpty() && !validName(value)) {
                return "Enter a valid middle name.";
            }
        }

        if (component == lastNameField) {
            if (!validName(lastNameField.getText().trim())) {
                return "Enter a valid last name.";
            }
        }

        if (component == genderBox) {
            if (genderBox.getSelectedIndex() == 0) {
                return "Please select the student's gender.";
            }
        }

        if (component == dateOfBirthPicker) {
            LocalDate dob = dateOfBirthPicker.getDate();

            if (dob == null) {
                return "Please select the date of birth.";
            }

            if (dob.isAfter(LocalDate.now())) {
                return "Date of birth cannot be in the future.";
            }
        }

        if (component == emailField) {
            String email = emailField.getText().trim();

            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return "Enter a valid email address.";
            }
        }

        if (component == phoneField) {
            if (!validPhone(phoneField.getText().trim())) {
                return "Enter a valid Kenyan phone number.";
            }
        }

        if (component == programmeBox) {
            if (programmeBox.getSelectedIndex() == 0) {
                return "Please select an academic programme.";
            }
        }

        if (component == departmentBox) {
            if (departmentBox.getSelectedIndex() != 1
                    || departmentBox.getSelectedItem() == null
                    || departmentBox.getSelectedItem()
                            .equals("Select a programme first")
                    || departmentBox.getSelectedItem()
                            .equals("No department assigned")) {

                return "Please select a valid programme with an assigned department.";
            }
        }

        if (component == yearBox) {
            if (yearBox.getSelectedItem() == null) {
                return "Please select the year of study.";
            }
        }

        if (component == admissionDatePicker) {
            LocalDate admissionDate = admissionDatePicker.getDate();

            if (admissionDate == null) {
                return "Please select the admission date.";
            }

            if (admissionDate.isAfter(LocalDate.now())) {
                return "Admission date cannot be in the future.";
            }

            LocalDate dob = dateOfBirthPicker.getDate();

            if (dob != null && admissionDate.isBefore(dob)) {
                return "Admission date cannot be earlier than date of birth.";
            }
        }

        return "";
    }

    private void validateComponent(JComponent component) {
        JLabel errorLabel = errorLabels.get(component);

        if (errorLabel == null) {
            return;
        }

        String message = getValidationMessage(component);

        errorLabel.setText(message.isEmpty() ? " " : message);
    }

    private boolean validateAllFields() {
        JComponent[] components = {
            registrationField,
            firstNameField,
            middleNameField,
            lastNameField,
            genderBox,
            dateOfBirthPicker,
            emailField,
            phoneField,
            programmeBox,
            departmentBox,
            yearBox,
            admissionDatePicker
        };

        JComponent firstInvalidComponent = null;

        for (JComponent component : components) {
            String message = getValidationMessage(component);
            JLabel errorLabel = errorLabels.get(component);

            if (errorLabel != null) {
                errorLabel.setText(
                        message.isEmpty() ? " " : message
                );
            }

            if (!message.isEmpty()
                    && firstInvalidComponent == null) {
                firstInvalidComponent = component;
            }
        }

        if (firstInvalidComponent != null) {
            statusLabel.setText(
                    "Please correct the highlighted fields."
            );
            statusLabel.setForeground(ThemeManager.DANGER);

            focusComponent(firstInvalidComponent);

            return false;
        }

        return true;
    }

    private void focusComponent(JComponent component) {
        if (component instanceof DatePicker datePicker) {
            datePicker.getComponentDateTextField()
                    .requestFocusInWindow();
        } else {
            component.requestFocusInWindow();
        }
    }

    private boolean validName(String name) {
        return name.length() >= 2
                && name.length() <= 50
                && NAME_PATTERN.matcher(name).matches();
    }

    private boolean validPhone(String phone) {
        String normalized = phone.replaceAll("[\\s\\-()]", "");

        return normalized.matches(
                "^(?:\\+254|254|0)(?:7|1)\\d{8}$"
        );
    }
    
    private void validateAndPreview() {
        if (!validateAllFields()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please correct the highlighted fields before reviewing.",
                    "Validation Required",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Student student = new Student(
                registrationField.getText().trim(),
                firstNameField.getText().trim(),
                middleNameField.getText().trim(),
                lastNameField.getText().trim(),
                (String) genderBox.getSelectedItem(),
                dateOfBirthPicker.getDate(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                (String) programmeBox.getSelectedItem(),
                (String) departmentBox.getSelectedItem(),
                (Integer) yearBox.getSelectedItem(),
                admissionDatePicker.getDate()
        );

        StudentReviewDialog reviewDialog = new StudentReviewDialog(
                SwingUtilities.getWindowAncestor(this),
                student,
                selectedPhotoFile,
                confirmedStudent -> {

                    // Normal new-student registration.
                    if (editingStudent == null) {
                        return mainFrame.addStudent(confirmedStudent);
                    }

                    // Update the existing student.
                    boolean updated = mainFrame.updateStudent(
                            editingStudent.getRegistrationNumber(),
                            confirmedStudent
                    );

                    if (updated) {

                        editingStudent = null;

                        clearFormFields();

                        headerTitle.setText("Student Registration");

                        headerSubtitle.setText(
                                "Register a new student and capture their academic details."
                        );

                        clearButton.setText("Clear Form");

                        validateButton.setText("Validate Details");

                        statusLabel.setText(
                                "Student information updated successfully."
                        );

                        statusLabel.setForeground(ThemeManager.SUCCESS);

                        mainFrame.showPage("students");
                    }

                    return updated;
                }
        );

        reviewDialog.setVisible(true);
    }

    private void confirmClear() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to clear all entered information?",
                "Confirm Form Reset",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        registrationField.setText("");
        firstNameField.setText("");
        middleNameField.setText("");
        lastNameField.setText("");

        genderBox.setSelectedIndex(0);

        dateOfBirthPicker.setDate(
                LocalDate.of(2000, 1, 1)
        );

        emailField.setText("");
        phoneField.setText("");

        programmeBox.setSelectedIndex(0);
        resetDepartments();

        yearBox.setSelectedIndex(0);

        admissionDatePicker.setDate(LocalDate.now());

        removeStudentPhoto();

        for (JLabel label : errorLabels.values()) {
            label.setText(" ");
        }

        statusLabel.setText("Form cleared successfully.");
        statusLabel.setForeground(ThemeManager.TEXT_SECONDARY);

        updateProgress();
        registrationField.requestFocusInWindow();
    }
}
