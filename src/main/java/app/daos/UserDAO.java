package app.daos;

import app.entities.Course;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class UserDAO
{
    private static EntityManagerFactory emf;

    public UserDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    public Course createCourse(Course course)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.persist(course);
            em.getTransaction().commit();
        }
        return course;
    }

    public Course update(Course course)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.merge(course);
            em.getTransaction().commit();
        }
        return course;
    }

    public List<Course> getAllCourses()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            TypedQuery<Course> query = em.createQuery("SELECT c from Course c", Course.class);
            return query.getResultList();
        }
    }

    public boolean delete(int courseId)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();

            Course course = em.find(Course.class, courseId);
            if (course == null)
            {
                em.getTransaction().rollback();
                return false;
            }

            em.remove(course);
            em.getTransaction().commit();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }


}
