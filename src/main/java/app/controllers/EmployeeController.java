package app.controllers;

import app.dtos.EmployeeDTO;
import app.services.interfaces.EmployeeService;
import io.javalin.http.Context;

public class EmployeeController
{
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService)
    {
        this.employeeService = employeeService;
    }

    public void getAll(Context ctx)
    {
        String activeParam = ctx.queryParam("active");
        Boolean active = activeParam != null ? Boolean.parseBoolean(activeParam) : null;

        ctx.status(200).json(employeeService.getAll(active));
    }

    public void get(Context ctx)
    {
        int id = Integer.parseInt(ctx.pathParam("id"));
        ctx.status(200).json(employeeService.get(id));
    }

    public void update(Context ctx)
    {
        int id = Integer.parseInt(ctx.pathParam("id"));
        EmployeeDTO employeeDTO = ctx.bodyValidator(EmployeeDTO.class)
                .check(dto -> dto.id() == null || dto.id().equals(id), "ID in URL and body must match")
                .check(dto -> dto.firstName() != null && !dto.firstName().trim().isEmpty() , "First Name is required")
                .check(dto -> dto.lastName() != null && !dto.lastName().trim().isEmpty(), "Last Name is required")
                .check(dto -> dto.email() != null && !dto.email().trim().isEmpty(), "Email is required")
                .check(dto -> dto.phone() != null&& !dto.phone().trim().isEmpty(), "Phone is required")
                .check(dto -> dto.role() != null, "Role is required")
                .get();

        ctx.status(200).json(employeeService.update(id, employeeDTO));
    }

    public void deactivate(Context ctx)
    {
        int id = Integer.parseInt(ctx.pathParam("id"));
        employeeService.deactivate(id);
        ctx.status(204);
    }


    public void activate(Context ctx)
    {
        int id = Integer.parseInt(ctx.pathParam("id"));
        employeeService.activate(id);
        ctx.status(204);
    }
}