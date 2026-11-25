package com.orangehrm.pages.components;

import com.orangehrm.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** OXD modal confirmation dialog, e.g. "Are you Sure?" with "Yes, Delete". */
public class ConfirmDialog extends BasePage {

    private static final By DIALOG = By.cssSelector(".oxd-dialog-sheet");

    public ConfirmDialog(WebDriver driver) {
        super(driver);
    }

    public void confirm(String buttonText) {
        visible(DIALOG);
        click(By.xpath("//div[contains(@class,'oxd-dialog-sheet')]//button[normalize-space()='" + buttonText + "']"));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(DIALOG));
    }
}
