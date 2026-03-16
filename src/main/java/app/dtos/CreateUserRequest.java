package app.dtos;

import app.entities.enums.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public record CreateUserRequest
        (
                String firstName,
                String lastName,
                String email,
                String phone,
                UserRole role,
                String password)
{
}