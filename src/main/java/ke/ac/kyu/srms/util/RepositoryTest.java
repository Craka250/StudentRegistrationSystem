
package ke.ac.kyu.srms.util;

import ke.ac.kyu.srms.model.Department;
import ke.ac.kyu.srms.model.Programme;
import ke.ac.kyu.srms.model.Student;
import ke.ac.kyu.srms.repository.DepartmentRepository;
import ke.ac.kyu.srms.repository.ProgrammeRepository;
import ke.ac.kyu.srms.repository.StudentRepository;

import java.util.List;

public class RepositoryTest {

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println(" STUDENT REGISTRATION SYSTEM");
        System.out.println(" REPOSITORY CONNECTION TEST");
        System.out.println("======================================");

        try {
            DepartmentRepository departmentRepository =
                    new DepartmentRepository();

            ProgrammeRepository programmeRepository =
                    new ProgrammeRepository();

            StudentRepository studentRepository =
                    new StudentRepository();

            // Test department retrieval
            List<Department> departments =
                    departmentRepository.findAll();

            System.out.println("\nDEPARTMENTS");
            System.out.println("--------------------------------------");
            System.out.println("Total departments: " + departments.size());

            for (Department department : departments) {
                System.out.println(
                        department.getName()
                                + " | ID: " + department.getId()
                );
            }

            // Test programme retrieval
            List<Programme> programmes =
                    programmeRepository.findAll();

            System.out.println("\nPROGRAMMES");
            System.out.println("--------------------------------------");
            System.out.println("Total programmes: " + programmes.size());

            for (Programme programme : programmes) {
                System.out.println(
                        programme.getName()
                                + " | Department ID: "
                                + programme.getDepartmentId()
                );
            }

            // Test programmes belonging to each department
            System.out.println("\nDEPARTMENT-PROGRAMME RELATIONSHIPS");
            System.out.println("--------------------------------------");

            for (Department department : departments) {

                List<Programme> departmentProgrammes =
                        programmeRepository.findByDepartmentId(
                                department.getId()
                        );

                System.out.println(
                        "\n" + department.getName()
                );

                for (Programme programme : departmentProgrammes) {
                    System.out.println("  - " + programme.getName());
                }
            }

            // Test student retrieval
            List<Student> students = studentRepository.findAll();

            System.out.println("\nSTUDENTS");
            System.out.println("--------------------------------------");
            System.out.println("Total students: " + students.size());

            for (Student student : students) {
                System.out.println(
                        student.getRegistrationNumber()
                                + " | " + student.getFullName()
                                + " | " + student.getProgramme()
                );
            }

            System.out.println("\n======================================");
            System.out.println(" ALL REPOSITORY TESTS COMPLETED");
            System.out.println(" DATABASE READ OPERATIONS SUCCESSFUL");
            System.out.println("======================================");

        } catch (Exception e) {

            System.err.println("\nREPOSITORY TEST FAILED!");
            System.err.println("Error: " + e.getMessage());

            e.printStackTrace();
        }
    }
}
