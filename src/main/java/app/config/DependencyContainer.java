package app.config;

import app.controllers.AssetController;
import app.controllers.MaintenanceLogController;
import app.controllers.UserController;
import app.controllers.routes.Routes;
import app.persistence.daos.AssetDAO;
import app.persistence.daos.MaintenanceLogDAO;
import app.persistence.daos.UserDAO;
import app.services.*;
import jakarta.persistence.EntityManagerFactory;

public class DependencyContainer {
    private final EntityManagerFactory emf;

    //DAOs
    private final UserDAO userDao;
    private final AssetDAO assetDao;
    private final MaintenanceLogDAO logDao;

    //Services
    private final UserService userService;
    private final AssetService assetService;
    private final MaintenanceLogService logService;

    //Controllers
    private final UserController userController;
    private final AssetController assetController;
    private final MaintenanceLogController maintenanceLogController;

    public DependencyContainer(EntityManagerFactory emf) {
        this.emf = emf;

        this.userDao = new UserDAO(emf);
        this.assetDao = new AssetDAO(emf);
        this.logDao = new MaintenanceLogDAO(emf);


        this.userService = new UserServiceImpl(userDao, userDao);
        this.assetService = new AssetServiceImpl(assetDao, assetDao);
        this.logService = new MaintenanceLogServiceImpl(logDao, logDao, assetDao, userDao);

        this.userController = new UserController(userService);
        this.assetController = new AssetController(assetService);
        this.maintenanceLogController = new MaintenanceLogController(logService);
    }

    public Routes getRoutes() {
        return new Routes(userController, assetController, maintenanceLogController);
    }
}