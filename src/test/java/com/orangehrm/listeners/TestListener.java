package com.orangehrm.listeners;

import com.orangehrm.driver.DriverManager;
import com.orangehrm.utils.ScreenshotUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Logs test lifecycle events and captures a failure screenshot, which {@link ScreenshotUtil}
 * also attaches to the Allure report. Runs before {@code @AfterMethod}, so the browser is still open.
 */
public class TestListener implements ITestListener {

    private static final Logger LOG = LoggerFactory.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        LOG.info("START  {}", name(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOG.info("PASS   {} ({} ms)", name(result), duration(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LOG.error("FAIL   {} ({} ms): {}", name(result), duration(result), result.getThrowable().toString());
        if (DriverManager.hasDriver()) {
            try {
                ScreenshotUtil.capture(DriverManager.getDriver(), "FAILED_" + name(result));
            } catch (RuntimeException e) {
                LOG.warn("Failure screenshot could not be taken: {}", e.getMessage());
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOG.warn("SKIP   {}", name(result));
    }

    private static String name(ITestResult result) {
        return result.getTestClass().getRealClass().getSimpleName() + "." + result.getMethod().getMethodName();
    }

    private static long duration(ITestResult result) {
        return result.getEndMillis() - result.getStartMillis();
    }
}
