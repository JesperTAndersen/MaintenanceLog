package app.security.controllers;

import app.dtos.CreateUserRequest;
import app.dtos.UserDTO;
import app.exceptions.ApiException;
import app.exceptions.ValidationException;
import app.security.SecurityService;
import app.security.UserLoginDTO;
import io.javalin.http.Context;

public class SecurityController
{
    private final SecurityService securityService;

    public SecurityController(SecurityService securityService)
    {
        this.securityService = securityService;
    }

    public void register(Context ctx)
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

        ctx.status(201).json(securityService.register(userRequest));
    }

    public void login(Context ctx)
    {
        UserLoginDTO loginDTO = ctx.bodyValidator(UserLoginDTO.class)
                .check(dto -> dto.email() != null, "Email is required")
                .check(dto -> dto.password() != null, "password is required")
                .get();

        ctx.status(200).json(securityService.login(loginDTO));
    }

}
