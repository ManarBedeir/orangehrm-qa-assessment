package com.orangehrm.pages.components;

import com.orangehrm.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OXD list table. Rows are read as header-to-cell maps so tests can locate and assert rows
 * by meaningful values (name, ID, status) instead of by position.
 */
public class DataTable extends BasePage {

    private static final By HEADERS = By.cssSelector(".oxd-table-header .oxd-table-th");
    private static final By ROWS = By.cssSelector(".oxd-table-body .oxd-table-card");
    private static final By CELLS = By.cssSelector(".oxd-table-cell");
    private static final By RECORD_COUNT = By.xpath("//span[contains(@class,'oxd-text') and contains(normalize-space(),'Found')]");
    private static final By NEXT_PAGE = By.xpath("//button[contains(@class,'oxd-pagination-page-item--previous-next')][.//i[contains(@class,'bi-chevron-right')]]");
    private static final By TABLE_LOADER = By.cssSelector(".oxd-table-loader, .oxd-loading-spinner");
    private static final Pattern COUNT = Pattern.compile("\\((\\d+)\\)");

    /** A table row: its cell texts keyed by column header, plus the row element for actions. */
    public record Row(Map<String, String> cells, WebElement element) {
        public String get(String column) {
            return cells.getOrDefault(column, "");
        }
    }

    public DataTable(WebDriver driver) {
        super(driver);
    }

    /**
     * Waits for a table refresh triggered by the caller (search, reset, delete). The loader can be
     * too quick to observe, so its appearance is awaited only briefly before waiting for it to go.
     */
    public void waitForRefresh() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.visibilityOfElementLocated(TABLE_LOADER));
        } catch (TimeoutException refreshAlreadyFinished) {
            // Nothing to wait for: the table re-rendered before the loader could be observed.
        }
        wait.until(ExpectedConditions.invisibilityOfElementLocated(TABLE_LOADER));
        visible(RECORD_COUNT);
    }

    /** Number from "(N) Records Found"; 0 for "No Records Found". */
    public int recordCount() {
        Matcher matcher = COUNT.matcher(text(RECORD_COUNT));
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    /** Rows on the current page. */
    public List<Row> rows() {
        waitForLoaders();
        List<String> headers = driver.findElements(HEADERS).stream().map(h -> h.getText().trim()).toList();
        List<Row> rows = new ArrayList<>();
        for (WebElement rowElement : driver.findElements(ROWS)) {
            List<WebElement> cells = rowElement.findElements(CELLS);
            Map<String, String> values = new LinkedHashMap<>();
            for (int i = 0; i < Math.min(headers.size(), cells.size()); i++) {
                values.put(headers.get(i), cells.get(i).getText().trim());
            }
            rows.add(new Row(values, rowElement));
        }
        return rows;
    }

    /** Rows across pages, following the pagination "next" button for at most {@code maxPages} pages. */
    public List<Row> rowsAcrossPages(int maxPages) {
        List<Row> rows = new ArrayList<>(rows());
        for (int page = 1; page < maxPages && isDisplayed(NEXT_PAGE); page++) {
            click(NEXT_PAGE);
            waitForRefresh();
            rows.addAll(rows());
        }
        return rows;
    }

    public Optional<Row> findRow(Predicate<Row> condition) {
        return rows().stream().filter(condition).findFirst();
    }

    /** Clicks a button or icon inside the given row, e.g. {@code By.cssSelector("i.bi-trash")}. */
    public void clickInRow(Row row, By action) {
        click(row.element().findElement(action));
    }
}
