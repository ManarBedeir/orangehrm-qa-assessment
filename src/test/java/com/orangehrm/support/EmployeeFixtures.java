package com.orangehrm.support;

import com.orangehrm.models.Employee;
import com.orangehrm.pages.pim.EmployeeListPage;
import com.orangehrm.pages.pim.PersonalDetailsPage;
import com.orangehrm.utils.TestDataFactory;
import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;

/** Creates employees through the UI and registers each one for deletion after the test. */
public final class EmployeeFixtures {

    private EmployeeFixtures() {
    }

    /** Creates a uniquely named employee and leaves the browser on its Personal Details page. */
    @Step("Create test employee")
    public static PersonalDetailsPage createEmployee(WebDriver driver, Employee employee) {
        registerForDeletion(employee.employeeId());
        return new EmployeeListPage(driver).open()
                .openAddEmployee()
                .fill(employee)
                .save()
                .waitUntilLoaded();
    }

    public static Employee createEmployee(WebDriver driver) {
        Employee employee = TestDataFactory.newEmployee();
        createEmployee(driver, employee);
        return employee;
    }

    /**
     * Registers deletion by Employee Id before the employee is saved, so a record is removed even
     * if the test fails right after creating it. A missing record is treated as already clean.
     */
    public static void registerForDeletion(String employeeId) {
        TestDataCleaner.register("delete employee " + employeeId, driver -> {
            EmployeeListPage list = new EmployeeListPage(driver).open().searchById(employeeId);
            if (list.findByEmployeeId(employeeId).isPresent()) {
                list.deleteEmployee(employeeId);
            }
        });
    }
}
