package app.integration.seeding;

import app.entities.Employee;
import app.entities.enums.EmployeeRole;
import app.integration.RandomUserClient;
import app.integration.RandomUserDTO;
import app.persistence.interfaces.ICrudDAO;

import java.util.ArrayList;
import java.util.List;

import static app.security.SecurityServiceImpl.hashPassword;

public class ApiUserServiceImpl implements ApiUserService
{
    private final RandomUserClient client;
    private final ICrudDAO<Employee> userDao;

    public ApiUserServiceImpl(RandomUserClient client, ICrudDAO<Employee> userDao)
    {
        this.client = client;
        this.userDao = userDao;
    }

    @Override
    public void seedUsers(int count, boolean multiThreaded, int threads)
    {
        if(!userDao.getAll().isEmpty()) //only seeds if database is empty
        {
            System.out.println("Database not empty - Skipping seeding");
            return;
        }

        List<RandomUserDTO> randomUsers = fetchUsers(count, multiThreaded, threads);

        List<Employee> convertedEmployees = userDtoToEntity(randomUsers);

        assignRoles(convertedEmployees);

        for (Employee u : convertedEmployees)
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

    private List<Employee> userDtoToEntity(List<RandomUserDTO> dtos)
    {
        List<Employee> convertedEmployees = new ArrayList<>();
        for (RandomUserDTO u : dtos)
        {
            convertedEmployees.add(
                    Employee.builder()
                            .firstName(u.getName().first())
                            .lastName(u.getName().last())
                            .phone(u.getPhone())
                            .email(u.getEmail())
                            .password(hashPassword(u.getLogin().password()))
                            .active(true)
                            .build());
        }
        return convertedEmployees;
    }

    private void assignRoles(List<Employee> employees)
    {
        int counter = 1;
        for (Employee u : employees)
        {
            if (counter % 5 == 0)
            {
                u.setRole(EmployeeRole.MANAGER);
            }
            else
            {
                u.setRole(EmployeeRole.TECHNICIAN);
            }
            counter++;
        }
    }


}