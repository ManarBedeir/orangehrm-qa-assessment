package com.orangehrm.listeners;

import com.orangehrm.driver.DriverManager;
import com.orangehrm.utils.ScreenshotUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Logs the test lifecycle and captures a screenshot when a test method fails.
 * The capture happens in {@link #afterInvocation}, which TestNG calls before any
 * {@code onTestFailure} listener. At that point the Allure test case is still open, so the
 * screenshot is attached to it, and the browser has not yet been closed by {@code @AfterMethod}.
 */
public class TestListener implements ITestListener, IInvokedMethodListener {

    private static final Logger LOG = LoggerFactory.getLogger(TestListener.class);

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult result) {
        if (method.isTestMethod() && result.getStatus() == ITestResult.FAILURE && DriverManager.hasDriver()) {
            try {
                ScreenshotUtil.capture(DriverManager.getDriver(), "FAILED_" + name(result));
            } catch (RuntimeException e) {
                LOG.warn("Failure screenshot could not be taken: {}", e.getMessage());
            }
        }
    }

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
