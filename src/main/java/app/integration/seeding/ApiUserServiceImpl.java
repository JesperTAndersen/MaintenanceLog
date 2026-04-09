package app.integration.seeding;

import app.entities.Employee;
import app.entities.enums.EmployeeRole;
import app.integration.RandomUserClient;
import app.integration.RandomUserDTO;
import app.persistence.EmployeeDAO;

import java.util.ArrayList;
import java.util.List;

import static app.services.SecurityServiceImpl.hashPassword;

public class ApiUserServiceImpl implements ApiUserService
{
    private final RandomUserClient client;
    private final EmployeeDAO employeeDao;

    public ApiUserServiceImpl(RandomUserClient client, EmployeeDAO employeeDao)
    {
        this.client = client;
        this.employeeDao = employeeDao;
    }

    @Override
    public void seedEmployees(int count, boolean multiThreaded, int threads)
    {
        List<RandomUserDTO> randomUsers = fetchUsers(count, multiThreaded, threads);

        List<Employee> convertedEmployees = userDtoToEntity(randomUsers);

        assignRoles(convertedEmployees);

        for (Employee employee : convertedEmployees)
        {
            System.out.println("creating employees");
            employeeDao.create(employee);
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

    private List<Employee> userDtoToEntity(List<RandomUserDTO> dtoList)
    {
        List<Employee> convertedEmployees = new ArrayList<>();
        for (RandomUserDTO u : dtoList)
        {
            convertedEmployees.add(
                    Employee.builder()
                            .firstName(u.getName().first().trim())
                            .lastName(u.getName().last().trim())
                            .phone(u.getPhone().trim())
                            .email(u.getEmail().trim())
                            .password(hashPassword(u.getLogin().password()))
                            .active(true)
                            .build());
        }
        return convertedEmployees;
    }

    private void assignRoles(List<Employee> employees)
    {
        int counter = 1;
        for (Employee employee : employees)
        {
            if (counter % 5 == 0)
            {
                employee.setRole(EmployeeRole.MANAGER);
            }
            else
            {
                employee.setRole(EmployeeRole.TECHNICIAN);
            }
            counter++;
        }
    }

}