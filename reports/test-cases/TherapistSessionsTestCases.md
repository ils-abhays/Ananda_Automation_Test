# Therapist Sessions Test Cases

## Positive Test Cases

1. Verify `Therapist Sessions` tab opens successfully from Reports.
2. Verify page title `Therapist Sessions` is displayed correctly.
3. Verify search box is visible and enabled.
4. Verify therapist dropdown filter is visible and enabled.
5. Verify date filter is visible and editable.
6. Verify `Download Excel` button is visible and clickable.
7. Verify therapist sessions table loads successfully for default filters.
8. Verify table headers are displayed correctly:
   `Guest Name`, `Gender`, `Guest Room Number`, `Doctor Name`, `Therapist Assigned`, `Session Name`, `Therapy Room Name`, `Session Time`.
9. Verify `Guest Name` values display properly.
10. Verify `Gender` values display correctly.
11. Verify `Guest Room Number` shows saved value or `-` safely.
12. Verify `Doctor Name` shows saved value or `-` safely.
13. Verify `Therapist Assigned` shows correct therapist name.
14. Verify `Session Name` displays properly even for long names.
15. Verify `Therapy Room Name` displays properly.
16. Verify `Session Time` displays in valid date-time format.
17. Verify searching by guest name returns matching records.
18. Verify searching by partial guest name returns relevant records.
19. Verify search is case-insensitive.
20. Verify therapist dropdown filter returns records only for selected therapist.
21. Verify selecting `All` in therapist dropdown shows all matching records.
22. Verify changing date filter refreshes session records correctly.
23. Verify combined filtering with therapist + search keyword works correctly.
24. Verify `Download Excel` works with current applied filters.
25. Verify page remains stable after refresh.

## Negative Test Cases

1. Verify search with invalid keyword shows no records or safe empty state.
2. Verify special characters in search do not break the page.
3. Verify very long search keyword is handled safely.
4. Verify no data for selected therapist shows safe empty state.
5. Verify invalid date with no records does not break page.
6. Verify future date filter shows no records without error.
7. Verify blank `Doctor Name` is handled safely.
8. Verify blank `Therapist Assigned` is handled safely.
9. Verify blank `Therapy Room Name` is handled safely.
10. Verify missing `Guest Room Number` does not break row rendering.
11. Verify invalid or unexpected `Session Time` format is handled safely.
12. Verify repeated dropdown switching does not cause UI instability.
13. Verify repeated search updates do not freeze or hang the page.
14. Verify clearing search restores stable results list.
15. Verify repeated `Download Excel` clicks do not break UI.
16. Verify search with spaces only is handled safely.
17. Verify one malformed row does not stop the full table from loading.
18. Verify unauthorized user cannot access Therapist Sessions tab if permission is missing.
19. Verify session timeout on the page is handled properly.
20. Verify continuous loader is not shown forever after applying filters.
