package app.integration.seeding;

public interface ApiUserService
{
    void seedEmployees(int count, boolean multiThreaded, int threads);
}
