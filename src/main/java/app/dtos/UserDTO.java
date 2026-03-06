package app.dtos;

import app.entities.enums.UserRole;
import app.entities.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO
{
    private Integer id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private UserRole role;
    private boolean active;

    public UserDTO(User user)
    {
        this.id = user.getUserId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.phone = user.getPhone();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.active = user.isActive();
    }
}
