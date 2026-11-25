package com.orangehrm.pages.components;

import com.orangehrm.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * OXD date input located by its field label. The display format is user-configurable in OrangeHRM
 * (the demo uses {@code yyyy-dd-mm}), so it is derived from the input placeholder instead of being assumed.
 */
public class DatePicker extends BasePage {

    private final By input;
    private final By label;

    public DatePicker(WebDriver driver, String fieldLabel) {
        super(driver);
        this.input = By.xpath(fieldXpath(fieldLabel) + "//input");
        this.label = By.xpath(fieldXpath(fieldLabel) + "//label");
    }

    public void set(LocalDate date) {
        type(input, format(date));
        // Clicking the field label closes the calendar popup and commits the typed value.
        click(label);
    }

    public String format(LocalDate date) {
        return formatter().format(date);
    }

    public DateTimeFormatter formatter() {
        String placeholder = visible(input).getDomAttribute("placeholder");
        return DateTimeFormatter.ofPattern(placeholder.replace("mm", "MM"));
    }
}
