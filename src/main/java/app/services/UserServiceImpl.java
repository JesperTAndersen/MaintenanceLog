package app.services;

import app.dtos.CreateUserRequest;
import app.dtos.UserDTO;
import app.entities.User;
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
    public UserDTO create(CreateUserRequest request)
    {
        if (userDaoExpanded.getByEmail(request.getEmail()) != null)
        {
            throw new ApiException(409, "Email already exists");
        }
        //TODO: validate inputs. implement validator util class
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
        return new UserDTO(userDao.get(id));
    }

    @Override
    public List<UserDTO> getAll(Boolean active)
    {
        List<User> users;

        if (active == null)
        {
            users = userDao.getAll();
        }
        else if (active)
        {
            users = userDaoExpanded.getActiveUsers(50);
        }
        else
        {
            users = userDaoExpanded.getInactiveUsers(50);
        }

        return users
                .stream()
                .map(UserDTO::new)
                .toList();
    }

    @Override
    public UserDTO update(Integer id, UserDTO userDTO)
    {
        User existingUser = userDao.get(id);

        //check first if email is changing, then for if taken
        if (!existingUser.getEmail().equals(userDTO.getEmail()))
        {
            User userWithEmail = userDaoExpanded.getByEmail(userDTO.getEmail());
            if (userWithEmail != null)
            {
                throw new ApiException(409, "Email already exists");
            }
        }

        existingUser.setFirstName(userDTO.getFirstName());
        existingUser.setLastName(userDTO.getLastName());
        existingUser.setPhone(userDTO.getPhone());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setRole(userDTO.getRole());
        existingUser.setActive(userDTO.isActive());

        return new UserDTO(userDao.update(existingUser));
    }

    @Override
    public UserDTO deactivate(Integer id)
    {
        User user = userDao.get(id);

        if (!user.isActive())
        {
            return new UserDTO(user);
        }

        user.setActive(false);
        return new UserDTO(userDao.update(user));
    }

    @Override
    public UserDTO activate(Integer id)
    {
        User user = userDao.get(id);

        if (user.isActive())
        {
            return new UserDTO(user);
        }

        user.setActive(true);
        return new UserDTO(userDao.update(user));
    }

    //TODO: ADD PASSWORD CHANGER
}