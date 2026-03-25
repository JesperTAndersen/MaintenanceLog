package app.controllers.routes;

import app.entities.enums.EmployeeRole;
import app.controllers.SecurityController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class SecurityRoutes
{
    private final SecurityController securityController;

    public SecurityRoutes(SecurityController securityController)
    {
        this.securityController = securityController;
    }

    public EndpointGroup getRoutes()
    {
        return () -> path("auth", () ->
        {
            get("/healthcheck", securityController::healthCheck);
            post("/register", securityController::register, EmployeeRole.MANAGER);
            post("/login", securityController::login);
            get("/protected", ctx -> ctx.json("Hello fom protected").status(200), EmployeeRole.ADMIN);
        });
    }
}