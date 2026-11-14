
package ke.ac.kyu.srms.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class DatabaseConfig {

    private static final Properties PROPERTIES = new Properties();

    private static final String CONFIG_PATH =
            System.getProperty("srms.config", "database/db.properties");

    static {
        loadConfiguration();
    }

    private DatabaseConfig() {
        // Prevent instantiation.
    }

    private static void loadConfiguration() {
        Path path = Path.of(CONFIG_PATH).toAbsolutePath();

        if (!Files.exists(path)) {
            throw new IllegalStateException(
                    "Database configuration file not found: " + path
                    + "\nCreate database/db.properties before running the application."
            );
        }

        try (InputStream input = Files.newInputStream(path)) {
            PROPERTIES.load(input);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to load database configuration.", exception
            );
        }
    }

    private static String getValue(String environmentVariable,
                                   String propertyKey) {

        String environmentValue = System.getenv(environmentVariable);

        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }

        String propertyValue = PROPERTIES.getProperty(propertyKey);

        if (propertyValue == null || propertyValue.isBlank()) {
            throw new IllegalStateException(
                    "Missing database configuration: " + propertyKey
            );
        }

        return propertyValue.trim();
    }

    public static String getUrl() {
        return getValue("SRMS_DB_URL", "db.url");
    }

    public static String getUsername() {
        return getValue("SRMS_DB_USERNAME", "db.username");
    }

    public static String getPassword() {
        return getValue("SRMS_DB_PASSWORD", "db.password");
    }
}
