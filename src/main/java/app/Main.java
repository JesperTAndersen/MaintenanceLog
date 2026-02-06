package app;

import app.config.HibernateConfig;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;


public class Main
{
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    public static void main(String[] args)
    {
        EntityManager em = emf.createEntityManager();



        // Close the database connection:
        em.close();
        emf.close();
    }

}