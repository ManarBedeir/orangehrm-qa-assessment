# Employee Leave Request: Questions, Ambiguities, Assumptions and Risks

**Feature:** Employee Leave Request
**Input reviewed:** user story, 10 acceptance criteria (AC1–AC10), business flow diagram
**Purpose:** points I would raise in refinement *before* test design is final. Each item has the
question, why it matters for testing, and the assumption I will test against if no answer arrives in time.

Priority: **H** = blocks test design or is likely to hide a defect. **M** = affects coverage. **L** = clarification.

---

## 1. User story

| # | Question / observation | Why it matters | Working assumption | Pri |
|---|---|---|---|---|
| US-1 | "Submit my planned absence for **manager** approval": who is the approver? The direct supervisor in the org chart, any manager, or HR? What if the employee has no supervisor, or several? | Routing, permissions and the Approve/Reject tests all depend on it. | The direct supervisor (reports-to) approves; HR Admin can also act. An employee without a supervisor cannot submit, or the request goes to HR Admin (**needs a decision**). | H |
| US-2 | "Planned" absence: is backdated (unplanned or sick) leave in scope? | Decides whether past dates are a negative or a positive case. | Past dates are allowed only within a configurable window (e.g. the current leave period); otherwise they are rejected. | H |
| US-3 | Which roles can use the page: ESS employee only, or also admins and supervisors on behalf of others? | Role and permission test matrix. | ESS employees request for themselves; "assign leave on behalf" is a separate feature, out of scope. | M |

## 2. Acceptance criteria, one by one

| AC | Question / ambiguity | Working assumption | Pri |
|---|---|---|---|
| **AC1** Employee can open the Leave Request page | From where (menu, dashboard shortcut, direct URL)? Is the page hidden from users without the leave module or without an entitlement? | The menu entry is visible to all ESS users. With no leave types or balance, the page shows an informative empty state instead of an empty form. | M |
| **AC2** Employee can select a Leave Type | Which types are listed: all active types, or only types with an entitlement or balance? Are inactive or deleted types hidden? Is there a default? Is the balance shown after selection? | Only active types the employee is eligible for. The current balance is displayed after selection. No default (forces a conscious choice). | H |
| **AC3** Employee can select From Date and To Date | Date format and locale? Can From = To (single day)? Max range length? Past dates (see US-2)? Dates outside the current leave period, or spanning two leave periods or years? Is To Date auto-filled from From Date? | Single-day requests allowed. To Date defaults to From Date. Cross-period requests are either split per period or rejected (**needs a decision**: this is a classic balance bug). | H |
| **AC4** System calculates the number of leave days | **The biggest gap.** Calendar days or working days? Are weekends excluded (and which days are the weekend: per location or work schedule)? Are public holidays excluded, and from which calendar (company, country, employee location)? Half-day holidays? Half days and hours (morning/afternoon, partial days)? Rounding? Recalculated live when dates change? Does the calculation use the server or the user time zone? | Working days only: weekends and the employee's holiday calendar are excluded, half-day leave counts as 0.5, and the count updates immediately when dates or duration change. **The displayed count must equal the amount deducted** (see the bug report). | H |
| **AC5** Employee can add an optional comment | Max length? Allowed characters (emoji, RTL text, HTML)? Is it visible to the manager, and can it be edited after submission? Is a comment mandatory for some leave types (e.g. sick)? | 250 characters (OrangeHRM default) with a visible counter or validation. Plain text, HTML escaped. Visible to the approver. Read-only after submit. | M |
| **AC6** Employee can submit the request | What exactly is validated on submit (see AC10)? Is there a confirmation step? What happens on double-click or a slow network: can duplicates be created? Is insufficient balance a hard stop or a warning? | A single request is created even on double submit. Insufficient balance is a hard stop unless the type allows a negative balance (**needs a decision**). | H |
| **AC7** Submitted request has Pending Approval status | Is the balance reduced or reserved at Pending, or only at Approved? Does the "available" balance shown to the employee include pending days? | Pending days are *reserved*: the available balance shown = entitlement − taken − scheduled − pending. Nothing is deducted twice when the request is approved. | H |
| **AC8** Employee can view submitted requests | Which list: own requests only? Default filters (status, date range)? Sorting, paging? What details are shown (dates, days, type, status, comments, approver)? History of status changes? | "My Leave" lists the employee's own requests in all statuses, newest first, with dates, day count, type, status and comments. | M |
| **AC9** Employee can cancel a pending request | Only *Pending*, or also *Approved / Scheduled* requests whose dates are in the future? Partial cancellation of a multi-day request? Is a reason required? Does the manager need to approve the cancellation? Is the balance restored immediately? | The employee can cancel Pending (and future Scheduled) requests, the reserved or deducted days are restored immediately, and the request shows status Cancelled. | H |
| **AC10** System prevents invalid leave requests | "Invalid" is not defined. It needs a list: missing type or dates, To < From, zero working days (weekend or holiday only), insufficient balance, overlap with an existing request, past dates outside the window, a range over the maximum length, dates outside any leave period, comment too long. What message is shown for each? | I will test every rule above and expect a specific inline message per field, with no request created. | H |

