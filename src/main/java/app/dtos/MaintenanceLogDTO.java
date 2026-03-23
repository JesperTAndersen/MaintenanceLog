package app.dtos;

import app.entities.MaintenanceLog;
import app.entities.enums.LogStatus;
import app.entities.enums.TaskType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public record MaintenanceLogDTO
        (
                Integer id,
                LocalDateTime performedDate,
                LogStatus status,
                TaskType taskType,
                String comment,
                Integer assetId,
                String assetName,
                Integer performedByEmployeeId,
                String performedByName
        )
{
}