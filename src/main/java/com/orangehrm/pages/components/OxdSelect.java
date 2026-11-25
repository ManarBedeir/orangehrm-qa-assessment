package com.orangehrm.pages.components;

import com.orangehrm.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.function.Predicate;

/** OXD custom dropdown ({@code .oxd-select-wrapper}) located by its field label. */
public class OxdSelect extends BasePage {

    private static final By OPTIONS = By.cssSelector("div[role='listbox'] .oxd-select-option");

    private final By trigger;

    public OxdSelect(WebDriver driver, String label) {
        super(driver);
        this.trigger = By.xpath(fieldXpath(label) + "//div[contains(@class,'oxd-select-text')]");
    }

    public void select(String optionText) {
        select(optionText::equals);
    }

    /**
     * Selects the first option whose text matches, skipping the "-- Select --" placeholder.
     *
     * @return the selected option text
     */
    public String select(Predicate<String> matching) {
        click(trigger);
        WebElement option = wait.until(d -> d.findElements(OPTIONS).stream()
                .filter(o -> !o.getText().trim().startsWith("--") && matching.test(o.getText().trim()))
                .findFirst().orElse(null));
        String optionText = option.getText().trim();
        click(option);
        wait.until(d -> selected().equals(optionText));
        return optionText;
    }

    public String selected() {
        return driver.findElement(trigger).getText().trim();
    }
}
