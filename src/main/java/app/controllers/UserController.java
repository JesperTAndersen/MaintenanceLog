package app.controllers;

import app.dtos.UserDTO;
import app.dtos.CreateUserRequest;
import app.exceptions.ApiException;
import app.services.UserService;
import io.javalin.http.Context;

public class UserController
{
    private final UserService userService;

    public UserController(UserService userService)
    {
        this.userService = userService;
    }

    public void create(Context ctx)
    {
        CreateUserRequest userRequest = ctx.bodyValidator(CreateUserRequest.class).
                check(dto -> dto.firstName() != null, "First name is required")
                .check(dto -> dto.lastName() != null, "Last name is required")
                .check(dto -> dto.email() != null, "Email is required")
                .check(dto -> dto.email().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"), "Invalid email format")
                .check(dto -> dto.password() != null, "Password is required")
                .check(dto -> dto.phone() != null, "Phone is required")
                .check(dto -> dto.role() != null, "Role is required")
                .get();

        ctx.status(201).json(userService.create(userRequest));
    }

    public void getAll(Context ctx)
    {
        String activeParam = ctx.queryParam("active");
        Boolean active = activeParam != null ? Boolean.parseBoolean(activeParam) : null;

        ctx.status(200).json(userService.getAll(active));
    }

    public void get(Context ctx)
    {
        int id = Integer.parseInt(ctx.pathParam("id"));
        ctx.status(200).json(userService.get(id));
    }

    public void update(Context ctx)
    {
        int id = Integer.parseInt(ctx.pathParam("id"));
        UserDTO userDTO = ctx.bodyValidator(UserDTO.class)
                .check(dto -> dto.id() == null || dto.id().equals(id), "ID in URL and body must match")
                .check(dto -> dto.email() == null || dto.email().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"), "Invalid email format")
                .get();

        ctx.status(200).json(userService.update(id, userDTO));
    }

    public void deactivate(Context ctx)
    {
        int id = Integer.parseInt(ctx.pathParam("id"));
        userService.deactivate(id);
        ctx.status(204);
    }


    public void activate(Context ctx)
    {
        int id = Integer.parseInt(ctx.pathParam("id"));
        userService.activate(id);
        ctx.status(204);
    }
}