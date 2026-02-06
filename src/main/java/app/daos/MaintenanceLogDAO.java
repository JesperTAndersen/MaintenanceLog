package app.daos;

import app.entities.MaintenanceLog;
import app.utils.LogStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class MaintenanceLogDAO
{
    private static EntityManagerFactory emf;

    public MaintenanceLogDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

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

    public MaintenanceLog getLog(int logId)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return em.find(MaintenanceLog.class, logId);
        }
    }

    public List<MaintenanceLog> getLogsByAsset(int assetId)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<MaintenanceLog> query = em.createQuery("SELECT m from MaintenanceLog m WHERE m.asset.assetId = :assetId", MaintenanceLog.class);
            query.setParameter("assetId", assetId);
            return query.getResultList();
        }
    }

    public List<MaintenanceLog> getLogsByStatus(LogStatus status)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<MaintenanceLog> query = em.createQuery("SELECT m from MaintenanceLog m WHERE m.status = :status", MaintenanceLog.class);
            query.setParameter("status", status);
            return query.getResultList();
        }
    }

    public List<MaintenanceLog> getLogsByStatus(LogStatus status, int assetId)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<MaintenanceLog> query = em.createQuery("SELECT m from MaintenanceLog m WHERE m.asset.assetId  = :assetId AND m.status = :status", MaintenanceLog.class);
            query.setParameter("assetId", assetId);
            query.setParameter("status", status);
            return query.getResultList();
        }
    }
}