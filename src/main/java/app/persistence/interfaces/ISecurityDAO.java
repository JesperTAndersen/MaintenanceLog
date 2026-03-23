package app.persistence.interfaces;

import app.entities.Employee;
import app.exceptions.ValidationException;

public interface ISecurityDAO extends ICreateDAO<Employee>, IEmployeeEmailQuery
{
    Employee getVerifiedEmployee(String email, String password) throws ValidationException;
}
