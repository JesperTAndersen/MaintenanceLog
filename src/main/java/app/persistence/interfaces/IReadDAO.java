package app.persistence.interfaces;

import java.util.List;

public interface IReadDAO<T>
{
    T get(Integer id);

    List<T> getAll();
}

