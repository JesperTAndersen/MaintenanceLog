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

class EmployeeRoutesTest
{
    private static EntityManagerFactory emf;
    private static DependencyContainer container;
    private static Javalin app;
    private static final int TEST_PORT = 7071;
    private Map<String, Employee> seeded;

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
    void testgetById()
    {
        Employee employee1 = seeded.get("employee1");

        given()
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
                .when()
                .get("/employees/999999")
                .then()
                .statusCode(404);
    }

    @Test
    void testPostNewEmployee()
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
                .post("/employees")
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
        Employee employee1 = seeded.get("employee1");

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
                        """, employee1.getEmail()))
                .when()
                .post("/employees")
                .then()
                .statusCode(409);
    }

    @Test
    void testPut()
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
                .when()
                .patch("/employees/" + activeEmployee.getEmployeeId())
                .then()
                .statusCode(204);

        given()
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
}