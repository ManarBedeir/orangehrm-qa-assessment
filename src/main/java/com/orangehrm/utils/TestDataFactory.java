package com.orangehrm.utils;

import com.orangehrm.models.Employee;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Builds unique, recognisable test data so runs never collide with each other or with other
 * users of the shared demo. Every generated employee is named {@code Automation Test QA<timestamp><suffix>}.
 */
public final class TestDataFactory {

    public static final String FIRST_NAME = "Automation";
    public static final String MIDDLE_NAME = "Test";
    /** OrangeHRM limits first, middle and last name to 30 characters. */
    public static final int NAME_MAX_LENGTH = 30;
    /** OrangeHRM limits the Employee Id to 10 characters. */
    private static final int EMPLOYEE_ID_LENGTH = 10;

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyMMddHHmmss");
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";

    private TestDataFactory() {
    }

    public static Employee newEmployee() {
        return new Employee(FIRST_NAME, MIDDLE_NAME, uniqueLastName(), uniqueEmployeeId());
    }

    /** For example {@code QA251125221530kx}: timestamp plus a random suffix. */
    public static String uniqueLastName() {
        return "QA" + STAMP.format(LocalDateTime.now()) + randomLetters(2);
    }

    /** "AT" followed by 8 random digits, for example {@code AT48213077}. */
    public static String uniqueEmployeeId() {
        StringBuilder id = new StringBuilder("AT");
        while (id.length() < EMPLOYEE_ID_LENGTH) {
            id.append(ThreadLocalRandom.current().nextInt(10));
        }
        return id.toString();
    }

    /** A unique name one character longer than OrangeHRM accepts. */
    public static String oversizedName() {
        String base = uniqueLastName();
        return base + "x".repeat(NAME_MAX_LENGTH + 1 - base.length());
    }

    /**
     * The first weekday on or after {@code daysAhead} days from today. A random offset is used by
     * leave tests so that repeated runs do not request overlapping dates.
     */
    public static LocalDate nextWeekday(int daysAhead) {
        LocalDate date = LocalDate.now().plusDays(daysAhead);
        while (isWeekend(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    /** A random weekday between {@code minDaysAhead} and {@code maxDaysAhead} days from today. */
    public static LocalDate randomWeekday(int minDaysAhead, int maxDaysAhead) {
        return nextWeekday(ThreadLocalRandom.current().nextInt(minDaysAhead, maxDaysAhead + 1));
    }

    public static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private static String randomLetters(int count) {
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < count; i++) {
            letters.append(LETTERS.charAt(ThreadLocalRandom.current().nextInt(LETTERS.length())));
        }
        return letters.toString();
    }
}
