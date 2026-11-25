package com.orangehrm.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Properties;

/**
 * Reads run configuration from {@code config.properties} on the classpath.
 * A JVM system property with the same key always wins, so any value can be overridden
 * from the command line (for example {@code -Dbrowser=firefox -Dheadless=true}).
 */
public final class ConfigReader {

    private static final String CONFIG_FILE = "config.properties";
    private static final Properties PROPERTIES = load();

    private ConfigReader() {
    }

    public static String baseUrl() {
        return get("baseUrl").replaceAll("/+$", "");
    }

    public static String username() {
        return get("username");
    }

    public static String password() {
        return get("password");
    }

    public static String browser() {
        return get("browser").toLowerCase();
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("headless"));
    }

    public static int windowWidth() {
        return Integer.parseInt(get("windowWidth"));
    }

    public static int windowHeight() {
        return Integer.parseInt(get("windowHeight"));
    }

    public static Duration explicitWait() {
        return Duration.ofSeconds(Long.parseLong(get("explicitWaitSeconds")));
    }

    public static Duration pageLoadTimeout() {
        return Duration.ofSeconds(Long.parseLong(get("pageLoadTimeoutSeconds")));
    }

    private static String get(String key) {
        String value = System.getProperty(key, PROPERTIES.getProperty(key));
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing configuration value: " + key);
        }
        return value.trim();
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new IllegalStateException(CONFIG_FILE + " not found on the classpath");
            }
            properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + CONFIG_FILE, e);
        }
    }
}
