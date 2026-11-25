package com.orangehrm.pages.pim;

import com.orangehrm.pages.BasePage;
import com.orangehrm.pages.components.Autocomplete;
import com.orangehrm.pages.components.ConfirmDialog;
import com.orangehrm.pages.components.DataTable;
import com.orangehrm.pages.components.DataTable.Row;
import com.orangehrm.pages.components.OxdSelect;
import com.orangehrm.pages.components.SideMenu;
import com.orangehrm.pages.components.Toast;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.Optional;

/** PIM - Employee List: search filters and the results table. */
public class EmployeeListPage extends BasePage {

    public static final String COL_ID = "Id";
    public static final String COL_FIRST_MIDDLE_NAME = "First (& Middle) Name";
    public static final String COL_LAST_NAME = "Last Name";
    public static final String COL_JOB_TITLE = "Job Title";
    public static final String COL_EMPLOYMENT_STATUS = "Employment Status";

    private static final String PATH = "/web/index.php/pim/viewEmployeeList";
    private static final By EMPLOYEE_ID = fieldInput("Employee Id");
    private static final By SEARCH_BUTTON = By.cssSelector(".oxd-table-filter button[type='submit']");
    private static final By RESET_BUTTON = By.cssSelector(".oxd-table-filter button[type='reset']");
    private static final By DELETE_ICON = By.cssSelector("i.bi-trash");

    private final Autocomplete employeeName;
    private final OxdSelect employmentStatus;
    private final OxdSelect jobTitle;
    private final DataTable table;

    public EmployeeListPage(WebDriver driver) {
        super(driver);
        this.employeeName = new Autocomplete(driver, "Employee Name");
        this.employmentStatus = new OxdSelect(driver, "Employment Status");
        this.jobTitle = new OxdSelect(driver, "Job Title");
        this.table = new DataTable(driver);
    }

    public EmployeeListPage open() {
        open(PATH);
        return waitUntilLoaded();
    }

    public EmployeeListPage waitUntilLoaded() {
        waitForUrlContaining("/pim/viewEmployeeList");
        table.waitForRefresh();
        return this;
    }

    public AddEmployeePage openAddEmployee() {
        new SideMenu(driver).openTab("Add Employee");
        return new AddEmployeePage(driver).waitUntilLoaded();
    }

    public EmployeeListPage enterEmployeeName(String name) {
        employeeName.typeText(name);
        return this;
    }

    public EmployeeListPage enterEmployeeId(String id) {
        type(EMPLOYEE_ID, id);
        return this;
    }

    public EmployeeListPage selectEmploymentStatus(String status) {
        employmentStatus.select(status);
        return this;
    }

    public EmployeeListPage selectJobTitle(String title) {
        jobTitle.select(title);
        return this;
    }

    @Step("Search the employee list")
    public EmployeeListPage search() {
        click(SEARCH_BUTTON);
        table.waitForRefresh();
        return this;
    }

    @Step("Reset the employee list filters")
    public EmployeeListPage reset() {
        click(RESET_BUTTON);
        table.waitForRefresh();
        return this;
    }

    @Step("Search employees by name '{name}'")
    public EmployeeListPage searchByName(String name) {
        return enterEmployeeName(name).search();
    }

    @Step("Search employees by ID '{id}'")
    public EmployeeListPage searchById(String id) {
        return enterEmployeeId(id).search();
    }

    public String employeeNameFilter() {
        return employeeName.value();
    }

    public String employeeIdFilter() {
        return value(EMPLOYEE_ID);
    }

    public String employmentStatusFilter() {
        return employmentStatus.selected();
    }

    public String jobTitleFilter() {
        return jobTitle.selected();
    }

    public int recordCount() {
        return table.recordCount();
    }

    public List<Row> rows() {
        return table.rows();
    }

    public List<Row> rowsAcrossPages(int maxPages) {
        return table.rowsAcrossPages(maxPages);
    }

    public Optional<Row> findByEmployeeId(String employeeId) {
        return table.findRow(row -> row.get(COL_ID).equals(employeeId));
    }

    @Step("Open employee '{employeeId}' from the list")
    public PersonalDetailsPage openEmployee(String employeeId) {
        Row row = findByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalStateException("Employee " + employeeId + " is not in the list"));
        click(row.element());
        return new PersonalDetailsPage(driver).waitUntilLoaded();
    }

    /** Deletes the employee with this ID from the current results and returns the toast text. */
    @Step("Delete employee '{employeeId}'")
    public String deleteEmployee(String employeeId) {
        Row row = findByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalStateException("Employee " + employeeId + " is not in the list"));
        table.clickInRow(row, DELETE_ICON);
        new ConfirmDialog(driver).confirm("Yes, Delete");
        String toast = new Toast(driver).waitAndRead();
        table.waitForRefresh();
        return toast;
    }
}
