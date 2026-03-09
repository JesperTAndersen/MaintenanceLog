package app.dtos;

import app.entities.Asset;
import app.entities.MaintenanceLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AssetDTO
{
    private Integer id;
    private String name;
    private String description;
    private boolean active;
    private LocalDateTime lastLogDate;

    public AssetDTO(Asset asset)
    {
        this.id = asset.getAssetId();
        this.name = asset.getName();
        this.description = asset.getDescription();
        this.active = asset.isActive();
    }
}

