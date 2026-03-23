package app.controllers.routes;

import app.controllers.AssetController;
import app.controllers.MaintenanceLogController;
import app.entities.enums.EmployeeRole;
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
            path("assets", () ->
            {
                get(assetController::getAll, EmployeeRole.AUTHENTICATED);
                get("/{id}", assetController::get, EmployeeRole.AUTHENTICATED);
                post(assetController::create, EmployeeRole.MANAGER);
                patch("/{id}", assetController::active, EmployeeRole.MANAGER);
                delete("/{id}", assetController::delete, EmployeeRole.ADMIN);

                path("/{id}/logs", () ->
                {
                    get(maintenanceLogController::getLogsByAsset, EmployeeRole.AUTHENTICATED);
                    post(maintenanceLogController::createLogForAsset, EmployeeRole.TECHNICIAN);
                });
            });
        };
    }
}