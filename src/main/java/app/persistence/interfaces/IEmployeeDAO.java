package app.persistence.interfaces;

import app.entities.Employee;
import app.security.dao.ISecurityDAO;

public interface IEmployeeDAO extends IReadDAO<Employee>, IUpdateDAO<Employee>, IEmployeeQueries, ISecurityDAO
{
}
