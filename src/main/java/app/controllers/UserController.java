package app.controllers;

import app.dtos.UserDTO;
import app.dtos.CreateUserRequest;
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
                check(dto -> dto.getFirstName() != null, "First name is required")
                .check(dto -> dto.getLastName() != null, "Last name is required")
                .check(dto -> dto.getEmail() != null, "Email is required")
                .check(dto ->dto.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"), "Invalid email format")
                .check(dto -> dto.getPassword() != null, "Password is required")
                .check(dto -> dto.getPhone() != null, "Phone is required")
                .check(dto -> dto.getRole() != null, "Role is required")
                .get();

        ctx.status(201).json(userService.create(userRequest));
    }

    public void getAll(Context ctx)
    {
        //TODO: ADD CODE
        ctx.result("get all user - not implemented yet");
    }

    public void get(Context ctx)
    {
        //TODO: ADD CODE
        ctx.result("Get user - not implemented yet");
    }

    public void update(Context ctx)
    {
        //TODO: ADD CODE
        ctx.result("update user - not implemented yet");
    }

    public void deactivate(Context ctx)
    {
        //TODO: ADD CODE
        ctx.result("delete user - not implemented yet");
    }
}