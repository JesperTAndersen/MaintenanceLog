package app.controllers.routes;

import app.config.AppConfig;
import app.config.DependencyContainer;
import app.config.HibernateTestConfig;
import app.entities.Asset;
import app.entities.MaintenanceLog;
import app.entities.User;
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

    private Map<String, User> users;
    private Map<String, Asset> assets;
    private Map<String, MaintenanceLog> logs;

    @BeforeAll
    public static void init()
    {
        emf = HibernateTestConfig.getEntityManagerFactory();
        container = new DependencyContainer(emf);
        app = AppConfig.start(container, TEST_PORT);

        RestAssured.baseURI = "http://localhost:" + TEST_PORT;
        RestAssured.basePath = "/" + Routes.getApiVersion();
    }

    @BeforeEach
    void setUp()
    {
        users = TestPopulator.populateUsers(emf);
        assets = TestPopulator.populateAssets(emf);
        logs = TestPopulator.populateMaintenanceLogs(emf, users, assets);
    }

    @AfterAll
    static void shutDown()
    {
        AppConfig.stop(app);
        emf.close();
    }

    @Test
    void testGetAllLogs()
    {
        given()
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
                .when()
                .get("/logs/999999")
                .then()
                .statusCode(404);
    }

    @Test
    void testGetByUser()
    {
        User user1 = users.get("user1");

        given()
                .when()
                .get("/logs/user/" + user1.getUserId())
                .then()
                .statusCode(200)
                .body("performedByUserId", everyItem(equalTo(user1.getUserId())))
                .body("size()", is(4));
    }

    @Test
    void testGetLogsOnActiveAssetsDefaultLimit()
    {
        Asset inactiveAsset = assets.get("asset4");

        given()
                .when()
                .get("/logs/active-assets")
                .then()
                .statusCode(200)
                .body("size()", is(5))
                .body("assetId", not(hasItem(inactiveAsset.getAssetId())));
    }

    @Test
    void testGetLogsOnActiveAssetsWithLimit()
    {
        given()
                .when()
                .get("/logs/active-assets?limit=2")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }
}

