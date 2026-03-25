package app.config;

import app.config.hibernate.HibernateConfig;
import app.controllers.AssetController;
import app.controllers.MaintenanceLogController;
import app.controllers.EmployeeController;
import app.controllers.routes.Routes;
import app.persistence.AssetDAO;
import app.persistence.MaintenanceLogDAO;
import app.persistence.EmployeeDAO;
import app.services.interfaces.AssetService;
import app.services.interfaces.EmployeeService;
import app.services.interfaces.MaintenanceLogService;
import app.services.interfaces.SecurityService;
import app.services.SecurityServiceImpl;
import app.controllers.SecurityController;
import app.services.*;
import jakarta.persistence.EntityManagerFactory;
import lombok.Getter;

public class DependencyContainer
{
    private final EmployeeController employeeController;
    private final AssetController assetController;
    private final MaintenanceLogController maintenanceLogController;
    private final SecurityController securityController;
    @Getter
    private final SecurityService securityService;

    public DependencyContainer()
    {
        this(HibernateConfig.getEntityManagerFactory());
    }

    public DependencyContainer(EntityManagerFactory emfTest)
    {
        EmployeeDAO employeeDaoImpl = new EmployeeDAO(emfTest);
        AssetDAO assetDaoImpl = new AssetDAO(emfTest);
        MaintenanceLogDAO logDaoImpl = new MaintenanceLogDAO(emfTest);

        EmployeeService employeeService = new EmployeeServiceImpl(employeeDaoImpl);
        AssetService assetService = new AssetServiceImpl(assetDaoImpl);
        MaintenanceLogService logService = new MaintenanceLogServiceImpl(logDaoImpl, assetDaoImpl, employeeDaoImpl);
        securityService = new SecurityServiceImpl(employeeDaoImpl);


        this.employeeController = new EmployeeController(employeeService);
        this.assetController = new AssetController(assetService);
        this.maintenanceLogController = new MaintenanceLogController(logService);
        this.securityController = new SecurityController(securityService);
    }

    public Routes getRoutes()
    {
        return new Routes(employeeController, assetController, maintenanceLogController, securityController);
    }
}