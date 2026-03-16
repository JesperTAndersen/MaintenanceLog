package app.persistence.interfaces;

import app.entities.MaintenanceLog;

public interface IMaintenanceLogDAO extends ICreateDAO<MaintenanceLog>, IReadDAO<MaintenanceLog>, IMaintenanceLogQueries
{
}
