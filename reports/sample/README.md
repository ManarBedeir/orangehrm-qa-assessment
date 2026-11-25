# Sample execution report

`index.html` is a self-contained **single-file Allure report**: open it directly in a browser, no server needed.
`summary.json` holds its headline numbers.

## What it contains
| Run | Command | Result |
|---|---|---|
| 1. Full suite | `mvn test -DsuiteXmlFile=testng.xml` | 16 / 16 passed |
| 2. Deliberate failure | `mvn test -Dtest=LoginTest#validLoginShowsDashboardAndUser -Dusername=invalid_user` | 1 broken |

Run 2 was added on purpose, so the report shows how a failure is presented: status, duration, the
stack trace and the **failure screenshot** attached by `TestListener` (the login page still open after the
rejected login). Because it is the same test, Allure shows it as the latest result of
`validLoginShowsDashboardAndUser`, with the earlier passing run in its history (Retries tab). The headline
numbers are therefore 15 passed and 1 broken out of 16.

The checkpoint screenshots of the passing tests (login/dashboard, search result, employee created and updated,
negative validation, leave request) are attached to their tests as well.

## How it was generated
```bash
mvn test -DsuiteXmlFile=testng.xml
mvn test -Dtest=LoginTest#validLoginShowsDashboardAndUser -Dusername=invalid_user   # same allure-results folder
mvn allure:report        # also downloads the Allure CLI into .allure/
.allure/allure-2.46.2/bin/allure generate --single-file target/allure-results -o reports/sample/allure-report --clean
```
The generated `index.html` and `summary.json` were then moved up into `reports/sample/`.
For everyday use, `mvn allure:serve` (opens the report) or `mvn allure:report` is enough.
