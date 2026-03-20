package app.controllers.routes;

import app.controllers.AssetController;
import app.controllers.MaintenanceLogController;
import app.controllers.EmployeeController;
import app.security.controllers.SecurityController;
import app.security.routes.SecurityRoutes;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.path;

public class Routes
{
    private static final String API_VERSION = "api/v1";

    private final UserRoutes userRoutes;
    private final AssetRoutes assetRoutes;
    private final MaintenanceLogRoutes maintenanceLogRoutes;
    private final SecurityRoutes securityRoutes;

    public Routes(EmployeeController employeeController, AssetController assetController, MaintenanceLogController maintenanceLogController, SecurityController securityController)
    {
        this.userRoutes = new UserRoutes(employeeController);
        this.assetRoutes = new AssetRoutes(assetController, maintenanceLogController);
        this.maintenanceLogRoutes = new MaintenanceLogRoutes(maintenanceLogController);
        this.securityRoutes = new SecurityRoutes(securityController);

    }

    public EndpointGroup getRoutes()
    {
        return () ->
                path(API_VERSION, () ->
                {
                    userRoutes.getRoutes().addEndpoints();
                    assetRoutes.getRoutes().addEndpoints();
                    maintenanceLogRoutes.getRoutes().addEndpoints();
                    securityRoutes.getRoutes().addEndpoints();
                });
    }


    public static String getApiVersion()
    {
        return API_VERSION;
    }
}