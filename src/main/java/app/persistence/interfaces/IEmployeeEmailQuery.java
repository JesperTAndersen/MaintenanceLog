package app.persistence.interfaces;

import app.entities.Employee;

public interface IEmployeeEmailQuery
{
    Employee getByEmail(String email);
}
