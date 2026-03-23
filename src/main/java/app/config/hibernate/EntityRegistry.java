package app.config.hibernate;

import app.entities.Asset;
import app.entities.Employee;
import app.entities.MaintenanceLog;
import org.hibernate.cfg.Configuration;

final class EntityRegistry
{

    private EntityRegistry()
    {
    }

    static void registerEntities(Configuration configuration)
    {
        configuration.addAnnotatedClass(Asset.class);
        configuration.addAnnotatedClass(Employee.class);
        configuration.addAnnotatedClass(MaintenanceLog.class);
        // TODO: Add more entities here...
    }
}