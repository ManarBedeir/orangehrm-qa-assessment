package com.orangehrm.pages.leave;

import com.orangehrm.pages.BasePage;
import com.orangehrm.pages.components.Autocomplete;
import com.orangehrm.pages.components.DataTable;
import com.orangehrm.pages.components.DataTable.Row;
import com.orangehrm.pages.components.DatePicker;
import com.orangehrm.pages.components.MultiSelect;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.time.LocalDate;
import java.util.List;

/** Leave - Leave List: every employee's leave requests, filtered by date range, status and employee. */
public class LeaveListPage extends BasePage {

    public static final String COL_DATE = "Date";
    public static final String COL_EMPLOYEE_NAME = "Employee Name";
    public static final String COL_LEAVE_TYPE = "Leave Type";
    public static final String COL_NUMBER_OF_DAYS = "Number of Days";
    public static final String COL_STATUS = "Status";
    public static final String COL_COMMENTS = "Comments";

    private static final String PATH = "/web/index.php/leave/viewLeaveList";
    private static final By SEARCH_BUTTON = By.cssSelector(".oxd-table-filter button[type='submit']");

    private final DatePicker fromDate;
    private final DatePicker toDate;
    private final MultiSelect statuses;
    private final Autocomplete employeeName;
    private final DataTable table;

    public LeaveListPage(WebDriver driver) {
        super(driver);
        this.fromDate = new DatePicker(driver, "From Date");
        this.toDate = new DatePicker(driver, "To Date");
        this.statuses = new MultiSelect(driver, "Show Leave with Status");
        this.employeeName = new Autocomplete(driver, "Employee Name");
        this.table = new DataTable(driver);
    }

    public LeaveListPage open() {
        open(PATH);
        table.waitForRefresh();
        return this;
    }

    /**
     * Searches one employee's leave in a date range for all the given statuses
     * (the page shows only "Pending Approval" by default).
     */
    @Step("Search leave of {employeeFullName} from {from} to {to}")
    public LeaveListPage search(String employeeFullName, LocalDate from, LocalDate to, String... statusNames) {
        fromDate.set(from);
        toDate.set(to);
        for (String status : statusNames) {
            statuses.add(status);
        }
        employeeName.select(employeeFullName, employeeFullName);
        click(SEARCH_BUTTON);
        table.waitForRefresh();
        return this;
    }

    public List<Row> rows() {
        return table.rows();
    }

    public int recordCount() {
        return table.recordCount();
    }

    public String format(LocalDate date) {
        return fromDate.format(date);
    }
}
