package app.config;

import app.controllers.routes.Routes;
import app.exceptions.ApiException;
import app.exceptions.DatabaseException;
import app.services.interfaces.SecurityService;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.json.JavalinJackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ApplicationConfig
{
    private static final Logger log = LoggerFactory.getLogger(ApplicationConfig.class);


    public static Javalin start(int port)
    {
        DependencyContainer container = new DependencyContainer();
        return start(container, port);
    }

    public static Javalin start(DependencyContainer container, int port) //used for test Container
    {
        Routes routes = container.getRoutes();
        SecurityService securityService = container.getSecurityService();

        return Javalin.create(config ->
        {
            configureCors(config);
            configurePlugins(config);
            configureRoutes(config, routes);
            configureSecurity(config, securityService);
            configureExceptionHandlers(config);
            configureJackson(config);
        }).start(port);
    }

    public static void stop(Javalin app)
    {
        app.stop();
    }

    private static void configureJackson(JavalinConfig config)
    {
        config.jsonMapper(new JavalinJackson().updateMapper(mapper ->
                mapper.registerModule(new JavaTimeModule())
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        ));
    }

    private static void configureCors(JavalinConfig config)
    {
        boolean isProduction = System.getenv("DEPLOYED") != null;

        config.bundledPlugins.enableCors(cors -> {
            cors.addRule(it -> {
                if (isProduction) {
                    it.allowHost("https://mlf.heltsort.dk/");
                } else {
                    it.anyHost();
                }
//                it.allowCredentials = true;
            });
        });
    }

    private static void configurePlugins(JavalinConfig config)
    {
        config.bundledPlugins.enableRouteOverview("/routes");
    }

    private static void configureRoutes(JavalinConfig config, Routes routes)
    {
        config.routes.apiBuilder(routes.getRoutes());
    }

    private static void configureSecurity(JavalinConfig config, SecurityService securityService)
    {
        config.routes.beforeMatched(securityService::authenticate);
        config.routes.beforeMatched(securityService::authorize);
    }

    private static void configureExceptionHandlers(JavalinConfig config)
    {
        config.routes.exception(DatabaseException.class, (e, ctx) ->
        {
            int statusCode = switch (e.getErrorType())
            {
                case NOT_FOUND -> 404;
                case CONSTRAINT_VIOLATION -> 409;
                case CONNECTION_FAILURE -> 503;
                case TRANSACTION_FAILURE, QUERY_FAILURE, UNKNOWN -> 500;
            };

            if (statusCode >= 500)
            {
                log.error("Database error [{}]: {}", e.getErrorType(), e.getMessage(), e);
            }
            else
            {
                log.warn("Database error [{}]: {}", e.getErrorType(), e.getMessage());
            }

            ctx.status(statusCode).json(Map.of("status", statusCode, "msg", e.getMessage()));
        });

        config.routes.exception(ApiException.class, (e, ctx) ->
        {
            log.warn("API error [{}]: {}", e.getCode(), e.getMessage());
            ctx.status(e.getCode()).json(Map.of("error", e.getMessage()));
        });

        config.routes.exception(RuntimeException.class, (e, ctx) ->
        {
            log.warn("Runtime error: {}", e.getMessage());
            ctx.status(400).json(e.getMessage());
        });
    }
}