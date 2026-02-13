package app.persistence.config;

import app.entities.model.Asset;
import app.entities.model.MaintenanceLog;
import app.entities.model.User;
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
        configuration.addAnnotatedClass(MaintenanceLog.class);
        // TODO: Add more entities here...
    }
}