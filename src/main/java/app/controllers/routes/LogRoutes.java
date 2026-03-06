package app.controllers.routes;

import app.controllers.LogController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.put;

public class LogRoutes
{
    private final LogController logController;

    public LogRoutes(LogController logController)
    {
        this.logController = logController;
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            path("api/v1/logs", () ->
            {
                get("/", logController::getAll);
                get("/{id}", logController::get);
                post("/", logController::create);
                put("/{id}", logController::update);
                delete("/{id}", logController::delete);
            });
        };
    }
}
