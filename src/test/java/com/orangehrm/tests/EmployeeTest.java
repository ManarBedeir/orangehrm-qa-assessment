package com.orangehrm.tests;

import com.orangehrm.base.LoggedInBaseTest;
import com.orangehrm.models.Employee;
import com.orangehrm.pages.components.DataTable.Row;
import com.orangehrm.pages.components.Toast;
import com.orangehrm.pages.pim.AddEmployeePage;
import com.orangehrm.pages.pim.EmployeeListPage;
import com.orangehrm.pages.pim.PersonalDetailsPage;
import com.orangehrm.support.EmployeeFixtures;
import com.orangehrm.utils.ScreenshotUtil;
import com.orangehrm.utils.TestDataFactory;
import io.qameta.allure.Feature;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.List;

import static com.orangehrm.pages.pim.EmployeeListPage.COL_EMPLOYMENT_STATUS;
import static com.orangehrm.pages.pim.EmployeeListPage.COL_FIRST_MIDDLE_NAME;
import static com.orangehrm.pages.pim.EmployeeListPage.COL_ID;
import static com.orangehrm.pages.pim.EmployeeListPage.COL_JOB_TITLE;
import static com.orangehrm.pages.pim.EmployeeListPage.COL_LAST_NAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

/** Steps 2-6 - PIM employee search, creation, verification, edit and persistence. */
@Feature("PIM - Employees")
public class EmployeeTest extends LoggedInBaseTest {

    @Test(groups = {"smoke", "regression"}, description = "Step 2: searching an existing employee by name returns only matching employees")
    public void searchExistingEmployeeByName() {
        EmployeeListPage list = dashboard.openPim();
        Row existing = list.rows().stream()
                .filter(row -> !row.get(COL_FIRST_MIDDLE_NAME).isBlank() && !row.get(COL_LAST_NAME).isBlank())
                .findFirst()
                .orElseThrow(() -> new SkipException("The demo has no employee with a first and last name to search for"));
        String searchName = existing.get(COL_FIRST_MIDDLE_NAME) + " " + existing.get(COL_LAST_NAME);

        list.searchByName(searchName);

        List<Row> results = list.rowsAcrossPages(5);
        assertFalse(results.isEmpty(), "Search for '" + searchName + "' returned results");
        for (Row row : results) {
            String rowName = row.get(COL_FIRST_MIDDLE_NAME) + " " + row.get(COL_LAST_NAME);
            assertTrue(rowName.toLowerCase().contains(searchName.toLowerCase()),
                    "Result '" + rowName + "' matches the searched name '" + searchName + "'");
        }
        Row found = list.findByEmployeeId(existing.get(COL_ID))
                .orElseThrow(() -> new AssertionError("Searched employee " + existing.get(COL_ID) + " is in the results"));
        SoftAssert softly = new SoftAssert();
        softly.assertEquals(found.get(COL_FIRST_MIDDLE_NAME), existing.get(COL_FIRST_MIDDLE_NAME), "First (& Middle) Name");
        softly.assertEquals(found.get(COL_LAST_NAME), existing.get(COL_LAST_NAME), "Last Name");
        softly.assertEquals(found.get(COL_JOB_TITLE), existing.get(COL_JOB_TITLE), "Job Title");
        softly.assertEquals(found.get(COL_EMPLOYMENT_STATUS), existing.get(COL_EMPLOYMENT_STATUS), "Employment Status");
        ScreenshotUtil.capture(driver(), "02-employee-search-result");
        softly.assertAll();
    }

