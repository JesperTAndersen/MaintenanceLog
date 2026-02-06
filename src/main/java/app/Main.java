package app;

import app.config.HibernateConfig;
import app.daos.CourseDAO;
import app.daos.PersonDAO;
import app.daos.StudentDAO;
import app.entities.Course;
import app.entities.Person;
import app.entities.User;
import app.utils.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Main
{
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    public static void main(String[] args)
    {
        EntityManager em = emf.createEntityManager();

        PersonDAO personDAO = new PersonDAO(emf);
        StudentDAO studentDAO = new StudentDAO(emf);
        CourseDAO courseDAO = new CourseDAO(emf);

        Person testPerson = new Person("Person", 30);
        personDAO.createPerson(testPerson);

        User newStudent = User.builder()
                .name("John")
                .email("John@mail.dk")
                .phone("0000000000")
                .address("FakeAdress 123")
                .status(UserRole.ACTIVE)
                .dateOfBirth(LocalDate.of(1994, 3, 13))
                .dateOfEnrollment(LocalDate.now())
                .courseIds(null)
                .build();

        studentDAO.createStudent(newStudent);

        Course testCourse = Course.builder()
                .name("Math")
                .teacher("Math Teacher")
                .semester("3rd")
                .classroom("3D")
                .build();

        courseDAO.createCourse(testCourse);

        newStudent = User.builder()
                .id(newStudent.getId())
                .name("Jane")
                .email("Jane@mail.dk")
                .phone("0000000000")
                .address("FakeAdress 123")
                .status(UserRole.ACTIVE)
                .dateOfBirth(LocalDate.of(1994, 3, 13))
                .dateOfEnrollment(LocalDate.now())
                .courseIds(null)
                .build();


        studentDAO.update(newStudent);

        List<Course> courses = new ArrayList<>(createCourses());

        courses.forEach(courseDAO::createCourse);


        // 9. List all courses for a specific student
        for (Course c : courses)
        {
            if (studentDAO.getStudentById(1).getCourseIds().contains(c.getId()));
            {
                System.out.println(c);
            }
        }


        // Close the database connection:
        em.close();
        emf.close();
    }

    public static List<Course> createCourses()
    {
        Course course1 = Course.builder()
                .name("Data Structures")
                .teacher("Dr. Alice Johnson")
                .semester("Spring 2025")
                .classroom("Room A101")
                .build();

        Course course2 = Course.builder()
                .name("Operating Systems")
                .teacher("Prof. Michael Brown")
                .semester("Fall 2024")
                .classroom("Room B203")
                .build();

        Course course3 = Course.builder()
                .name("Database Systems")
                .teacher("Dr. Sarah Lee")
                .semester("Spring 2025")
                .classroom("Room C305")
                .build();

        Course course4 = Course.builder()
                .name("Software Engineering")
                .teacher("Prof. Daniel Smith")
                .semester("Fall 2024")
                .classroom("Room D410")
                .build();

        Course course5 = Course.builder()
                .name("Computer Networks")
                .teacher("Dr. Emily Davis")
                .semester("Spring 2025")
                .classroom("Room E212")
                .build();

        return List.of(course1, course2, course3, course4, course5);
    }
}