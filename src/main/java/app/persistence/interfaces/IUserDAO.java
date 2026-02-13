package app.persistence.interfaces;

import app.entities.model.User;

import java.util.List;

public interface IUserDAO
{
    User getByEmail(String email);
    List<User> getActiveUsers(int limit);
}
