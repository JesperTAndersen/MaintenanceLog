package app.persistence.interfaces;

import app.entities.User;

import java.util.List;

public interface IUserQueries
{
    User getByEmail(String email);
    List<User> getInactiveUsers(int limit);
    List<User> getActiveUsers(int limit);
}
