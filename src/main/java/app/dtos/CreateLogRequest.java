package app.dtos;

import app.entities.enums.LogStatus;
import app.entities.enums.TaskType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public record CreateLogRequest
        (
                LocalDateTime performedDate,
                LogStatus status,
                TaskType taskType,
                String comment,
                Integer performedByUserId
        )
{
}