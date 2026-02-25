package app.integration.client;

import app.integration.dto.RandomUserDTO;
import app.integration.util.APIReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

public class RandomUserClient
{
    private final APIReader apiReader;

    private final String endpoint = "https://randomuser.me/api/?results=%d";

    public RandomUserClient(APIReader apiReader)
    {
        this.apiReader = apiReader;
    }

    public List<RandomUserDTO> fetchUsersFromAPI(int amount)
    {
        String formatted = String.format(Locale.US, endpoint, amount);

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
                try //if a batch fails, it just continues with the rest and return however many it got back
                {
                    List<RandomUserDTO> userDTOList = f.get();
                    users.addAll(userDTOList);
                }
                catch (ExecutionException e)
                {
                    System.out.println("Batch failed: " + e.getCause().getMessage());
                }
            }

            return users;
        }
        catch (InterruptedException e)
        {
            System.out.println("Thread interrupted: " + e.getCause().getMessage());
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
