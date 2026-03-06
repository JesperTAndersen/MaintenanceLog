package app.controllers.routes;

import app.controllers.AssetController;
import app.controllers.LogController;
import app.controllers.UserController;
import io.javalin.apibuilder.EndpointGroup;

public class Routes
{
    private final UserRoutes userRoutes;
    private final AssetRoutes assetRoutes;
    private final LogRoutes logRoutes;

    public Routes(UserController userController, AssetController assetController, LogController logController)
    {
        userRoutes = new UserRoutes(userController);
        assetRoutes = new AssetRoutes(assetController);
        logRoutes = new LogRoutes(logController);

    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            userRoutes.getRoutes().addEndpoints();
            assetRoutes.getRoutes().addEndpoints();
            logRoutes.getRoutes().addEndpoints();
        };
    }
}
