package app.controllers.routes;

import app.controllers.AssetController;
import app.controllers.LogController;
import app.controllers.UserController;
import io.javalin.apibuilder.EndpointGroup;

public class Routes
{
    private final UserRoutes userRoutes;
    private final AssetRoutes assetRoutes;
    private final MaintenanceLogRoutes maintenanceLogRoutes;

    public Routes(UserController userController, AssetController assetController, LogController logController)
    {
        userRoutes = new UserRoutes(userController);
        assetRoutes = new AssetRoutes(assetController, logController);
        maintenanceLogRoutes = new MaintenanceLogRoutes(logController);

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
