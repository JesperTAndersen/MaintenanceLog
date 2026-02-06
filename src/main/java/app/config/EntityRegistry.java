package app.config;

import app.entities.Asset;
import app.entities.MaintenanceLog;
import app.entities.Task;
import app.entities.User;
import org.hibernate.cfg.Configuration;

final class EntityRegistry
{

    private EntityRegistry()
    {
    }

    static void registerEntities(Configuration configuration)
    {
        configuration.addAnnotatedClass(Asset.class);
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Task.class);
        configuration.addAnnotatedClass(MaintenanceLog.class);
        // TODO: Add more entities here...
    }
}