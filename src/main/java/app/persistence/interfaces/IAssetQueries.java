package app.persistence.interfaces;

import app.entities.Asset;

import java.util.List;

public interface IAssetQueries
{
    Asset setActive(Integer id, boolean active);

    List<Asset> getAllByStatus(boolean active);
}