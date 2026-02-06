package app.daos;

import app.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class UserDAO
{
    private static EntityManagerFactory emf;

    public UserDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    public User createUser(User user)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        }
        return user;
    }

    public User get(int userId)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return em.find(User.class, userId);
        }
    }

    public User getByEmail(String email)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<User> query = em.createQuery("SELECT u from User u WHERE u.email = :email AND u.active = true", User.class);
            query.setParameter("email", email);
            try
            {
                return query.getSingleResult();
            }
            catch (NoResultException e)
            {
                return null;
            }
        }
    }

    public User update(User user)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.merge(user);
            em.getTransaction().commit();
        }
        return user;
    }
}