package app.persistence.daos;

import app.entities.model.Asset;
import app.exceptions.DatabaseException;
import app.exceptions.enums.DatabaseErrorType;
import app.persistence.interfaces.IAssetDAO;
import app.persistence.interfaces.IDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class AssetDAO implements IDAO<Asset>, IAssetDAO
{
    private final EntityManagerFactory emf;

    public AssetDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public Asset create(Asset asset)
    {
        if (asset == null)
        {
            throw new IllegalArgumentException("Asset cant be null");
        }
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();

            try
            {
                em.persist(asset);
                em.getTransaction().commit();
                return asset;
            }
            catch (PersistenceException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw new DatabaseException("Create Asset failed", DatabaseErrorType.TRANSACTION_FAILURE, e);
            }
            catch (RuntimeException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw new DatabaseException("Create Asset failed", DatabaseErrorType.UNKNOWN, e);
            }
        }
    }

    @Override
    public Asset get(Integer id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Asset id is required");
        }
        try (EntityManager em = emf.createEntityManager())
        {
            Asset asset = em.find(Asset.class, id);
            if (asset != null)
            {
                return asset;
            }
            throw new DatabaseException("Asset not found", DatabaseErrorType.NOT_FOUND);
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Get asset failed", DatabaseErrorType.QUERY_FAILURE, e);
        }
    }

    @Override
    public List<Asset> getAll()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<Asset> query = em.createQuery("SELECT a FROM Asset a WHERE a.active = true ORDER BY a.assetId DESC", Asset.class);
            return query.getResultList();
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Get active assets failed", DatabaseErrorType.QUERY_FAILURE, e);
        }
    }

    @Override
    public Asset update(Asset asset)
    {
        throw new UnsupportedOperationException("Assets are immutable");
    }

    @Override
    public Asset setActive(Integer id, boolean active)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            Asset asset = em.find(Asset.class, id);
            if (asset == null)
            {
                throw new DatabaseException("Asset not found", DatabaseErrorType.NOT_FOUND);
            }

            em.getTransaction().begin();

            try
            {
                asset.setActive(active);
                em.getTransaction().commit();
                return asset;
            }
            catch (PersistenceException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw new DatabaseException("Update Asset failed", DatabaseErrorType.TRANSACTION_FAILURE, e);
            }
            catch (RuntimeException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw new DatabaseException("Update Asset failed", DatabaseErrorType.UNKNOWN, e);
            }
        }
    }

    @Override
    public List<Asset> getInactiveAssets()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            TypedQuery<Asset> query = em.createQuery("SELECT a FROM Asset a WHERE a.active = false ORDER BY a.assetId DESC", Asset.class);
            return query.getResultList();
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Get inactive assets failed", DatabaseErrorType.QUERY_FAILURE, e);
        }
    }
}
