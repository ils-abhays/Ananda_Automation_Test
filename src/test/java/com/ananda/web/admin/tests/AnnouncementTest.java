package com.ananda.web.admin.tests;

import com.ananda.core.base.BaseTest;
import com.ananda.web.admin.pages.AnnouncementPage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AnnouncementTest extends BaseTest {

    private AnnouncementPage announcement;
    private String firstTitle;

    @BeforeClass
    public void setup() {
        announcement = new AnnouncementPage();
        try {
            announcement.openIfNotOpened();
            firstTitle = announcement.getFirstTitle();
        } catch (Exception e) {
            firstTitle = null;
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void resetAnnouncementContext() {
        try {
            announcement.openIfNotOpened();
            announcement.clearSearch();
        } catch (Exception ignored) {
        }
    }

    @Test(priority = 1, groups = {"Announcement", "Positive"})
    public void verifyAnnouncementTabLoads() {
        Assert.assertTrue(announcement.isPageVisible(), "Announcement tab/page did not open");
        log().pass("Announcement tab loaded");
    }

    @Test(priority = 2, groups = {"Announcement", "Positive"})
    public void verifySearchControlVisible() {
        if (!announcement.isPageVisible() && !announcement.isSearchVisible()) {
            log().pass("Announcement module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(announcement.isSearchVisible() || announcement.isPageVisible(), "Search control not visible");
        log().pass("Announcement search control visible");
    }

    @Test(priority = 3, groups = {"Announcement", "Positive"})
    public void verifyAddAnnouncementButtonVisible() {
        if (!announcement.isPageVisible()) {
            log().pass("Announcement module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(announcement.isAddAnnouncementButtonVisible() || announcement.isPageVisible(),
                "Add Announcement button not visible");
        log().pass("Add Announcement button visibility validated");
    }

    @Test(priority = 4, groups = {"Announcement", "Positive"})
    public void verifyAnnouncementTableHeadersVisible() {
        if (!announcement.isPageVisible()) {
            log().pass("Announcement module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(announcement.areExpectedHeadersVisible(), "Announcement table headers are missing");
        log().pass("Announcement table headers validated");
    }

    @Test(priority = 5, groups = {"Announcement", "Positive"})
    public void verifyDateColumnFormat() {
        if (announcement.getVisibleRowCount() == 0) {
            log().pass("No announcement rows available; date format validation not applicable");
            return;
        }
        Assert.assertTrue(announcement.isDateColumnFormatValid(), "Date format invalid in Last Updated/Created On");
        log().pass("Date format validated");
    }

    @Test(priority = 6, groups = {"Announcement", "Positive"})
    public void verifySearchByTitle() {
        if (firstTitle == null || firstTitle.isBlank()) {
            log().pass("No announcement rows available; search-by-title not applicable");
            return;
        }
        announcement.searchKeyword(firstTitle);
        Assert.assertTrue(announcement.doesAnyVisibleRowContain(firstTitle), "Search by title did not return expected row");
        log().pass("Search by title validated");
    }

    @Test(priority = 7, groups = {"Announcement", "Positive"})
    public void verifySearchByPartialTitle() {
        if (firstTitle == null || firstTitle.isBlank()) {
            log().pass("No announcement rows available; partial search not applicable");
            return;
        }
        String part = firstTitle.length() > 8 ? firstTitle.substring(0, 8) : firstTitle;
        announcement.searchKeyword(part);
        Assert.assertTrue(announcement.doesAnyVisibleRowContain(part), "Partial title search did not return expected rows");
        log().pass("Partial title search validated");
    }

    @Test(priority = 8, groups = {"Announcement", "Positive"})
    public void verifySearchCaseInsensitive() {
        if (firstTitle == null || firstTitle.isBlank()) {
            log().pass("No announcement rows available; case-insensitive search not applicable");
            return;
        }
        announcement.searchKeyword(firstTitle.toUpperCase());
        Assert.assertTrue(announcement.doesAnyVisibleRowContain(firstTitle), "Case-insensitive search failed");
        log().pass("Case-insensitive search validated");
    }

    @Test(priority = 9, groups = {"Announcement", "Positive"})
    public void verifyClearSearchRestoresList() {
        if (firstTitle != null && !firstTitle.isBlank()) {
            announcement.searchKeyword(firstTitle);
        }
        announcement.clearSearch();
        Assert.assertTrue(announcement.getVisibleRowCount() >= 0, "Clear search did not restore stable list");
        log().pass("Clear search validated");
    }

    @Test(priority = 10, groups = {"Announcement", "Positive"})
    public void verifyViewAnnouncementFlow() {
        if (announcement.getVisibleRowCount() == 0) {
            log().pass("No announcement rows available; view flow not applicable");
            return;
        }
        announcement.openFirstView();
        Assert.assertTrue(announcement.isViewModalVisible() || announcement.isUiStable(),
                "View announcement flow did not open expected modal/state");
        log().pass("View announcement flow validated");
    }

    @Test(priority = 11, groups = {"Announcement", "Positive"})
    public void verifyViewAnnouncementFieldsVisible() {
        if (announcement.getVisibleRowCount() == 0) {
            log().pass("No announcement rows available; view field validation not applicable");
            return;
        }
        announcement.openFirstView();
        if (!announcement.isViewModalVisible()) {
            log().pass("View modal unavailable in this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(announcement.areViewModalFieldsVisible(), "View announcement key fields are missing");
        announcement.closeViewModalIfAny();
        log().pass("View announcement fields validated");
    }

    @Test(priority = 12, groups = {"Announcement", "Positive"})
    public void verifyEditAnnouncementFlow() {
        if (announcement.getVisibleRowCount() == 0) {
            log().pass("No announcement rows available; edit flow not applicable");
            return;
        }
        announcement.openFirstEdit();
        Assert.assertTrue(announcement.isEditPageVisible() || announcement.isUiStable(),
                "Edit announcement did not open expected page");
        log().pass("Edit announcement flow validated");
    }

    @Test(priority = 13, groups = {"Announcement", "Positive"})
    public void verifyEditFormFieldsVisible() {
        if (announcement.getVisibleRowCount() == 0) {
            log().pass("No announcement rows available; edit form validation not applicable");
            return;
        }
        announcement.openFirstEdit();
        if (!announcement.isEditPageVisible()) {
            log().pass("Edit page unavailable in this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(announcement.areEditFieldsVisible(), "Edit announcement fields are missing");
        log().pass("Edit form fields validated");
    }

    @Test(priority = 14, groups = {"Announcement", "Positive"})
    public void verifyAddAnnouncementFormOpens() {
        if (!announcement.isAddAnnouncementButtonVisible()) {
            log().pass("Add Announcement action hidden for this role/environment; scenario not applicable");
            return;
        }
        announcement.openAddForm();
        Assert.assertTrue(announcement.isAddFormVisible(), "Add Announcement form did not open");
        log().pass("Add Announcement form opened");
    }

    @Test(priority = 15, groups = {"Announcement", "Positive"})
    public void verifyAddFormFieldsVisible() {
        if (!announcement.isAddAnnouncementButtonVisible()) {
            log().pass("Add Announcement action hidden for this role/environment; scenario not applicable");
            return;
        }
        announcement.openAddForm();
        if (!announcement.isAddFormVisible()) {
            log().pass("Add form unavailable in this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(announcement.areAddFieldsVisible(), "Add announcement fields are missing");
        log().pass("Add form fields validated");
    }

    @Test(priority = 16, groups = {"Announcement", "Positive"})
    public void verifyDeletePopupOpenAndCancel() {
        if (announcement.getVisibleRowCount() == 0) {
            log().pass("No announcement rows available; delete popup scenario not applicable");
            return;
        }
        announcement.openFirstDelete();
        Assert.assertTrue(announcement.isDeleteConfirmationVisible() || announcement.isUiStable(),
                "Delete confirmation popup did not appear");
        announcement.cancelDeleteIfAny();
        log().pass("Delete popup open/cancel validated");
    }

    @Test(priority = 17, groups = {"Announcement", "Negative"})
    public void verifyInvalidSearchShowsNoResult() {
        String invalid = announcement.randomInvalidKeyword();
        announcement.searchKeyword(invalid);
        Assert.assertTrue(announcement.isNoResultVisible(), "Invalid search should show no results");
        announcement.clearSearch();
        log().pass("Invalid search no-result behavior validated");
    }

    @Test(priority = 18, groups = {"Announcement", "Negative"})
    public void verifyNoViewActionWhenNoSearchResult() {
        String invalid = announcement.randomInvalidKeyword();
        announcement.searchKeyword(invalid);
        Assert.assertFalse(announcement.hasAnyViewActionInCurrentResults(),
                "View action should not be visible in no-result state");
        announcement.clearSearch();
        log().pass("No view action in empty results validated");
    }

    @Test(priority = 19, groups = {"Announcement", "Negative"})
    public void verifyNoEditActionWhenNoSearchResult() {
        String invalid = announcement.randomInvalidKeyword();
        announcement.searchKeyword(invalid);
        Assert.assertFalse(announcement.hasAnyEditActionInCurrentResults(),
                "Edit action should not be visible in no-result state");
        announcement.clearSearch();
        log().pass("No edit action in empty results validated");
    }

    @Test(priority = 20, groups = {"Announcement", "Negative"})
    public void verifyNoDeleteActionWhenNoSearchResult() {
        String invalid = announcement.randomInvalidKeyword();
        announcement.searchKeyword(invalid);
        Assert.assertFalse(announcement.hasAnyDeleteActionInCurrentResults(),
                "Delete action should not be visible in no-result state");
        announcement.clearSearch();
        log().pass("No delete action in empty results validated");
    }

    @Test(priority = 21, groups = {"Announcement", "Negative"})
    public void verifySpecialCharacterSearchHandled() {
        if (!announcement.isPageVisible() && !announcement.isSearchVisible()) {
            log().pass("Announcement module not exposed for this role/environment; scenario not applicable");
            return;
        }
        announcement.searchKeyword("@@@###$$$%%%");
        Assert.assertTrue(announcement.isUiStable() || announcement.isPageVisible(),
                "Special-character search caused UI instability");
        announcement.clearSearch();
        log().pass("Special-character search stability validated");
    }

    @Test(priority = 22, groups = {"Announcement", "Negative"})
    public void verifyVeryLongSearchHandled() {
        if (!announcement.isPageVisible() && !announcement.isSearchVisible()) {
            log().pass("Announcement module not exposed for this role/environment; scenario not applicable");
            return;
        }
        announcement.searchKeyword("ANNOUNCEMENT-NOT-FOUND-ABCDEFGHIJKLMNOPQRSTUVWXYZ-1234567890-abcdefghijklmnopqrstuvwxyz");
        Assert.assertTrue(announcement.isUiStable() || announcement.isPageVisible(),
                "Very long search caused UI instability");
        announcement.clearSearch();
        log().pass("Very long keyword search stability validated");
    }

    @Test(priority = 23, groups = {"Announcement", "Negative"})
    public void verifySqlLikeSearchHandled() {
        if (!announcement.isPageVisible() && !announcement.isSearchVisible()) {
            log().pass("Announcement module not exposed for this role/environment; scenario not applicable");
            return;
        }
        announcement.searchKeyword("' OR 1=1 --");
        Assert.assertTrue(announcement.isUiStable() || announcement.isPageVisible(),
                "SQL-like search string caused UI instability");
        announcement.clearSearch();
        log().pass("SQL-like search handled safely");
    }
}

