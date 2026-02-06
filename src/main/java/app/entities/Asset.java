package app.entities;

import app.utils.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@NoArgsConstructor
@Builder
@ToString
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
}

