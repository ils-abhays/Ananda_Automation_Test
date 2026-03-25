# Guest Comments Test Cases

## Positive Test Cases

1. Verify `Guest Comments` tab opens successfully from the Reports module.
2. Verify the page title `Guest Comments` is displayed correctly.
3. Verify the data table is visible when records are available.
4. Verify date range filters are visible and enabled.
5. Verify the `Download Excel` button is visible and clickable.
6. Verify table headers are displayed correctly:
   `Guest Name`, `Check Out`, `What are the highlights of your Ananda experience`, `What could we do to improve your wellness experience`, `Therapists Recognised`, `Service Staff Recognised`.
7. Verify guest comments data loads correctly for a valid default date range.
8. Verify `Guest Name` values are displayed properly.
9. Verify `Check Out` date is shown in valid date format.
10. Verify `Highlights of your Ananda experience` column shows saved data correctly.
11. Verify `Improvement of wellness experience` column shows saved data correctly.
12. Verify `Therapists Recognised` column displays saved therapist names correctly.
13. Verify `Service Staff Recognised` column displays saved staff names correctly.
14. Verify the table supports records where some text fields contain `-` or blank values without breaking layout.
15. Verify changing the date range refreshes the data correctly.
16. Verify filtered results show only records within the selected date range.
17. Verify the `Download Excel` action works for available records.
18. Verify long comments are displayed without UI overlap or broken alignment.
19. Verify special characters in comment text display correctly.
20. Verify page remains stable after refresh.

## Negative Test Cases

1. Verify page handles no data scenario gracefully for a date range with no records.
2. Verify invalid date range where `From Date > To Date` does not crash the page.
3. Verify future date range with no records shows safe empty state.
4. Verify blank comment values are handled without UI break.
5. Verify blank `Therapists Recognised` value is handled correctly.
6. Verify blank `Service Staff Recognised` value is handled correctly.
7. Verify very long comment text does not overflow outside the table.
8. Verify special characters and symbols in comments do not break rendering.
9. Verify HTML-like text in comments is shown as text and not executed.
10. Verify repeated clicks on `Download Excel` do not freeze the page.
11. Verify refreshing after applying filters does not leave the page in unstable state.
12. Verify page remains stable when scrolling through long content.
13. Verify one invalid or partial record does not block the entire table from loading.
14. Verify null or missing guest name is handled safely.
15. Verify corrupted or unexpected date format in `Check Out` does not break the page.
16. Verify network delay/loading delay does not keep the screen stuck forever.
17. Verify unauthorized user does not get access to the tab if permissions are restricted.
18. Verify session timeout while on Guest Comments page redirects safely or shows proper message.

