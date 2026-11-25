package com.orangehrm.tests;

import com.orangehrm.base.BaseTest;
import com.orangehrm.config.ConfigReader;
import com.orangehrm.pages.DashboardPage;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.utils.ScreenshotUtil;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/** Step 1 - Login. */
@Feature("Login")
public class LoginTest extends BaseTest {

    @Test(groups = "smoke", description = "Login page shows the username and password fields and the Login button")
    public void loginPageDisplaysAllElements() {
        LoginPage loginPage = new LoginPage(driver()).open();

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(loginPage.isDisplayed(), "Login page title is displayed");
        softly.assertTrue(loginPage.isUsernameFieldAvailable(), "Username field is available");
        softly.assertTrue(loginPage.isPasswordFieldAvailable(), "Password field is available and masked");
        softly.assertTrue(loginPage.isLoginButtonAvailable(), "Login button is available");
        softly.assertAll();
    }

    @Test(groups = {"smoke", "regression"}, description = "Valid credentials open the dashboard with the logged-in user shown")
    @Description("Logs in with the configured credentials and checks the dashboard and the user dropdown name.")
    public void validLoginShowsDashboardAndUser() {
        DashboardPage dashboard = new LoginPage(driver())
                .open()
                .loginAs(ConfigReader.username(), ConfigReader.password())
                .waitUntilLoaded();

        assertTrue(dashboard.isDisplayed(), "Dashboard is displayed after login");
        assertFalse(dashboard.loggedInUserName().isBlank(), "Logged-in user name is shown in the top bar");
        ScreenshotUtil.capture(driver(), "01-login-dashboard");
    }
}
