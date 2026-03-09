package app.services;

import app.dtos.AssetDTO;

import java.util.List;

public interface AssetService
{
    AssetDTO create(AssetDTO dto);

    AssetDTO get(Integer id);

    List<AssetDTO> getAll(Boolean active);

    AssetDTO activate(Integer id);

    AssetDTO deactivate(Integer id);
}
