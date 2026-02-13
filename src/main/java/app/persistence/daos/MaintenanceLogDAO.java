package app.persistence.daos;

import app.entities.model.MaintenanceLog;
import app.entities.enums.LogStatus;
import app.entities.enums.TaskType;
import app.persistence.interfaces.IDAO;
import app.persistence.interfaces.IMaintenanceLogDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
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
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.persist(log);
            em.getTransaction().commit();
        }
        return log;
    }

    @Override
    public MaintenanceLog get(Integer logId)
    {
        // TODO: check for null in id
        try (EntityManager em = emf.createEntityManager())
        {
            return em.find(MaintenanceLog.class, logId);
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
    }

    @Override
    public MaintenanceLog update(MaintenanceLog maintenanceLog)
    {
        return null;
    }

    @Override
    public List<MaintenanceLog> getByAsset(Integer assetId)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<MaintenanceLog> query = em.createQuery("SELECT m from MaintenanceLog m WHERE m.asset.assetId = :assetId", MaintenanceLog.class);
            query.setParameter("assetId", assetId);
            return new ArrayList<>(query.getResultList());
        }
    }

    @Override
    public List<MaintenanceLog> getByAssetAndTask(Integer assetId, TaskType taskType)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<MaintenanceLog> query = em.createQuery("SELECT m from MaintenanceLog m WHERE m.asset.assetId = :assetId AND m.taskType = :taskType", MaintenanceLog.class);
            query.setParameter("assetId", assetId);
            query.setParameter("taskType", taskType);
            return new ArrayList<>(query.getResultList());
        }
    }

    @Override
    public List<MaintenanceLog> getByStatus(LogStatus status)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<MaintenanceLog> query = em.createQuery("SELECT m from MaintenanceLog m WHERE m.status = :status", MaintenanceLog.class);
            query.setParameter("status", status);
            return new ArrayList<>(query.getResultList());
        }
    }

    @Override
    public List<MaintenanceLog> getByStatusAndAsset(LogStatus status, Integer assetId)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<MaintenanceLog> query = em.createQuery("SELECT m from MaintenanceLog m WHERE m.asset.assetId  = :assetId AND m.status = :status", MaintenanceLog.class);
            query.setParameter("assetId", assetId);
            query.setParameter("status", status);
            return new ArrayList<>(query.getResultList());
        }
    }

    @Override
    public List<MaintenanceLog> getByPerformedUser(Integer userId)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<MaintenanceLog> query = em.createQuery("SELECT m FROM MaintenanceLog m WHERE m.performedBy.userId = :userId", MaintenanceLog.class);
            query.setParameter("userId", userId);
            return new ArrayList<>(query.getResultList());
        }
    }

    @Override
    public List<MaintenanceLog> getLogsOnActiveAssets(int limit)
    {
        if (limit <= 0)
        {
            return List.of();
        }

        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<MaintenanceLog> query = em.createQuery("SELECT m FROM MaintenanceLog m WHERE m.asset.active = true ORDER BY m.asset.assetId DESC, m.performedDate DESC", MaintenanceLog.class);
            query.setMaxResults(limit);
            return new ArrayList<>(query.getResultList());
        }
    }
}