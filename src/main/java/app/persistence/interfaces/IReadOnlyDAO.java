package app.persistence.interfaces;

import java.util.List;

public interface IReadOnlyDAO<T>
{
    T get(Integer id);

    List<T> getAll();
}

