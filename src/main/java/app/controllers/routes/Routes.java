package app.controllers.routes;

import app.controllers.AssetController;
import app.controllers.MaintenanceLogController;
import app.controllers.UserController;
import io.javalin.apibuilder.EndpointGroup;

public class Routes
{
    private final UserRoutes userRoutes;
    private final AssetRoutes assetRoutes;
    private final MaintenanceLogRoutes maintenanceLogRoutes;

    public Routes(UserController userController, AssetController assetController, MaintenanceLogController maintenanceLogController)
    {
        userRoutes = new UserRoutes(userController);
        assetRoutes = new AssetRoutes(assetController, maintenanceLogController);
        maintenanceLogRoutes = new MaintenanceLogRoutes(maintenanceLogController);

    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            userRoutes.getRoutes().addEndpoints();
            assetRoutes.getRoutes().addEndpoints();
            maintenanceLogRoutes.getRoutes().addEndpoints();
        };
    }
}
