package com.orangehrm.pages.components;

import com.orangehrm.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** OXD toast notification shown after save, update and delete operations. */
public class Toast extends BasePage {

    private static final By TOAST = By.cssSelector(".oxd-toast");
    private static final By TITLE = By.cssSelector(".oxd-text--toast-title");
    private static final By MESSAGE = By.cssSelector(".oxd-text--toast-message");

    public Toast(WebDriver driver) {
        super(driver);
    }

    /** Waits for the next toast and returns it as {@code "<title>: <message>"}, e.g. {@code "Success: Successfully Saved"}. */
    public String waitAndRead() {
        WebElement toast = wait.until(d -> d.findElements(TOAST).stream().filter(WebElement::isDisplayed).findFirst().orElse(null));
        return toast.findElement(TITLE).getText().trim() + ": " + toast.findElement(MESSAGE).getText().trim();
    }
}
