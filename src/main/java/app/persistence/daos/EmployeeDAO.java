package app.persistence.daos;

import app.entities.Employee;
import app.exceptions.DatabaseException;
import app.exceptions.ValidationException;
import app.exceptions.enums.DatabaseErrorType;
import app.persistence.interfaces.IEmployeeDAO;
import app.security.SecurityServiceImpl;
import jakarta.persistence.*;

import java.util.List;

public class EmployeeDAO implements IEmployeeDAO
{
    private final EntityManagerFactory emf;

    public EmployeeDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public Employee create(Employee employee)
    {
        if (employee == null)
        {
            throw new IllegalArgumentException("User cant be null");
        }

        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();

            try
            {
                em.persist(employee);
                em.getTransaction().commit();
                return employee;
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
    public Employee get(Integer id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("User id is required");
        }

        try (EntityManager em = emf.createEntityManager())
        {
            Employee employee = em.find(Employee.class, id);
            if (employee != null)
            {
                return employee;
            }
            throw new DatabaseException("User not found", DatabaseErrorType.NOT_FOUND);
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Get user failed", DatabaseErrorType.QUERY_FAILURE, e);
        }
    }

    @Override
    public Employee getVerifiedUser(String email, String password) throws ValidationException
    {
        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<Employee> query = em.createQuery("SELECT u FROM Employee u WHERE u.email = :email AND u.active = true", Employee.class);
            query.setParameter("email", email);

            try
            {
                Employee employee = query.getSingleResult();

                if (SecurityServiceImpl.verifyPassword(password, employee.getPassword()))
                {
                    return employee;
                }
                else throw new ValidationException("Could not Authenticate login info");
            }
            catch (NoResultException e)
            {
                throw new ValidationException("Could not Authenticate login info");
            }
        }
    }

        @Override
        public List<Employee> getAll ()
        {
            try (EntityManager em = emf.createEntityManager())
            {
                TypedQuery<Employee> query = em.createQuery("SELECT u FROM Employee u", Employee.class);
                return query.getResultList();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Get users failed", DatabaseErrorType.QUERY_FAILURE, e);
            }
        }

        @Override
        public Employee update (Employee u)
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
                    Employee managed = em.find(Employee.class, u.getUserId());
                    if (managed == null)
                    {
                        if (em.getTransaction().isActive())
                        {
                            em.getTransaction().rollback();
                        }
                        throw new DatabaseException("User not found or invalid", DatabaseErrorType.NOT_FOUND);
                    }

                    managed = em.merge(u);
                    em.getTransaction().commit();
                    return managed;
                }
                catch (DatabaseException e)
                {
                    throw e;
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
        public Employee getByEmail (String email)
        {
            if (email == null || email.isBlank())
            {
                throw new IllegalArgumentException("Email is required");
            }

            try (EntityManager em = emf.createEntityManager())
            {
                TypedQuery<Employee> query = em.createQuery("SELECT u from Employee u WHERE u.email = :email AND u.active = true", Employee.class);
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
            catch (PersistenceException e)
            {
                throw new DatabaseException("Get user by email failed", DatabaseErrorType.QUERY_FAILURE, e);
            }
        }

        @Override
        public List<Employee> getInactiveUsers (int limit)
        {
            if (limit <= 0)
            {
                throw new IllegalArgumentException("Input needs to be bigger than 0");
            }

            try (EntityManager em = emf.createEntityManager())
            {
                TypedQuery<Employee> query = em.createQuery("SELECT u FROM Employee u WHERE u.active = false", Employee.class);
                query.setMaxResults(limit);
                return query.getResultList();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Get active users failed", DatabaseErrorType.QUERY_FAILURE, e);
            }
        }

        @Override
        public List<Employee> getActiveUsers (int limit)
        {
            if (limit <= 0)
            {
                throw new IllegalArgumentException("Input needs to be bigger than 0");
            }

            try (EntityManager em = emf.createEntityManager())
            {
                TypedQuery<Employee> query = em.createQuery("SELECT u FROM Employee u WHERE u.active = true", Employee.class);
                query.setMaxResults(limit);
                return query.getResultList();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Get active users failed", DatabaseErrorType.QUERY_FAILURE, e);
            }
        }
    }