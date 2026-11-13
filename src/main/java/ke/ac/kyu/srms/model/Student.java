
package ke.ac.kyu.srms.model;

import java.time.LocalDate;

/**
 * Represents a student registered in the SRMS application.
 */
public class Student {

    private final String registrationNumber;
    private final String firstName;
    private final String middleName;
    private final String lastName;
    private final String gender;
    private final LocalDate dateOfBirth;
    private final String email;
    private final String phoneNumber;
    private final String programme;
    private final String department;
    private final int yearOfStudy;
    private final LocalDate admissionDate;

    public Student(
            String registrationNumber,
            String firstName,
            String middleName,
            String lastName,
            String gender,
            LocalDate dateOfBirth,
            String email,
            String phoneNumber,
            String programme,
            String department,
            int yearOfStudy,
            LocalDate admissionDate
    ) {
        this.registrationNumber = registrationNumber;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.programme = programme;
        this.department = department;
        this.yearOfStudy = yearOfStudy;
        this.admissionDate = admissionDate;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getGender() {
        return gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProgramme() {
        return programme;
    }

    public String getDepartment() {
        return department;
    }

    public int getYearOfStudy() {
        return yearOfStudy;
    }

    public LocalDate getAdmissionDate() {
        return admissionDate;
    }

    public String getFullName() {
        return (firstName + " " +
                (middleName.isBlank() ? "" : middleName + " ") +
                lastName).trim();
    }

    @Override
    public String toString() {
        return "Student{" +
                "registrationNumber='" + registrationNumber + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", programme='" + programme + '\'' +
                ", yearOfStudy=" + yearOfStudy +
                '}';
    }
}
