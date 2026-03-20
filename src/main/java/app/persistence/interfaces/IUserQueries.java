package app.persistence.interfaces;

import app.entities.User;

import java.util.List;

public interface IUserQueries
{
    List<User> getInactiveUsers(int limit);
    List<User> getActiveUsers(int limit);
}
