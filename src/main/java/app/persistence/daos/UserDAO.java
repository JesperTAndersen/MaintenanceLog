package app.persistence.daos;

import app.entities.model.User;
import app.exceptions.DatabaseException;
import app.exceptions.enums.DatabaseErrorType;
import app.persistence.interfaces.IDAO;
import app.persistence.interfaces.IUserDAO;
import jakarta.persistence.*;

import java.util.List;

public class UserDAO implements IDAO<User>, IUserDAO
{
    private final EntityManagerFactory emf;

    public UserDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public User create(User user)
    {
        if (user == null)
        {
            throw new IllegalArgumentException("User cant be null");
        }
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();

            try
            {
                em.persist(user);
                em.getTransaction().commit();
                return user;
            }
            catch (PersistenceException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw new DatabaseException("Create User failed", DatabaseErrorType.TRANSACTION_FAILURE, e);
            }
            catch (RuntimeException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw new DatabaseException("Create User failed", DatabaseErrorType.UNKNOWN, e);
            }
        }
    }

    @Override
    public User get(Integer id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("User id is required");
        }
        try (EntityManager em = emf.createEntityManager())
        {
            User user = em.find(User.class, id);
            if (user != null)
            {
                return user;
            }
            throw new DatabaseException("User not found", DatabaseErrorType.NOT_FOUND);
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Get user failed", DatabaseErrorType.QUERY_FAILURE, e);
        }
    }

    @Override
    public List<User> getAll()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
            return query.getResultList();
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Get users failed", DatabaseErrorType.QUERY_FAILURE, e);
        }
    }


    @Override
    public User update(User u)
    {
        if (u == null || u.getUserId() == null)
        {
            throw new IllegalArgumentException("User and user id are required");
        }

        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();

            try
            {
                User merged = em.merge(u);
                em.getTransaction().commit();
                return merged;
            }
            catch (IllegalArgumentException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw new DatabaseException("User not found or invalid", DatabaseErrorType.NOT_FOUND, e);
            }
            catch (PersistenceException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw new DatabaseException("Update User failed", DatabaseErrorType.TRANSACTION_FAILURE, e);
            }
            catch (RuntimeException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw new DatabaseException("Update User failed", DatabaseErrorType.UNKNOWN, e);
            }
        }
    }

    @Override
    public User getByEmail(String email)
    {
        if (email == null || email.isBlank())
        {
            throw new IllegalArgumentException("Email is required");
        }

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
                throw new DatabaseException("User not found", DatabaseErrorType.NOT_FOUND);
            }
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Get user by email failed", DatabaseErrorType.QUERY_FAILURE, e);
        }
    }


    @Override
    public List<User> getActiveUsers(int limit)
    {
        if (limit <= 0)
        {
            throw new IllegalArgumentException("Input needs to be bigger than 0");
        }
        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.active = true", User.class);
            query.setMaxResults(limit);
            return query.getResultList();
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Get active users failed", DatabaseErrorType.QUERY_FAILURE, e);
        }
    }
}