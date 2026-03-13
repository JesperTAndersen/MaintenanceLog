package app.persistence.interfaces;

import java.util.List;

public interface IDAO<T> extends IReadOnlyDAO<T>
{
    T create(T t);

    List<T> getAll();

    T update(T t);
}