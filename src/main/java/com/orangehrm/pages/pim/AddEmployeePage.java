package com.orangehrm.pages.pim;

import com.orangehrm.models.Employee;
import com.orangehrm.pages.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** PIM - Add Employee form. */
public class AddEmployeePage extends BasePage {

    private static final String PATH = "/web/index.php/pim/addEmployee";
    private static final By FIRST_NAME = By.name("firstName");
    private static final By MIDDLE_NAME = By.name("middleName");
    private static final By LAST_NAME = By.name("lastName");
    private static final By EMPLOYEE_ID = fieldInput("Employee Id");
    private static final By SAVE_BUTTON = By.cssSelector("form button[type='submit']");

    public AddEmployeePage(WebDriver driver) {
        super(driver);
    }

    public AddEmployeePage open() {
        open(PATH);
        return waitUntilLoaded();
    }

    public AddEmployeePage waitUntilLoaded() {
        waitForUrlContaining("/pim/addEmployee");
        // The Employee Id field is pre-filled asynchronously; typing before that would be overwritten.
        waitForValue(EMPLOYEE_ID);
        return this;
    }

    /** The Employee Id OrangeHRM pre-fills for a new employee. */
    public String employeeId() {
        return value(EMPLOYEE_ID);
    }

    @Step("Fill the Add Employee form with {employee}")
    public AddEmployeePage fill(Employee employee) {
        fillNames(employee);
        type(EMPLOYEE_ID, employee.employeeId());
        return this;
    }

    /** Fills only the name fields and keeps the generated Employee Id. */
    @Step("Fill the employee name fields with {employee}")
    public AddEmployeePage fillNames(Employee employee) {
        type(FIRST_NAME, employee.firstName());
        type(MIDDLE_NAME, employee.middleName());
        type(LAST_NAME, employee.lastName());
        return this;
    }

    /** Clicks Save and waits for the redirect to the new employee's Personal Details page. */
    @Step("Save the new employee")
    public PersonalDetailsPage save() {
        clickSave();
        waitForUrlContaining("/pim/viewPersonalDetails/empNumber/");
        return new PersonalDetailsPage(driver);
    }

    /** Clicks Save without waiting for the outcome, so the caller can read the toast or validation messages. */
    public AddEmployeePage clickSave() {
        click(SAVE_BUTTON);
        return this;
    }

    public boolean isOpen() {
        return driver.getCurrentUrl().contains("/pim/addEmployee");
    }

    public String firstNameError() {
        return nameFieldError("firstName");
    }

    public String lastNameError() {
        return nameFieldError("lastName");
    }

    public String employeeIdError() {
        return text(fieldError("Employee Id"));
    }

    /** First, middle and last name share one label, so their errors are located from the input itself. */
    private String nameFieldError(String inputName) {
        return text(By.xpath("//input[@name='" + inputName + "']/ancestor::div[contains(concat(' ', @class, ' '),' oxd-input-group ')][1]"
                + "//span[contains(@class,'oxd-input-field-error-message')]"));
    }
}
