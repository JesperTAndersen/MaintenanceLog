package app.services;

import app.dtos.CreateUserRequest;
import app.dtos.UserDTO;
import app.entities.model.User;
import app.exceptions.ApiException;
import app.persistence.interfaces.IDAO;
import app.persistence.interfaces.IUserDAO;

import java.util.List;

import static app.utils.CredentialsHandler.hashPassword;

public class UserServiceImpl implements UserService
{
    private final IDAO<User> userDao;
    private final IUserDAO userDaoExpanded;

    public UserServiceImpl(IDAO<User> userDao, IUserDAO userDaoExpanded)
    {
        this.userDao = userDao;
        this.userDaoExpanded = userDaoExpanded;
    }

    @Override
    public UserDTO create(CreateUserRequest request) {
        if (userDaoExpanded.getByEmail(request.getEmail()) != null) {
            throw new ApiException(409, "Email already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(request.getRole())
                .password(hashPassword(request.getPassword()))
                .active(true)
                .build();

        User created = userDao.create(user);
        return new UserDTO(created);
    }

    @Override
    public UserDTO get(Integer id)
    {
        return null;
    }

    @Override
    public List<UserDTO> getAll()
    {
        return List.of();
    }

    @Override
    public UserDTO update(UserDTO userDTO)
    {
        return null;
    }

    @Override
    public UserDTO deactivate(Integer id)
    {
        return null;
    }
}
