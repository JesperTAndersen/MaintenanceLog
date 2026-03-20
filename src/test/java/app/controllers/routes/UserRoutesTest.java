package app.controllers.routes;

import app.config.ApplicationConfig;
import app.config.DependencyContainer;
import app.config.HibernateTestConfig;
import app.entities.User;
import app.persistence.testutils.TestPopulator;
import io.javalin.Javalin;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class UserRoutesTest
{
    private static EntityManagerFactory emf;
    private static DependencyContainer container;
    private static Javalin app;
    private static final int TEST_PORT = 7071;
    private Map<String, User> seeded;

    @BeforeAll
    public static void init()
    {
        emf = HibernateTestConfig.getEntityManagerFactory();
        container = new DependencyContainer(emf);
        app = ApplicationConfig.start(container, TEST_PORT);

        RestAssured.baseURI = "http://localhost:" + TEST_PORT;
        RestAssured.basePath = "/" + Routes.getApiVersion();
    }

    @BeforeEach
    void setUp()
    {
        seeded = TestPopulator.populateUsers(emf);
    }

    @AfterAll
    static void shutDown()
    {
        ApplicationConfig.stop(app);
        emf.close();
    }

    @Test
    void testGetAllActiveUsers()
    {
        given()
                .when()
                .get("/users?active=true")
                .then()
                .statusCode(200)
                .body("email", containsInAnyOrder(seeded.values()
                        .stream()
                        .filter(User::isActive)
                        .map(User::getEmail)
                        .toArray()));
    }

    @Test
    void testGetAllInactiveUsers()
    {
        given()
                .when()
                .get("/users?active=false")
                .then()
                .statusCode(200)
                .body("email", containsInAnyOrder(seeded.values()
                        .stream()
                        .filter(user -> !user.isActive())
                        .map(User::getEmail)
                        .toArray()));
    }

    @Test
    void testgetById()
    {
        User user1 = seeded.get("user1");

        given()
                .when()
                .get("/users/" + user1.getUserId())
                .then()
                .statusCode(200)
                .body("id", equalTo(user1.getUserId()))
                .body("email", equalTo(user1.getEmail()))
                .body("firstName", equalTo(user1.getFirstName()));
    }

    @Test
    void testGetByIdFails()
    {
        given()
                .when()
                .get("/users/999999")
                .then()
                .statusCode(404);
    }

    @Test
    void testPostNewUser()
    {
        given()
                .contentType("application/json")
                .body("""
                        {
                            "firstName": "Test",
                            "lastName": "User",
                            "email": "test@example.com",
                            "phone": "12345678",
                            "role": "TECHNICIAN",
                            "password": "password123"
                        }
                        """)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("firstName", equalTo("Test"))
                .body("lastName", equalTo("User"))
                .body("email", equalTo("test@example.com"))
                .body("phone", equalTo("12345678"))
                .body("role", equalTo("TECHNICIAN"))
                .body("password", nullValue());
    }

    @Test
    void testPostExistingEmailReturns409()
    {
        User user1 = seeded.get("user1");

        given()
                .contentType("application/json")
                .body(String.format("""
                        {
                            "firstName": "Test",
                            "lastName": "User",
                            "email": "%s",
                            "phone": "12345678",
                            "role": "TECHNICIAN",
                            "password": "password123"
                        }
                        """, user1.getEmail()))
                .when()
                .post("/users")
                .then()
                .statusCode(409);
    }

    @Test
    void testPut()
    {
        User user1 = seeded.get("user1");

        given()
                .contentType("application/json")
                .body("""
                        {
                            "firstName": "Test",
                            "lastName": "User",
                            "email": "test@example.com",
                            "phone": "12345678",
                            "role": "TECHNICIAN",
                            "active": true
                        }
                        """)
                .when()
                .put("/users/" + user1.getUserId())
                .then()
                .statusCode(200)
                .body("id", equalTo(user1.getUserId()))
                .body("firstName", equalTo("Test"))
                .body("lastName", equalTo("User"))
                .body("email", equalTo("test@example.com"))
                .body("phone", equalTo("12345678"))
                .body("role", equalTo("TECHNICIAN"))
                .body("active", equalTo(true));
    }

    @Test
    void testPatchActivate()
    {

        User user1 = seeded.values().stream().filter(user -> !user.isActive()).findAny().get();

        given()
                .when()
                .patch("/users/" + user1.getUserId())
                .then()
                .statusCode(204);
    }

    @Test
    void testPatchActivateAlreadyActiveUser()
    {
        User activeUser = seeded.values().stream().filter(User::isActive).findAny().orElseThrow();

        given()
                .when()
                .patch("/users/" + activeUser.getUserId())
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/users/" + activeUser.getUserId())
                .then()
                .statusCode(200)
                .body("active", equalTo(true));
    }

    @Test
    void testDeleteDeactivate()
    {
        User user1 = seeded.values().stream().filter(User::isActive).findAny().get();
        given()
                .when()
                .delete("/users/" + user1.getUserId())
                .then()
                .statusCode(204);
    }

    @Test
    void testPutInvalidEmailFormatReturns400()
    {
        User user1 = seeded.get("user1");

        given()
                .contentType("application/json")
                .body("""
                        {
                            "firstName": "Test",
                            "lastName": "User",
                            "email": "not-an-email",
                            "phone": "12345678",
                            "role": "TECHNICIAN",
                            "active": true
                        }
                        """)
                .when()
                .put("/users/" + user1.getUserId())
                .then()
                .statusCode(400);
    }
}