package app.controllers.routes;

import app.controllers.AssetController;
import app.controllers.MaintenanceLogController;
import app.controllers.EmployeeController;
import app.controllers.SecurityController;
import io.javalin.apibuilder.EndpointGroup;

import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Routes
{
    private static final String API_VERSION = "api/v1";
    private final EmployeeRoutes employeeRoutes;
    private final AssetRoutes assetRoutes;
    private final MaintenanceLogRoutes maintenanceLogRoutes;
    private final SecurityRoutes securityRoutes;

    public Routes(EmployeeController employeeController, AssetController assetController, MaintenanceLogController maintenanceLogController, SecurityController securityController)
    {
        this.employeeRoutes = new EmployeeRoutes(employeeController);
        this.assetRoutes = new AssetRoutes(assetController, maintenanceLogController);
        this.maintenanceLogRoutes = new MaintenanceLogRoutes(maintenanceLogController);
        this.securityRoutes = new SecurityRoutes(securityController);

    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            get("/", ctx -> ctx.status(200).json(Map.of("message", "Welcome to the Maintenance Log!")));

            path(API_VERSION, () ->
            {
                employeeRoutes.getRoutes().addEndpoints();
                assetRoutes.getRoutes().addEndpoints();
                maintenanceLogRoutes.getRoutes().addEndpoints();
                securityRoutes.getRoutes().addEndpoints();
            });
        };
    }

    public static String getApiVersion()
    {
        return API_VERSION;
    }
}