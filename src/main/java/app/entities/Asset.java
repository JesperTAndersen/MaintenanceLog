package app.entities;

import jakarta.persistence.*;
import lombok.*;

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
    int assetId;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "description", nullable = false)
    String description;

    @Column(name = "status", nullable = false)
    boolean active;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "asset")
    List<MaintenanceLog> logs;
}

