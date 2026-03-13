package app.controllers.routes;

import app.controllers.MaintenanceLogController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class MaintenanceLogRoutes
{
    private final MaintenanceLogController maintenanceLogController;

    public MaintenanceLogRoutes(MaintenanceLogController maintenanceLogController)
    {
        this.maintenanceLogController = maintenanceLogController;
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            path("logs", () ->
            {
                get(maintenanceLogController::getAll);
                get("/{id}", maintenanceLogController::get);
                get("/user/{userId}", maintenanceLogController::getByUser);
                get("/active-assets", maintenanceLogController::getLogsOnActiveAssets);
            });
        };
    }
}
