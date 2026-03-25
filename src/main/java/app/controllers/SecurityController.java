package app.controllers;

import app.dtos.CreateEmployeeRequest;
import app.services.interfaces.SecurityService;
import app.dtos.EmployeeLoginDTO;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

public class SecurityController
{
    private final SecurityService securityService;

    public SecurityController(SecurityService securityService)
    {
        this.securityService = securityService;
    }

    public void register(Context ctx)
    {
        CreateEmployeeRequest userRequest = ctx.bodyValidator(CreateEmployeeRequest.class).
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
        EmployeeLoginDTO loginDTO = ctx.bodyValidator(EmployeeLoginDTO.class)
                .check(dto -> dto.email() != null, "Email is required")
                .check(dto -> dto.password() != null, "password is required")
                .get();

        ctx.status(200).json(securityService.login(loginDTO));
    }

    public void healthCheck(Context ctx) {
        ctx.status(200).json("{\"msg\": \"API is up and running\"}");
    }
}
