package app.persistence.daos;

import app.config.HibernateTestConfig;
import app.entities.Employee;
import app.entities.enums.UserRole;
import app.exceptions.DatabaseException;
import app.exceptions.enums.DatabaseErrorType;
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
        seeded = TestPopulator.populateUsers(emf);
        employeeDAO = new EmployeeDAO(emf);
    }

    @AfterAll
    void shutdown()
    {
        emf.close();
    }

    @Test
    @DisplayName("Create - should persist user and generate ID")
    void create()
    {
        Employee employee = new Employee("Test", "Doe", "12345678", "test@mail.dk", UserRole.TECHNICIAN, true);
        Employee created = employeeDAO.create(employee);

        assertThat(created.getUserId(), notNullValue());
        Employee fetched = employeeDAO.get(created.getUserId());
        assertThat(fetched.getFirstName(), is("Test"));
        assertThat(fetched.getLastName(), is("Doe"));
        assertThat(fetched.getPhone(), is("12345678"));
        assertThat(fetched.getEmail(), is("test@mail.dk"));
        assertThat(fetched.getRole(), is(UserRole.TECHNICIAN));
        assertThat(fetched.isActive(), is(true));
    }

    @Test
    @DisplayName("Create - should throw exception when user is null")
    void createNullUserThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> employeeDAO.create(null));

        assertTrue(exception.getMessage().contains("User cant be null"));
    }

    @Test
    @DisplayName("Get - should retrieve existing user by ID")
    void get()
    {
        Employee employee1 = seeded.get("user1");

        Employee fetched = employeeDAO.get(employee1.getUserId());

        assertThat(fetched, notNullValue());
        assertThat(fetched.getUserId(), is(employee1.getUserId()));
        assertThat(fetched.getFirstName(), is("John"));
        assertThat(fetched.getLastName(), is("Doe"));
        assertThat(fetched.getEmail(), is("Johndoe@mail.dk"));
        assertThat(fetched.getRole(), is(UserRole.TECHNICIAN));
    }

    @Test
    @DisplayName("Get - should throw DatabaseException when user not found")
    void getNotFoundThrowsException()
    {
        DatabaseException exception = assertThrows(DatabaseException.class, () -> employeeDAO.get(99999));

        assertThat(exception.getMessage(), containsString("User not found"));
        assertThat(exception.getErrorType(), is(DatabaseErrorType.NOT_FOUND));
    }

    @Test
    @DisplayName("Get - should throw IllegalArgumentException when ID is null")
    void getNullIdThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> employeeDAO.get(null));

        assertThat(exception.getMessage(), containsString("User id is required"));
    }

    @Test
    @DisplayName("GetAll - should retrieve all users")
    void getAll()
    {
        List<Employee> allEmployees = employeeDAO.getAll();

        assertThat(allEmployees, notNullValue());
        assertThat(allEmployees.size(), is(4)); // 3 active + 1 inactive from TestPopulator

        // Verify active users are included
        assertThat(allEmployees, hasItem(hasProperty("email", is("Johndoe@mail.dk"))));
        assertThat(allEmployees, hasItem(hasProperty("email", is("Janedoe@mail.dk"))));
        assertThat(allEmployees, hasItem(hasProperty("email", is("Jeffdoe@mail.dk"))));

        // Verify inactive user is included
        assertThat(allEmployees, hasItem(hasProperty("email", is("Clarkkent@mail.dk"))));
    }

    @Test
    @DisplayName("Update - should update existing user successfully")
    void update()
    {
        Employee employee1 = seeded.get("user1");
        employee1.setFirstName("UpdatedJohn");
        employee1.setEmail("updatedjohn@mail.dk");
        employee1.setRole(UserRole.ADMIN);

        Employee updated = employeeDAO.update(employee1);

        assertThat(updated, notNullValue());
        assertThat(updated.getUserId(), is(employee1.getUserId()));

        Employee fetched = employeeDAO.get(employee1.getUserId());
        assertThat(fetched.getFirstName(), is("UpdatedJohn"));
        assertThat(fetched.getEmail(), is("updatedjohn@mail.dk"));
        assertThat(fetched.getRole(), is(UserRole.ADMIN));
    }

    @Test
    @DisplayName("Update - should throw DatabaseException when user not found")
    void updateNonExistentUserThrowsException()
    {
        Employee nonExistentEmployee = Employee.builder()
                .userId(99999)
                .firstName("Ghost")
                .lastName("User")
                .phone("00000000")
                .email("ghost@mail.dk")
                .password("default")
                .role(UserRole.TECHNICIAN)
                .active(true)
                .build();

        DatabaseException exception = assertThrows(DatabaseException.class, () -> employeeDAO.update(nonExistentEmployee));

        assertThat(exception.getMessage(), containsString("User not found"));
        assertThat(exception.getErrorType(), is(DatabaseErrorType.NOT_FOUND));
    }

    @Test
    @DisplayName("Update - should throw IllegalArgumentException when user is null")
    void updateNullUserThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> employeeDAO.update(null));

        assertThat(exception.getMessage(), containsString("User and user id are required"));
    }

    @Test
    @DisplayName("Update - should throw IllegalArgumentException when user ID is null")
    void updateUserWithNullIdThrowsException()
    {
        Employee employeeWithoutId = new Employee("Test", "User", "12345678", "test@mail.dk", UserRole.TECHNICIAN, true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> employeeDAO.update(employeeWithoutId));

        assertThat(exception.getMessage(), containsString("User and user id are required"));
    }

    @Test
    @DisplayName("GetByEmail - should retrieve active user by email")
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
    @DisplayName("GetByEmail - should return null for inactive user")
    void getByEmailInactiveUserThrowsException()
    {
        // Clark Kent is inactive
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
    @DisplayName("GetInactiveUsers - should retrieve only inactive users with limit")
    void getInactiveUsers()
    {
        List<Employee> inactiveEmployees = employeeDAO.getInactiveUsers(5);

        assertThat(inactiveEmployees, notNullValue());
        assertThat(inactiveEmployees.size(), is(1));

        // Verify users are inactive
        for (Employee employee : inactiveEmployees)
        {
            assertThat(employee.isActive(), is(false));
        }

        // inactive user is included
        assertThat(inactiveEmployees, hasItem(hasProperty("email", is("Clarkkent@mail.dk"))));

        // active users are not included
        assertThat(inactiveEmployees, not(hasItem(hasProperty("email", is("Johndoe@mail.dk")))));
        assertThat(inactiveEmployees, not(hasItem(hasProperty("email", is("Janedoe@mail.dk")))));
        assertThat(inactiveEmployees, not(hasItem(hasProperty("email", is("Jeffdoe@mail.dk")))));
    }

    @Test
    @DisplayName("GetInactiveUsers - should respect the limit parameter")
    void getInactiveUsersWithLimit()
    {
        List<Employee> inactiveEmployees = employeeDAO.getInactiveUsers(2);

        assertThat(inactiveEmployees, notNullValue());
        assertThat(inactiveEmployees.size(), is(1)); // Only 1 inactive user in test data

        // Verify all returned users are inactive
        for (Employee employee : inactiveEmployees)
        {
            assertThat(employee.isActive(), is(false));
        }
    }

    @Test
    @DisplayName("GetInactiveUsers - should throw IllegalArgumentException when limit is zero")
    void getInactiveUsersZeroLimitThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> employeeDAO.getInactiveUsers(0));

        assertThat(exception.getMessage(), containsString("Input needs to be bigger than 0"));
    }

    @Test
    @DisplayName("GetInactiveUsers - should throw IllegalArgumentException when limit is negative")
    void getInactiveUsersNegativeLimitThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> employeeDAO.getInactiveUsers(-1));

        assertThat(exception.getMessage(), containsString("Input needs to be bigger than 0"));
    }

    @Test
    @DisplayName("GetActiveUsers - should retrieve only active users with limit")
    void getActiveUsers()
    {
        List<Employee> activeEmployees = employeeDAO.getActiveUsers(5);

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
    @DisplayName("GetActiveUsers - should respect the limit parameter")
    void getActiveUsersWithLimit()
    {
        List<Employee> activeEmployees = employeeDAO.getActiveUsers(2);

        assertThat(activeEmployees, notNullValue());
        assertThat(activeEmployees.size(), is(2));

        for (Employee employee : activeEmployees)
        {
            assertThat(employee.isActive(), is(true));
        }
    }

    @Test
    @DisplayName("GetActiveUsers - should throw IllegalArgumentException when limit is zero")
    void getActiveUsersZeroLimitThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> employeeDAO.getActiveUsers(0));

        assertThat(exception.getMessage(), containsString("Input needs to be bigger than 0"));
    }

    @Test
    @DisplayName("GetActiveUsers - should throw IllegalArgumentException when limit is negative")
    void getActiveUsersNegativeLimitThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> employeeDAO.getActiveUsers(-1));

        assertThat(exception.getMessage(), containsString("Input needs to be bigger than 0"));
    }
}