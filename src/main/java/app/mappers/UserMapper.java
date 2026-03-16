package app.mappers;

import app.dtos.UserDTO;
import app.entities.Asset;
import app.entities.User;

public class UserMapper
{
    public static UserDTO toDTO(User user)
    {
        return new UserDTO(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                user.isActive()
        );
    }
}