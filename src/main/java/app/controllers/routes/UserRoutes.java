package app.controllers.routes;

import app.controllers.EmployeeController;
import io.javalin.apibuilder.EndpointGroup;


import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.put;

public class UserRoutes
{
    private final EmployeeController employeeController;

    public UserRoutes(EmployeeController employeeController)
    {
        this.employeeController = employeeController;
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            path("users", () ->
            {
                get(employeeController::getAll);
                get("/{id}", employeeController::get);
                put("/{id}", employeeController::update);
                delete("/{id}", employeeController::deactivate);
                patch("/{id}", employeeController::activate);
            });
        };
    }
}