package app.persistence.interfaces;

import app.entities.User;
import app.security.dao.ISecurityDAO;

public interface IUserDAO extends ICrudDAO<User>, ISecurityDAO, IUserQueries, IUserEmailQuery
{
}
