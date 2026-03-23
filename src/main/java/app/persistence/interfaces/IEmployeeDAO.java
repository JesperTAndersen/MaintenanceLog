package app.persistence.interfaces;

import app.entities.Employee;

public interface IEmployeeDAO extends IReadDAO<Employee>, IUpdateDAO<Employee>, IEmployeeQueries, ISecurityDAO
{
}
