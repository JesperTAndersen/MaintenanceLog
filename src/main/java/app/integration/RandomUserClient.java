package app.integration;

import app.utils.APIReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

public class RandomUserClient
{
    private final APIReader apiReader;
    private final String endpointFixed = "https://randomuser.me/api/?results=%d&nat=gb,dk&inc=name,login,email,phone&seed=myfixedseed123"; //returns a number of users of choice, but with the same specific information each time
    private final String endpointRandom = "https://randomuser.me/api/?results=%d&nat=gb,dk&inc=name,login,email,phone";

    public RandomUserClient(APIReader apiReader)
    {
        this.apiReader = apiReader;
    }

    private static final Logger log = LoggerFactory.getLogger(RandomUserClient.class);

    public List<RandomUserDTO> fetchUsersFromAPI(int amount)
    {
        String formatted = String.format(Locale.US, endpointFixed, amount);

        return apiReader.getAndConvertDataList(formatted, RandomUserDTO.class);
    }

    public List<RandomUserDTO> fetchUsersFromAPIMultiThreaded(int threads, int totalUsers)
    {
        List<RandomUserDTO> users = new ArrayList<>();

        int usersPerThread = totalUsers / threads;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        try
        {
            List<Callable<List<RandomUserDTO>>> callables = new ArrayList<>();

            for (int i = 0; i < threads; i++)
            {
                callables.add(fetchUsers(usersPerThread));
            }

            List<Future<List<RandomUserDTO>>> futures = executor.invokeAll(callables);

            awaitTerminationAfterShutdown(executor);

            for (Future<List<RandomUserDTO>> f : futures)
            {
                try
                {
                    users.addAll(f.get());
                }
                catch (ExecutionException e)
                {
                    log.error("Batch failed to fetch users", e.getCause());
                }
            }

            log.info("Successfully fetched {} users from API", users.size());
            return users;
        }
        catch (InterruptedException e)
        {
            log.warn("User fetching interrupted, returning partial results ({} users)", users.size());
            Thread.currentThread().interrupt();
            return users;
        }
    }

    private Callable<List<RandomUserDTO>> fetchUsers(int amount)
    {
        return () ->
                fetchUsersFromAPI(amount);

    }

    private void awaitTerminationAfterShutdown(ExecutorService threadPool)
    {
        threadPool.shutdown();
        try
        {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS))
            {
                threadPool.shutdownNow();
            }
        }
        catch (InterruptedException ex)
        {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
