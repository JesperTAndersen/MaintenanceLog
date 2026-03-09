package app.controllers.routes;

import app.controllers.LogController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class MaintenanceLogRoutes
{
    private final LogController logController;

    public MaintenanceLogRoutes(LogController logController)
    {
        this.logController = logController;
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            path("api/v1/logs", () ->
            {
                get(logController::getAll);
                get("/{id}", logController::get);
                get("/user/{userId}", logController::getByUser);
                get("/active-assets", logController::getLogsOnActiveAssets);
            });
        };
    }
}
