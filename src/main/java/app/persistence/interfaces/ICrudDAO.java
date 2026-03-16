package app.persistence.interfaces;

public interface ICrudDAO<T> extends ICreateDAO<T>, IReadOnlyDAO<T>, IUpdateDAO<T>
{
}