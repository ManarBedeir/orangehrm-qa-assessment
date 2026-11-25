# Employee Leave Request: Test Scenarios and Test Cases

**Feature:** Employee Leave Request (user story, AC1–AC10, business flow)
**Assumptions:** as listed in [01-questions-ambiguities-risks.md](01-questions-ambiguities-risks.md), section 5. Cases that depend on an open question say so in the expected result.
**Spreadsheet version:** [test-cases.xlsx](test-cases.xlsx) (same content, filterable).

## Common precondition P1
ESS employee "Emp A" logged in; supervisor "Mgr M"; work week Mon-Fri; holiday calendar loaded; leave period = calendar year; Annual Leave entitlement 10.00 days; no other leave in the period.

## Test scenarios (high level)

| # | Scenario | Covered by |
|---|---|---|
| S1 | Employee reaches the leave request page and sees only the leave types they are eligible for, with their balance | LR-001 – LR-006 |
| S2 | Date selection and validation, including single day, past, cross-period and invalid input | LR-008 – LR-015 |
| S3 | Day calculation excludes weekends and holidays, supports half and partial days, and **equals the deducted amount** | LR-016 – LR-025 |
| S4 | Optional comment, including its limits and special characters | LR-026 – LR-029 |
| S5 | Submission, including double submit, balance limits and overlaps | LR-030 – LR-036 |
| S6 | Status lifecycle: Pending → Approved / Rejected / Cancelled, with the balance correct at every step | LR-037, LR-042 – LR-049, LR-057 |
| S7 | Viewing own requests; permissions and roles | LR-039 – LR-041, LR-048, LR-050 |
| S8 | Notifications, UI/UX and non-functional checks | LR-051 – LR-056 |

## Coverage summary

| Type | Count | | Priority | Count |
|---|---|---|---|---|
| Positive | 25 | | High | 29 |
| Negative | 12 | | Medium | 20 |
| Boundary | 8 | | Low | 8 |
| Edge | 12 | |  |  |
| **Total** | **57** | | | |

## Test cases

