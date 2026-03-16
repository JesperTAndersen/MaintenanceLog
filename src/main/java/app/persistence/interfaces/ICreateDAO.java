package app.persistence.interfaces;

public interface ICreateDAO<T>
{
    T create(T t);
}
