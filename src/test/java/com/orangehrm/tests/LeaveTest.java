package com.orangehrm.tests;

import com.orangehrm.base.LoggedInBaseTest;
import com.orangehrm.models.Employee;
import com.orangehrm.pages.components.DataTable.Row;
import com.orangehrm.pages.components.SideMenu;
import com.orangehrm.pages.components.Toast;
import com.orangehrm.pages.leave.AddEntitlementPage;
import com.orangehrm.pages.leave.AssignLeavePage;
import com.orangehrm.pages.leave.HolidayListPage;
import com.orangehrm.pages.leave.LeaveListPage;
import com.orangehrm.support.EmployeeFixtures;
import com.orangehrm.utils.ScreenshotUtil;
import com.orangehrm.utils.TestDataFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.orangehrm.pages.leave.LeaveListPage.COL_COMMENTS;
import static com.orangehrm.pages.leave.LeaveListPage.COL_DATE;
import static com.orangehrm.pages.leave.LeaveListPage.COL_EMPLOYEE_NAME;
import static com.orangehrm.pages.leave.LeaveListPage.COL_LEAVE_TYPE;
import static com.orangehrm.pages.leave.LeaveListPage.COL_NUMBER_OF_DAYS;
import static com.orangehrm.pages.leave.LeaveListPage.COL_STATUS;
import static org.testng.Assert.assertEquals;

/**
 * Step 8 - Leave request.
 * <p>
 * The demo Admin's own employee record is renamed by other users and often has no leave balance,
 * so the test creates its own employee, gives it an entitlement and assigns leave to it.
 * Deleting that employee afterwards also removes its leave and entitlement from every list.
 */
@Feature("Leave")
public class LeaveTest extends LoggedInBaseTest {

    private static final int ENTITLEMENT_DAYS = 5;
    private static final String SCHEDULED = "Scheduled";

    @Test(groups = {"smoke", "regression"}, description = "Step 8: a two-day leave request is saved and can be found in the Leave List")
    @Description("Leave dates are calculated: the first two consecutive working days 2-6 weeks ahead, "
            + "skipping weekends and the holidays configured in OrangeHRM.")
    public void assignedLeaveIsSavedAndListed() {
        Employee employee = EmployeeFixtures.createEmployee(driver());
        LocalDate[] dates = pickTwoWorkingDays();
        LocalDate from = dates[0];
        LocalDate to = dates[1];
        String leaveType = new AddEntitlementPage(driver()).open()
                .addEntitlement(employee.fullName(), null, from.getYear(), ENTITLEMENT_DAYS);
        String comment = "Automation leave request " + employee.lastName();

        SideMenu menu = new SideMenu(driver());
        menu.openModule("Leave");
        menu.openTab("Assign Leave");
        AssignLeavePage assignPage = new AssignLeavePage(driver()).waitUntilLoaded()
                .selectEmployee(employee.fullName())
                .selectLeaveType(leaveType)
                .setDates(from, to);
        // The balance is shown for the leave period of the selected dates, so it is checked after they are set.
        assertEquals(assignPage.balance(), ENTITLEMENT_DAYS + ".00 Day(s)", "Balance shows the added entitlement");
        assignPage.enterComment(comment);
        ScreenshotUtil.capture(driver(), "06-leave-request");
        assignPage.clickAssign();

        assertEquals(new Toast(driver()).waitAndRead(), "Success: Successfully Saved", "Leave saved notification");

        menu.openTab("Leave List");
        LeaveListPage leaveList = new LeaveListPage(driver())
                .search(employee.fullName(), from, to, SCHEDULED);
        List<Row> rows = leaveList.rows();
        assertEquals(rows.size(), 1, "Exactly one leave request for the test employee in the date range");
        Row leave = rows.get(0);
        SoftAssert softly = new SoftAssert();
        softly.assertEquals(leave.get(COL_EMPLOYEE_NAME), employee.fullName(), "Employee Name");
        softly.assertEquals(leave.get(COL_LEAVE_TYPE), leaveType, "Leave Type");
        softly.assertEquals(leave.get(COL_DATE), leaveList.format(from) + " to " + leaveList.format(to), "Date range");
        softly.assertEquals(leave.get(COL_NUMBER_OF_DAYS), "2.00", "Number of days (two working days)");
        softly.assertTrue(leave.get(COL_STATUS).startsWith(SCHEDULED), "Status is Scheduled, was: " + leave.get(COL_STATUS));
        softly.assertEquals(leave.get(COL_COMMENTS), comment, "Comment");
        softly.assertAll();
    }

    /** Two consecutive working days 2-6 weeks ahead, randomised so repeated runs do not overlap. */
    private LocalDate[] pickTwoWorkingDays() {
        LocalDate start = TestDataFactory.randomWeekday(14, 42);
        Set<LocalDate> holidays = new HolidayListPage(driver()).open().holidaysBetween(start, start.plusDays(14));
        return TestDataFactory.consecutiveWorkingDays(start, holidays);
    }
}
