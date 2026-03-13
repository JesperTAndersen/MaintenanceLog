package app.controllers.routes;

import app.config.AppConfig;
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

import static io.restassured.RestAssured.*;
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
        app = AppConfig.start(container, TEST_PORT);

        RestAssured.baseURI = "http://localhost:" + TEST_PORT;
//        RestAssured.basePath = "/" + Routes.getAPI_VERSION();
    }

    @BeforeEach
    void setUp()
    {
        seeded = TestPopulator.populateUsers(emf);
    }

    @AfterAll
    static void shutDown()
    {
        AppConfig.stop(app);
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
    void get()
    {
    }

    void post()
    {
    }

    void put()
    {
    }

    void patch()
    {
    }

    void delete()
    {
    }
}