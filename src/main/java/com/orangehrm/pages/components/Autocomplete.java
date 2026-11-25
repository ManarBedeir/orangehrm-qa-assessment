package com.orangehrm.pages.components;

import com.orangehrm.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** OXD autocomplete input (employee name pickers) located by its field label. */
public class Autocomplete extends BasePage {

    private static final By OPTIONS = By.cssSelector("div[role='listbox'] .oxd-autocomplete-option");

    private final By input;

    public Autocomplete(WebDriver driver, String label) {
        super(driver);
        this.input = By.xpath(fieldXpath(label) + "//input");
    }

    /** Types {@code searchText} and picks the first suggestion that contains {@code optionText}. */
    public void select(String searchText, String optionText) {
        type(input, searchText);
        WebElement option = wait.until(d -> d.findElements(OPTIONS).stream()
                .filter(o -> o.getText().contains(optionText))
                .findFirst().orElse(null));
        click(option);
    }

    /** Types free text without picking a suggestion (supported by the PIM employee-name filter). */
    public void typeText(String text) {
        type(input, text);
    }

    public String value() {
        return value(input);
    }
}
