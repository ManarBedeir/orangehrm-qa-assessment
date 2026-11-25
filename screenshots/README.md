# Representative screenshots

Captured by the framework (`ScreenshotUtil`) during the full-suite run against the OrangeHRM demo.

| File | Test | Shows |
|---|---|---|
| `01-login-dashboard.png` | `LoginTest.validLoginShowsDashboardAndUser` | Dashboard after login, with the logged-in user in the top bar |
| `02-employee-search-result.png` | `EmployeeTest.searchExistingEmployeeByName` | Employee List filtered by an existing employee's name |
| `03-employee-created.png` | `EmployeeTest.addEmployeeGeneratesIdAndShowsSuccess` | New employee's Personal Details with the generated Employee Id |
| `04-employee-updated.png` | `EmployeeTest.editEmployeeUpdatesDetails` | Personal Details after updating the last name and Other Id |
| `05-negative-validation.png` | `NegativeTest.addEmployeeWithoutLastNameShowsRequired` | "Required" validation under Last Name; nothing saved |
| `06-leave-request.png` | `LeaveTest.assignedLeaveIsSavedAndListed` | Assign Leave form for two working days (the 28 Dec holiday was skipped) with a 5.00 day balance |

Runtime screenshots (`<timestamp>_<name>.png`, including `FAILED_*` failure captures) are written to this
folder on every run and are git-ignored. Failure screenshots are also attached to the Allure report.
