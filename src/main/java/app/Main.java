package app;

import app.config.AppConfig;
import app.config.DependencyContainer;
import app.controllers.routes.Routes;
import app.config.hibernate.HibernateConfig;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main
{
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    public static void main(String[] args)
    {
        DependencyContainer container = new DependencyContainer(emf);
        Routes routes = container.getRoutes();

        AppConfig.create(routes).start(7070);

        log.info("Server started on port 7070");
    }
}