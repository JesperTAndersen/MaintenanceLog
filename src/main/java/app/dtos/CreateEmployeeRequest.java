package app.dtos;

import app.entities.enums.EmployeeRole;

public record CreateEmployeeRequest
        (
                String firstName,
                String lastName,
                String email,
                String phone,
                EmployeeRole role,
                String password)
{
}