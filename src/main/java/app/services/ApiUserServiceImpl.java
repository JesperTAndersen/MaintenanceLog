package app.services;

import app.entities.enums.UserRole;
import app.entities.model.User;
import app.integration.client.RandomUserClient;
import app.integration.dto.RandomUserDTO;
import app.persistence.daos.UserDAO;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;

public class ApiUserServiceImpl implements ApiUserService
{
    private final RandomUserClient client;
    private final UserDAO userDao;

    public ApiUserServiceImpl(RandomUserClient client, UserDAO userDao)
    {
        this.client = client;
        this.userDao = userDao;
    }

    @Override
    public void seedUsers(int count, boolean multiThreaded, int threads)
    {
        List<RandomUserDTO> randomUsers = fetchUsers(count, multiThreaded, threads);

        List<User> convertedUsers = userDtoToEntity(randomUsers);

        assignRoles(convertedUsers);

        for (User u : convertedUsers)
        {
            userDao.create(u);
        }
    }

    private List<RandomUserDTO> fetchUsers(int count, boolean multiThreaded, int threads)
    {
        List<RandomUserDTO> randomUsers = new ArrayList<>();
        if (multiThreaded)
        {
            randomUsers.addAll(client.fetchUsersFromAPIMultiThreaded(threads, count));
        }
        else
        {
            randomUsers.addAll(client.fetchUsersFromAPI(count));
        }
        return randomUsers;
    }

    private List<User> userDtoToEntity(List<RandomUserDTO> dtos)
    {
        List<User> convertedUsers = new ArrayList<>();
        for (RandomUserDTO u : dtos)
        {
            convertedUsers.add(
                    User.builder()
                            .firstName(u.getName().first())
                            .lastName(u.getName().last())
                            .phone(u.getPhone())
                            .email(u.getEmail())
                            .password(hashPassword(u.getLogin().password()))
                            .active(true)
                            .build());
        }
        return convertedUsers;
    }

    private void assignRoles(List<User> users)
    {
        int counter = 1;
        for (User u : users)
        {
            if (counter % 5 == 0)
            {
                u.setRole(UserRole.MANAGER);
            }
            else
            {
                u.setRole(UserRole.TECHNICIAN);
            }
            counter++;
        }
    }

    private String hashPassword(String password)
    {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}