package app.controllers.routes;

import app.controllers.AssetController;
import app.controllers.LogController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class AssetRoutes
{
    private final AssetController assetController;
    private final LogController logController;

    public AssetRoutes(AssetController assetController, LogController logController)
    {
        this.assetController = assetController;
        this.logController = logController;
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
                patch("/{id}", assetController::active);
                delete("/{id}", assetController::delete);

                path("/{id}/logs", () ->
                {
                    get(logController::getLogsByAsset);
                    post(logController::createLogForAsset);
                });
            });
        };
    }
}