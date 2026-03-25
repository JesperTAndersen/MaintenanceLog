package app.controllers.routes;

import app.config.ApplicationConfig;
import app.config.DependencyContainer;
import app.config.HibernateTestConfig;
import app.entities.Asset;
import app.entities.MaintenanceLog;
import app.entities.Employee;
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
import org.testcontainers.junit.jupiter.Testcontainers;


class AssetRoutesTest
{
    private static EntityManagerFactory emf;
    private static DependencyContainer container;
    private static Javalin app;
    private static final int TEST_PORT = 7071;
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
    void testGetAllActiveAssets()
    {
        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .when()
                .get("/assets?active=true")
                .then()
                .statusCode(200)
                .body("name", containsInAnyOrder(
                        assets.get("asset1").getName(),
                        assets.get("asset2").getName(),
                        assets.get("asset3").getName()))
                .body("name", not(hasItem(assets.get("asset4").getName())));
    }

    @Test
    void testGetAllInactiveAssets()
    {
        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .when()
                .get("/assets?active=false")
                .then()
                .statusCode(200)
                .body("name", contains(assets.get("asset4").getName()));
    }

    @Test
    void testGetById()
    {
        Asset asset1 = assets.get("asset1");

        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .when()
                .get("/assets/" + asset1.getAssetId())
                .then()
                .statusCode(200)
                .body("id", equalTo(asset1.getAssetId()))
                .body("name", equalTo(asset1.getName()))
                .body("description", equalTo(asset1.getDescription()));
    }

    @Test
    void testGetByIdFails()
    {
        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .when()
                .get("/assets/999999")
                .then()
                .statusCode(404);
    }

    @Test
    void testPostNewAsset()
    {
        given()
                .header("Authorization", "Bearer " + managerToken)
                .contentType("application/json")
                .body("""
                        {
                            "name": "Machine X",
                            "description": "Test asset",
                            "active": true
                        }
                        """)
                .when()
                .post("/assets")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Machine X"))
                .body("description", equalTo("Test asset"))
                .body("active", equalTo(true));
    }

    @Test
    void testPatchActivate()
    {
        Asset inactiveAsset = assets.get("asset4");

        given()
                .header("Authorization", "Bearer " + managerToken)
                .when()
                .patch("/assets/" + inactiveAsset.getAssetId())
                .then()
                .statusCode(204);

        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .when()
                .get("/assets/" + inactiveAsset.getAssetId())
                .then()
                .statusCode(200)
                .body("active", equalTo(true));
    }

    @Test
    void testDeleteDeactivate()
    {
        Asset activeAsset = assets.get("asset1");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/assets/" + activeAsset.getAssetId())
                .then()
                .statusCode(204);

        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .when()
                .get("/assets/" + activeAsset.getAssetId())
                .then()
                .statusCode(200)
                .body("active", equalTo(false));
    }

    @Test
    void testGetLogsByAsset()
    {
        Asset asset1 = assets.get("asset1");

        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .when()
                .get("/assets/" + asset1.getAssetId() + "/logs")
                .then()
                .statusCode(200)
                .body("assetId", everyItem(equalTo(asset1.getAssetId())));
    }

    @Test
    void testPostLogForAsset()
    {
        Asset asset1 = assets.get("asset1");
        Employee employee1 = employees.get("employee1");

        given()
                .header("Authorization", "Bearer " + managerToken)
                .contentType("application/json")
                .body(String.format("""
                        {
                            "performedDate": "2024-07-01T10:00:00",
                            "status": "DONE",
                            "taskType": "MAINTENANCE",
                            "comment": "Test log",
                            "performedByEmployeeId": %d
                        }
                        """, employee1.getEmployeeId()))
                .when()
                .post("/assets/" + asset1.getAssetId() + "/logs")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("assetId", equalTo(asset1.getAssetId()))
                .body("performedByEmployeeId", equalTo(employee1.getEmployeeId()))
                .body("status", equalTo("DONE"))
                .body("taskType", equalTo("MAINTENANCE"));
    }

