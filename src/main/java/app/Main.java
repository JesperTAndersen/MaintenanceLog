package app;

import app.entities.model.User;
import app.integration.client.RandomUserClient;
import app.integration.dto.RandomUserDTO;
import app.integration.util.APIReader;
import app.persistence.config.HibernateConfig;

import app.persistence.daos.UserDAO;
import app.persistence.interfaces.IDAO;
import app.services.ApiUserService;
import app.services.ApiUserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.ArrayList;
import java.util.List;


public class Main
{
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    public static void main(String[] args)
    {
        EntityManager em = emf.createEntityManager();

        ObjectMapper objectMapper = new ObjectMapper();
        APIReader apiReader = new APIReader(objectMapper);
        RandomUserClient client = new RandomUserClient(apiReader);

        IDAO<User> userDao = new UserDAO(emf);

        ApiUserService apiUserService = new ApiUserServiceImpl(client, userDao);

        apiUserService.seedUsers(50, false,0);


        // Close the database connection:
        em.close();
        emf.close();

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