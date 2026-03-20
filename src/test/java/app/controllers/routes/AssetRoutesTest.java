package app.controllers.routes;

import app.config.ApplicationConfig;
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

class AssetRoutesTest
{
    private static EntityManagerFactory emf;
    private static DependencyContainer container;
    private static Javalin app;
    private static final int TEST_PORT = 7071;

    private Map<String, User> users;
    private Map<String, Asset> assets;
    private Map<String, MaintenanceLog> logs;

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
        users = TestPopulator.populateUsers(emf);
        assets = TestPopulator.populateAssets(emf);
        logs = TestPopulator.populateMaintenanceLogs(emf, users, assets);
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
                .when()
                .get("/assets/999999")
                .then()
                .statusCode(404);
    }

    @Test
    void testPostNewAsset()
    {
        given()
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
                .when()
                .patch("/assets/" + inactiveAsset.getAssetId())
                .then()
                .statusCode(204);

        given()
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
                .when()
                .delete("/assets/" + activeAsset.getAssetId())
                .then()
                .statusCode(204);

        given()
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
        User user1 = users.get("user1");

        given()
                .contentType("application/json")
                .body(String.format("""
                        {
                            "performedDate": "2024-07-01T10:00:00",
                            "status": "DONE",
                            "taskType": "MAINTENANCE",
                            "comment": "Test log",
                            "performedByUserId": %d
                        }
                        """, user1.getUserId()))
                .when()
                .post("/assets/" + asset1.getAssetId() + "/logs")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("assetId", equalTo(asset1.getAssetId()))
                .body("performedByUserId", equalTo(user1.getUserId()))
                .body("status", equalTo("DONE"))
                .body("taskType", equalTo("MAINTENANCE"));
    }
}

