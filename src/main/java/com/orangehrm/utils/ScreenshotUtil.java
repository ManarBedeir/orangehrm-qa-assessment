package com.orangehrm.utils;

import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Captures screenshots to {@code screenshots/} and attaches them to the Allure report. */
public final class ScreenshotUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ScreenshotUtil.class);
    private static final Path DIRECTORY = Paths.get("screenshots");
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private ScreenshotUtil() {
    }

    /**
     * Saves a screenshot named {@code <timestamp>_<name>.png} and attaches it to the current Allure test.
     *
     * @return the saved file
     */
    public static Path capture(WebDriver driver, String name) {
        byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        Allure.addAttachment(name, "image/png", new ByteArrayInputStream(png), ".png");
        Path file = DIRECTORY.resolve(TIMESTAMP.format(LocalDateTime.now()) + "_" + sanitize(name) + ".png");
        try {
            Files.createDirectories(DIRECTORY);
            Files.write(file, png);
            LOG.info("Screenshot saved: {}", file);
        } catch (IOException e) {
            LOG.warn("Could not write screenshot {}: {}", file, e.getMessage());
        }
        return file;
    }

    private static String sanitize(String name) {
        return name.replaceAll("[^A-Za-z0-9._-]+", "-");
    }
}
