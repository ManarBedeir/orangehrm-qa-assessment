package com.orangehrm.base;

import com.orangehrm.driver.DriverManager;
import com.orangehrm.support.TestDataCleaner;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Starts a fresh browser for every test method and closes it afterwards, so tests share no
 * browser state and can run in any order (or in parallel, thanks to the ThreadLocal driver).
 */
public abstract class BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        DriverManager.start();
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUpAndQuit() {
        try {
            TestDataCleaner.cleanUp();
        } finally {
            DriverManager.quit();
        }
    }

    protected WebDriver driver() {
        return DriverManager.getDriver();
    }
}
