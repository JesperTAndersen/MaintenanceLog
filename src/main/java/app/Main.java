package app;

import app.controllers.AssetController;
import app.controllers.LogController;
import app.controllers.UserController;
import app.controllers.routes.Routes;
import app.entities.model.User;
import app.exceptions.ApiException;
import app.integration.client.RandomUserClient;
import app.integration.dto.RandomUserDTO;
import app.integration.util.APIReader;
import app.persistence.config.HibernateConfig;
import app.persistence.daos.UserDAO;
import app.persistence.interfaces.IDAO;
import app.persistence.interfaces.IUserDAO;
import app.services.ApiUserService;
import app.services.ApiUserServiceImpl;
import app.services.UserService;
import app.services.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Map;

public class Main
{
    //TODO: SET UP LOGGING USING LOGBACK
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    public static void main(String[] args)
    {
        IDAO<User> userDao = new UserDAO(emf);
        IUserDAO userDaoExpanded = new UserDAO(emf);

        UserService userService = new UserServiceImpl(userDao, userDaoExpanded);
        UserController userController = new UserController(userService);

        AssetController assetController = new AssetController();
        LogController logController = new LogController();

        Routes routes = new Routes(userController, assetController, logController);

        Javalin app = Javalin.create(config ->
        {
            config.bundledPlugins.enableRouteOverview("/routes");
            config.routes.apiBuilder(routes.getRoutes());

            config.routes.exception(ApiException.class, (e, ctx) ->
            {
                ctx.status(e.getCode()).json(Map.of("error", e.getMessage()));
            });

            config.routes.exception(RuntimeException.class, (e, ctx) ->
            {
                ctx.status(400).json(e.getMessage());
            });

        }).start(7070);

    }

    private static void seedUsers(IDAO<User> userDao)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        APIReader apiReader = new APIReader(objectMapper);
        RandomUserClient client = new RandomUserClient(apiReader);
        ApiUserService apiUserService = new ApiUserServiceImpl(client, userDao);
        apiUserService.seedUsers(50, false, 0);
    }

    private static void testThreads(RandomUserClient client)
    {
        // Sequential timing
        long startSeq = System.currentTimeMillis();
        for (int i = 0; i < 5; i++)
        {
            client.fetchUsersFromAPI(10);
        }
        long endSeq = System.currentTimeMillis();
        System.out.println("Sequential (5x10): " + (endSeq - startSeq) + "ms");

        // Concurrent timing
        long startCon = System.currentTimeMillis();
        List<RandomUserDTO> users = client.fetchUsersFromAPIMultiThreaded(5, 50);
        long endCon = System.currentTimeMillis();
        System.out.println("Concurrent (5 threads, 50 total): " + (endCon - startCon) + "ms");
        System.out.println("Users fetched: " + users.size());
        System.out.println("Speedup: " + ((endSeq - startSeq) / (double) (endCon - startCon)) + "x");
    }
}