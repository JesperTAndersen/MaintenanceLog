package app.persistence.daos;

import app.config.HibernateTestConfig;
import app.entities.enums.UserRole;
import app.entities.model.User;
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
class UserDAOTest
{

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();

    private UserDAO userDAO;
    private Map<String, User> seeded;

    @BeforeEach
    void beforeEach()
    {
        seeded = TestPopulator.populateUsers(emf);
        userDAO = new UserDAO(emf);
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
        User user = new User("Test", "Doe", "12345678", "test@mail.dk", UserRole.TECHNICIAN, true);
        User created = userDAO.create(user);

        assertThat(created.getUserId(), notNullValue());
        User fetched = userDAO.get(created.getUserId());
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
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userDAO.create(null));

        assertTrue(exception.getMessage().contains("User cant be null"));
    }

    @Test
    @DisplayName("Get - should retrieve existing user by ID")
    void get()
    {
        User user1 = seeded.get("user1");

        User fetched = userDAO.get(user1.getUserId());

        assertThat(fetched, notNullValue());
        assertThat(fetched.getUserId(), is(user1.getUserId()));
        assertThat(fetched.getFirstName(), is("John"));
        assertThat(fetched.getLastName(), is("Doe"));
        assertThat(fetched.getEmail(), is("Johndoe@mail.dk"));
        assertThat(fetched.getRole(), is(UserRole.TECHNICIAN));
    }

    @Test
    @DisplayName("Get - should throw DatabaseException when user not found")
    void getNotFoundThrowsException()
    {
        DatabaseException exception = assertThrows(DatabaseException.class, () -> userDAO.get(99999));

        assertThat(exception.getMessage(), containsString("User not found"));
        assertThat(exception.getErrorType(), is(DatabaseErrorType.NOT_FOUND));
    }

    @Test
    @DisplayName("Get - should throw IllegalArgumentException when ID is null")
    void getNullIdThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userDAO.get(null));

        assertThat(exception.getMessage(), containsString("User id is required"));
    }

    @Test
    @DisplayName("GetAll - should retrieve all users including inactive ones")
    void getAll()
    {
        List<User> allUsers = userDAO.getAll();

        assertThat(allUsers, notNullValue());
        assertThat(allUsers.size(), is(4)); // 3 active + 1 inactive from TestPopulator

        // Verify both active and inactive users
        long activeCount = allUsers.stream().filter(User::isActive).count();
        long inactiveCount = allUsers.stream().filter(u -> !u.isActive()).count();

        assertThat(activeCount, is(3L));
        assertThat(inactiveCount, is(1L));

        assertThat(allUsers, hasItem(hasProperty("email", is("Johndoe@mail.dk"))));
        assertThat(allUsers, hasItem(hasProperty("email", is("Clarkkent@mail.dk"))));
    }

    @Test
    @DisplayName("Update - should update existing user successfully")
    void update()
    {
        User user1 = seeded.get("user1");
        user1.setFirstName("UpdatedJohn");
        user1.setEmail("updatedjohn@mail.dk");
        user1.setRole(UserRole.ADMIN);

        User updated = userDAO.update(user1);

        assertThat(updated, notNullValue());
        assertThat(updated.getUserId(), is(user1.getUserId()));

        User fetched = userDAO.get(user1.getUserId());
        assertThat(fetched.getFirstName(), is("UpdatedJohn"));
        assertThat(fetched.getEmail(), is("updatedjohn@mail.dk"));
        assertThat(fetched.getRole(), is(UserRole.ADMIN));
    }

    @Test
    @DisplayName("Update - should throw DatabaseException when user not found")
    void updateNonExistentUserThrowsException()
    {
        User nonExistentUser = User.builder()
                .userId(99999)
                .firstName("Ghost")
                .lastName("User")
                .phone("00000000")
                .email("ghost@mail.dk")
                .role(UserRole.TECHNICIAN)
                .active(true)
                .build();

        DatabaseException exception = assertThrows(DatabaseException.class, () -> userDAO.update(nonExistentUser));

        assertThat(exception.getMessage(), containsString("User not found"));
        assertThat(exception.getErrorType(), is(DatabaseErrorType.NOT_FOUND));
    }

    @Test
    @DisplayName("Update - should throw IllegalArgumentException when user is null")
    void updateNullUserThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userDAO.update(null));

        assertThat(exception.getMessage(), containsString("User and user id are required"));
    }

    @Test
    @DisplayName("Update - should throw IllegalArgumentException when user ID is null")
    void updateUserWithNullIdThrowsException()
    {
        User userWithoutId = new User("Test", "User", "12345678", "test@mail.dk", UserRole.TECHNICIAN, true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userDAO.update(userWithoutId));

        assertThat(exception.getMessage(), containsString("User and user id are required"));
    }

    @Test
    @DisplayName("GetByEmail - should retrieve active user by email")
    void getByEmail()
    {
        User fetched = userDAO.getByEmail("Johndoe@mail.dk");

        assertThat(fetched, notNullValue());
        assertThat(fetched.getEmail(), is("Johndoe@mail.dk"));
        assertThat(fetched.getFirstName(), is("John"));
        assertThat(fetched.getLastName(), is("Doe"));
        assertThat(fetched.isActive(), is(true));
    }

    @Test
    @DisplayName("GetByEmail - should throw DatabaseException when email not found")
    void getByEmailNotFoundThrowsException()
    {
        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> userDAO.getByEmail("nonexistent@mail.dk"));

        assertThat(exception.getMessage(), containsString("User not found"));
        assertThat(exception.getErrorType(), is(DatabaseErrorType.NOT_FOUND));
    }

    @Test
    @DisplayName("GetByEmail - should not retrieve inactive user")
    void getByEmailInactiveUserThrowsException()
    {
        // Clark Kent is inactive
        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> userDAO.getByEmail("Clarkkent@mail.dk"));

        assertThat(exception.getMessage(), containsString("User not found"));
        assertThat(exception.getErrorType(), is(DatabaseErrorType.NOT_FOUND));
    }

    @Test
    @DisplayName("GetByEmail - should throw IllegalArgumentException when email is null")
    void getByEmailNullThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userDAO.getByEmail(null));

        assertThat(exception.getMessage(), containsString("Email is required"));
    }

    @Test
    @DisplayName("GetByEmail - should throw IllegalArgumentException when email is blank")
    void getByEmailBlankThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userDAO.getByEmail("   "));

        assertThat(exception.getMessage(), containsString("Email is required"));
    }

    @Test
    @DisplayName("GetActiveUsers - should retrieve only active users with limit")
    void getActiveUsers()
    {
        List<User> activeUsers = userDAO.getActiveUsers(5);

        assertThat(activeUsers, notNullValue());
        assertThat(activeUsers.size(), is(3));

        // Verify users are active
        for (User user : activeUsers) {
            assertThat(user.isActive(), is(true));
        }

        // inactive user is not included
        assertThat(activeUsers, not(hasItem(hasProperty("email", is("Clarkkent@mail.dk")))));

        // active users are included
        assertThat(activeUsers, hasItem(hasProperty("email", is("Johndoe@mail.dk"))));
        assertThat(activeUsers, hasItem(hasProperty("email", is("Janedoe@mail.dk"))));
        assertThat(activeUsers, hasItem(hasProperty("email", is("Jeffdoe@mail.dk"))));
    }

    @Test
    @DisplayName("GetActiveUsers - should respect the limit parameter")
    void getActiveUsersWithLimit()
    {
        List<User> activeUsers = userDAO.getActiveUsers(2);

        assertThat(activeUsers, notNullValue());
        assertThat(activeUsers.size(), is(2)); // Should only return 2 users

        // Verify all returned users are active
        for (User user : activeUsers) {
            assertThat(user.isActive(), is(true));
        }
    }

    @Test
    @DisplayName("GetActiveUsers - should throw IllegalArgumentException when limit is zero")
    void getActiveUsersZeroLimitThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userDAO.getActiveUsers(0));

        assertThat(exception.getMessage(), containsString("Input needs to be bigger than 0"));
    }

    @Test
    @DisplayName("GetActiveUsers - should throw IllegalArgumentException when limit is negative")
    void getActiveUsersNegativeLimitThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userDAO.getActiveUsers(-1));

        assertThat(exception.getMessage(), containsString("Input needs to be bigger than 0"));
    }
}