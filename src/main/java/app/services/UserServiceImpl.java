package app.services;

import app.dtos.CreateUserRequest;
import app.dtos.UserDTO;
import app.entities.User;
import app.exceptions.ApiException;
import app.mappers.UserMapper;
import app.persistence.interfaces.IUserDAO;

import java.util.List;

import static app.utils.CredentialsHandler.hashPassword;

public class UserServiceImpl implements UserService
{
    private final IUserDAO userDao;

    public UserServiceImpl(IUserDAO userDao)
    {
        this.userDao = userDao;
    }

    @Override
    public UserDTO create(CreateUserRequest request)
    {
        if (userDao.getByEmail(request.email()) != null)
        {
            throw new ApiException(409, "Email already exists");
        }
        //TODO: validate inputs. implement validator util class
        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phone(request.phone())
                .role(request.role())
                .password(hashPassword(request.password()))
                .active(true)
                .build();

        User created = userDao.create(user);
        return UserMapper.toDTO(created);
    }

    @Override
    public UserDTO get(Integer id)
    {
        return UserMapper.toDTO(userDao.get(id));
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
            users = userDao.getActiveUsers(100);
        }
        else
        {
            users = userDao.getInactiveUsers(100);
        }

        return users
                .stream()
                .map(UserMapper::toDTO)
                .toList();
    }

    @Override
    public UserDTO update(Integer id, UserDTO userDTO)
    {
        User existingUser = userDao.get(id);

        //check first if email is changing, then for if taken
        if (!existingUser.getEmail().equals(userDTO.email()))
        {
            User userWithEmail = userDao.getByEmail(userDTO.email());
            if (userWithEmail != null)
            {
                throw new ApiException(409, "Email already exists");
            }
        }

        existingUser.setFirstName(userDTO.firstName());
        existingUser.setLastName(userDTO.lastName());
        existingUser.setPhone(userDTO.phone());
        existingUser.setEmail(userDTO.email());
        existingUser.setRole(userDTO.role());
        existingUser.setActive(userDTO.active());

        return UserMapper.toDTO(userDao.update(existingUser));
    }

    @Override
    public UserDTO deactivate(Integer id)
    {
        User user = userDao.get(id);

        if (!user.isActive())
        {
            return UserMapper.toDTO(user);
        }

        user.setActive(false);
        return UserMapper.toDTO(userDao.update(user));
    }

    @Override
    public UserDTO activate(Integer id)
    {
        User user = userDao.get(id);

        if (user.isActive())
        {
            return UserMapper.toDTO(user);
        }

        user.setActive(true);
        return UserMapper.toDTO(userDao.update(user));
    }

    //TODO: ADD PASSWORD CHANGER
}