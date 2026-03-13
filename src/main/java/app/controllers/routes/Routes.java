package app.controllers.routes;

import app.controllers.AssetController;
import app.controllers.MaintenanceLogController;
import app.controllers.UserController;
import io.javalin.apibuilder.EndpointGroup;
import lombok.Getter;

import static io.javalin.apibuilder.ApiBuilder.path;

public class Routes
{
    @Getter
    private static final String API_VERSION = "api/v1";

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
                path(API_VERSION, () ->
                {
                    userRoutes.getRoutes().addEndpoints();
                    assetRoutes.getRoutes().addEndpoints();
                    maintenanceLogRoutes.getRoutes().addEndpoints();
                });
    }
}