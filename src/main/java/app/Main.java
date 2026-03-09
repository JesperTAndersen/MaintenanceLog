package app;

import app.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main
{
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args)
    {
        AppConfig.start(7070);
        log.info("Server started on port 7070");
    }
}