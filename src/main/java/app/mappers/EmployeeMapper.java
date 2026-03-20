package app.mappers;

import app.dtos.EmployeeDTO;
import app.entities.Employee;

public class EmployeeMapper
{
    public static EmployeeDTO toDTO(Employee employee)
    {
        return new EmployeeDTO(
                employee.getEmployeeId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getPhone(),
                employee.getEmail(),
                employee.getRole(),
                employee.isActive()
        );
    }
}