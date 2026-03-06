package app.controllers.routes;

import app.controllers.AssetController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.put;

public class AssetRoutes
{
    private final AssetController assetController;

    public AssetRoutes(AssetController assetController)
    {
        this.assetController = assetController;
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            path("api/v1/assets", () ->
            {
                get(assetController::getAll);
                get("/{id}", assetController::get);
                post(assetController::create);
                put("/{id}", assetController::update);
                delete("/{id}", assetController::delete);
            });
        };
    }
}