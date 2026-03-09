package app.dtos;

import app.entities.enums.LogStatus;
import app.entities.enums.TaskType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CreateLogRequest
{
    private LocalDateTime performedDate;
    private LogStatus status;
    private TaskType taskType;
    private String comment;
    private Integer performedByUserId;
}