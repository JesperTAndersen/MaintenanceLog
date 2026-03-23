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
//        if(!employeeDao.getAll().isEmpty()) //only seeds if database is empty
//        {
//            System.out.println("Database not empty - Skipping seeding");
//            return;
//        }

        List<RandomUserDTO> randomUsers = fetchUsers(count, multiThreaded, threads);

        List<Employee> convertedEmployees = userDtoToEntity(randomUsers);

        assignRoles(convertedEmployees);

        for (Employee employee : convertedEmployees)
        {
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