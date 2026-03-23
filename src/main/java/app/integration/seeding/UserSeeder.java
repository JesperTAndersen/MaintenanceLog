package app.integration.seeding;

import app.config.hibernate.HibernateConfig;
import app.integration.RandomUserClient;
import app.integration.RandomUserDTO;
import app.persistence.EmployeeDAO;
import app.utils.APIReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class UserSeeder {

    public static void main(String[] args) {
        System.out.println("User Seeder Demo \n");

        // Setup
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        EmployeeDAO employeeDao = new EmployeeDAO(emf);
        ObjectMapper objectMapper = new ObjectMapper();
        APIReader apiReader = new APIReader(objectMapper);
        RandomUserClient client = new RandomUserClient(apiReader);

        // Test threading performance
        System.out.println("1. Testing Sequential vs Concurrent API Calls:");
        testThreads(client);

        // Seed employees
        System.out.println("\n2. Seeding Employees:");
        seedEmployees(employeeDao);

        System.out.println("\nDemo Complete");
        emf.close();
    }

    public static void seedEmployees(EmployeeDAO employeeDao) {
        ObjectMapper objectMapper = new ObjectMapper();
        APIReader apiReader = new APIReader(objectMapper);
        RandomUserClient client = new RandomUserClient(apiReader);
        ApiUserService apiUserService = new ApiUserServiceImpl(client, employeeDao);

        System.out.println("Seeding 50 employees...");
        apiUserService.seedEmployees(50, false, 0);
        System.out.println("Seeding complete!");
    }

    public static void testThreads(RandomUserClient client) {
        // Sequential timing
        System.out.println("Running sequential API calls (5x10 users)...");
        long startSeq = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            client.fetchUsersFromAPI(10);
        }
        long endSeq = System.currentTimeMillis();
        System.out.println("Sequential time: " + (endSeq - startSeq) + "ms");

        // Concurrent timing
        System.out.println("\nRunning concurrent API calls (5 threads, 50 total users)...");
        long startCon = System.currentTimeMillis();
        List<RandomUserDTO> employees = client.fetchUsersFromAPIMultiThreaded(5, 50);
        long endCon = System.currentTimeMillis();

        System.out.println("Concurrent time: " + (endCon - startCon) + "ms");
        System.out.println("Employees fetched: " + employees.size());
        System.out.println("Speedup: " + String.format("%.2f", (endSeq - startSeq) / (double) (endCon - startCon)) + "x");
    }
}