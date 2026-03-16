package app.dtos;

import app.entities.enums.UserRole;

public record UserDTO
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