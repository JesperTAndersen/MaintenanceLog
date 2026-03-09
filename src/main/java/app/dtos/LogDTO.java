package app.dtos;

import app.entities.MaintenanceLog;
import app.entities.enums.LogStatus;
import app.entities.enums.TaskType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class LogDTO
{
    private Integer id;
    private LocalDateTime performedDate;
    private LogStatus status;
    private TaskType taskType;
    private String comment;

    private Integer assetId;
    private String assetName;

    private Integer performedByUserId;
    private String performedByName;

    public LogDTO(MaintenanceLog log) {
        this.id = log.getLogId();
        this.performedDate = log.getPerformedDate();
        this.status = log.getStatus();
        this.taskType = log.getTaskType();
        this.comment = log.getComment();

        this.assetId = log.getAsset().getAssetId();
        this.assetName = log.getAsset().getName();

        this.performedByUserId = log.getPerformedBy().getUserId();
        this.performedByName = log.getPerformedBy().getFirstName() + " " + log.getPerformedBy().getLastName();
    }
}