package app.security.dao;

import app.entities.User;
import app.exceptions.ValidationException;
import app.persistence.interfaces.ICreateDAO;
import app.persistence.interfaces.IUserEmailQuery;
import app.persistence.interfaces.IUserQueries;

public interface ISecurityDAO extends ICreateDAO<User>, IUserEmailQuery
{
    User getVerifiedUser(String email, String password) throws ValidationException;
}
