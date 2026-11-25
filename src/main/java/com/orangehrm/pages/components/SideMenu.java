package com.orangehrm.pages.components;

import com.orangehrm.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Left-hand main menu (PIM, Leave, ...), the module top navigation and the logged-in user dropdown. */
public class SideMenu extends BasePage {

    private static final By PAGE_HEADER = By.cssSelector(".oxd-topbar-header-breadcrumb h6");
    private static final By USER_NAME = By.cssSelector(".oxd-userdropdown-name");

    public SideMenu(WebDriver driver) {
        super(driver);
    }

    public void openModule(String module) {
        click(By.xpath("//a[contains(@class,'oxd-main-menu-item')][normalize-space()='" + module + "']"));
        wait.until(d -> text(PAGE_HEADER).equals(module));
    }

    public void openTab(String tab) {
        click(By.xpath("//a[contains(@class,'oxd-topbar-body-nav-tab-item')][normalize-space()='" + tab + "']"));
        waitForLoaders();
    }

    public String pageHeader() {
        return text(PAGE_HEADER);
    }

    public String loggedInUserName() {
        return text(USER_NAME);
    }
}
