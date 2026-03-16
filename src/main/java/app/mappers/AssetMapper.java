package app.mappers;

import app.dtos.AssetDTO;
import app.entities.Asset;

import java.time.LocalDateTime;

public class AssetMapper
{
    public static AssetDTO toDTO(Asset asset, LocalDateTime lastLogDate)
    {
        return new AssetDTO(asset.getAssetId(),
                asset.getName(),
                asset.getDescription(),
                asset.isActive(),
                lastLogDate);
    }

    public static AssetDTO toDTO(Asset asset)
    {
        return toDTO(asset,null);
    }

    public static Asset toEntity(AssetDTO dto) {
        return new Asset(
                dto.name(),
                dto.description(),
                dto.active(),
                null
        );
    }
}
