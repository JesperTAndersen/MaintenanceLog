package app.dtos;

import app.entities.enums.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateUserRequest
{
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserRole role;
    private String password;
}
