package com.orangehrm.pages;

import com.orangehrm.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.function.Supplier;

/**
 * Shared explicit-wait and action helpers for pages and OXD components.
 * Every interaction waits for OrangeHRM's loading overlays to disappear first,
 * which removes the main source of flakiness on the demo site.
 */
public abstract class BasePage {

    private static final By LOADERS = By.cssSelector(".oxd-loading-spinner, .oxd-form-loader");

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, ConfigReader.explicitWait());
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    /** XPath of the OXD input group whose label text is exactly {@code label}. */
    protected static String fieldXpath(String label) {
        return "//label[normalize-space()='" + label + "']/ancestor::div[contains(concat(' ', @class, ' '),' oxd-input-group ')][1]";
    }

    protected static By fieldGroup(String label) {
        return By.xpath(fieldXpath(label));
    }

    /** The text input or textarea inside the field group labelled {@code label}. */
    protected static By fieldInput(String label) {
        return By.xpath(fieldXpath(label) + "//*[self::input or self::textarea]");
    }

    /** The validation message shown under the field labelled {@code label}. */
    protected static By fieldError(String label) {
        return By.xpath(fieldXpath(label) + "//span[contains(@class,'oxd-input-field-error-message')]");
    }

    protected void waitForLoaders() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADERS));
    }

    protected WebElement visible(By locator) {
        waitForLoaders();
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected boolean isDisplayed(By locator) {
        return driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
    }

    /** Clicks once the element is clickable, retrying while an overlay intercepts the click. */
    protected void click(By locator) {
        waitForLoaders();
        wait.until(d -> {
            try {
                WebElement element = ExpectedConditions.elementToBeClickable(locator).apply(d);
                if (element == null) {
                    return false;
                }
                scrollIntoView(element);
                element.click();
                return true;
            } catch (ElementNotInteractableException interceptedOrNotReady) {
                return false;
            }
        });
    }

    protected void click(WebElement element) {
        waitForLoaders();
        wait.until(d -> {
            try {
                scrollIntoView(element);
                element.click();
                return true;
            } catch (ElementNotInteractableException interceptedOrNotReady) {
                return false;
            }
        });
    }

    /**
     * Replaces the value of a Vue-bound input. {@code clear()} alone does not fire the input
     * events OXD listens to, so the text is selected and overwritten from the keyboard.
     */
    protected void type(By locator, String text) {
        WebElement input = visible(locator);
        click(input);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        wait.until(d -> input.getDomProperty("value").isEmpty());
        if (!text.isEmpty()) {
            input.sendKeys(text);
        }
    }

    protected String text(By locator) {
        return visible(locator).getText().trim();
    }

    protected String value(By locator) {
        return visible(locator).getDomProperty("value");
    }

    /** Waits until the input has a non-empty value, used where OrangeHRM loads form data asynchronously. */
    protected String waitForValue(By locator) {
        return wait.until(d -> {
            String value = d.findElement(locator).getDomProperty("value");
            return value == null || value.isEmpty() ? null : value;
        });
    }

    protected void waitForUrlContaining(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
        waitForLoaders();
    }

    protected <T> T waitFor(Supplier<T> condition) {
        return wait.until(d -> condition.get());
    }

    protected void open(String path) {
        driver.get(ConfigReader.baseUrl() + path);
        waitForLoaders();
    }

    private void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    }
}
