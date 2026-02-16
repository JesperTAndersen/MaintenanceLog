package app.persistence.testutils;

import app.entities.enums.UserRole;
import app.entities.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestPopulator
{
    public static Map<String, User> populate(EntityManagerFactory emf)
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
}