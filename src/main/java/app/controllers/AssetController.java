package app.controllers;

import app.dtos.AssetDTO;
import app.services.AssetService;
import io.javalin.http.Context;

public class AssetController
{
    private AssetService assetService;

    public AssetController(AssetService assetService)
    {
        this.assetService = assetService;
    }

    public void create(Context ctx)
    {
        AssetDTO assetDTO = ctx.bodyValidator(AssetDTO.class)
                .check(dto -> dto.name() != null, "Name is required")
                .check(dto -> dto.description() != null, "Description is required")
                .get();

        ctx.status(201).json(assetService.create(assetDTO));
    }

    public void getAll(Context ctx)
    {
        String activeParam = ctx.queryParam("active");
        Boolean active = activeParam != null ? Boolean.parseBoolean(activeParam) : null;

        ctx.json(assetService.getAll(active));
    }

    public void get(Context ctx)
    {
        int id = Integer.parseInt(ctx.pathParam("id"));
        ctx.status(200).json(assetService.get(id));
    }

    public void active(Context ctx)
    {
        int id = Integer.parseInt(ctx.pathParam("id"));
        assetService.activate(id);
        ctx.status(204);
    }

    public void delete(Context ctx)
    {
        int id = Integer.parseInt(ctx.pathParam("id"));
        assetService.deactivate(id);
        ctx.status(204);
    }
}