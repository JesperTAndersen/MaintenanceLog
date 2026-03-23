package app.dtos;

import app.entities.enums.EmployeeRole;

public record EmployeeDTO
        (
                Integer id,
                String firstName,
                String lastName,
                String phone,
                String email,
                EmployeeRole role,
                boolean active
        )
{
}