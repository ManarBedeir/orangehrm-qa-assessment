package com.orangehrm.pages;

import com.orangehrm.pages.components.SideMenu;
import com.orangehrm.pages.pim.EmployeeListPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Landing page after login ({@code /dashboard/index}). */
public class DashboardPage extends BasePage {

    private static final By WIDGETS = By.cssSelector(".orangehrm-dashboard-widget");

    private final SideMenu menu;

    public DashboardPage(WebDriver driver) {
        super(driver);
        this.menu = new SideMenu(driver);
    }

    public DashboardPage waitUntilLoaded() {
        waitForUrlContaining("/dashboard/index");
        visible(WIDGETS);
        return this;
    }

    public boolean isDisplayed() {
        return driver.getCurrentUrl().contains("/dashboard/index") && menu.pageHeader().equals("Dashboard");
    }

    public String loggedInUserName() {
        return menu.loggedInUserName();
    }

    public EmployeeListPage openPim() {
        menu.openModule("PIM");
        return new EmployeeListPage(driver).waitUntilLoaded();
    }
}
