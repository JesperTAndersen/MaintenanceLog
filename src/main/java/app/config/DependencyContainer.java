package app.config;

import app.config.hibernate.HibernateConfig;
import app.controllers.AssetController;
import app.controllers.MaintenanceLogController;
import app.controllers.UserController;
import app.controllers.routes.Routes;
import app.persistence.daos.AssetDAO;
import app.persistence.daos.MaintenanceLogDAO;
import app.persistence.daos.UserDAO;
import app.services.*;
import jakarta.persistence.EntityManagerFactory;

public class DependencyContainer
{
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private final UserController userController;
    private final AssetController assetController;
    private final MaintenanceLogController maintenanceLogController;

    public DependencyContainer()
    {
        this(emf);
    }

    public DependencyContainer(EntityManagerFactory emfTest)
    {
        UserDAO userDaoImpl = new UserDAO(emfTest);
        AssetDAO assetDaoImpl = new AssetDAO(emfTest);
        MaintenanceLogDAO logDaoImpl = new MaintenanceLogDAO(emfTest);

        // Services uses different interface implementations and upcast automatically
        UserService userService = new UserServiceImpl(userDaoImpl, userDaoImpl);
        AssetService assetService = new AssetServiceImpl(assetDaoImpl, assetDaoImpl);
        MaintenanceLogService logService = new MaintenanceLogServiceImpl(logDaoImpl, logDaoImpl, assetDaoImpl, userDaoImpl);

        this.userController = new UserController(userService);
        this.assetController = new AssetController(assetService);
        this.maintenanceLogController = new MaintenanceLogController(logService);
    }

    public Routes getRoutes()
    {
        return new Routes(userController, assetController, maintenanceLogController);
    }
}