    @Test
    void testGetAssetsNoAccessWithoutToken()
    {
        given()
                .when()
                .get("/assets?active=true")
                .then()
                .statusCode(403);
    }

    @Test
    void testGetAssetByIdNoAccessWithoutToken()
    {
        Asset asset1 = assets.get("asset1");

        given()
                .when()
                .get("/assets/" + asset1.getAssetId())
                .then()
                .statusCode(403);
    }

    @Test
    void testPostAssetNoAccessWithoutToken()
    {
        given()
                .contentType("application/json")
                .body("""
                        {
                            "name": "Machine Z",
                            "description": "No token",
                            "active": true
                        }
                        """)
                .when()
                .post("/assets")
                .then()
                .statusCode(403);
    }

    @Test
    void testPatchAssetNoAccessWithoutToken()
    {
        Asset inactiveAsset = assets.get("asset4");

        given()
                .when()
                .patch("/assets/" + inactiveAsset.getAssetId())
                .then()
                .statusCode(403);
    }

    @Test
    void testDeleteAssetNoAccessWithoutToken()
    {
        Asset activeAsset = assets.get("asset1");

        given()
                .when()
                .delete("/assets/" + activeAsset.getAssetId())
                .then()
                .statusCode(403);
    }

    @Test
    void testGetAssetLogsNoAccessWithoutToken()
    {
        Asset asset1 = assets.get("asset1");

        given()
                .when()
                .get("/assets/" + asset1.getAssetId() + "/logs")
                .then()
                .statusCode(403);
    }

    @Test
    void testPostAssetLogNoAccessWithoutToken()
    {
        Asset asset1 = assets.get("asset1");
        Employee employee1 = employees.get("employee1");

        given()
                .contentType("application/json")
                .body(String.format("""
                        {
                            "performedDate": "2024-07-01T10:00:00",
                            "status": "DONE",
                            "taskType": "MAINTENANCE",
                            "comment": "No token",
                            "performedByEmployeeId": %d
                        }
                        """, employee1.getEmployeeId()))
                .when()
                .post("/assets/" + asset1.getAssetId() + "/logs")
                .then()
                .statusCode(403);
    }

    @Test
    void testPostAssetForbiddenForAuthenticated()
    {
        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .contentType("application/json")
                .body("""
                        {
                            "name": "Machine Y",
                            "description": "Should fail",
                            "active": true
                        }
                        """)
                .when()
                .post("/assets")
                .then()
                .statusCode(403);
    }

    @Test
    void testPatchAssetForbiddenForAuthenticated()
    {
        Asset inactiveAsset = assets.get("asset4");

        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .when()
                .patch("/assets/" + inactiveAsset.getAssetId())
                .then()
                .statusCode(403);
    }

    @Test
    void testDeleteAssetForbiddenForManager()
    {
        Asset activeAsset = assets.get("asset1");

        given()
                .header("Authorization", "Bearer " + managerToken)
                .when()
                .delete("/assets/" + activeAsset.getAssetId())
                .then()
                .statusCode(403);
    }

    @Test
    void testPostLogForAssetForbiddenForAuthenticated()
    {
        Asset asset1 = assets.get("asset1");
        Employee employee1 = employees.get("employee1");

        given()
                .header("Authorization", "Bearer " + authenticatedToken)
                .contentType("application/json")
                .body(String.format("""
                        {
                            "performedDate": "2024-07-01T10:00:00",
                            "status": "DONE",
                            "taskType": "MAINTENANCE",
                            "comment": "Should fail",
                            "performedByEmployeeId": %d
                        }
                        """, employee1.getEmployeeId()))
                .when()
                .post("/assets/" + asset1.getAssetId() + "/logs")
                .then()
                .statusCode(403);
    }
}

