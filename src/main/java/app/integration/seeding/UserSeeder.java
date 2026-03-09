package app.integration.seeding;

import app.entities.User;
import app.integration.RandomUserClient;
import app.integration.RandomUserDTO;
import app.utils.APIReader;
import app.persistence.interfaces.IDAO;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class UserSeeder
{
    public static void seedUsers(IDAO<User> userDao)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        APIReader apiReader = new APIReader(objectMapper);
        RandomUserClient client = new RandomUserClient(apiReader);
        ApiUserService apiUserService = new ApiUserServiceImpl(client, userDao);
        apiUserService.seedUsers(50, false, 0);
    }

    public static void testThreads(RandomUserClient client)
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