package app.mappers;

import app.dtos.MaintenanceLogDTO;
import app.entities.MaintenanceLog;

public class MaintenanceLogMapper
{
    public static MaintenanceLogDTO toDTO(MaintenanceLog log)
    {
        return new MaintenanceLogDTO(
                log.getLogId(),
                log.getPerformedDate(),
                log.getStatus(),
                log.getTaskType(),
                log.getComment(),

                log.getAsset().getAssetId(),
                log.getAsset().getName(),

                log.getPerformedBy().getEmployeeId(),
                log.getPerformedBy().getFirstName() + " " + log.getPerformedBy().getLastName());
    }
}