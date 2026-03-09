package app.controllers.routes;

import app.controllers.AssetController;
import app.controllers.MaintenanceLogController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class AssetRoutes
{
    private final AssetController assetController;
    private final MaintenanceLogController maintenanceLogController;

    public AssetRoutes(AssetController assetController, MaintenanceLogController maintenanceLogController)
    {
        this.assetController = assetController;
        this.maintenanceLogController = maintenanceLogController;
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            path("api/v1/assets", () ->
            {
                get(assetController::getAll);
                get("/{id}", assetController::get);
                post(assetController::create);
                patch("/{id}", assetController::active);
                delete("/{id}", assetController::delete);

                path("/{id}/logs", () ->
                {
                    get(maintenanceLogController::getLogsByAsset);
                    post(maintenanceLogController::createLogForAsset);
                });
            });
        };
    }
}