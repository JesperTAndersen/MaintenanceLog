package app.controllers.routes;

import app.controllers.UserController;
import io.javalin.apibuilder.EndpointGroup;


import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.put;

public class UserRoutes
{
    private final UserController userController;

    public UserRoutes(UserController userController)
    {
        this.userController = userController;
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            path("api/v1/users", () ->
            {
                get(userController::getAll);
                get("/{id}", userController::get);
                post(userController::create);
                put("/{id}", userController::update);
                delete("/{id}", userController::deactivate);
            });
        };
    }
}