| ID | Title | Linked AC | Preconditions | Steps | Test Data | Expected Result | Priority | Type |
|---|---|---|---|---|---|---|---|---|
| LR-001 | Open Leave Request page from the main menu | AC1 | P1 | 1. Click Leave in the main menu<br>2. Open "Apply" | - | Apply Leave page opens; Leave Type, From Date, To Date, Comments and Apply are visible; mandatory fields are marked * | High | Positive |
| LR-002 | Page access via direct URL as ESS user | AC1 | P1 | 1. Paste the Apply Leave URL into the browser | /leave/applyLeave | Page opens without error for an authenticated ESS user | Medium | Positive |
| LR-003 | Unauthenticated access is redirected to login | AC1 | User logged out | 1. Open the Apply Leave URL | /leave/applyLeave | Redirect to Login; after login the user lands on Apply Leave or the Dashboard; no data exposed | High | Negative |
| LR-004 | Empty state when the employee has no leave types or balance | AC1, AC2 | ESS user with no entitlement for any type | 1. Open Apply Leave | - | Informative message (e.g. "No Leave Types with Leave Balance") instead of an unusable form | Medium | Edge |
| LR-005 | Leave Type list contains only eligible, active types | AC2 | P1; one inactive leave type exists | 1. Open the Leave Type dropdown | - | Only active types the employee is entitled to are listed; inactive or deleted types are absent; no default is preselected | High | Positive |
| LR-006 | Balance is shown after selecting a type | AC2 | P1 | 1. Select Annual Leave | Annual Leave | Leave Balance shows 10.00 Day(s); a link or popup shows the breakdown (entitled, taken, scheduled, pending) | High | Positive |
| LR-007 | Submit without a Leave Type | AC2, AC10 | P1 | 1. Leave the type empty<br>2. Enter valid dates<br>3. Click Apply | Dates: next Mon-Tue | "Required" under Leave Type; no request created | High | Negative |
| LR-008 | Select a valid multi-day range with the date picker | AC3 | P1 | 1. Select type<br>2. Pick From and To in the calendar | Next Mon to next Wed | Both dates are shown in the configured format; the calendar closes; the day count is calculated | High | Positive |
| LR-009 | To Date defaults to From Date | AC3 | P1 | 1. Pick From Date only | Next Mon | To Date is auto-filled with the same date; Duration (Full/Half day) options appear for a single day | Medium | Positive |
| LR-010 | Typed date in an invalid format | AC3, AC10 | P1 | 1. Type a date manually in a wrong format | "10/08/2026", "2026-13-40", "abc" | "Should be a valid date in yyyy-dd-mm format" (or the configured format); Apply blocked | Medium | Negative |
| LR-011 | To Date before From Date | AC3, AC10 | P1 | 1. From = next Wed<br>2. To = next Tue<br>3. Click Apply | From Wed, To Tue | "To date should be after from date"; no request created; balance unchanged | High | Negative |
| LR-012 | Single-day request (From = To) | AC3, AC4 | P1 | 1. From = To = next Tue<br>2. Duration Full Day<br>3. Apply | Next Tue | Request saved with 1.00 day | High | Boundary |
| LR-013 | Past dates | AC3, AC10 | P1 | 1. Select a range last week<br>2. Apply | Last Mon-Tue | Behaves as confirmed in Q US-2: either accepted (backdated) or blocked with a clear message. Never silently changes the dates | Medium | Edge |
| LR-014 | Range spanning two leave periods (year end) | AC3, AC4 | P1; next-year period and entitlement exist | 1. From = 30 Dec<br>2. To = 2 Jan<br>3. Apply | 30 Dec - 2 Jan | Days are counted per period, excluding weekends and 1 Jan; each period balance is reduced by its own share (or the request is blocked per the agreed rule) | High | Edge |
| LR-015 | Date outside any defined leave period | AC3, AC10 | P1; no period defined 2 years ahead | 1. Select dates 2 years ahead<br>2. Apply | +2 years | Clear validation message; no request created | Low | Negative |
| LR-016 | Working days Mon-Fri are counted | AC4 | P1 | 1. Select Mon to Fri of one week | Mon-Fri, no holidays | Number of days = 5.00 | High | Positive |
| LR-017 | Weekend inside the range is excluded | AC4 | P1 | 1. Select Fri to next Mon | Fri-Mon | Number of days = 2.00 (Sat and Sun excluded) | High | Boundary |
| LR-018 | Range consisting only of weekend days | AC4, AC10 | P1 | 1. Select Sat-Sun<br>2. Apply | Sat-Sun | Blocked: "No working days selected" (or equivalent); balance unchanged | High | Edge |
| LR-019 | Public holiday inside the range is excluded | AC4 | P1; holiday on a Wednesday | 1. Select Mon-Fri of the holiday week | Week with a Wed holiday | Number of days = 4.00 | High | Boundary |
| LR-020 | Half-day holiday inside the range | AC4 | P1; half-day holiday on Fri | 1. Select Thu-Fri | Thu + half-day Fri | Number of days = 1.50 | Medium | Edge |
| LR-021 | Half-day request (morning / afternoon) | AC4 | P1 | 1. Single day<br>2. Duration = Half Day - Morning<br>3. Apply | Next Tue AM | Number of days = 0.50; balance reduced by 0.50 only | High | Boundary |
| LR-022 | Partial days on a multi-day request | AC4 | P1 | 1. Mon-Wed<br>2. Partial Days = Start Day only, Half Day Afternoon | Mon PM - Wed | Number of days = 2.50 | Medium | Boundary |
| LR-023 | Displayed days equal days deducted from the balance | AC4, AC7 | P1 | 1. Note the balance<br>2. Apply Mon-Wed<br>3. Check the request days and the balance | Mon-Wed (3 working days) | Request shows 3.00; balance goes from 10.00 to 7.00 (pending/reserved). Covers the 10-12 Aug defect | High | Positive |
| LR-024 | Count recalculates when dates change | AC4 | P1 | 1. Select Mon-Wed<br>2. Change To Date to Fri | Mon-Wed, then Mon-Fri | Count updates from 3.00 to 5.00 without a page reload | Medium | Positive |
| LR-025 | Employee in a different time zone from the server | AC3, AC4 | P1; browser and profile time zone UTC+10 and UTC-8 (two runs) | 1. Apply 10-12 Aug<br>2. Check the stored dates in My Leave and the API response | Mon 10 - Wed 12 Aug 2026 | Stored and displayed dates are exactly 10-12 Aug; count 3.00 in both zones (no -1/+1 day shift) | Medium | Edge |
| LR-026 | Submit without a comment | AC5, AC6 | P1 | 1. Fill type and dates<br>2. Leave the comment empty<br>3. Apply | - | Request saved; comment shown empty | High | Positive |
| LR-027 | Comment at the maximum length | AC5 | P1 | 1. Enter a 250-character comment<br>2. Apply | 250 chars | Saved; the full comment is visible to the employee and the manager | Medium | Boundary |
| LR-028 | Comment over the maximum length | AC5, AC10 | P1 | 1. Enter a 251-character comment<br>2. Apply | 251 chars | "Should not exceed 250 characters"; not saved | Medium | Boundary |
| LR-029 | Comment with special characters and script | AC5 | P1 | 1. Enter the comment<br>2. Apply<br>3. View it in My Leave and the manager list | "Öçé – 😀 <script>alert(1)</script> مرحبا" | Stored and displayed literally (escaped); no script execution; RTL text renders correctly | Medium | Edge |
| LR-030 | Submit a valid request | AC6 | P1 | 1. Select Annual Leave<br>2. Next Mon-Wed<br>3. Comment<br>4. Apply | Mon-Wed | "Successfully Saved" toast; the form resets; the request appears in My Leave | High | Positive |
| LR-031 | Double-click on Apply | AC6 | P1 | 1. Fill a valid request<br>2. Double-click Apply quickly | Mon-Wed | Exactly one request is created; balance reduced once | High | Edge |
| LR-032 | Insufficient balance | AC6, AC10 | P1; balance 2.00 | 1. Apply Mon-Wed (3 days) | 3 days vs 2 balance | Blocked with "Balance not sufficient" (or a confirmation, per the agreed rule); no request created when blocked | High | Negative |
| LR-033 | Request that uses exactly the remaining balance | AC6 | P1; balance 3.00 | 1. Apply Mon-Wed | 3 days vs 3 balance | Accepted; remaining balance 0.00 | High | Boundary |
| LR-034 | Overlap with an existing Pending or Scheduled request | AC6, AC10 | P1; Pending request Tue-Wed exists | 1. Apply Mon-Tue | Mon-Tue | Blocked with an overlap message listing the conflicting request; nothing saved | High | Negative |
| LR-035 | Overlap with a Rejected or Cancelled request is allowed | AC6 | P1; Cancelled request Tue-Wed exists | 1. Apply Tue-Wed again | Tue-Wed | Accepted | Medium | Positive |
| LR-036 | Server error or timeout on submit | AC6 | P1; API forced to fail (dev tools / proxy) | 1. Apply a valid request | - | User-friendly error; no partial request; retrying does not create duplicates | Low | Negative |
| LR-037 | New request has status Pending Approval | AC7 | P1 | 1. Submit a valid request<br>2. Open My Leave | Mon-Wed | Status "Pending Approval (3.00)" | High | Positive |
| LR-038 | Manager sees the pending request | AC7 | LR-037 done; Mgr M logged in | 1. Open Leave List or the pending approvals widget | - | Request of Emp A listed with dates, days, type and comment; Approve and Reject available | High | Positive |
| LR-039 | Employee sees own requests with correct details | AC8 | P1; requests in several statuses | 1. Open My Leave<br>2. Filter by the period and all statuses | - | Every own request listed once with dates, type, days, status and comment; no other employee's requests | High | Positive |
| LR-040 | Filter My Leave by status and dates | AC8 | As LR-039 | 1. Filter Status = Cancelled<br>2. Filter a date range | - | Every returned row matches the filters; Reset restores the defaults | Medium | Positive |
| LR-041 | Request details and history | AC8 | As LR-039 | 1. Open a request's details | - | Per-day breakdown, comments and status history (who, when) | Low | Positive |
| LR-042 | Cancel a Pending request | AC9 | P1; Pending request Mon-Wed (3 days) | 1. My Leave<br>2. Cancel the request<br>3. Confirm | - | Status "Cancelled"; balance back to 10.00; manager no longer sees it as pending | High | Positive |
| LR-043 | Cancel is not offered for Rejected or Taken requests | AC9 | P1; Rejected and Taken requests exist | 1. Open My Leave | - | No Cancel action for Rejected or Taken rows | Medium | Negative |
| LR-044 | Cancel a future Approved (Scheduled) request | AC9 | P1; Scheduled request next month | 1. Cancel the request | - | As agreed (BF-1): cancelled with the balance restored, or blocked with a message | Medium | Edge |
| LR-045 | Cancelling twice does not double-credit the balance | AC9 | Two browser tabs on the same Pending request | 1. Cancel in tab 1<br>2. Cancel in tab 2 | - | Second action refused or no-op; balance restored only once | Medium | Edge |
| LR-046 | Manager approves a request | Flow | Pending request Mon-Wed; Mgr M logged in | 1. Approve | - | Status "Scheduled" (or "Approved"); balance taken = 3.00 exactly once; employee notified | High | Positive |
| LR-047 | Manager rejects a request | Flow | Pending request Mon-Wed | 1. Reject with a comment | - | Status "Rejected"; 3.00 days released; employee notified with the comment | High | Positive |
| LR-048 | Manager cannot act on own leave or on non-subordinates | Flow | Mgr M has a pending request; Emp Z is not his subordinate | 1. Try to approve own leave<br>2. Try to approve Emp Z via the URL or API | - | Both refused (no button, and 403 on the API) | High | Negative |
| LR-049 | Approve and cancel at the same time | Flow, AC9 | Pending request; manager and employee in two browsers | 1. Employee cancels<br>2. Manager approves within seconds | - | One action wins consistently; the other gets a clear message; balance consistent | Low | Edge |
| LR-050 | Employee cannot view or cancel another employee's leave | AC8, AC9 | Emp A logged in; request ID of Emp B known | 1. Call view or cancel with Emp B's request ID (URL or API) | - | 403 or not found; no data leaked | High | Negative |
| LR-051 | Notifications on submit, approve, reject and cancel | Flow | Email/SMTP sandbox configured | 1. Run submit, approve, reject and cancel | - | Manager notified on submit; employee on approve and reject; manager on cancel; emails contain the dates, days and type | Medium | Positive |
| LR-052 | Mandatory markers, labels, tab order and keyboard use | UI | P1 | 1. Complete the form using only the keyboard | - | Logical tab order; date pickers usable by keyboard; Enter submits; errors shown next to the fields | Low | Positive |
| LR-053 | Responsive layout | UI | P1 | 1. Open on a 375px mobile viewport and on a tablet | - | No horizontal scroll; dropdowns and pickers usable; buttons reachable | Low | Positive |
| LR-054 | Accessibility basics | NFR | P1 | 1. Run axe or Lighthouse<br>2. Check with a screen reader | - | Fields have accessible names; errors are announced; contrast meets WCAG AA | Low | Positive |
| LR-055 | Performance of submit and list | NFR | Employee with 200+ historical requests | 1. Submit a request<br>2. Open My Leave | - | Submit < 2 s; list < 3 s at p95 on the test environment | Low | Positive |
| LR-056 | Security: input and session | NFR | P1 | 1. Tamper with the request payload (days, employee ID, status) via a proxy<br>2. Replay after logout | - | Server recalculates the days and ignores client values; cannot submit for others or set the status; session expired -> 401 | Medium | Negative |
| LR-057 | Request is not auto-approved and is routed to the right approver | AC7, Flow | Emp A has no supervisor assigned (second run: supervisor on leave) | 1. Submit a valid request<br>2. Check status and the approver inbox | Mon-Wed | Status stays Pending Approval (never Approved without a decision); request routed to HR Admin or a clear message per US-1 | Medium | Edge |

