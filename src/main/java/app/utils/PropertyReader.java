package app.utils;

import app.exceptions.ApiException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader
{

    private static final Properties DEFAULTS = new Properties();
    static {
        // JWT/token defaults (used by tests)
        DEFAULTS.setProperty("ISSUER", "maintenance-log-test");
        DEFAULTS.setProperty("TOKEN_EXPIRE_TIME", "1800000");
        DEFAULTS.setProperty("SECRET_KEY", "insecure-test-secret-change-me");
    }

    public static String getPropertyValue(String propName, String resourceName)  {
        String envValue = System.getenv(propName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        String defaultValue = DEFAULTS.getProperty(propName);

        try (InputStream is = PropertyReader.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                if (defaultValue != null) {
                    return defaultValue;
                }
                throw new ApiException(500, String.format(
                        "Property file %s was not found on the classpath and environment variable %s is not set",
                        resourceName,
                        propName
                ));
            }

            Properties prop = new Properties();
            prop.load(is);

            String value = prop.getProperty(propName);
            if (value != null) {
                return value.trim();  // Trim whitespace
            } else {
                if (defaultValue != null) {
                    return defaultValue;
                }
                throw new ApiException(500, String.format("Property %s not found in %s", propName, resourceName));
            }
        } catch (IOException ex) {
            throw new ApiException(500, String.format("Could not read property %s.", propName));
        }
    }
}