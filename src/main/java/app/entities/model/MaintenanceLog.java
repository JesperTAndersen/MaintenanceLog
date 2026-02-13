package app.entities.model;

import app.entities.enums.LogStatus;
import app.entities.enums.TaskType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@Entity
public class MaintenanceLog
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id", nullable = false)
    private Integer logId;

    @Column(name = "performed_date", nullable = false)
    private LocalDate performedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LogStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private TaskType taskType;

    @Column(name = "comment", nullable = false)
    private String comment;

    @Setter
    @ManyToOne(optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Setter
    @ManyToOne(optional = false)
    @JoinColumn(name = "performed_by_user_id", nullable = false)
    private User performedBy;
}