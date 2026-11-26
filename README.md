# OrangeHRM UI Automation and Manual Testing Assessment

A Selenium 4 / TestNG / Allure framework that automates the core PIM and Leave flows of the public
[OrangeHRM demo](https://opensource-demo.orangehrmlive.com/), plus the manual-testing deliverables for an
"Employee Leave Request" feature (see [docs/manual-testing](docs/manual-testing)).

---

## Contents
1. [Prerequisites](#1-prerequisites)
2. [Technologies](#2-technologies)
3. [Framework architecture](#3-framework-architecture)
4. [Project structure](#4-project-structure)
5. [Running the tests](#5-running-the-tests)
6. [Reports and screenshots](#6-reports-and-screenshots)
7. [Configuration](#7-configuration)
8. [Test coverage](#8-test-coverage)
9. [Test-data strategy](#9-test-data-strategy)
10. [Test-data cleanup](#10-test-data-cleanup)
11. [Known limitations](#11-known-limitations)
12. [Manual testing deliverables](#12-manual-testing-deliverables)

---

## 1. Prerequisites

| Tool | Version | Notes |
|---|---|---|
| Java JDK | **17 or newer** (developed and run on Temurin 21.0.13) | `java -version` |
| Maven | **3.9.x** (3.9.16 via the bundled wrapper) | A global Maven is optional: use `./mvnw` (macOS/Linux/Git Bash) or `mvnw.cmd` (Windows) |
| Browser | Chrome (default), Firefox or Edge | Drivers are resolved automatically by **Selenium Manager** |
| Internet access | n/a | The tests run against the public demo site |

No driver downloads, environment variables or Allure installation are needed.

## 2. Technologies

| Purpose | Library | Version |
|---|---|---|
| Browser automation | Selenium WebDriver (with Selenium Manager) | 4.54.0 |
| Test runner | TestNG | 7.12.0 |
| Reporting | Allure TestNG + allure-maven plugin (Allure CLI 2.46.2) | 2.35.6 / 2.18.0 |
| AOP for Allure `@Step` | AspectJ weaver | 1.9.27 |
| Logging | SLF4J simple | 2.0.20 |
| Build | Maven Surefire | 3.5.4 |

## 3. Framework architecture

```
           TestNG suite / groups / -Dtest
                       │
   ┌───────────────────▼───────────────────┐
   │ Tests (src/test)                      │  assertions on data, not just visibility
   │  BaseTest → LoggedInBaseTest          │  fresh browser + login per test method
   │  EmployeeFixtures, TestDataCleaner    │  data setup and fail-safe cleanup
   │  TestListener                         │  logging + failure screenshot → Allure
   └───────────────────┬───────────────────┘
                       │ uses
   ┌───────────────────▼───────────────────┐
   │ Page Objects (src/main)               │  one class per screen, fluent API
   │  LoginPage, DashboardPage             │
   │  pim/*  leave/*                       │
   ├───────────────────────────────────────┤
   │ OXD components (reusable widgets)     │  Autocomplete, OxdSelect, MultiSelect,
   │                                       │  DatePicker, DataTable, Toast, ConfirmDialog
   ├───────────────────────────────────────┤
   │ BasePage                              │  explicit waits, loader handling, click/type
   ├───────────────────────────────────────┤
   │ DriverManager (ThreadLocal)           │  DriverFactory: chrome | firefox | edge, headless
   │ ConfigReader                          │  config.properties ← -D overrides
   │ TestDataFactory, ScreenshotUtil       │
   └───────────────────────────────────────┘
```

Key design decisions:
- **Page Object Model with component objects.** OrangeHRM is built from "OXD" Vue components. Each widget
  (autocomplete, custom select, multi-select chips, date input, data table, toast, modal) is wrapped once,
  located by its **field label**, and reused by every page.
- **Explicit waits only.** `BasePage` waits for OrangeHRM's loaders (`.oxd-loading-spinner`, `.oxd-form-loader`)
  before every interaction, and uses `WebDriverWait` conditions for everything else. There is no `Thread.sleep`
  and no implicit wait.
- **Readable, stable locators:** `name` attributes, OXD class names and label-relative XPath
  (`//label[normalize-space()='Employee Id']/ancestor::div[...oxd-input-group...]//input`). No absolute XPath.
- **No positional rows.** `DataTable` reads each row into a `header → cell` map. Tests find rows by Employee Id
  or name and assert on column values.
- **Thread-safe driver.** `DriverManager` keeps one `WebDriver` per thread (`ThreadLocal`), so the suite can run
  in parallel (`parallel="methods"` in a suite file). It runs sequentially by default to be gentle on the shared demo.
- **Independent tests.** Every test method starts its own browser, logs in, creates its own data and cleans it up.
  Any test can run alone or in any order.

## 4. Project structure

```
├── pom.xml                         Maven build, Surefire configuration and "suite" profile
├── mvnw, mvnw.cmd, .mvn/           Maven Wrapper (no global Maven needed)
├── testng.xml                      Full suite
├── testng-smoke.xml                Smoke group suite
├── testng-regression.xml           Regression group suite
├── testng-negative.xml             Negative group suite
├── src/main/java/com/orangehrm/
│   ├── config/ConfigReader.java            config.properties + system property overrides
│   ├── driver/DriverFactory.java           browser creation (Selenium Manager, headless)
│   ├── driver/DriverManager.java           ThreadLocal driver holder
│   ├── models/Employee.java                employee test-data record
│   ├── pages/BasePage.java                 shared wait and action helpers
│   ├── pages/LoginPage.java, DashboardPage.java
│   ├── pages/components/                   Autocomplete, OxdSelect, MultiSelect, DatePicker,
│   │                                       DataTable, Toast, ConfirmDialog, SideMenu
│   ├── pages/pim/                          EmployeeListPage, AddEmployeePage, PersonalDetailsPage
│   ├── pages/leave/                        AddEntitlementPage, AssignLeavePage, LeaveListPage, HolidayListPage
│   └── utils/                              TestDataFactory, ScreenshotUtil
├── src/test/java/com/orangehrm/
│   ├── base/BaseTest.java, LoggedInBaseTest.java
│   ├── listeners/TestListener.java         logging + failure screenshots
│   ├── support/EmployeeFixtures.java       create employees, register them for deletion
│   ├── support/TestDataCleaner.java        per-thread, fail-safe cleanup registry
│   └── tests/                              LoginTest, EmployeeTest, EmployeeFilterTest, LeaveTest, NegativeTest
├── src/test/resources/
│   ├── config.properties                   URL, credentials, browser, timeouts
│   ├── allure.properties
│   └── simplelogger.properties
├── screenshots/                            representative screenshots (runtime ones are git-ignored)
├── reports/sample/                         example Allure report (single HTML file)
└── docs/manual-testing/                    requirements review, test cases (+ .xlsx), bug report (Word + PDF)
```

## 5. Running the tests

`mvn` and `./mvnw` are interchangeable. On Windows PowerShell use `.\mvnw.cmd` if Maven is not installed.

| What | Command |
|---|---|
| All tests (classpath scan) | `mvn test` |
| **Individual test class** | `mvn test -Dtest=EmployeeTest` |
| Individual test method | `mvn test -Dtest=EmployeeTest#addEmployeeGeneratesIdAndShowsSuccess` |
| Several classes | `mvn test -Dtest=LoginTest,LeaveTest` |
| **TestNG suite** | `mvn test -DsuiteXmlFile=testng.xml` |
| Group suite file | `mvn test -DsuiteXmlFile=testng-smoke.xml` |
| **Test group** | `mvn test -Dgroups=smoke` |
| | `mvn test -Dgroups=regression` |
| | `mvn test -Dgroups=negative` |
| Exclude a group | `mvn test -DexcludedGroups=negative` |
| Headless / another browser | `mvn test -Dgroups=smoke -Dheadless=true -Dbrowser=firefox` |

**How the three run modes avoid conflicts:** by default Surefire scans `**/*Test.java`, so `-Dtest` and
`-Dgroups` work natively. A Maven profile named `suite` is activated **only** when `-DsuiteXmlFile` is passed.
It switches Surefire to that TestNG XML file, so a suite run never collides with the scan or the group
filter. The `TestListener` is registered by Surefire in every mode, so suite files don't repeat it.

Typical durations against the demo (Chrome, sequential): smoke ≈ 2 min (5 tests), full suite ≈ 8–10 min (16 tests).

## 6. Reports and screenshots

### Allure report
Every run writes Allure results to `target/allure-results`. Each test shows its name, status, duration,
`@Step`s, checkpoint screenshots, and for failures the stack trace plus the **failure screenshot**.

```bash
mvn allure:serve      # build the report and open it in a browser
mvn allure:report     # build it into target/site/allure-maven-plugin/index.html
```
`allure:report` and `allure:serve` download the Allure CLI on first use. No local install is needed.

**Sample report:** [`reports/sample/index.html`](reports/sample/index.html) is a self-contained single-file
Allure report; open it directly in a browser. It combines a full green run of `testng.xml` with one deliberate
failing run (`mvn test -Dtest=LoginTest#validLoginShowsDashboardAndUser -Dusername=invalid_user`),
so you can see how a failure is reported with its screenshot. See [`reports/sample/README.md`](reports/sample/README.md).

Surefire's plain reports are also available in `target/surefire-reports/` after every run.

### Screenshots
- **Checkpoint screenshots** are taken at login/dashboard, employee search result, employee created, employee
  updated, negative validation and leave request. They are saved as `screenshots/<timestamp>_<name>.png` and attached to Allure.
- **Failure screenshots** are captured automatically by `TestListener` (`FAILED_<Class>.<method>`).
  The capture runs in `afterInvocation`, before Allure closes the test, so the image is attached to the
  failing test and the browser is still open.
- Six representative screenshots are committed in [`screenshots/`](screenshots); runtime screenshots are git-ignored.

## 7. Configuration

All run settings live in [`src/test/resources/config.properties`](src/test/resources/config.properties).
**Any key can be overridden with a JVM system property** (`-Dkey=value`), which takes precedence over the file.

| Key | Default | Description |
|---|---|---|
| `baseUrl` | `https://opensource-demo.orangehrmlive.com` | Application URL |
| `username` / `password` | demo Admin credentials | The only place credentials exist; page and test classes read them via `ConfigReader` |
| `browser` | `chrome` | `chrome`, `firefox` or `edge` |
| `headless` | `false` | `true` for CI or headless runs |
| `windowWidth` / `windowHeight` | `1920` / `1080` | Browser size (also applied in headless mode) |
| `explicitWaitSeconds` | `30` | `WebDriverWait` timeout (generous because the demo is shared and can be slow) |
| `pageLoadTimeoutSeconds` | `90` | Page load timeout |

Example: `mvn test -Dgroups=smoke -Dheadless=true -DexplicitWaitSeconds=40`.
For a real environment, keep secrets out of the file and pass them at runtime, e.g. `-Dpassword=$ORANGEHRM_PASSWORD`.

## 8. Test coverage

| Test class.method | Assessment step | Groups |
|---|---|---|
| `LoginTest.loginPageDisplaysAllElements` | 1 – login page, fields, button | smoke |
| `LoginTest.validLoginShowsDashboardAndUser` | 1 – login, dashboard, logged-in user | smoke, regression |
| `LoginTest.invalidPasswordIsRejected` | extra negative | negative, regression |
| `EmployeeTest.searchExistingEmployeeByName` | 2 – search, every result row matches | smoke, regression |
| `EmployeeTest.addEmployeeGeneratesIdAndShowsSuccess` | 3 – create, toast, generated ID, listed | smoke, regression |
| `EmployeeTest.createdEmployeeIsPersisted` | 4 – re-search, open, data matches | regression |
| `EmployeeTest.editEmployeeUpdatesDetails` | 5 – edit last name and Other Id | regression |
| `EmployeeTest.updatedEmployeePersistsAfterRefreshAndNavigation` | 6 – refresh, navigate away and back | regression |
| `EmployeeFilterTest.filterByNameAndEmployeeIdReturnsOnlyMatch` | 7 – two criteria | regression |
| `EmployeeFilterTest.filterByEmploymentStatusAndJobTitleReturnsMatchingRows` | 7 – every row matches both filters | regression |
| `EmployeeFilterTest.resetClearsFiltersAndRestoresResults` | 7 – reset restores the result set | regression |
| `LeaveTest.assignedLeaveIsSavedAndListed` | 8 – leave request saved and found | smoke, regression |
| `NegativeTest.addEmployeeWithoutLastNameShowsRequired` | 9A – required field | negative, regression |
| `NegativeTest.addEmployeeWithOversizedFirstNameIsRejected` | 9B – invalid (31 chars) data | negative, regression |
| `NegativeTest.addEmployeeWithDuplicateEmployeeIdIsRejected` | 9B – duplicate Employee Id | negative, regression |
| `NegativeTest.leaveWithToDateBeforeFromDateIsRejected` | 9C – invalid leave dates | negative, regression |
| Cleanup (`TestDataCleaner`) | 10 | n/a |

Every negative test asserts **both** the validation message **and** that nothing was saved (a follow-up search returns no record).

## 9. Test-data strategy

- **Generated, unique data:** `TestDataFactory` builds employees named `Automation Test QA<yyMMddHHmmss><2 random letters>`
  with an Employee Id `AT<8 random digits>`. The values are recognisable (anyone on the demo can see they are
  automation data), unique per run and thread, and within OrangeHRM's field limits (30-character names, 10-character IDs).
- **Own data for every assertion:** tests that need a known employee create it, so they don't depend on demo data
  that other users change. The Step 2 search deliberately uses an *existing* employee, but picks it at runtime
  from the current list, so it is never hard-coded.
- **Calculated leave dates:** `LeaveTest` takes a random weekday 2–6 weeks ahead, reads the **holidays configured in
  OrangeHRM** for that window (Leave → Holidays), and selects the first two consecutive working days (no weekend,
  no holiday). It can therefore assert an exact count of `2.00` days. The randomisation prevents overlaps with earlier runs.
- **Leave entitlement / Assign Leave choice:** the demo Admin's own employee record is renamed by other users
  ("manda user" at the time of writing) and usually has a 0.00 balance. Applying leave as Admin would therefore depend
  on unstable shared data. Instead the test:
  1. creates its own employee,
  2. adds a 5-day entitlement for it (Leave → Entitlements → Add, confirming the "Updating Entitlement" dialog),
  3. records the request through **Leave → Assign Leave** (the admin flow; status *Scheduled*),
  4. finds it again in **Leave → Leave List** by employee, date range and status, and checks the type, dates,
     number of days, status and comment.

  This makes Step 8 deterministic and fully cleanable. The *employee self-service* "Apply → Pending Approval" path
  is covered by the manual test cases.
- **No credentials in code:** they are read only from `config.properties` (or `-D` overrides).

## 10. Test-data cleanup

| Data created | Cleaned up? | How |
|---|---|---|
| Employees (Steps 3–7, 8, 9) | **Yes** | After each test, `TestDataCleaner` searches PIM by the Employee Id and deletes the employee (PIM → trash icon → "Yes, Delete"). |
| Leave entitlement and leave request (Step 8) | **Yes, indirectly** | They belong to the test employee, so deleting the employee removes them from every leave list and report. |
| Employees from negative tests | Nothing should exist | The deletion is still registered, in case the application accepts the invalid data (that would be a defect). |

How it works:
- The deletion is **registered before the employee is saved** (`EmployeeFixtures.registerForDeletion`), so data is
  removed even when a test fails half-way.
- Cleanup actions run in `@AfterMethod(alwaysRun = true)`, in reverse order, **before** the browser closes.
- Cleanup is **fail-safe**: each action is wrapped and a failure is logged as a `WARN`
  (`Cleanup failed (ignored): ...`). It never changes a test's result. A record that is already gone
  (demo reset, another user) counts as clean.

What cannot be fully cleaned up, and why:
- OrangeHRM's PIM **delete** removes the employee from the application, but the database keeps the history.
  Physically purging it needs *Maintenance → Purge Records*, which asks the admin to re-enter their password and
  irreversibly purges data on a shared environment. That is not something an automated UI test should do.
- **Leave requests cannot be deleted** in OrangeHRM at all; they can only be cancelled, which keeps an audit trail.
  This is why the leave test uses a throw-away employee instead of the shared Admin account: nothing remains
  visible on the Admin's leave history.
- If the run is killed abruptly (for example the JVM is stopped), `@AfterMethod` does not run. Leftovers are easy to find
  (search PIM for *Automation Test*) and are also removed by the demo's periodic reset.

## 11. Known limitations

- **Shared public demo:** the site is used by many people at once, is reset periodically and is sometimes very slow.
  The framework uses generous waits (30s explicit, 90s page load) but **no automatic retries**, so a real defect
  is never hidden. An occasional timeout caused by the demo itself is possible; rerun the affected test.
  One demo-specific workaround exists: `AddEmployeePage.save()` clicks Save a second time if the first click had no
  effect (no redirect and no validation message). The Javadoc explains why this cannot create duplicates.
- **Pagination:** filter assertions check every row on up to 5 result pages (250 rows). Larger result sets are
  checked on the first 5 pages only.
- **"Reset restores results"** compares the record count before filtering and after Reset. If another user adds or deletes an
  employee in those few seconds, the counts can differ.
- The suite runs sequentially by default. Parallel execution is supported by the design (ThreadLocal driver,
  per-thread cleanup), but it is not enabled, to avoid overloading the shared demo.

## 12. Manual testing deliverables

Each document is provided as Word (.docx) and PDF.

| Document | Content |
|---|---|
| Requirements review ([PDF](docs/manual-testing/01-Requirements-Review-Questions-and-Risks.pdf), [Word](docs/manual-testing/01-Requirements-Review-Questions-and-Risks.docx)) | Questions per user story, AC and flow step; gaps between the flow and the ACs; assumptions; risks |
| Test scenarios and test cases ([PDF](docs/manual-testing/02-Test-Scenarios-and-Test-Cases.pdf), [Word](docs/manual-testing/02-Test-Scenarios-and-Test-Cases.docx)) | 8 scenarios, 57 test cases (ID, AC, preconditions, steps, data, expected result, priority, type), traceability matrix |
| [test-cases.xlsx](docs/manual-testing/test-cases.xlsx) | The same test cases as a filterable spreadsheet, with Status and Actual-result columns for execution |
| Bug report ([PDF](docs/manual-testing/03-Bug-Report-Leave-Balance-Deduction.pdf), [Word](docs/manual-testing/03-Bug-Report-Leave-Balance-Deduction.docx)) | Defect report: 10–12 Aug shows 3 days but deducts 4 (10 Aug 2026 is a Monday) |
