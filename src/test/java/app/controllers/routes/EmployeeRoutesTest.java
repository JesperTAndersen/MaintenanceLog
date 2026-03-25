package app.controllers.routes;

import app.config.ApplicationConfig;
import app.config.DependencyContainer;
import app.config.HibernateTestConfig;
import app.entities.Employee;
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
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class EmployeeRoutesTest
{
    private static EntityManagerFactory emf;
    private static DependencyContainer container;
    private static Javalin app;
    private static final int TEST_PORT = 7072;
    private Map<String, Employee> seeded;
    private static String authenticatedToken;
    private static String managerToken;
    private static String adminToken;

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
        seeded = TestPopulator.populateEmployees(emf);

        authenticatedToken = loginAsEmployee("Johndoe@mail.dk", "password123");
        managerToken = loginAsEmployee("Janedoe@mail.dk", "password123");
        adminToken = loginAsEmployee("Jeffdoe@mail.dk", "password123");
    }

    private String loginAsEmployee(String email, String password)
    {
        return given()
                .contentType("application/json")
                .body(String.format("""
                        {
                            "email": "%s",
                            "password": "%s"
                        }
                        """, email, password))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    @AfterAll
    static void shutDown()
    {
        ApplicationConfig.stop(app);
        emf.close();
    }

    @Test
    void testGetAllActiveEmployees()
    {
        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .when()
                .get("/employees?active=true")
                .then()
                .statusCode(200)
                .body("email", containsInAnyOrder(seeded.values()
                        .stream()
                        .filter(Employee::isActive)
                        .map(Employee::getEmail)
                        .toArray()));
    }

    @Test
    void testGetAllInactiveEmployees()
    {
        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .when()
                .get("/employees?active=false")
                .then()
                .statusCode(200)
                .body("email", containsInAnyOrder(seeded.values()
                        .stream()
                        .filter(employee -> !employee.isActive())
                        .map(Employee::getEmail)
                        .toArray()));
    }

    @Test
    void testGetById()
    {
        Employee employee1 = seeded.get("employee1");

        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .when()
                .get("/employees/" + employee1.getEmployeeId())
                .then()
                .statusCode(200)
                .body("id", equalTo(employee1.getEmployeeId()))
                .body("email", equalTo(employee1.getEmail()))
                .body("firstName", equalTo(employee1.getFirstName()));
    }

    @Test
    void testGetByIdFails()
    {
        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .when()
                .get("/employees/999999")
                .then()
                .statusCode(404);
    }

    @Test
    void testPut()
    {
        Employee employee1 = seeded.get("employee1");

        given()
                .header("Authorization", "Bearer " + managerToken)
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
                .put("/employees/" + employee1.getEmployeeId())
                .then()
                .statusCode(200)
                .body("id", equalTo(employee1.getEmployeeId()))
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

        Employee employee1 = seeded.values().stream().filter(employee -> !employee.isActive()).findAny().get();

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .patch("/employees/" + employee1.getEmployeeId())
                .then()
                .statusCode(204);
    }

    @Test
    void testPatchActivateAlreadyActiveEmployee()
    {
        Employee activeEmployee = seeded.values().stream().filter(Employee::isActive).findAny().orElseThrow();

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .patch("/employees/" + activeEmployee.getEmployeeId())
                .then()
                .statusCode(204);

        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .when()
                .get("/employees/" + activeEmployee.getEmployeeId())
                .then()
                .statusCode(200)
                .body("active", equalTo(true));
    }

    @Test
    void testDeleteDeactivate()
    {
        Employee employee1 = seeded.values().stream().filter(Employee::isActive).findAny().get();
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/employees/" + employee1.getEmployeeId())
                .then()
                .statusCode(204);
    }

    @Test
    void testPutInvalidEmailFormatReturns400()
    {
        Employee employee1 = seeded.get("employee1");

        given()
                .header("Authorization", "Bearer " + managerToken)
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
                .put("/employees/" + employee1.getEmployeeId())
                .then()
                .statusCode(400);
    }

    @Test
    void testGetEmployeesNoAccessWithoutToken()
    {
        given()
                .when()
                .get("/employees?active=true")
                .then()
                .statusCode(403);
    }

    @Test
    void testGetEmployeeByIdNoAccessWithoutToken()
    {
        Employee employee1 = seeded.get("employee1");

        given()
                .when()
                .get("/employees/" + employee1.getEmployeeId())
                .then()
                .statusCode(403);
    }

    @Test
    void testPutEmployeeNoAccessWithoutToken()
    {
        Employee employee1 = seeded.get("employee1");

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
                .put("/employees/" + employee1.getEmployeeId())
                .then()
                .statusCode(403);
    }

    @Test
    void testPatchEmployeeNoAccessWithoutToken()
    {
        Employee inactiveEmployee = seeded.values().stream().filter(employee -> !employee.isActive()).findAny().orElseThrow();

        given()
                .when()
                .patch("/employees/" + inactiveEmployee.getEmployeeId())
                .then()
                .statusCode(403);
    }

    @Test
    void testDeleteEmployeeNoAccessWithoutToken()
    {
        Employee activeEmployee = seeded.values().stream().filter(Employee::isActive).findAny().orElseThrow();

        given()
                .when()
                .delete("/employees/" + activeEmployee.getEmployeeId())
                .then()
                .statusCode(403);
    }

    @Test
    void testPutEmployeeForbiddenForAuthenticated()
    {
        Employee employee1 = seeded.get("employee1");

        given()
                .header("Authorization", "Bearer " + authenticatedToken)
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
                .put("/employees/" + employee1.getEmployeeId())
                .then()
                .statusCode(403);
    }

    @Test
    void testPatchEmployeeForbiddenForManager()
    {
        Employee inactiveEmployee = seeded.values().stream().filter(employee -> !employee.isActive()).findAny().orElseThrow();

        given()
                .header("Authorization", "Bearer " + managerToken)
                .when()
                .patch("/employees/" + inactiveEmployee.getEmployeeId())
                .then()
                .statusCode(403);
    }

    @Test
    void testDeleteEmployeeForbiddenForManager()
    {
        Employee activeEmployee = seeded.values().stream().filter(Employee::isActive).findAny().orElseThrow();

        given()
                .header("Authorization", "Bearer " + managerToken)
                .when()
                .delete("/employees/" + activeEmployee.getEmployeeId())
                .then()
                .statusCode(403);
    }
}