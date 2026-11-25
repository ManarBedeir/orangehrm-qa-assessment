# BUG: Leave request 10–12 Aug shows 3 days but deducts 4 days from the leave balance

| Field | Value |
|---|---|
| **ID** | LEAVE-BUG-001 (draft, to be raised in the tracker) |
| **Title** | Leave request for 10–12 Aug 2026 (Mon–Wed) displays 3.00 days but reduces the leave balance by 4.00 days |
| **Component** | Leave → Apply Leave / leave balance calculation |
| **Reported by / date** | QA, 25 Nov 2026 |
| **Environment** | QA / staging: `<URL>`. Browser Chrome 158 (Windows 11); reproduce also on Firefox to rule out the client. Server time zone: `<to confirm>`; user time zone: `<to confirm>` |
| **Build / version** | `<build number / commit>` (OrangeHRM OS 5.9-based build under test) |
| **Severity** | **Major (S2)** |
| **Priority** | **High (P1)**: fix before release |
| **Frequency** | `<x/5 attempts>`; reproduce on a clean employee 5 times before submitting (see notes) |
| **Status** | New |

### Date check (done before writing the report)
**10 August 2026 is a Monday.** 10–12 Aug 2026 is therefore **Mon, Tue, Wed**: three working days with no weekend
inside the range. In the default calendar there is no public holiday on these dates (to verify against the
environment's holiday list). So **3 days is the correct count, and the 4-day deduction is wrong.**

> If the report came from data in a different year, the analysis changes. In **2025**, 10 Aug was a **Sunday**,
> so 10–12 Aug 2025 = Sun–Tue = **2** working days, and *both* the displayed 3 and the deducted 4 would be wrong.
> In 2024, 10 Aug was a Saturday (also 2 working days). The reporter must confirm the year; this report assumes 2026.

### Severity justification
The balance is a payroll-relevant figure. Employees silently lose entitlement, and the error compounds with
every request. It does not crash the system, and there is a manual workaround (an HR adjustment of the
entitlement), so it is **Major**, not Critical. If it is shown to happen for all multi-day requests, raise it to Critical.

### Priority justification
It affects every employee using the core flow in this release. It undermines trust in the leave feature and
creates HR correction work and possible legal or payroll exposure. It must be fixed before go-live.

### Preconditions
1. Employee user **Emp A** (ESS role) with supervisor **Mgr M**.
2. Leave type **Annual Leave**, leave period 2026-01-01 – 2026-12-31.
3. Emp A entitlement: **10.00** days of Annual Leave; no other leave requests in the period (balance = 10.00).
4. Work week: Mon–Fri working, Sat–Sun non-working. No holidays configured between 10 and 12 Aug 2026.

### Steps to reproduce
1. Log in as Emp A.
2. Leave → **Entitlements / My Leave Entitlements**: note the balance for Annual Leave → **10.00** (screenshot 1).
3. Leave → **Apply**. Leave Type = *Annual Leave*. The page shows "Leave Balance 10.00 Day(s)".
4. From Date = **2026-08-10**, To Date = **2026-08-12**. Partial Days = *None* (full days).
5. Observe the calculated duration on the form, or after saving → **3.00** (screenshot 2).
6. Add the comment "Bug repro" and click **Apply**. The toast says "Successfully Saved".
7. Leave → **My Leave**: the request shows **Pending Approval (3.00)** (screenshot 3).
8. Open the balance again (step 2) → **6.00** remaining (screenshot 4).
9. (Extension) Log in as Mgr M, **approve** the request, and check Emp A's balance again (screenshot 5) to see
   whether the deduction changes at approval (for example 7 → 6 at approval = double deduction on status change).

### Test data
| Item | Value |
|---|---|
| Employee | Emp A (ESS), Employee Id `<id>` |
| Leave type | Annual Leave |
| Dates | From 2026-08-10 (Mon), To 2026-08-12 (Wed) |
| Duration | Full days, Partial Days = None |
| Balance before | 10.00 |

### Expected result
- The duration shown on the form and in My Leave = **3.00** days.
- The balance after submission (pending/reserved) = 10.00 − 3.00 = **7.00**.
- After approval the balance stays at **7.00**: the days are not deducted a second time.
- The per-day breakdown lists exactly 10, 11 and 12 Aug, each 1.00 day.

### Actual result
- The duration shown = **3.00** days (correct).
- The balance after submission = **6.00**: **4.00** days were deducted.
- `<Fill in: the balance after approval, and whether the request details list a 4th day, e.g. 9 or 13 Aug>`

### Attachments needed
1. Balance **before** the request (entitlement / usage report for Emp A).
2. The Apply Leave form with the dates and the calculated duration (3.00) visible.
3. My Leave row showing "Pending Approval (3.00)", and the request details page with the per-day breakdown.
4. Balance **after** submission (6.00), and after approval if step 9 was run.
5. The network log (HAR) of the Apply request and the balance API responses, showing the request payload dates and the server's day calculation.
6. Optional: the DB rows for the request (`ohrm_leave_request`, `ohrm_leave`, `ohrm_leave_entitlement` / `days_used`) if access is available.

### Notes for investigation
Possible causes to check, roughly from most to least likely:

1. **Inclusive vs exclusive date math:** the deduction uses `(to − from) + 1` with `to` stored as
   `2026-08-13 00:00` (end of day stored as the next midnight), or counts both boundaries twice: 13 − 10 + 1 = 4.
2. **Time-zone / date-boundary shift:** the dates are converted to UTC on save. For a user ahead of UTC, `2026-08-10 00:00`
   becomes `2026-08-09 22:00Z`, so the range is stored as 9–12 Aug. 9 Aug 2026 is a **Sunday**, which a weekend-aware
   calculation would ignore, *but* a deduction path that does not apply the work week would count it as a 4th day.
   Check the stored `date` values of the leave rows.
3. **Different calculation paths:** the UI duration uses the work week and holiday calendar, while the entitlement
   deduction uses plain calendar days, or a different work week (e.g. the default company work week instead of the
   employee's location work week).
4. **Holiday calendar mismatch:** a holiday exists only in one of the calendars (location vs company) or is a half day,
   so it is applied in one path and not the other.
5. **Half-day / duration setting:** a "Partial Days" or duration default (e.g. a stale 'Specify Time' value or the
   working-hours length) changes the conversion from hours to days for one of the days.
6. **Double deduction on status change:** the days are reserved at Pending and deducted again when the status changes
   (Pending → Scheduled), or a retry or double-submit created a second leave row for one date. Look for duplicate `ohrm_leave` rows.
7. **Rounding:** the duration is stored in hours (e.g. 8h/day) and converted with a different hours-per-day value (e.g. 6h)
   → 24h / 6 = 4 days.

Suggested isolating tests: a single-day request (1 vs 1?); a Tue–Thu request; a range touching a weekend (Fri–Mon);
the same request with the user time zone set to UTC; and the same request from Assign Leave (admin path). Comparing these
quickly separates the date-math, time-zone and work-week causes.

### Impact
- **Employees:** lose entitlement silently. Repeated requests make the error bigger, and some will be told they have
  "insufficient balance" when they don't.
- **HR / payroll:** wrong balances feed payroll (leave encashment, unpaid leave) and need manual corrections, which creates audit and compliance risk.
- **Product:** AC4 (system calculates leave days) is effectively not met, because the number shown is not the number applied.
- **Data:** existing requests may already have corrupted balances. A data-fix script and re-calculation may be needed after the code fix.

### Related
- Test cases: LR-016, LR-017, LR-019, **LR-023** (displayed days = deducted days), LR-025 (time zones), LR-046 (approval deduction).
- Open questions: Q AC4 and BF-6 in `01-questions-ambiguities-risks.md`.
