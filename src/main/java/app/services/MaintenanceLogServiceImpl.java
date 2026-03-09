package app.services;

import app.entities.Asset;
import app.entities.MaintenanceLog;
import app.entities.User;
import app.persistence.interfaces.IDAO;

public class MaintenanceLogServiceImpl
{
    private final IDAO<MaintenanceLog> logDao;
    private final IDAO<Asset> assetDao;
    private final IDAO<User> userDao;

    public MaintenanceLogServiceImpl(IDAO<MaintenanceLog> logDao, IDAO<Asset> assetDao, IDAO<User> userDao)
    {
        this.logDao = logDao;
        this.assetDao = assetDao;
        this.userDao = userDao;
    }
}

