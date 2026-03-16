package app.persistence.interfaces;

import app.entities.Asset;

public interface IAssetDAO extends ICreateDAO<Asset>, IReadOnlyDAO<Asset>, IAssetQueries
{
}
