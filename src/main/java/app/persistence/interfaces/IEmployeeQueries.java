package app.persistence.interfaces;

import app.entities.Employee;

import java.util.List;

public interface IEmployeeQueries
{
    List<Employee> getInactiveEmployees(int limit);
    List<Employee> getActiveEmployees(int limit);
}
