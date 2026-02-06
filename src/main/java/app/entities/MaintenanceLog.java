package app.entities;

import app.utils.LogStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Builder
@ToString
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

    @Column(name = "comment")
    String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    LogStatus status;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    Asset asset;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    Task task;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performed_by_user_id", nullable = false)
    User performedBy;
}