## Traceability matrix

| Requirement | Description | Test cases | Count |
|---|---|---|---|
| AC1 | Employee can open the Leave Request page | LR-001, LR-002, LR-003, LR-004 | 4 |
| AC2 | Employee can select a Leave Type | LR-004, LR-005, LR-006, LR-007 | 4 |
| AC3 | Employee can select From Date and To Date | LR-008, LR-009, LR-010, LR-011, LR-012, LR-013, LR-014, LR-015, LR-025 | 9 |
| AC4 | System calculates the number of leave days | LR-012, LR-014, LR-016, LR-017, LR-018, LR-019, LR-020, LR-021, LR-022, LR-023, LR-024, LR-025 | 12 |
| AC5 | Employee can add an optional comment | LR-026, LR-027, LR-028, LR-029 | 4 |
| AC6 | Employee can submit the request | LR-026, LR-030, LR-031, LR-032, LR-033, LR-034, LR-035, LR-036 | 8 |
| AC7 | Submitted request has Pending Approval status | LR-023, LR-037, LR-038, LR-057 | 4 |
| AC8 | Employee can view submitted requests | LR-039, LR-040, LR-041, LR-050 | 4 |
| AC9 | Employee can cancel a pending request | LR-042, LR-043, LR-044, LR-045, LR-049, LR-050 | 6 |
| AC10 | System prevents invalid leave requests | LR-007, LR-010, LR-011, LR-013, LR-015, LR-018, LR-028, LR-032, LR-034 | 9 |
| Flow | Business flow: manager Approve / Reject, notifications | LR-046, LR-047, LR-048, LR-049, LR-051, LR-057 | 6 |
| UI | UI / UX | LR-052, LR-053 | 2 |
| NFR | Non-functional: accessibility, performance, security | LR-054, LR-055, LR-056 | 3 |

Every acceptance criterion has at least one positive and one negative, boundary or edge case.
The highest-risk area, AC4 (day calculation), gets the most cases, because the reported defect (see
[03-bug-report.md](03-bug-report.md)) shows that the count shown to the user and the amount deducted can diverge.

## Execution notes
- Run the High priority cases first as the release smoke; Medium and Low cases follow as regression and exploratory work.
- For every case that changes data, record the **balance before and after**: most leave defects show up only in the balance.
- The automated suite in this repository covers the OrangeHRM equivalents of LR-011 (To Date before From Date) and LR-030 (save a request and find it again in the Leave List). Because the demo admin has no reliable balance, it records the leave with Assign Leave (status Scheduled) for a test employee, and asserts a count of 2.00 for two working days chosen around weekends and configured holidays.
