package com.orangehrm.pages.components;

import com.orangehrm.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/** OXD custom dropdown ({@code .oxd-select-wrapper}) located by its field label. */
public class OxdSelect extends BasePage {

    private static final By OPTIONS = By.cssSelector("div[role='listbox'] .oxd-select-option");

    private final By trigger;

    public OxdSelect(WebDriver driver, String label) {
        super(driver);
        this.trigger = By.xpath(fieldXpath(label)
                + "//div[contains(@class,'oxd-select-text')]");
    }

    public void select(String optionText) {
        click(trigger);
        WebElement option = wait.until(d -> d.findElements(OPTIONS).stream()
                .filter(o -> o.getText().trim().equals(optionText))
                .findFirst().orElse(null));
        click(option);
        wait.until(d -> selected().equals(optionText));
    }

    /** All selectable options except the "-- Select --" placeholder. */
    public List<String> options() {
        click(trigger);
        List<String> options = allVisible(OPTIONS).stream()
                .map(o -> o.getText().trim())
                .filter(t -> !t.startsWith("--"))
                .toList();
        click(trigger);
        return options;
    }

    public String selected() {
        return driver.findElement(trigger).getText().trim();
    }
}
