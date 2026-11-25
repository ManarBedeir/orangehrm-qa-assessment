package com.orangehrm.support;

import com.orangehrm.driver.DriverManager;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

/**
 * Per-thread registry of clean-up actions for data a test created (employees, leave requests).
 * Actions run in reverse order after each test. A failing action is logged and skipped:
 * clean-up must never change a test's result.
 */
public final class TestDataCleaner {

    private static final Logger LOG = LoggerFactory.getLogger(TestDataCleaner.class);
    private static final ThreadLocal<Deque<CleanupAction>> ACTIONS = ThreadLocal.withInitial(ArrayDeque::new);

    private record CleanupAction(String description, Consumer<WebDriver> action) {
    }

    private TestDataCleaner() {
    }

    public static void register(String description, Consumer<WebDriver> action) {
        ACTIONS.get().push(new CleanupAction(description, action));
    }

    public static void cleanUp() {
        Deque<CleanupAction> actions = ACTIONS.get();
        try {
            while (!actions.isEmpty()) {
                CleanupAction cleanup = actions.pop();
                try {
                    cleanup.action().accept(DriverManager.getDriver());
                    LOG.info("Cleanup done: {}", cleanup.description());
                } catch (RuntimeException e) {
                    LOG.warn("Cleanup failed (ignored): {} -> {}", cleanup.description(), e.toString());
                }
            }
        } finally {
            ACTIONS.remove();
        }
    }
}
