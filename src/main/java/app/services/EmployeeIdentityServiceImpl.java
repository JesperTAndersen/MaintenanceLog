package app.services;

import app.entities.Employee;
import app.persistence.interfaces.IEmployeeEmailQuery;
import app.services.interfaces.EmployeeIdentityService;

public class EmployeeIdentityServiceImpl implements EmployeeIdentityService
{
    private final IEmployeeEmailQuery employeeDao;

    public EmployeeIdentityServiceImpl(IEmployeeEmailQuery employeeDao)
    {
        this.employeeDao = employeeDao;
    }

    @Override
    public Integer getEmployeeIdByEmail(String email)
    {
        Employee employee = employeeDao.getByEmail(email);

        return employee == null ? null : employee.getEmployeeId();
    }
}
