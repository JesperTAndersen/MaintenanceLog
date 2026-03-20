package app.persistence.testutils;

import app.entities.Employee;
import app.entities.enums.LogStatus;
import app.entities.enums.TaskType;
import app.entities.enums.EmployeeRole;
import app.entities.Asset;
import app.entities.MaintenanceLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class TestPopulator
{
    public static Map<String, Employee> populateEmployees(EntityManagerFactory emf)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            Employee employee1 = new Employee("John", "Doe", "12345678", "Johndoe@mail.dk", EmployeeRole.TECHNICIAN, true);
            Employee employee2 = new Employee("Jane", "Doe", "23456789", "Janedoe@mail.dk", EmployeeRole.MANAGER, true);
            Employee employee3 = new Employee("Jeff", "Doe", "34567890", "Jeffdoe@mail.dk", EmployeeRole.ADMIN, true);
            Employee employee4 = new Employee("Clark", "Kent", "00000000", "Clarkkent@mail.dk", EmployeeRole.TECHNICIAN, false);

            try
            {
                em.createNativeQuery("TRUNCATE TABLE employees RESTART IDENTITY CASCADE").executeUpdate();
                em.persist(employee1);
                em.persist(employee2);
                em.persist(employee3);
                em.persist(employee4);
                em.flush();
            }
            catch (PersistenceException e)
            {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }

            em.getTransaction().commit();

            Map<String, Employee> seeded = new LinkedHashMap<>();
            seeded.put("employee1", employee1);
            seeded.put("employee2", employee2);
            seeded.put("employee3", employee3);
            seeded.put("employee4", employee4);
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

    public static Map<String, MaintenanceLog> populateMaintenanceLogs(EntityManagerFactory emf, Map<String, Employee> employees, Map<String, Asset> assets)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();

            Employee employee1 = em.merge(employees.get("employee1"));
            Employee employee2 = em.merge(employees.get("employee2"));
            Asset asset1 = em.merge(assets.get("asset1"));
            Asset asset2 = em.merge(assets.get("asset2"));
            Asset asset3 = em.merge(assets.get("asset3"));
            Asset asset4 = em.merge(assets.get("asset4"));

            MaintenanceLog log1 = new MaintenanceLog(LocalDateTime.of(2024, 1, 15, 10, 0), LogStatus.DONE, TaskType.MAINTENANCE, "Regular maintenance completed", asset1, employee1);
            MaintenanceLog log2 = new MaintenanceLog(LocalDateTime.of(2024, 2, 10, 14, 30), LogStatus.DONE, TaskType.PRODUCTION, "Production run successful", asset1, employee1);
            MaintenanceLog log3 = new MaintenanceLog(LocalDateTime.of(2024, 3, 5, 9, 15), LogStatus.FAILED, TaskType.ERROR, "Error occurred during operation", asset2, employee2);
            MaintenanceLog log4 = new MaintenanceLog(LocalDateTime.of(2024, 4, 20, 11, 45), LogStatus.DONE, TaskType.MAINTENANCE, "Preventive maintenance", asset2, employee1);
            MaintenanceLog log5 = new MaintenanceLog(LocalDateTime.of(2024, 5, 15, 16, 20), LogStatus.DONE, TaskType.PRODUCTION, "Production completed", asset3, employee2);
            MaintenanceLog log6 = new MaintenanceLog(LocalDateTime.of(2024, 6, 1, 8, 0), LogStatus.FAILED, TaskType.ERROR, "Machine malfunction", asset4, employee1);

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