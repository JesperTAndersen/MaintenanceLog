package app.controllers.routes;

import app.controllers.MaintenanceLogController;
import app.entities.enums.EmployeeRole;
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
                get(maintenanceLogController::getAll, EmployeeRole.AUTHENTICATED);
                get("/{id}", maintenanceLogController::get, EmployeeRole.AUTHENTICATED);
                get("/user/{userId}", maintenanceLogController::getByUser, EmployeeRole.MANAGER);
            });
        };
    }
}
