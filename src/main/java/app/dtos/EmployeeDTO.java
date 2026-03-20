package app.dtos;

import app.entities.enums.UserRole;

public record EmployeeDTO
        (
                Integer id,
                String firstName,
                String lastName,
                String phone,
                String email,
                UserRole role,
                boolean active
        )
{
}