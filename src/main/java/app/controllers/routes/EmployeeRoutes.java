package app.controllers.routes;

import app.controllers.EmployeeController;
import app.entities.enums.EmployeeRole;
import io.javalin.apibuilder.EndpointGroup;


import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.put;

public class EmployeeRoutes
{
    private final EmployeeController employeeController;

    public EmployeeRoutes(EmployeeController employeeController)
    {
        this.employeeController = employeeController;
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            path("employees", () ->
            {
                get(employeeController::getAll, EmployeeRole.AUTHENTICATED);
                get("/{id}", employeeController::get, EmployeeRole.AUTHENTICATED);
                put("/{id}", employeeController::update, EmployeeRole.MANAGER);
                delete("/{id}", employeeController::deactivate, EmployeeRole.ADMIN);
                patch("/{id}", employeeController::activate, EmployeeRole.ADMIN);
            });
        };
    }
}