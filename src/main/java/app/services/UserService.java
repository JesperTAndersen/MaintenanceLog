package app.services;

import app.dtos.CreateUserRequest;
import app.dtos.UserDTO;

import java.util.List;

public interface UserService
{
    UserDTO create(CreateUserRequest userRequest);

    UserDTO get(Integer id);

    List<UserDTO> getAll();

    UserDTO update(Integer id, UserDTO userDTO);

    UserDTO deactivate(Integer id);

    UserDTO activate(Integer id);
}
