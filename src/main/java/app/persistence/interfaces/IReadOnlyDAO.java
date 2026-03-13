package app.persistence.interfaces;

public interface IReadOnlyDAO<T>
{
    T get(Integer id);
}

