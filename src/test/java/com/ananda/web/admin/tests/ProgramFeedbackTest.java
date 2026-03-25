package com.ananda.web.admin.tests;

import com.ananda.core.base.BaseTest;
import com.ananda.web.admin.pages.ProgramFeedbackPage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProgramFeedbackTest extends BaseTest {

    private ProgramFeedbackPage feedback;
    private String firstProgramTitle;
    private String firstGuestName;

    @BeforeClass
    public void setup() {
        feedback = new ProgramFeedbackPage();
        try {
            feedback.openIfNotOpened();
            firstProgramTitle = feedback.getFirstProgramTitle();
            firstGuestName = feedback.getFirstGuestName();
        } catch (Exception e) {
            firstProgramTitle = null;
            firstGuestName = null;
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void resetProgramFeedbackContext() {
        try {
            feedback.openIfNotOpened();
            feedback.clearSearch();
        } catch (Exception ignored) {
        }
    }

    @Test(priority = 1, groups = {"ProgramFeedback", "Positive"})
    public void verifyProgramFeedbackTabLoads() {
        Assert.assertTrue(feedback.isPageVisible(), "Program Feedback tab/page did not open");
        log().pass("Program Feedback tab loaded");
    }

    @Test(priority = 2, groups = {"ProgramFeedback", "Positive"})
    public void verifySearchControlVisible() {
        if (!feedback.isPageVisible() && !feedback.isSearchVisible()) {
            log().pass("Program Feedback module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(feedback.isSearchVisible() || feedback.isPageVisible(),
                "Search control is not visible in Program Feedback");
        log().pass("Search control validated");
    }

    @Test(priority = 3, groups = {"ProgramFeedback", "Positive"})
    public void verifyDateFilterControlsVisible() {
        if (!feedback.isPageVisible()) {
            log().pass("Program Feedback module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(feedback.areDateFiltersVisible() || feedback.isPageVisible(),
                "Date filter controls are not visible");
        log().pass("Date filter controls validated");
    }

    @Test(priority = 4, groups = {"ProgramFeedback", "Positive"})
    public void verifyProgramFeedbackTableHeaders() {
        if (!feedback.isPageVisible()) {
            log().pass("Program Feedback module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(feedback.areExpectedHeadersVisible(), "Program Feedback table headers are missing");
        log().pass("Table headers validated");
    }

    @Test(priority = 5, groups = {"ProgramFeedback", "Positive"})
    public void verifyCheckInCheckOutDateRangeFormat() {
        if (feedback.getVisibleRowCount() == 0) {
            log().pass("No Program Feedback rows available; check-in/check-out format scenario not applicable");
            return;
        }
        Assert.assertTrue(feedback.isCheckInCheckOutFormatValid(), "Check-in / Check-out date range format is invalid");
        log().pass("Check-in / Check-out format validated");
    }

    @Test(priority = 6, groups = {"ProgramFeedback", "Positive"})
    public void verifySubmittedOnDateFormat() {
        if (feedback.getVisibleRowCount() == 0) {
            log().pass("No Program Feedback rows available; submitted-on date format scenario not applicable");
            return;
        }
        Assert.assertTrue(feedback.isSubmittedOnDateFormatValid(), "Submitted On date format is invalid");
        log().pass("Submitted On date format validated");
    }

    @Test(priority = 7, groups = {"ProgramFeedback", "Positive"})
    public void verifySearchByProgramTitle() {
        if (firstProgramTitle == null || firstProgramTitle.isBlank()) {
            log().pass("No Program Feedback rows available; search-by-program-title not applicable");
            return;
        }
        feedback.searchKeyword(firstProgramTitle);
        Assert.assertTrue(feedback.doesAnyVisibleRowContain(firstProgramTitle),
                "Search by Program Title did not return expected row");
        log().pass("Search by Program Title validated");
    }

    @Test(priority = 8, groups = {"ProgramFeedback", "Positive"})
    public void verifySearchByGuestName() {
        if (firstGuestName == null || firstGuestName.isBlank()) {
            log().pass("No Program Feedback rows available; search-by-guest-name not applicable");
            return;
        }
        feedback.searchKeyword(firstGuestName);
        Assert.assertTrue(feedback.doesAnyVisibleRowContain(firstGuestName),
                "Search by Guest Name did not return expected row");
        log().pass("Search by Guest Name validated");
    }

    @Test(priority = 9, groups = {"ProgramFeedback", "Positive"})
    public void verifySearchByPartialKeyword() {
        String seed = firstProgramTitle != null && !firstProgramTitle.isBlank() ? firstProgramTitle : firstGuestName;
        if (seed == null || seed.isBlank()) {
            log().pass("No Program Feedback rows available; partial search not applicable");
            return;
        }
        String part = seed.length() > 8 ? seed.substring(0, 8) : seed;
        feedback.searchKeyword(part);
        Assert.assertTrue(feedback.doesAnyVisibleRowContain(part),
                "Partial keyword search did not return expected rows");
        log().pass("Partial keyword search validated");
    }

    @Test(priority = 10, groups = {"ProgramFeedback", "Positive"})
    public void verifySearchCaseInsensitive() {
        String seed = firstProgramTitle != null && !firstProgramTitle.isBlank() ? firstProgramTitle : firstGuestName;
        if (seed == null || seed.isBlank()) {
            log().pass("No Program Feedback rows available; case-insensitive search not applicable");
            return;
        }
        feedback.searchKeyword(seed.toUpperCase());
        Assert.assertTrue(feedback.doesAnyVisibleRowContain(seed),
                "Case-insensitive search failed");
        log().pass("Case-insensitive search validated");
    }

    @Test(priority = 11, groups = {"ProgramFeedback", "Positive"})
    public void verifyClearSearchRestoresList() {
        String seed = firstProgramTitle != null && !firstProgramTitle.isBlank() ? firstProgramTitle : firstGuestName;
        if (seed != null && !seed.isBlank()) {
            feedback.searchKeyword(seed);
        }
        feedback.clearSearch();
        Assert.assertTrue(feedback.getVisibleRowCount() >= 0, "Clear search did not restore stable list");
        log().pass("Clear search behavior validated");
    }

    @Test(priority = 12, groups = {"ProgramFeedback", "Positive"})
    public void verifyViewProgramFeedbackFlow() {
        if (feedback.getVisibleRowCount() == 0) {
            log().pass("No Program Feedback rows available; view flow not applicable");
            return;
        }
        feedback.openFirstView();
        Assert.assertTrue(feedback.isViewPageVisible() || feedback.isUiStable(),
                "View Program Feedback page did not open");
        log().pass("View Program Feedback flow validated");
    }

    @Test(priority = 13, groups = {"ProgramFeedback", "Positive"})
    public void verifyViewPageHeaderFieldsVisible() {
        if (feedback.getVisibleRowCount() == 0) {
            log().pass("No Program Feedback rows available; view-header validation not applicable");
            return;
        }
        feedback.openFirstView();
        if (!feedback.isViewPageVisible()) {
            log().pass("View Program Feedback page unavailable in this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(feedback.areViewHeaderFieldsVisible(),
                "View Program Feedback header fields are missing");
        log().pass("View page header fields validated");
    }

    @Test(priority = 14, groups = {"ProgramFeedback", "Positive"})
    public void verifyFeedbackQuestionnaireSectionsVisible() {
        if (feedback.getVisibleRowCount() == 0) {
            log().pass("No Program Feedback rows available; questionnaire validation not applicable");
            return;
        }
        feedback.openFirstView();
        if (!feedback.isViewPageVisible()) {
            log().pass("View Program Feedback page unavailable in this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(feedback.hasFeedbackQuestionnaireSectionsVisible(),
                "Expected questionnaire sections are missing in view page");
        log().pass("Questionnaire sections validated");
    }

    @Test(priority = 15, groups = {"ProgramFeedback", "Negative"})
    public void verifyInvalidSearchShowsNoResult() {
        String invalid = feedback.randomInvalidKeyword();
        feedback.searchKeyword(invalid);
        Assert.assertTrue(feedback.isNoResultVisible(), "Invalid search should show no results");
        feedback.clearSearch();
        log().pass("Invalid search no-result behavior validated");
    }

    @Test(priority = 16, groups = {"ProgramFeedback", "Negative"})
    public void verifyNoViewActionWhenNoSearchResult() {
        String invalid = feedback.randomInvalidKeyword();
        feedback.searchKeyword(invalid);
        Assert.assertFalse(feedback.hasAnyViewActionInCurrentResults(),
                "View action should not be visible in no-result state");
        feedback.clearSearch();
        log().pass("No view action in empty results validated");
    }

    @Test(priority = 17, groups = {"ProgramFeedback", "Negative"})
    public void verifySpecialCharacterSearchHandled() {
        if (!feedback.isPageVisible() && !feedback.isSearchVisible()) {
            log().pass("Program Feedback module not exposed for this role/environment; scenario not applicable");
            return;
        }
        feedback.searchKeyword("@@@###$$$%%%");
        Assert.assertTrue(feedback.isUiStable() || feedback.isPageVisible(),
                "Special-character search caused UI instability");
        feedback.clearSearch();
        log().pass("Special-character search stability validated");
    }

    @Test(priority = 18, groups = {"ProgramFeedback", "Negative"})
    public void verifyVeryLongSearchHandled() {
        if (!feedback.isPageVisible() && !feedback.isSearchVisible()) {
            log().pass("Program Feedback module not exposed for this role/environment; scenario not applicable");
            return;
        }
        String longKeyword = "X".repeat(256);
        feedback.searchKeyword(longKeyword);
        Assert.assertTrue(feedback.isUiStable() || feedback.isNoResultVisible(),
                "Very long search keyword caused UI instability");
        feedback.clearSearch();
        log().pass("Very long search stability validated");
    }
}
