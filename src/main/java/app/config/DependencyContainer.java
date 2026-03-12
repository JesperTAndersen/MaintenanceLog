package app.config;

import app.config.hibernate.HibernateConfig;
import app.controllers.AssetController;
import app.controllers.MaintenanceLogController;
import app.controllers.UserController;
import app.controllers.routes.Routes;
import app.entities.Asset;
import app.entities.MaintenanceLog;
import app.entities.User;
import app.persistence.daos.AssetDAO;
import app.persistence.daos.MaintenanceLogDAO;
import app.persistence.daos.UserDAO;
import app.persistence.interfaces.IAssetDAO;
import app.persistence.interfaces.IDAO;
import app.persistence.interfaces.IMaintenanceLogDAO;
import app.persistence.interfaces.IUserDAO;
import app.services.*;
import jakarta.persistence.EntityManagerFactory;

public class DependencyContainer
{
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    //DAOs
    private final IDAO<User> userDao;
    private final IDAO<Asset> assetDao;
    private final IDAO<MaintenanceLog> logDao;
    private final IUserDAO userDaoExpanded;
    private final IAssetDAO assetDaoExpanded;
    private final IMaintenanceLogDAO logDaoExpanded;
    //Services
    private final UserService userService;
    private final AssetService assetService;
    private final MaintenanceLogService logService;
    //Controllers
    private final UserController userController;
    private final AssetController assetController;
    private final MaintenanceLogController maintenanceLogController;

    public DependencyContainer()
    {
        this.userDao = new UserDAO(emf);
        this.userDaoExpanded = new UserDAO(emf);
        this.assetDao = new AssetDAO(emf);
        this.assetDaoExpanded = new AssetDAO(emf);
        this.logDao = new MaintenanceLogDAO(emf);
        this.logDaoExpanded = new MaintenanceLogDAO(emf);

        this.userService = new UserServiceImpl(userDao, userDaoExpanded);
        this.assetService = new AssetServiceImpl(assetDao, assetDaoExpanded);
        this.logService = new MaintenanceLogServiceImpl(logDao, logDaoExpanded, assetDao, userDao);

        this.userController = new UserController(userService);
        this.assetController = new AssetController(assetService);
        this.maintenanceLogController = new MaintenanceLogController(logService);
    }

    public Routes getRoutes()
    {
        return new Routes(userController, assetController, maintenanceLogController);
    }
}