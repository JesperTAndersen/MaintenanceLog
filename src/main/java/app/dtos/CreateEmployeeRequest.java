package app.dtos;

import app.entities.enums.UserRole;

public record CreateEmployeeRequest
        (
                String firstName,
                String lastName,
                String email,
                String phone,
                UserRole role,
                String password)
{
}