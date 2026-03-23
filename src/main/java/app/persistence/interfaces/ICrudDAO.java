package app.persistence.interfaces;

public interface ICrudDAO<T> extends ICreateDAO<T>, IReadDAO<T>, IUpdateDAO<T>
{
}