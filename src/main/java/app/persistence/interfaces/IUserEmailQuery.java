package app.persistence.interfaces;

import app.entities.User;

public interface IUserEmailQuery
{
    User getByEmail(String email);
}
