package app.config;

import app.controllers.routes.Routes;
import app.exceptions.ApiException;
import app.exceptions.DatabaseException;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class AppConfig {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    public static Javalin create(Routes routes) {
        return Javalin.create(config -> {
            configurePlugins(config);
            configureRoutes(config, routes);
            configureExceptionHandlers(config);
        });
    }

    private static void configurePlugins(JavalinConfig config) {
        config.bundledPlugins.enableRouteOverview("/routes");
    }

    private static void configureRoutes(JavalinConfig config, Routes routes) {
        config.routes.apiBuilder(routes.getRoutes());
    }

    private static void configureExceptionHandlers(io.javalin.config.JavalinConfig config) {
        config.routes.exception(DatabaseException.class, (e, ctx) -> {
            int statusCode = switch (e.getErrorType()) {
                case NOT_FOUND -> 404;
                case CONSTRAINT_VIOLATION -> 409;
                case CONNECTION_FAILURE -> 503;
                case TRANSACTION_FAILURE, QUERY_FAILURE, UNKNOWN -> 500;
            };

            if (statusCode >= 500) {
                log.error("Database error [{}]: {}", e.getErrorType(), e.getMessage(), e);
            } else {
                log.warn("Database error [{}]: {}", e.getErrorType(), e.getMessage());
            }

            ctx.status(statusCode).json(Map.of("status", statusCode, "msg", e.getMessage()));
        });

        config.routes.exception(ApiException.class, (e, ctx) -> {
            log.warn("API error [{}]: {}", e.getCode(), e.getMessage());
            ctx.status(e.getCode()).json(Map.of("error", e.getMessage()));
        });

        config.routes.exception(RuntimeException.class, (e, ctx) -> {
            log.warn("Runtime error: {}", e.getMessage());
            ctx.status(400).json(e.getMessage());
        });
    }
}