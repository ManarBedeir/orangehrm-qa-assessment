package com.orangehrm.pages.leave;

import com.orangehrm.pages.BasePage;
import com.orangehrm.pages.components.Autocomplete;
import com.orangehrm.pages.components.ConfirmDialog;
import com.orangehrm.pages.components.OxdSelect;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Leave - Entitlements - Add Entitlements. */
public class AddEntitlementPage extends BasePage {

    private static final String PATH = "/web/index.php/leave/addLeaveEntitlement";
    private static final By ENTITLEMENT = fieldInput("Entitlement");
    private static final By SAVE_BUTTON = By.cssSelector("form button[type='submit']");
    private static final By DIALOG = By.cssSelector(".oxd-dialog-sheet");

    private final Autocomplete employeeName;
    private final OxdSelect leaveType;
    private final OxdSelect leavePeriod;

    public AddEntitlementPage(WebDriver driver) {
        super(driver);
        this.employeeName = new Autocomplete(driver, "Employee Name");
        this.leaveType = new OxdSelect(driver, "Leave Type");
        this.leavePeriod = new OxdSelect(driver, "Leave Period");
    }

    public AddEntitlementPage open() {
        open(PATH);
        visible(ENTITLEMENT);
        return this;
    }

    /**
     * Adds {@code days} of entitlement for the employee in the leave period of {@code periodYear}.
     * The demo uses calendar-year leave periods, so the period is chosen by the year it starts in.
     *
     * @param leaveTypeName the leave type to use, or {@code null} to use the first available one
     * @return the leave type the entitlement was added for
     */
    @Step("Add {days} day(s) of entitlement for {employeeFullName}")
    public String addEntitlement(String employeeFullName, String leaveTypeName, int periodYear, int days) {
        employeeName.select(employeeFullName, employeeFullName);
        String selectedType = leaveTypeName == null
                ? leaveType.select(type -> true)
                : leaveType.select(leaveTypeName::equals);
        leavePeriod.select(period -> period.startsWith(periodYear + "-"));
        type(ENTITLEMENT, String.valueOf(days));
        click(SAVE_BUTTON);
        // OrangeHRM asks to confirm the change ("Updating Entitlement") before it saves.
        waitFor(() -> isDisplayed(DIALOG) || driver.getCurrentUrl().contains("/leave/viewLeaveEntitlements"));
        if (isDisplayed(DIALOG)) {
            new ConfirmDialog(driver).confirm("Confirm");
        }
        waitForUrlContaining("/leave/viewLeaveEntitlements");
        return selectedType;
    }
}
