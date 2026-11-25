package com.orangehrm.tests;

import com.orangehrm.base.LoggedInBaseTest;
import com.orangehrm.models.Employee;
import com.orangehrm.pages.components.SideMenu;
import com.orangehrm.pages.leave.AssignLeavePage;
import com.orangehrm.pages.leave.LeaveListPage;
import com.orangehrm.pages.pim.AddEmployeePage;
import com.orangehrm.pages.pim.EmployeeListPage;
import com.orangehrm.support.EmployeeFixtures;
import com.orangehrm.utils.ScreenshotUtil;
import com.orangehrm.utils.TestDataFactory;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;

import java.time.LocalDate;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Step 9 - Negative validation. Every test checks both the validation message and, by searching
 * afterwards, that nothing was saved.
 */
@Feature("Negative validation")
public class NegativeTest extends LoggedInBaseTest {

    private static final String[] ALL_LEAVE_STATUSES = {"Rejected", "Cancelled", "Pending Approval", "Scheduled", "Taken"};

    @Test(groups = {"negative", "regression"}, description = "Step 9A: an employee without the required Last Name is not created")
    public void addEmployeeWithoutLastNameShowsRequired() {
        Employee employee = new Employee(TestDataFactory.FIRST_NAME, TestDataFactory.MIDDLE_NAME, "",
                TestDataFactory.uniqueEmployeeId());
        EmployeeFixtures.registerForDeletion(employee.employeeId());

        AddEmployeePage addPage = dashboard.openPim().openAddEmployee().fill(employee).clickSave();

        assertEquals(addPage.lastNameError(), "Required", "Last Name validation message");
        ScreenshotUtil.capture(driver(), "05-negative-validation");
        assertTrue(addPage.isOpen(), "User stays on the Add Employee form");
        assertNotSaved(employee.employeeId());
    }

    @Test(groups = {"negative", "regression"}, description = "Step 9B: a first name longer than 30 characters is rejected")
    public void addEmployeeWithOversizedFirstNameIsRejected() {
        Employee employee = new Employee(TestDataFactory.oversizedName(), TestDataFactory.MIDDLE_NAME,
                TestDataFactory.uniqueLastName(), TestDataFactory.uniqueEmployeeId());
        assertEquals(employee.firstName().length(), TestDataFactory.NAME_MAX_LENGTH + 1, "Test data is one character over the limit");
        EmployeeFixtures.registerForDeletion(employee.employeeId());

        AddEmployeePage addPage = dashboard.openPim().openAddEmployee().fill(employee).clickSave();

        assertEquals(addPage.firstNameError(), "Should not exceed 30 characters", "First Name validation message");
        assertTrue(addPage.isOpen(), "User stays on the Add Employee form");
        assertNotSaved(employee.employeeId());
    }

    @Test(groups = {"negative", "regression"}, description = "Step 9B: an Employee Id that already exists is rejected")
    public void addEmployeeWithDuplicateEmployeeIdIsRejected() {
        Employee existing = EmployeeFixtures.createEmployee(driver());
        Employee duplicate = TestDataFactory.newEmployee().withEmployeeId(existing.employeeId());

        AddEmployeePage addPage = new EmployeeListPage(driver()).open().openAddEmployee().fill(duplicate).clickSave();

        assertEquals(addPage.employeeIdError(), "Employee Id already exists", "Employee Id validation message");
        assertTrue(addPage.isOpen(), "User stays on the Add Employee form");
        EmployeeListPage list = new EmployeeListPage(driver()).open().searchByName(duplicate.fullName());
        assertEquals(list.recordCount(), 0, "No employee was created with the duplicate ID");
    }

    @Test(groups = {"negative", "regression"}, description = "Step 9C: a leave request whose To Date is before its From Date is not submitted")
    public void leaveWithToDateBeforeFromDateIsRejected() {
        Employee employee = EmployeeFixtures.createEmployee(driver());
        LocalDate from = TestDataFactory.nextWeekday(21);
        LocalDate to = from.minusDays(1);

        SideMenu menu = new SideMenu(driver());
        menu.openModule("Leave");
        menu.openTab("Assign Leave");
        AssignLeavePage assignPage = new AssignLeavePage(driver()).waitUntilLoaded()
                .selectEmployee(employee.fullName())
                .selectAnyLeaveType()
                .setDates(from, to)
                .clickAssign();

        assertEquals(assignPage.toDateError(), "To date should be after from date", "To Date validation message");
        assertTrue(driver().getCurrentUrl().contains("/leave/assignLeave"), "User stays on the Assign Leave form");

        menu.openTab("Leave List");
        LeaveListPage leaveList = new LeaveListPage(driver())
                .search(employee.fullName(), to, from, ALL_LEAVE_STATUSES);
        assertEquals(leaveList.recordCount(), 0, "No leave request of any status exists for the employee");
    }

    private void assertNotSaved(String employeeId) {
        EmployeeListPage list = new EmployeeListPage(driver()).open().searchById(employeeId);
        assertEquals(list.recordCount(), 0, "No employee exists with ID " + employeeId);
    }
}
