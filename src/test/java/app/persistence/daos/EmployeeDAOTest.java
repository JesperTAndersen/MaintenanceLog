package app.persistence.daos;

import app.config.HibernateTestConfig;
import app.entities.Employee;
import app.entities.enums.EmployeeRole;
import app.exceptions.DatabaseException;
import app.exceptions.enums.DatabaseErrorType;
import app.persistence.EmployeeDAO;
import app.persistence.testutils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmployeeDAOTest
{
    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private EmployeeDAO employeeDAO;
    private Map<String, Employee> seeded;

    @BeforeEach
    void beforeEach()
    {
        seeded = TestPopulator.populateEmployees(emf);
        employeeDAO = new EmployeeDAO(emf);
    }

    @AfterAll
    void shutdown()
    {
        emf.close();
    }

    @Test
    @DisplayName("Create - should persist employee and generate ID")
    void create()
    {
        Employee employee = new Employee("Test", "Doe", "12345678", "test@mail.dk", EmployeeRole.TECHNICIAN, true);
        Employee created = employeeDAO.create(employee);

        assertThat(created.getEmployeeId(), notNullValue());
        Employee fetched = employeeDAO.get(created.getEmployeeId());
        assertThat(fetched.getFirstName(), is("Test"));
        assertThat(fetched.getLastName(), is("Doe"));
        assertThat(fetched.getPhone(), is("12345678"));
        assertThat(fetched.getEmail(), is("test@mail.dk"));
        assertThat(fetched.getRole(), is(EmployeeRole.TECHNICIAN));
        assertThat(fetched.isActive(), is(true));
    }

    @Test
    @DisplayName("Create - should throw exception when employee is null")
    void createNullEmployeeThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> employeeDAO.create(null));

        assertTrue(exception.getMessage().contains("Employee cant be null"));
    }

    @Test
    @DisplayName("Get - should retrieve existing employee by ID")
    void get()
    {
        Employee employee1 = seeded.get("employee1");

        Employee fetched = employeeDAO.get(employee1.getEmployeeId());

        assertThat(fetched, notNullValue());
        assertThat(fetched.getEmployeeId(), is(employee1.getEmployeeId()));
        assertThat(fetched.getFirstName(), is("John"));
        assertThat(fetched.getLastName(), is("Doe"));
        assertThat(fetched.getEmail(), is("Johndoe@mail.dk"));
        assertThat(fetched.getRole(), is(EmployeeRole.TECHNICIAN));
    }

    @Test
    @DisplayName("Get - should throw DatabaseException when employee not found")
    void getNotFoundThrowsException()
    {
        DatabaseException exception = assertThrows(DatabaseException.class, () -> employeeDAO.get(99999));

        assertThat(exception.getMessage(), containsString("Employee not found"));
        assertThat(exception.getErrorType(), is(DatabaseErrorType.NOT_FOUND));
    }

    @Test
    @DisplayName("Get - should throw IllegalArgumentException when ID is null")
    void getNullIdThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> employeeDAO.get(null));

        assertThat(exception.getMessage(), containsString("Employee id is required"));
    }

    @Test
    @DisplayName("GetAll - should retrieve all employees")
    void getAll()
    {
        List<Employee> allEmployees = employeeDAO.getAll();

        assertThat(allEmployees, notNullValue());
        assertThat(allEmployees.size(), is(4)); // 3 active + 1 inactive from TestPopulator

        // Verify active employees are included
        assertThat(allEmployees, hasItem(hasProperty("email", is("Johndoe@mail.dk"))));
        assertThat(allEmployees, hasItem(hasProperty("email", is("Janedoe@mail.dk"))));
        assertThat(allEmployees, hasItem(hasProperty("email", is("Jeffdoe@mail.dk"))));

        // Verify inactive employee is included
        assertThat(allEmployees, hasItem(hasProperty("email", is("Clarkkent@mail.dk"))));
    }

    @Test
    @DisplayName("Update - should update existing employee successfully")
    void update()
    {
        Employee employee1 = seeded.get("employee1");
        employee1.setFirstName("UpdatedJohn");
        employee1.setEmail("updatedjohn@mail.dk");
        employee1.setRole(EmployeeRole.ADMIN);

        Employee updated = employeeDAO.update(employee1);

        assertThat(updated, notNullValue());
        assertThat(updated.getEmployeeId(), is(employee1.getEmployeeId()));

        Employee fetched = employeeDAO.get(employee1.getEmployeeId());
        assertThat(fetched.getFirstName(), is("UpdatedJohn"));
        assertThat(fetched.getEmail(), is("updatedjohn@mail.dk"));
        assertThat(fetched.getRole(), is(EmployeeRole.ADMIN));
    }

    @Test
    @DisplayName("Update - should throw DatabaseException when employee not found")
    void updateNonExistentEmployeeThrowsException()
    {
        Employee nonExistentEmployee = Employee.builder()
                .employeeId(99999)
                .firstName("Ghost")
                .lastName("User")
                .phone("00000000")
                .email("ghost@mail.dk")
                .password("default")
                .role(EmployeeRole.TECHNICIAN)
                .active(true)
                .build();

        DatabaseException exception = assertThrows(DatabaseException.class, () -> employeeDAO.update(nonExistentEmployee));

        assertThat(exception.getMessage(), containsString("Employee not found"));
        assertThat(exception.getErrorType(), is(DatabaseErrorType.NOT_FOUND));
    }

    @Test
    @DisplayName("Update - should throw IllegalArgumentException when employee is null")
    void updateNullEmployeeThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> employeeDAO.update(null));

        assertThat(exception.getMessage(), containsString("Employee and employee id are required"));
    }

    @Test
    @DisplayName("Update - should throw IllegalArgumentException when employee ID is null")
    void updateEmployeeWithNullIdThrowsException()
    {
        Employee employeeWithoutId = new Employee("Test", "User", "12345678", "test@mail.dk", EmployeeRole.TECHNICIAN, true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> employeeDAO.update(employeeWithoutId));

        assertThat(exception.getMessage(), containsString("Employee and employee id are required"));
    }

    @Test
    @DisplayName("GetByEmail - should retrieve active employee by email")
    void getByEmail()
    {
        Employee fetched = employeeDAO.getByEmail("Johndoe@mail.dk");

        assertThat(fetched, notNullValue());
        assertThat(fetched.getEmail(), is("Johndoe@mail.dk"));
        assertThat(fetched.getFirstName(), is("John"));
        assertThat(fetched.getLastName(), is("Doe"));
        assertThat(fetched.isActive(), is(true));
    }

    @Test
    @DisplayName("GetByEmail - should return null when email not found")
    void getByEmailNotFoundThrowsException()
    {
        Employee result = employeeDAO.getByEmail("nonexistent@mail.dk");

        assertThat(result, nullValue());
    }

    @Test
    @DisplayName("GetByEmail - should return null for inactive employee")
    void getByEmailInactiveEmployeeThrowsException()
    {
        // Clark Kent is inactive employee
        Employee result = employeeDAO.getByEmail("Clarkkent@mail.dk");

        assertThat(result, nullValue());
    }

    @Test
    @DisplayName("GetByEmail - should throw IllegalArgumentException when email is null")
    void getByEmailNullThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> employeeDAO.getByEmail(null));

        assertThat(exception.getMessage(), containsString("Email is required"));
    }

    @Test
    @DisplayName("GetByEmail - should throw IllegalArgumentException when email is blank")
    void getByEmailBlankThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> employeeDAO.getByEmail("   "));

        assertThat(exception.getMessage(), containsString("Email is required"));
    }

    @Test
    @DisplayName("GetInactiveEmployees - should retrieve only inactive employees with limit")
    void getInactiveEmployees()
    {
        List<Employee> inactiveEmployees = employeeDAO.getInactiveEmployees(5);

        assertThat(inactiveEmployees, notNullValue());
        assertThat(inactiveEmployees.size(), is(1));

        // Verify employees are inactive
        for (Employee employee : inactiveEmployees)
        {
            assertThat(employee.isActive(), is(false));
        }

        // inactive employee is included
        assertThat(inactiveEmployees, hasItem(hasProperty("email", is("Clarkkent@mail.dk"))));

        // active employees are not included
        assertThat(inactiveEmployees, not(hasItem(hasProperty("email", is("Johndoe@mail.dk")))));
        assertThat(inactiveEmployees, not(hasItem(hasProperty("email", is("Janedoe@mail.dk")))));
        assertThat(inactiveEmployees, not(hasItem(hasProperty("email", is("Jeffdoe@mail.dk")))));
    }

    @Test
    @DisplayName("GetInactiveEmployees - should respect the limit parameter")
    void getInactiveEmployeesWithLimit()
    {
        List<Employee> inactiveEmployees = employeeDAO.getInactiveEmployees(2);

        assertThat(inactiveEmployees, notNullValue());
        assertThat(inactiveEmployees.size(), is(1)); // Only 1 inactive employee in test data

        // Verify all returned employees are inactive
        for (Employee employee : inactiveEmployees)
        {
            assertThat(employee.isActive(), is(false));
        }
    }

    @Test
    @DisplayName("GetInactiveEmployees - should throw IllegalArgumentException when limit is zero")
    void getInactiveEmployeesZeroLimitThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> employeeDAO.getInactiveEmployees(0));

        assertThat(exception.getMessage(), containsString("Input needs to be bigger than 0"));
    }

    @Test
    @DisplayName("GetInactiveEmployees - should throw IllegalArgumentException when limit is negative")
    void getInactiveEmployeesNegativeLimitThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> employeeDAO.getInactiveEmployees(-1));

        assertThat(exception.getMessage(), containsString("Input needs to be bigger than 0"));
    }

    @Test
    @DisplayName("GetActiveEmployees - should retrieve only active employees with limit")
    void getActiveEmployees()
    {
        List<Employee> activeEmployees = employeeDAO.getActiveEmployees(5);

        assertThat(activeEmployees, notNullValue());
        assertThat(activeEmployees.size(), is(3));

        for (Employee employee : activeEmployees)
        {
            assertThat(employee.isActive(), is(true));
        }

        assertThat(activeEmployees, hasItem(hasProperty("email", is("Johndoe@mail.dk"))));
        assertThat(activeEmployees, hasItem(hasProperty("email", is("Janedoe@mail.dk"))));
        assertThat(activeEmployees, hasItem(hasProperty("email", is("Jeffdoe@mail.dk"))));
        assertThat(activeEmployees, not(hasItem(hasProperty("email", is("Clarkkent@mail.dk")))));
    }

    @Test
    @DisplayName("GetActiveEmployees - should respect the limit parameter")
    void getActiveEmployeesWithLimit()
    {
        List<Employee> activeEmployees = employeeDAO.getActiveEmployees(2);

        assertThat(activeEmployees, notNullValue());
        assertThat(activeEmployees.size(), is(2));

        for (Employee employee : activeEmployees)
        {
            assertThat(employee.isActive(), is(true));
        }
    }

    @Test
    @DisplayName("GetActiveEmployees - should throw IllegalArgumentException when limit is zero")
    void getActiveEmployeesZeroLimitThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> employeeDAO.getActiveEmployees(0));

        assertThat(exception.getMessage(), containsString("Input needs to be bigger than 0"));
    }

    @Test
    @DisplayName("GetActiveEmployees - should throw IllegalArgumentException when limit is negative")
    void getActiveEmployeesNegativeLimitThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> employeeDAO.getActiveEmployees(-1));

        assertThat(exception.getMessage(), containsString("Input needs to be bigger than 0"));
    }
}