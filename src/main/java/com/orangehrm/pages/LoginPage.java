package com.orangehrm.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** OrangeHRM login page ({@code /auth/login}). */
public class LoginPage extends BasePage {

    private static final String PATH = "/web/index.php/auth/login";
    private static final By TITLE = By.cssSelector("h5.orangehrm-login-title");
    private static final By USERNAME = By.name("username");
    private static final By PASSWORD = By.name("password");
    private static final By LOGIN_BUTTON = By.cssSelector("button.orangehrm-login-button");
    private static final By ERROR_ALERT = By.cssSelector(".oxd-alert-content-text");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    @Step("Open the login page")
    public LoginPage open() {
        open(PATH);
        visible(USERNAME);
        return this;
    }

    public boolean isDisplayed() {
        return isDisplayed(TITLE) && text(TITLE).equals("Login");
    }

    public boolean isUsernameFieldAvailable() {
        return visible(USERNAME).isEnabled();
    }

    public boolean isPasswordFieldAvailable() {
        return visible(PASSWORD).isEnabled() && "password".equals(visible(PASSWORD).getDomAttribute("type"));
    }

    public boolean isLoginButtonAvailable() {
        return visible(LOGIN_BUTTON).isEnabled();
    }

    @Step("Log in as {username}")
    public DashboardPage loginAs(String username, String password) {
        type(USERNAME, username);
        type(PASSWORD, password);
        click(LOGIN_BUTTON);
        return new DashboardPage(driver);
    }

    public String errorMessage() {
        return text(ERROR_ALERT);
    }
}
