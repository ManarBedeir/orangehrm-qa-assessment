package com.orangehrm.tests;

import com.orangehrm.base.LoggedInBaseTest;
import com.orangehrm.models.Employee;
import com.orangehrm.pages.components.DataTable.Row;
import com.orangehrm.pages.pim.EmployeeListPage;
import com.orangehrm.support.EmployeeFixtures;
import io.qameta.allure.Feature;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.List;

import static com.orangehrm.pages.pim.EmployeeListPage.COL_EMPLOYMENT_STATUS;
import static com.orangehrm.pages.pim.EmployeeListPage.COL_FIRST_MIDDLE_NAME;
import static com.orangehrm.pages.pim.EmployeeListPage.COL_ID;
import static com.orangehrm.pages.pim.EmployeeListPage.COL_JOB_TITLE;
import static com.orangehrm.pages.pim.EmployeeListPage.COL_LAST_NAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/** Step 7 - Employee list filtering: results are checked row by row against the filter criteria. */
@Feature("PIM - Employee list filters")
public class EmployeeFilterTest extends LoggedInBaseTest {

    /** Upper bound on result pages read when checking that every row matches a filter. */
    private static final int MAX_PAGES = 5;
    private static final String NO_SELECTION = "-- Select --";

    @Test(groups = "regression", description = "Step 7: filtering by employee name and Employee Id returns only that employee")
    public void filterByNameAndEmployeeIdReturnsOnlyMatch() {
        Employee employee = EmployeeFixtures.createEmployee(driver());

        EmployeeListPage list = dashboard.openPim()
                .enterEmployeeName(employee.fullName())
                .enterEmployeeId(employee.employeeId())
                .search();

        assertEquals(list.recordCount(), 1, "Record count for a unique name + ID");
        List<Row> rows = list.rows();
        assertEquals(rows.size(), 1, "Rows shown for a unique name + ID");
        Row row = rows.get(0);
        assertEquals(row.get(COL_ID), employee.employeeId(), "Id column");
        assertEquals(row.get(COL_FIRST_MIDDLE_NAME), employee.firstAndMiddleName(), "First (& Middle) Name column");
        assertEquals(row.get(COL_LAST_NAME), employee.lastName(), "Last Name column");
    }

    @Test(groups = "regression", description = "Step 7: filtering by Employment Status and Job Title returns only rows with both values")
    public void filterByEmploymentStatusAndJobTitleReturnsMatchingRows() {
        EmployeeListPage list = dashboard.openPim();
        int unfilteredCount = list.recordCount();
        // Take the criteria from real data so the filter is guaranteed to have at least one match.
        Row sample = list.rowsAcrossPages(MAX_PAGES).stream()
                .filter(row -> !row.get(COL_EMPLOYMENT_STATUS).isBlank() && !row.get(COL_JOB_TITLE).isBlank())
                .findFirst()
                .orElseThrow(() -> new SkipException("No employee on the demo has both an employment status and a job title"));
        String status = sample.get(COL_EMPLOYMENT_STATUS);
        String jobTitle = sample.get(COL_JOB_TITLE);

        list.selectEmploymentStatus(status).selectJobTitle(jobTitle).search();

        List<Row> rows = list.rowsAcrossPages(MAX_PAGES);
        assertFalse(rows.isEmpty(), "Filter returns at least the sampled employee");
        assertTrue(list.recordCount() <= unfilteredCount, "Filtered result set is not larger than the unfiltered one");
        for (Row row : rows) {
            assertEquals(row.get(COL_EMPLOYMENT_STATUS), status, "Employment Status of employee " + row.get(COL_ID));
            assertEquals(row.get(COL_JOB_TITLE), jobTitle, "Job Title of employee " + row.get(COL_ID));
        }
        assertTrue(rows.stream().anyMatch(row -> row.get(COL_ID).equals(sample.get(COL_ID))),
                "Sampled employee " + sample.get(COL_ID) + " is in the filtered results");
    }

    @Test(groups = "regression", description = "Step 7: Reset clears every filter and restores the full result set")
    public void resetClearsFiltersAndRestoresResults() {
        Employee employee = EmployeeFixtures.createEmployee(driver());
        EmployeeListPage list = dashboard.openPim();
        int unfilteredCount = list.recordCount();

        list.enterEmployeeName(employee.fullName()).enterEmployeeId(employee.employeeId()).search();
        assertEquals(list.recordCount(), 1, "Filters narrow the list to the test employee");

        list.reset();

        assertEquals(list.employeeNameFilter(), "", "Employee Name filter is cleared");
        assertEquals(list.employeeIdFilter(), "", "Employee Id filter is cleared");
        assertEquals(list.employmentStatusFilter(), NO_SELECTION, "Employment Status filter is cleared");
        assertEquals(list.jobTitleFilter(), NO_SELECTION, "Job Title filter is cleared");
        assertEquals(list.recordCount(), unfilteredCount, "Reset restores the unfiltered record count");
    }
}