    @Test(groups = {"smoke", "regression"}, description = "Step 3: a new employee is created with a generated ID and appears in the employee list")
    public void addEmployeeGeneratesIdAndShowsSuccess() {
        AddEmployeePage addPage = dashboard.openPim().openAddEmployee();
        String generatedId = addPage.employeeId();
        assertFalse(generatedId.isBlank(), "Employee Id is generated when the form opens");
        Employee employee = TestDataFactory.newEmployee().withEmployeeId(generatedId);
        EmployeeFixtures.registerForDeletion(generatedId);

        addPage.fillNames(employee).clickSave();
        String toast = new Toast(driver()).waitAndRead();

        assertEquals(toast, "Success: Successfully Saved", "Success notification");
        PersonalDetailsPage details = new PersonalDetailsPage(driver()).waitUntilLoaded();
        assertEquals(details.employeeId(), generatedId, "Generated Employee Id is displayed on the new record");
        assertEquals(details.headerName(), employee.firstName() + " " + employee.lastName(), "Profile header name");
        ScreenshotUtil.capture(driver(), "03-employee-created");

        EmployeeListPage list = details.backToEmployeeList().searchById(employee.employeeId());
        Row row = list.findByEmployeeId(employee.employeeId())
                .orElseThrow(() -> new AssertionError("New employee is listed in the employee list"));
        assertEquals(row.get(COL_FIRST_MIDDLE_NAME), employee.firstAndMiddleName(), "First (& Middle) Name in list");
        assertEquals(row.get(COL_LAST_NAME), employee.lastName(), "Last Name in list");
    }

    @Test(groups = "regression", description = "Step 4: the created employee is persisted and opens with the same data")
    public void createdEmployeeIsPersisted() {
        Employee employee = EmployeeFixtures.createEmployee(driver());

        EmployeeListPage list = new EmployeeListPage(driver()).open().searchByName(employee.fullName());
        assertEquals(list.recordCount(), 1, "Exactly one employee matches the unique generated name");
        PersonalDetailsPage details = list.openEmployee(employee.employeeId());

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(details.firstName(), employee.firstName(), "First Name");
        softly.assertEquals(details.middleName(), employee.middleName(), "Middle Name");
        softly.assertEquals(details.lastName(), employee.lastName(), "Last Name");
        softly.assertEquals(details.employeeId(), employee.employeeId(), "Employee Id is populated and correct");
        softly.assertEquals(details.headerName(), employee.firstName() + " " + employee.lastName(), "Profile header name");
        softly.assertAll();
    }

    @Test(groups = "regression", description = "Step 5: editing the last name and Other Id is saved and shown")
    public void editEmployeeUpdatesDetails() {
        Employee original = EmployeeFixtures.createEmployee(driver());
        PersonalDetailsPage details = new PersonalDetailsPage(driver()).waitUntilLoaded();
        String newLastName = TestDataFactory.uniqueLastName();
        String newOtherId = TestDataFactory.uniqueEmployeeId();
        assertNotEquals(newLastName, original.lastName(), "New last name differs from the original");

        String toast = details.setLastName(newLastName).setOtherId(newOtherId).save();

        assertEquals(toast, "Success: Successfully Updated", "Update notification");
        assertEquals(details.lastName(), newLastName, "Updated last name is shown");
        assertEquals(details.otherId(), newOtherId, "Updated Other Id is shown");
        ScreenshotUtil.capture(driver(), "04-employee-updated");
    }

    @Test(groups = "regression", description = "Step 6: an update survives a page refresh and navigating away and back")
    public void updatedEmployeePersistsAfterRefreshAndNavigation() {
        Employee original = EmployeeFixtures.createEmployee(driver());
        PersonalDetailsPage details = new PersonalDetailsPage(driver()).waitUntilLoaded();
        Employee updated = original.withLastName(TestDataFactory.uniqueLastName());
        String newOtherId = TestDataFactory.uniqueEmployeeId();
        details.setLastName(updated.lastName()).setOtherId(newOtherId).save();

        details.refresh();
        assertEquals(details.lastName(), updated.lastName(), "Last name after refresh");
        assertEquals(details.otherId(), newOtherId, "Other Id after refresh");
        assertNotEquals(details.lastName(), original.lastName(), "Record did not revert to the original last name");

        EmployeeListPage list = dashboard.openPim().searchByName(original.fullName());
        assertEquals(list.recordCount(), 0, "No employee is found under the old name");

        list.reset().searchById(updated.employeeId());
        Row row = list.findByEmployeeId(updated.employeeId())
                .orElseThrow(() -> new AssertionError("Updated employee is still listed"));
        assertEquals(row.get(COL_LAST_NAME), updated.lastName(), "Employee list shows the updated last name");

        PersonalDetailsPage reopened = list.openEmployee(updated.employeeId());
        assertEquals(reopened.lastName(), updated.lastName(), "Last name after navigating away and back");
        assertEquals(reopened.otherId(), newOtherId, "Other Id after navigating away and back");
    }
}
