package app.persistence.interfaces;

public interface IUpdateDAO<T>
{
    T update(T t);
}
