package com.orangehrm.pages.components;

import com.orangehrm.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/** OXD multi-select with chips (e.g. "Show Leave with Status"), located by its field label. */
public class MultiSelect extends BasePage {

    private static final By OPTIONS = By.cssSelector("div[role='listbox'] .oxd-select-option");

    private final By trigger;
    private final By chips;
    private final By labelElement;

    public MultiSelect(WebDriver driver, String label) {
        super(driver);
        this.trigger = By.xpath(fieldXpath(label) + "//div[contains(@class,'oxd-select-text')]");
        this.chips = By.xpath(fieldXpath(label) + "//span[contains(@class,'oxd-chip')]");
        this.labelElement = By.xpath(fieldXpath(label) + "//label");
    }

    /** Adds {@code optionText} as a chip unless it is already selected. */
    public void add(String optionText) {
        if (selected().contains(optionText)) {
            return;
        }
        click(trigger);
        WebElement option = wait.until(d -> d.findElements(OPTIONS).stream()
                .filter(o -> o.getText().trim().equals(optionText))
                .findFirst().orElse(null));
        click(option);
        wait.until(d -> selected().contains(optionText));
        // Clicking the label closes the option list so it does not cover neighbouring fields.
        click(labelElement);
    }

    public List<String> selected() {
        return driver.findElements(chips).stream().map(chip -> chip.getText().trim()).toList();
    }
}
