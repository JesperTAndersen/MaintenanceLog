package app.persistence.interfaces;

import app.entities.Employee;

import java.util.List;

public interface IEmployeeQueries
{
    List<Employee> getInactiveUsers(int limit);
    List<Employee> getActiveUsers(int limit);
}
