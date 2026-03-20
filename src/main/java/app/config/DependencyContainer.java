package app.config;

import app.config.hibernate.HibernateConfig;
import app.controllers.AssetController;
import app.controllers.MaintenanceLogController;
import app.controllers.UserController;
import app.controllers.routes.Routes;
import app.persistence.daos.AssetDAO;
import app.persistence.daos.MaintenanceLogDAO;
import app.persistence.daos.UserDAO;
import app.security.SecurityService;
import app.security.SecurityServiceImpl;
import app.security.controllers.SecurityController;
import app.services.*;
import jakarta.persistence.EntityManagerFactory;
import lombok.Getter;

public class DependencyContainer
{
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private final UserController userController;
    private final AssetController assetController;
    private final MaintenanceLogController maintenanceLogController;
    private final SecurityController securityController;
    @Getter
    private final SecurityService securityService;

    public DependencyContainer()
    {
        this(emf);
    }

    public DependencyContainer(EntityManagerFactory emfTest)
    {
        UserDAO userDaoImpl = new UserDAO(emfTest);
        AssetDAO assetDaoImpl = new AssetDAO(emfTest);
        MaintenanceLogDAO logDaoImpl = new MaintenanceLogDAO(emfTest);

        UserService userService = new UserServiceImpl(userDaoImpl);
        AssetService assetService = new AssetServiceImpl(assetDaoImpl);
        MaintenanceLogService logService = new MaintenanceLogServiceImpl(logDaoImpl, assetDaoImpl, userDaoImpl);
        securityService = new SecurityServiceImpl(userDaoImpl);


        this.userController = new UserController(userService);
        this.assetController = new AssetController(assetService);
        this.maintenanceLogController = new MaintenanceLogController(logService);
        this.securityController = new SecurityController(securityService);
    }

    public Routes getRoutes()
    {
        return new Routes(userController, assetController, maintenanceLogController, securityController);
    }
}