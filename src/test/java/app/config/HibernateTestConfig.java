package app.config;

import app.config.hibernate.HibernateEmfBuilder;
import app.config.hibernate.HibernateBaseProperties;
import jakarta.persistence.EntityManagerFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Properties;

public final class HibernateTestConfig {

    private static volatile EntityManagerFactory emf;
    private static final PostgreSQLContainer<?> postgresContainer;

    static {
        postgresContainer = new PostgreSQLContainer<>("postgres:16.2")
                .withDatabaseName("test_db")
                .withUsername("postgres")
                .withPassword("secret");
        postgresContainer.start();
    }

    private HibernateTestConfig() {}

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null || !emf.isOpen()) {
            synchronized (HibernateTestConfig.class) {
                if (emf == null || !emf.isOpen()) {
                    Properties props = HibernateBaseProperties.createBase();
                    props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
                    props.put("hibernate.connection.url", postgresContainer.getJdbcUrl());
                    props.put("hibernate.connection.username", postgresContainer.getUsername());
                    props.put("hibernate.connection.password", postgresContainer.getPassword());
                    props.put("hibernate.hbm2ddl.auto", "create-drop");

                    emf = HibernateEmfBuilder.build(props);
                }
            }
        }
        return emf;
    }
}