# Consultant Ratings Test Cases

## Positive Test Cases

1. Verify `Consultant Ratings` tab opens successfully from Reports.
2. Verify the page title `Consultant Ratings` is displayed.
3. Verify date range filters are visible and enabled.
4. Verify the `Download Excel` button is visible and clickable.
5. Verify consultant ratings table loads correctly for valid date range.
6. Verify table headers are displayed correctly:
   `Consultant / Therapist`, `Total Session`, `Total Rating`, `D`, `%`, `N`, `%`, `P`, `%`, `QMS Score`.
7. Verify consultant or therapist name is displayed correctly.
8. Verify `Total Session` column shows numeric value.
9. Verify `Total Rating` column shows numeric value.
10. Verify `D` count column shows numeric value.
11. Verify `N` count column shows numeric value.
12. Verify `P` count column shows numeric value.
13. Verify percentage columns display valid percentage values.
14. Verify `QMS Score` column displays valid numeric value.
15. Verify changing the date range updates the records correctly.
16. Verify records shown belong only to the selected date range.
17. Verify the `Download Excel` action works for available consultant rating data.
18. Verify page stays stable after refresh.
19. Verify zero-value rows are displayed correctly without UI issues.
20. Verify decimal and negative score values are shown properly if supported by business logic.

## Negative Test Cases

1. Verify page handles no data condition for date range with no consultant rating records.
2. Verify invalid date range where `From Date > To Date` is handled safely.
3. Verify future date range shows empty state without page failure.
4. Verify percentage columns do not display invalid text values.
5. Verify `QMS Score` does not break layout when value is negative.
6. Verify `QMS Score` does not break layout when value is decimal.
7. Verify very large numeric values do not overlap table columns.
8. Verify null or blank consultant/therapist name is handled safely.
9. Verify rows with zero sessions do not cause divide-by-zero or percentage display issue.
10. Verify repeated clicks on `Download Excel` do not hang the page.
11. Verify refresh after applying date filters keeps UI stable.
12. Verify the table remains stable during long loading or delayed API response.
13. Verify broken or partial data in one row does not stop other rows from loading.
14. Verify unauthorized user cannot access Consultant Ratings if permission is missing.
15. Verify session timeout on this page is handled safely.
16. Verify unexpected non-numeric values in metric fields do not crash the screen.
17. Verify the page does not freeze when no consultant records exist.
18. Verify invalid backend values in `D`, `N`, `P`, or `%` columns are handled safely in UI.

