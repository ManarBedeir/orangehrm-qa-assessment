package com.orangehrm.driver;

import com.orangehrm.config.ConfigReader;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * Creates a configured {@link WebDriver}. Driver binaries are resolved by Selenium Manager,
 * so no driver download or path setup is needed.
 */
public final class DriverFactory {

    private DriverFactory() {
    }

    public static WebDriver create() {
        WebDriver driver = switch (ConfigReader.browser()) {
            case "chrome" -> new ChromeDriver(chromeOptions());
            case "firefox" -> new FirefoxDriver(firefoxOptions());
            case "edge" -> new EdgeDriver(edgeOptions());
            default -> throw new IllegalArgumentException("Unsupported browser: " + ConfigReader.browser());
        };
        driver.manage().timeouts().pageLoadTimeout(ConfigReader.pageLoadTimeout());
        driver.manage().window().setSize(new Dimension(ConfigReader.windowWidth(), ConfigReader.windowHeight()));
        return driver;
    }

    private static ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();
        if (ConfigReader.headless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--disable-search-engine-choice-screen", "--disable-notifications");
        return options;
    }

    private static FirefoxOptions firefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();
        if (ConfigReader.headless()) {
            options.addArguments("-headless");
        }
        return options;
    }

    private static EdgeOptions edgeOptions() {
        EdgeOptions options = new EdgeOptions();
        if (ConfigReader.headless()) {
            options.addArguments("--headless=new");
        }
        return options;
    }
}
