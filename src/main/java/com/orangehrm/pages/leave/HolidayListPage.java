package com.orangehrm.pages.leave;

import com.orangehrm.pages.BasePage;
import com.orangehrm.pages.components.DataTable;
import com.orangehrm.pages.components.DatePicker;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

/** Leave - Configure - Holidays. Used to pick leave dates that are real working days. */
public class HolidayListPage extends BasePage {

    private static final String PATH = "/web/index.php/leave/viewHolidayList";
    private static final By SEARCH_BUTTON = By.cssSelector(".oxd-table-filter button[type='submit']");

    private final DatePicker fromDate;
    private final DatePicker toDate;
    private final DataTable table;

    public HolidayListPage(WebDriver driver) {
        super(driver);
        this.fromDate = new DatePicker(driver, "From");
        this.toDate = new DatePicker(driver, "To");
        this.table = new DataTable(driver);
    }

    public HolidayListPage open() {
        open(PATH);
        table.waitForRefresh();
        return this;
    }

    @Step("Read holidays from {from} to {to}")
    public Set<LocalDate> holidaysBetween(LocalDate from, LocalDate to) {
        fromDate.set(from);
        toDate.set(to);
        click(SEARCH_BUTTON);
        table.waitForRefresh();
        DateTimeFormatter format = fromDate.formatter();
        return table.rows().stream()
                .map(row -> LocalDate.parse(row.get("Date"), format))
                .collect(Collectors.toSet());
    }
}
