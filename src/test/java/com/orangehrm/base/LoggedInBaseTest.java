package com.orangehrm.base;

import com.orangehrm.config.ConfigReader;
import com.orangehrm.pages.DashboardPage;
import com.orangehrm.pages.LoginPage;
import org.testng.annotations.BeforeMethod;

/** Base class for tests that start from the dashboard as the configured user. */
public abstract class LoggedInBaseTest extends BaseTest {

    protected DashboardPage dashboard;

    @BeforeMethod(alwaysRun = true)
    public void logIn() {
        dashboard = new LoginPage(driver())
                .open()
                .loginAs(ConfigReader.username(), ConfigReader.password())
                .waitUntilLoaded();
    }
}
