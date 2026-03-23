package app.controllers.routes;

import app.config.ApplicationConfig;
import app.config.DependencyContainer;
import app.config.HibernateTestConfig;
import app.entities.Asset;
import app.entities.Employee;
import app.entities.MaintenanceLog;
import app.persistence.testutils.TestPopulator;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class MaintenanceLogRoutesTest
{
    private static EntityManagerFactory emf;
    private static DependencyContainer container;
    private static Javalin app;
    private static final int TEST_PORT = 7073;

    private Map<String, Employee> employees;
    private Map<String, Asset> assets;
    private Map<String, MaintenanceLog> logs;
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
        employees = TestPopulator.populateEmployees(emf);
        assets = TestPopulator.populateAssets(emf);
        logs = TestPopulator.populateMaintenanceLogs(emf, employees, assets);

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
    void testGetAllLogs()
    {
        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .when()
                .get("/logs")
                .then()
                .statusCode(200)
                .body("size()", is(6));
    }

    @Test
    void testGetLogsByStatus()
    {
        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .when()
                .get("/logs?status=DONE")
                .then()
                .statusCode(200)
                .body("status", everyItem(equalTo("DONE")))
                .body("size()", is(4));
    }

    @Test
    void testGetLogsByStatusInvalid()
    {
        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .when()
                .get("/logs?status=not-a-status")
                .then()
                .statusCode(400);
    }

    @Test
    void testGetById()
    {
        MaintenanceLog log1 = logs.get("log1");

        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .when()
                .get("/logs/" + log1.getLogId())
                .then()
                .statusCode(200)
                .body("id", equalTo(log1.getLogId()))
                .body("status", equalTo(log1.getStatus().name()))
                .body("taskType", equalTo(log1.getTaskType().name()));
    }

    @Test
    void testGetByIdFails()
    {
        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .when()
                .get("/logs/999999")
                .then()
                .statusCode(404);
    }

    @Test
    void testGetByEmployee()
    {
        Employee employee1 = employees.get("employee1");

        given()
                .header("Authorization", "Bearer " + managerToken)
                .when()
                .get("/logs/employee/" + employee1.getEmployeeId())
                .then()
                .statusCode(200)
                .body("performedByEmployeeId", everyItem(equalTo(employee1.getEmployeeId())))
                .body("size()", is(4));
    }

    @Test
    void testGetLogsNoAccessWithoutToken()
    {
        given()
                .when()
                .get("/logs")
                .then()
                .statusCode(403);
    }

    @Test
    void testGetLogByIdNoAccessWithoutToken()
    {
        MaintenanceLog log1 = logs.get("log1");

        given()
                .when()
                .get("/logs/" + log1.getLogId())
                .then()
                .statusCode(403);
    }

    @Test
    void testGetLogsByEmployeeNoAccessWithoutToken()
    {
        Employee employee1 = employees.get("employee1");

        given()
                .when()
                .get("/logs/employee/" + employee1.getEmployeeId())
                .then()
                .statusCode(403);
    }

    @Test
    void testGetLogsByEmployeeForbiddenForAuthenticated()
    {
        Employee employee1 = employees.get("employee1");

        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .when()
                .get("/logs/employee/" + employee1.getEmployeeId())
                .then()
                .statusCode(403);
    }
}