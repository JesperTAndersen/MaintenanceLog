package app.persistence.daos;

import app.entities.model.MaintenanceLog;
import app.entities.enums.LogStatus;
import app.entities.enums.TaskType;
import app.exceptions.DatabaseException;
import app.exceptions.enums.DatabaseErrorType;
import app.persistence.interfaces.IDAO;
import app.persistence.interfaces.IMaintenanceLogDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;

public class MaintenanceLogDAO implements IDAO<MaintenanceLog>, IMaintenanceLogDAO
{
    private final EntityManagerFactory emf;

    public MaintenanceLogDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public MaintenanceLog create(MaintenanceLog log)
    {
        if (log == null)
        {
            throw new IllegalArgumentException("Log cant be null");
        }

        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();

            try
            {
                em.persist(log);
                em.getTransaction().commit();
                return log;
            }
            catch (PersistenceException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw new DatabaseException("Create log failed", DatabaseErrorType.TRANSACTION_FAILURE, e);
            }
            catch (RuntimeException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw new DatabaseException("Create log failed", DatabaseErrorType.UNKNOWN, e);
            }
        }
    }

    @Override
    public MaintenanceLog get(Integer id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Log id is required");
        }

        try (EntityManager em = emf.createEntityManager())
        {
            MaintenanceLog log = em.find(MaintenanceLog.class, id);
            if (log != null)
            {
                return log;
            }
            throw new DatabaseException("Log not found", DatabaseErrorType.NOT_FOUND);
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Get log failed", DatabaseErrorType.QUERY_FAILURE, e);
        }
    }

    @Override
    public List<MaintenanceLog> getAll()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<MaintenanceLog> query = em.createQuery("SELECT m FROM MaintenanceLog m", MaintenanceLog.class);
            return new ArrayList<>(query.getResultList());
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Get logs failed", DatabaseErrorType.QUERY_FAILURE, e);
        }
    }

    @Override
    public MaintenanceLog update(MaintenanceLog maintenanceLog)
    {
        throw new UnsupportedOperationException("Maintenance logs are immutable");
    }

    @Override
    public List<MaintenanceLog> getByAsset(Integer assetId)
    {
        if (assetId == null)
        {
            throw new IllegalArgumentException("Asset id is required");
        }

        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<MaintenanceLog> query = em.createQuery("SELECT m from MaintenanceLog m WHERE m.asset.assetId = :assetId", MaintenanceLog.class);
            query.setParameter("assetId", assetId);
            return new ArrayList<>(query.getResultList());
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Get logs by asset failed", DatabaseErrorType.QUERY_FAILURE, e);
        }
    }

    @Override
    public List<MaintenanceLog> getByAssetAndTask(Integer assetId, TaskType taskType)
    {
        if (assetId == null)
        {
            throw new IllegalArgumentException("Asset id is required");
        }
        if (taskType == null)
        {
            throw new IllegalArgumentException("Task type is required");
        }

        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<MaintenanceLog> query = em.createQuery("SELECT m from MaintenanceLog m WHERE m.asset.assetId = :assetId AND m.taskType = :taskType", MaintenanceLog.class);
            query.setParameter("assetId", assetId);
            query.setParameter("taskType", taskType);
            return new ArrayList<>(query.getResultList());
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Get logs by asset and task failed", DatabaseErrorType.QUERY_FAILURE, e);
        }
    }

    @Override
    public List<MaintenanceLog> getByStatus(LogStatus status)
    {
        if (status == null)
        {
            throw new IllegalArgumentException("Status is required");
        }

        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<MaintenanceLog> query = em.createQuery("SELECT m from MaintenanceLog m WHERE m.status = :status", MaintenanceLog.class);
            query.setParameter("status", status);
            return new ArrayList<>(query.getResultList());
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Get logs by status failed", DatabaseErrorType.QUERY_FAILURE, e);
        }
    }

    @Override
    public List<MaintenanceLog> getByStatusAndAsset(LogStatus status, Integer assetId)
    {
        if (status == null)
        {
            throw new IllegalArgumentException("Status is required");
        }
        if (assetId == null)
        {
            throw new IllegalArgumentException("Asset id is required");
        }

        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<MaintenanceLog> query = em.createQuery("SELECT m from MaintenanceLog m WHERE m.asset.assetId  = :assetId AND m.status = :status", MaintenanceLog.class);
            query.setParameter("assetId", assetId);
            query.setParameter("status", status);
            return new ArrayList<>(query.getResultList());
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Get logs by status and asset failed", DatabaseErrorType.QUERY_FAILURE, e);
        }
    }

    @Override
    public List<MaintenanceLog> getByPerformedUser(Integer userId)
    {
        if (userId == null)
        {
            throw new IllegalArgumentException("User id is required");
        }

        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<MaintenanceLog> query = em.createQuery("SELECT m FROM MaintenanceLog m WHERE m.performedBy.userId = :userId", MaintenanceLog.class);
            query.setParameter("userId", userId);
            return new ArrayList<>(query.getResultList());
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Get logs by performed user failed", DatabaseErrorType.QUERY_FAILURE, e);
        }
    }

    @Override
    public List<MaintenanceLog> getLogsOnActiveAssets(int limit)
    {
        if (limit <= 0)
        {
            throw new IllegalArgumentException("Input needs to be bigger than 0");
        }

        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<MaintenanceLog> query = em.createQuery("SELECT m FROM MaintenanceLog m WHERE m.asset.active = true ORDER BY m.asset.assetId DESC, m.performedDate DESC", MaintenanceLog.class);
            query.setMaxResults(limit);
            return new ArrayList<>(query.getResultList());
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Get logs on active assets failed", DatabaseErrorType.QUERY_FAILURE, e);
        }
    }
}