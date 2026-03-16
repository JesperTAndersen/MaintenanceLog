package app.services;

import app.dtos.AssetDTO;
import app.entities.Asset;
import app.persistence.interfaces.IAssetDAO;

import java.time.LocalDateTime;
import java.util.List;

public class AssetServiceImpl implements AssetService
{
    private final IAssetDAO assetDao;

    public AssetServiceImpl(IAssetDAO assetDao)
    {
        this.assetDao = assetDao;
    }

    @Override
    public AssetDTO create(AssetDTO dto)
    {
        Asset asset = Asset.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .active(dto.isActive()).
                build();

        Asset created = assetDao.create(asset);
        return new AssetDTO(created);
    }

    @Override
    public AssetDTO get(Integer id)
    {
        Asset asset = assetDao.get(id);

        LocalDateTime lastLogDate = null;
        if (!asset.getLogs().isEmpty())
        {
            lastLogDate = asset.getLogs().get(0).getPerformedDate();
        }

        AssetDTO dto = new AssetDTO(asset);
        dto.setLastLogDate(lastLogDate);
        return dto;
    }

    @Override
    public List<AssetDTO> getAll(Boolean active)
    {
        List<Asset> assets;

        if (active == null)
        {
            assets = assetDao.getAll();
        }
        else
        {
            assets = assetDao.getAllByStatus(active);
        }

        return assets.stream()
                .map(AssetDTO::new)
                .toList();
    }

    @Override
    public AssetDTO activate(Integer id)
    {
        Asset activated = assetDao.setActive(id, true);
        return new AssetDTO(activated);
    }

    @Override
    public AssetDTO deactivate(Integer id)
    {
        Asset deactivated = assetDao.setActive(id, false);
        return new AssetDTO(deactivated);
    }
}