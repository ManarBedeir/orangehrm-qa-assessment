package com.orangehrm.pages.pim;

import com.orangehrm.pages.BasePage;
import com.orangehrm.pages.components.SideMenu;
import com.orangehrm.pages.components.Toast;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** PIM - an employee's Personal Details tab ({@code /pim/viewPersonalDetails/empNumber/<n>}). */
public class PersonalDetailsPage extends BasePage {

    private static final By FIRST_NAME = By.name("firstName");
    private static final By MIDDLE_NAME = By.name("middleName");
    private static final By LAST_NAME = By.name("lastName");
    private static final By EMPLOYEE_ID = fieldInput("Employee Id");
    private static final By OTHER_ID = fieldInput("Other Id");
    private static final By HEADER_NAME = By.cssSelector(".orangehrm-edit-employee-name h6");
    private static final By SAVE_BUTTON = By.xpath("//input[@name='firstName']/ancestor::form//button[@type='submit']");

    public PersonalDetailsPage(WebDriver driver) {
        super(driver);
    }

    /** Waits until the form has been populated from the server, not just rendered. */
    public PersonalDetailsPage waitUntilLoaded() {
        waitForUrlContaining("/pim/viewPersonalDetails/empNumber/");
        waitForValue(FIRST_NAME);
        waitForValue(EMPLOYEE_ID);
        return this;
    }

    public String firstName() {
        return value(FIRST_NAME);
    }

    public String middleName() {
        return value(MIDDLE_NAME);
    }

    public String lastName() {
        return value(LAST_NAME);
    }

    public String employeeId() {
        return value(EMPLOYEE_ID);
    }

    public String otherId() {
        return value(OTHER_ID);
    }

    /** Name shown above the profile picture, e.g. "Automation QA2511…". */
    public String headerName() {
        // The header renders empty first and is filled in once the employee data arrives.
        return waitFor(() -> {
            String name = text(HEADER_NAME);
            return name.isEmpty() ? null : name;
        });
    }

    public PersonalDetailsPage setLastName(String lastName) {
        type(LAST_NAME, lastName);
        return this;
    }

    public PersonalDetailsPage setOtherId(String otherId) {
        type(OTHER_ID, otherId);
        return this;
    }

    /** Saves the Personal Details form and returns the toast text. */
    @Step("Save personal details")
    public String save() {
        click(SAVE_BUTTON);
        String toast = new Toast(driver).waitAndRead();
        waitForLoaders();
        return toast;
    }

    @Step("Reload the employee page")
    public PersonalDetailsPage refresh() {
        driver.navigate().refresh();
        return waitUntilLoaded();
    }

    public EmployeeListPage backToEmployeeList() {
        new SideMenu(driver).openTab("Employee List");
        return new EmployeeListPage(driver).waitUntilLoaded();
    }
}
