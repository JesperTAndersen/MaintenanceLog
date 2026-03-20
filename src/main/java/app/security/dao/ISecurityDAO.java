package app.security.dao;

import app.entities.Employee;
import app.exceptions.ValidationException;
import app.persistence.interfaces.ICreateDAO;
import app.persistence.interfaces.IEmployeeEmailQuery;

public interface ISecurityDAO extends ICreateDAO<Employee>, IEmployeeEmailQuery
{
    Employee getVerifiedEmployee(String email, String password) throws ValidationException;
}
