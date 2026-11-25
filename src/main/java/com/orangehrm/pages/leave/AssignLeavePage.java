package com.orangehrm.pages.leave;

import com.orangehrm.pages.BasePage;
import com.orangehrm.pages.components.Autocomplete;
import com.orangehrm.pages.components.DatePicker;
import com.orangehrm.pages.components.OxdSelect;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.time.LocalDate;

/** Leave - Assign Leave: an admin or supervisor records leave on behalf of an employee. */
public class AssignLeavePage extends BasePage {

    private static final String PATH = "/web/index.php/leave/assignLeave";
    private static final By BALANCE = By.cssSelector(".orangehrm-leave-balance-text");
    private static final By COMMENTS = fieldInput("Comments");
    private static final By ASSIGN_BUTTON = By.cssSelector("form button[type='submit']");

    private final Autocomplete employeeName;
    private final OxdSelect leaveType;
    private final DatePicker fromDate;
    private final DatePicker toDate;

    public AssignLeavePage(WebDriver driver) {
        super(driver);
        this.employeeName = new Autocomplete(driver, "Employee Name");
        this.leaveType = new OxdSelect(driver, "Leave Type");
        this.fromDate = new DatePicker(driver, "From Date");
        this.toDate = new DatePicker(driver, "To Date");
    }

    public AssignLeavePage open() {
        open(PATH);
        return waitUntilLoaded();
    }

    public AssignLeavePage waitUntilLoaded() {
        waitForUrlContaining("/leave/assignLeave");
        visible(ASSIGN_BUTTON);
        return this;
    }

    @Step("Select employee {fullName}")
    public AssignLeavePage selectEmployee(String fullName) {
        employeeName.select(fullName, fullName);
        return this;
    }

    @Step("Select leave type {type}")
    public AssignLeavePage selectLeaveType(String type) {
        leaveType.select(type);
        return this;
    }

    /** Selects the first available leave type, for tests where the type does not matter. */
    @Step("Select any leave type")
    public AssignLeavePage selectAnyLeaveType() {
        leaveType.select(type -> true);
        return this;
    }

    /** Balance shown for the selected employee and leave type, e.g. "5.00 Day(s)". */
    public String balance() {
        return waitFor(() -> {
            String text = text(BALANCE);
            return text.startsWith("0.00") ? null : text;
        });
    }

    @Step("Set leave dates {from} to {to}")
    public AssignLeavePage setDates(LocalDate from, LocalDate to) {
        fromDate.set(from);
        // OrangeHRM copies From Date into To Date; DatePicker.set overwrites that value.
        toDate.set(to);
        return this;
    }

    public AssignLeavePage enterComment(String comment) {
        type(COMMENTS, comment);
        return this;
    }

    /** Clicks Assign without waiting for the outcome, so the caller can read the toast or validation messages. */
    @Step("Click Assign")
    public AssignLeavePage clickAssign() {
        click(ASSIGN_BUTTON);
        return this;
    }

    public String toDateError() {
        return text(fieldError("To Date"));
    }

    public String format(LocalDate date) {
        return fromDate.format(date);
    }
}
