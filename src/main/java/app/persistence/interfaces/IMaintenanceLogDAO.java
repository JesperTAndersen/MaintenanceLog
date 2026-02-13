package app.persistence.interfaces;

import app.entities.enums.LogStatus;
import app.entities.enums.TaskType;
import app.entities.model.MaintenanceLog;

import java.util.List;

public interface IMaintenanceLogDAO
{
    List<MaintenanceLog> getByAsset(Integer assetId);

    List<MaintenanceLog> getByAssetAndTask(Integer assetId, TaskType taskType);

    List<MaintenanceLog> getByStatus(LogStatus status);

    List<MaintenanceLog> getByStatusAndAsset(LogStatus status, Integer assetId);

    List<MaintenanceLog> getByPerformedUser(Integer userId);

    List<MaintenanceLog> getLogsOnActiveAssets(int limit);
}
