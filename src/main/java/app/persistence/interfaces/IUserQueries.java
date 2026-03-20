package app.persistence.interfaces;

import app.entities.User;

import java.util.List;

public interface IUserQueries extends IUserEmailQuery
{
    List<User> getInactiveUsers(int limit);
    List<User> getActiveUsers(int limit);
}
