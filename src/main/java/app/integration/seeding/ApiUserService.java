package app.integration.seeding;

public interface ApiUserService
{
    void seedUsers(int count, boolean multiThreaded, int threads);
}
