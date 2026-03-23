package app.services;

import app.dtos.EmployeeDTO;
import app.entities.Employee;
import app.exceptions.ApiException;
import app.mappers.EmployeeMapper;
import app.persistence.interfaces.IEmployeeDAO;
import app.services.interfaces.EmployeeService;

import java.util.List;

public class EmployeeServiceImpl implements EmployeeService
{
    private final IEmployeeDAO employeeDao;

    public EmployeeServiceImpl(IEmployeeDAO employeeDao)
    {
        this.employeeDao = employeeDao;
    }

    @Override
    public EmployeeDTO get(Integer id)
    {
        return EmployeeMapper.toDTO(employeeDao.get(id));
    }

    @Override
    public List<EmployeeDTO> getAll(Boolean active)
    {
        List<Employee> employees;

        if (active == null)
        {
            employees = employeeDao.getAll();
        }
        else if (active)
        {
            employees = employeeDao.getActiveEmployees(100);
        }
        else
        {
            employees = employeeDao.getInactiveEmployees(100);
        }

        return employees
                .stream()
                .map(EmployeeMapper::toDTO)
                .toList();
    }

    @Override
    public EmployeeDTO update(Integer id, EmployeeDTO employeeDTO)
    {
        Employee existingEmployee = employeeDao.get(id);

        //check first if email is changing, then for if taken
        if (!existingEmployee.getEmail().equals(employeeDTO.email()))
        {
            Employee employeeWithEmail = employeeDao.getByEmail(employeeDTO.email());
            if (employeeWithEmail != null)
            {
                throw new ApiException(409, "Email already exists");
            }
        }

        existingEmployee.setFirstName(employeeDTO.firstName());
        existingEmployee.setLastName(employeeDTO.lastName());
        existingEmployee.setPhone(employeeDTO.phone());
        existingEmployee.setEmail(employeeDTO.email());
        existingEmployee.setRole(employeeDTO.role());
        existingEmployee.setActive(employeeDTO.active());

        return EmployeeMapper.toDTO(employeeDao.update(existingEmployee));
    }

    @Override
    public EmployeeDTO deactivate(Integer id)
    {
        Employee employee = employeeDao.get(id);

        if (!employee.isActive())
        {
            return EmployeeMapper.toDTO(employee);
        }

        employee.setActive(false);
        return EmployeeMapper.toDTO(employeeDao.update(employee));
    }

    @Override
    public EmployeeDTO activate(Integer id)
    {
        Employee employee = employeeDao.get(id);

        if (employee.isActive())
        {
            return EmployeeMapper.toDTO(employee);
        }

        employee.setActive(true);
        return EmployeeMapper.toDTO(employeeDao.update(employee));
    }

    //TODO: ADD PASSWORD CHANGER
}