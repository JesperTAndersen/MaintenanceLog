package app.entities.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Getter
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@Entity
public class Asset
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_id", nullable = false)
    private Integer assetId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Setter
    @Column(name = "status", nullable = false)
    private boolean active;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "asset")
    @OrderBy("performedDate DESC")
    private List<MaintenanceLog> logs = new ArrayList<>();

    public void addLog(MaintenanceLog log)
    {
        logs.add(log);
        log.setAsset(this);
    }
}