package app.services;

import app.dtos.CreateLogRequest;
import app.dtos.MaintenanceLogDTO;
import app.entities.enums.LogStatus;
import app.entities.enums.TaskType;

import java.util.List;

public interface MaintenanceLogService
{
    MaintenanceLogDTO create(Integer assetId, CreateLogRequest request);

    MaintenanceLogDTO get(Integer id);

    List<MaintenanceLogDTO> getAll();

    List<MaintenanceLogDTO> getByAsset(Integer assetId);

    List<MaintenanceLogDTO> getByAssetAndTask(Integer assetId, TaskType taskType);

    List<MaintenanceLogDTO> getByStatus(LogStatus status);

    List<MaintenanceLogDTO> getByStatusAndAsset(LogStatus status, Integer assetId);

    List<MaintenanceLogDTO> getByPerformedEmployee(Integer userId);

    List<MaintenanceLogDTO> getLogsOnActiveAssets(int limit);
}

