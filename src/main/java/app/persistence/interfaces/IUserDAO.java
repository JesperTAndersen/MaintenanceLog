package app.persistence.interfaces;

import app.entities.User;

public interface IUserDAO extends ICrudDAO<User>, IUserQueries
{
}