## 3. Business flow vs. acceptance criteria (gaps)

| # | Gap | Impact | Pri |
|---|---|---|---|
| BF-1 | **The flow has no Cancel path**, although AC9 requires one. There is also no *Cancelled* end state. | Cancel is untested from a flow and state perspective: valid transitions, and who can cancel in which state. | H |
| BF-2 | The flow ends at Approved / Rejected. Missing: *Taken* (dates passed), cancellation of approved leave, the manager editing a request, and the request expiring if the manager never decides. | State model incomplete; no expected behaviour for stale Pending requests. | M |
| BF-3 | Validation happens only once, before Submit. Is it repeated at approval time? (Example: the balance was enough at submit but not at approve, because another request was approved first.) | Race and concurrency defects in the balance. | H |
| BF-4 | "Calculate Leave Days" sits *before* validation, but there is no step showing the balance or the result to the user. | Unclear whether the user sees the calculated days and the remaining balance before submitting. | M |
| BF-5 | No notifications in the flow (to the manager on submit; to the employee on approve, reject or cancel). | Missing requirement, or out of scope? It needs confirming. | M |
| BF-6 | The Approve and Reject branches do not say what happens to the balance (deduct on approve; release on reject). | Core balance integrity. | H |
| BF-7 | No error path for a system failure on submit (timeout, server error). | User may resubmit and create duplicates. | L |

## 4. Specific topics to confirm

- **Weekends and holidays:** which work week applies (per location)? Are holidays inside the range excluded? What if the whole range is non-working days ("no working days selected": block, or allow 0 days)?
- **Half days and partial days:** are half days (AM/PM) or hours supported? How are partial days on multi-day requests (first/last day) counted?
- **Overlapping requests:** block any overlap with Pending, Scheduled or Approved leave? Is overlap allowed with Rejected or Cancelled requests? (It should be.) Two half days on the same date (AM + PM)?
- **Insufficient balance:** a hard stop, or a warning plus manager discretion? Do some types allow a negative balance (e.g. unpaid leave)?
- **Past dates:** allowed? How far back? Do past-dated requests go straight to *Taken* after approval?
- **Cross-year and cross-period ranges:** for example 30 Dec – 3 Jan. Which period's balance is used? Is it split per period? What if next year's period or entitlement does not exist yet?
- **Comment limit:** maximum length and the behaviour at the limit (truncate, block, counter). Encoding of special characters.
- **Who can cancel, and when:** the employee only while Pending? The manager or HR at any time? Can past (Taken) leave be cancelled? Partial cancel?
- **Balance on reject or cancel:** the days are restored exactly once, with no double credit when a cancel follows a reject or the request is cancelled twice.
- **Notifications:** email or in-app, to whom, and for which transitions; the content must include the dates and day count.
- **Time zones:** the employee and the manager in different time zones; the server in UTC. A date chosen as 10 Aug must be stored as 10 Aug (no −1 day shift); "today" for the past-date rule is in which time zone?
- **Leave period:** calendar year or fiscal year; what happens on the period boundary; carry-forward rules.
- **Audit:** is every status change stored with who did it and when?
- **Concurrency:** the employee cancels while the manager approves at the same moment.

## 5. Assumptions used for test design (until confirmed)

1. Leave days = working days in the range; weekends (Sat/Sun) and the employee's public holidays are excluded.
2. Displayed days = days reserved at Pending = days deducted at Approved.
3. Reject and Cancel release the reserved or deducted days immediately and exactly once.
4. The employee can cancel Pending and future Scheduled requests; status becomes *Cancelled*.
5. Overlap with active (Pending, Scheduled, Approved, Taken) leave is blocked; overlap with Rejected or Cancelled leave is allowed.
6. Insufficient balance blocks submission (unless the leave type allows a negative balance).
7. The comment is optional, up to 250 characters, plain text.
8. Dates are stored as calendar dates without a time component (no time-zone conversion).

## 6. Risks

| Risk | Likelihood | Impact | Mitigation in testing |
|---|---|---|---|
| Day count displayed ≠ days deducted (calendar vs working days, holidays, inclusive or exclusive end date) | High | High: payroll and trust | Boundary tests around weekends, holidays and single-day requests; compare the balance before and after every transition. |
| Balance double-deducted or double-restored on status changes | Medium | High | Check the balance after submit, approve, reject, cancel and cancel-after-approve. |
| Time-zone or date-boundary shift (stored date −1 day) | Medium | High | Run with the browser and user in UTC+ and UTC− zones; check the stored dates via the list and the API. |
| Cross-year requests break the balance per period | Medium | High | Dedicated cross-period cases. |
| Missing cancel and notification requirements discovered late | High | Medium | Raise BF-1 and BF-5 in refinement now. |
| Permission leaks (an employee viewing or cancelling another employee's leave via the URL or API) | Low | High | Negative role tests with direct URLs and IDs. |
| Concurrency (approve vs cancel; two approvals using the same balance) | Low | Medium | Exploratory session with two browsers. |
