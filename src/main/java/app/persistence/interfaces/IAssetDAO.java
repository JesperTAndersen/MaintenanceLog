package app.persistence.interfaces;

import app.entities.model.Asset;

import java.util.List;

public interface IAssetDAO
{
    Asset setActive(Integer id, boolean active);

    List<Asset> getInactiveAssets();
}
