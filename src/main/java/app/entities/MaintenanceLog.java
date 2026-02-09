package app.entities;

import app.utils.LogStatus;
import app.utils.TaskType;
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
    int logId;

    @Column(name = "performed_date", nullable = false)
    LocalDate performedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    LogStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    TaskType taskType;

    @Column(name = "comment", nullable = false)
    String comment;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    Asset asset;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performed_by_user_id", nullable = false)
    User performedBy;
}