package app.persistence.testutils;

import app.entities.enums.LogStatus;
import app.entities.enums.TaskType;
import app.entities.enums.UserRole;
import app.entities.model.Asset;
import app.entities.model.MaintenanceLog;
import app.entities.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class TestPopulator
{
    public static Map<String, User> populateUsers(EntityManagerFactory emf)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            User user1 = new User("John", "Doe", "12345678", "Johndoe@mail.dk", UserRole.TECHNICIAN, true);
            User user2 = new User("Jane", "Doe", "23456789", "Janedoe@mail.dk", UserRole.MANAGER, true);
            User user3 = new User("Jeff", "Doe", "34567890", "Jeffdoe@mail.dk", UserRole.ADMIN, true);
            User user4 = new User("Clark", "Kent", "00000000", "Clarkkent@mail.dk", UserRole.TECHNICIAN, false);

            try
            {
                em.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY CASCADE").executeUpdate();
                em.persist(user1);
                em.persist(user2);
                em.persist(user3);
                em.persist(user4);
                em.flush();
            }
            catch (PersistenceException e)
            {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }

            em.getTransaction().commit();

            Map<String, User> seeded = new LinkedHashMap<>();
            seeded.put("user1", user1);
            seeded.put("user2", user2);
            seeded.put("user3", user3);
            seeded.put("user4", user4);
            return seeded;
        }
    }

    public static Map<String, Asset> populateAssets(EntityManagerFactory emf)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            Asset asset1 = new Asset("Machine A", "Primary production machine", true, null);
            Asset asset2 = new Asset("Machine B", "Secondary production machine", true, null);
            Asset asset3 = new Asset("Machine C", "Backup machine", true, null);
            Asset asset4 = new Asset("Machine D", "Decommissioned machine", false, null);

            try
            {
                em.createNativeQuery("TRUNCATE TABLE assets RESTART IDENTITY CASCADE").executeUpdate();
                em.persist(asset1);
                em.persist(asset2);
                em.persist(asset3);
                em.persist(asset4);
                em.flush();
            }
            catch (PersistenceException e)
            {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }

            em.getTransaction().commit();

            Map<String, Asset> seeded = new LinkedHashMap<>();
            seeded.put("asset1", asset1);
            seeded.put("asset2", asset2);
            seeded.put("asset3", asset3);
            seeded.put("asset4", asset4);
            return seeded;
        }
    }

    public static Map<String, MaintenanceLog> populateMaintenanceLogs(EntityManagerFactory emf, Map<String, User> users, Map<String, Asset> assets)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();

            User user1 = em.merge(users.get("user1"));
            User user2 = em.merge(users.get("user2"));
            Asset asset1 = em.merge(assets.get("asset1"));
            Asset asset2 = em.merge(assets.get("asset2"));
            Asset asset3 = em.merge(assets.get("asset3"));
            Asset asset4 = em.merge(assets.get("asset4"));

            MaintenanceLog log1 = new MaintenanceLog(LocalDate.of(2024, 1, 15), LogStatus.DONE, TaskType.MAINTENANCE, "Regular maintenance completed", asset1, user1);
            MaintenanceLog log2 = new MaintenanceLog(LocalDate.of(2024, 2, 10), LogStatus.DONE, TaskType.PRODUCTION, "Production run successful", asset1, user1);
            MaintenanceLog log3 = new MaintenanceLog(LocalDate.of(2024, 3, 5), LogStatus.FAILED, TaskType.ERROR, "Error occurred during operation", asset2, user2);
            MaintenanceLog log4 = new MaintenanceLog(LocalDate.of(2024, 4, 20), LogStatus.DONE, TaskType.MAINTENANCE, "Preventive maintenance", asset2, user1);
            MaintenanceLog log5 = new MaintenanceLog(LocalDate.of(2024, 5, 15), LogStatus.DONE, TaskType.PRODUCTION, "Production completed", asset3, user2);
            MaintenanceLog log6 = new MaintenanceLog(LocalDate.of(2024, 6, 1), LogStatus.FAILED, TaskType.ERROR, "Machine malfunction", asset4, user1);

            try
            {
                em.createNativeQuery("TRUNCATE TABLE maintenance_logs RESTART IDENTITY CASCADE").executeUpdate();
                em.persist(log1);
                em.persist(log2);
                em.persist(log3);
                em.persist(log4);
                em.persist(log5);
                em.persist(log6);
                em.flush();
            }
            catch (PersistenceException e)
            {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }

            em.getTransaction().commit();

            Map<String, MaintenanceLog> seeded = new LinkedHashMap<>();
            seeded.put("log1", log1);
            seeded.put("log2", log2);
            seeded.put("log3", log3);
            seeded.put("log4", log4);
            seeded.put("log5", log5);
            seeded.put("log6", log6);
            return seeded;
        }
    }
}