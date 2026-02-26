package app.services;

public interface ApiUserService
{
    void seedUsers(int count, boolean multiThreaded, int threads);
}
