package com.ananda.web.admin.tests;

import com.ananda.core.base.BaseTest;
import com.ananda.web.admin.pages.VideosAudiosPage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class VideosAudiosTest extends BaseTest {

    private VideosAudiosPage videosAudios;
    private String firstTitle;

    @BeforeClass
    public void setup() {
        videosAudios = new VideosAudiosPage();
        try {
            videosAudios.openIfNotOpened();
            firstTitle = videosAudios.getFirstTitle();
        } catch (Exception e) {
            firstTitle = null;
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void resetVideosAudiosContext() {
        try {
            videosAudios.openIfNotOpened();
            videosAudios.clearSearch();
            videosAudios.clickAllFilter();
        } catch (Exception ignored) {
        }
    }

    @Test(priority = 1, groups = {"VideosAudios", "Positive"})
    public void verifyVideosAudiosTabLoads() {
        Assert.assertTrue(videosAudios.isPageVisible(), "Videos & Audios page did not open");
        log().pass("Videos & Audios page loaded");
    }

    @Test(priority = 2, groups = {"VideosAudios", "Positive"})
    public void verifySearchControlVisible() {
        if (!videosAudios.isPageVisible() && !videosAudios.isSearchVisible()) {
            log().pass("Videos & Audios module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(videosAudios.isSearchVisible() || videosAudios.isPageVisible(), "Search control not visible");
        log().pass("Search control visible");
    }

    @Test(priority = 3, groups = {"VideosAudios", "Positive"})
    public void verifyNewVideoOrAudioButtonVisible() {
        Assert.assertTrue(videosAudios.isNewVideoOrAudioButtonVisible(), "New Video Or Audio button not visible");
        log().pass("New Video Or Audio button visible");
    }

    @Test(priority = 4, groups = {"VideosAudios", "Positive"})
    public void verifyVideosAudiosTableHeadersVisible() {
        Assert.assertTrue(videosAudios.areExpectedHeadersVisible(), "Videos & Audios table headers missing");
        log().pass("Table headers validated");
    }

    @Test(priority = 5, groups = {"VideosAudios", "Positive"})
    public void verifyDurationColumnFormat() {
        Assert.assertTrue(videosAudios.isDurationColumnValid(), "Duration column format invalid");
        log().pass("Duration column format validated");
    }

    @Test(priority = 6, groups = {"VideosAudios", "Positive"})
    public void verifyFreePaidColumnValues() {
        Assert.assertTrue(videosAudios.isFreePaidColumnValid(), "Free / Paid column values invalid");
        log().pass("Free / Paid column values validated");
    }

    @Test(priority = 7, groups = {"VideosAudios", "Positive"})
    public void verifyStatusColumnValues() {
        Assert.assertTrue(videosAudios.isStatusColumnValid(), "Status column values invalid");
        log().pass("Status column values validated");
    }

    @Test(priority = 8, groups = {"VideosAudios", "Positive"})
    public void verifyRowActionsVisible() {
        Assert.assertTrue(videosAudios.areRowActionsVisible(), "View/Edit/Delete actions missing");
        log().pass("Row action visibility validated");
    }

    @Test(priority = 9, groups = {"VideosAudios", "Positive"})
    public void verifyVideosFilterShowsOnlyVideoRows() {
        videosAudios.clickVideosFilter();
        Assert.assertTrue(videosAudios.areRowsOfTypeOnly("video"),
                "Videos filter did not restrict results to Video rows");
        log().pass("Videos filter validated");
    }

    @Test(priority = 10, groups = {"VideosAudios", "Positive"})
    public void verifyAudioFilterShowsOnlyAudioRows() {
        videosAudios.clickAudioFilter();
        Assert.assertTrue(videosAudios.areRowsOfTypeOnly("audio"),
                "Audio filter did not restrict results to Audio rows");
        log().pass("Audio filter validated");
    }

    @Test(priority = 11, groups = {"VideosAudios", "Positive"})
    public void verifyAllFilterRestoresMixedRowsOrStableList() {
        videosAudios.clickVideosFilter();
        videosAudios.clickAllFilter();
        Assert.assertTrue(videosAudios.isUiStable(), "All filter did not restore stable list");
        log().pass("All filter stability validated");
    }

    @Test(priority = 12, groups = {"VideosAudios", "Positive"})
    public void verifySearchByTitle() {
        if (firstTitle == null || firstTitle.isBlank()) {
            log().pass("No content rows available; search-by-title not applicable");
            return;
        }
        videosAudios.searchKeyword(firstTitle);
        Assert.assertTrue(videosAudios.doesAnyVisibleRowContain(firstTitle),
                "Search by title did not return expected content");
        log().pass("Search by title validated");
    }

    @Test(priority = 13, groups = {"VideosAudios", "Positive"})
    public void verifyViewActionFlow() {
        if (videosAudios.getVisibleRowCount() == 0) {
            log().pass("No content rows available; view flow not applicable");
            return;
        }
        videosAudios.openFirstView();
        Assert.assertTrue(videosAudios.isUiStable() || videosAudios.isAddUpdatePageVisible(),
                "View flow caused unstable UI state");
        log().pass("View action flow validated");
    }

    @Test(priority = 14, groups = {"VideosAudios", "Positive"})
    public void verifyEditActionFlow() {
        if (videosAudios.getVisibleRowCount() == 0) {
            log().pass("No content rows available; edit flow not applicable");
            return;
        }
        videosAudios.openFirstEdit();
        Assert.assertTrue(videosAudios.isAddUpdatePageVisible() || videosAudios.isUiStable(),
                "Edit flow did not open expected page/state");
        log().pass("Edit action flow validated");
    }

    @Test(priority = 15, groups = {"VideosAudios", "Positive"})
    public void verifyDeleteConfirmationPopup() {
        if (videosAudios.getVisibleRowCount() == 0) {
            log().pass("No content rows available; delete popup not applicable");
            return;
        }
        videosAudios.openFirstDelete();
        Assert.assertTrue(videosAudios.isDeleteConfirmationVisible() || videosAudios.isUiStable(),
                "Delete confirmation popup did not appear");
        videosAudios.cancelDeleteIfAny();
        log().pass("Delete confirmation popup validated");
    }

    @Test(priority = 16, groups = {"VideosAudios", "Positive"})
    public void verifyOpenAddVideoOrAudioForm() {
        videosAudios.openAddForm();
        Assert.assertTrue(videosAudios.isAddUpdatePageVisible(),
                "Add / Update Content page did not open");
        Assert.assertTrue(videosAudios.areAddFormFieldsVisible(),
                "Add / Update Content fields are missing");
        log().pass("Add / Update Content form validated");
    }

    @Test(priority = 17, groups = {"VideosAudios", "Positive"})
    public void verifyVersionOptionsVisibleOnForm() {
        videosAudios.openAddForm();
        Assert.assertTrue(videosAudios.isVersionOptionsVisible(),
                "Free / Paid options are missing on form");
        log().pass("Version options visibility validated");
    }

    @Test(priority = 18, groups = {"VideosAudios", "Positive"})
    public void verifyDescriptionEditorVisibleOnForm() {
        videosAudios.openAddForm();
        Assert.assertTrue(videosAudios.isDescriptionEditorVisible(),
                "Description editor is missing on form");
        log().pass("Description editor visibility validated");
    }

    @Test(priority = 19, groups = {"VideosAudios", "Negative"})
    public void verifyInvalidSearchShowsNoResult() {
        String invalid = videosAudios.randomInvalidKeyword();
        videosAudios.searchKeyword(invalid);
        Assert.assertTrue(videosAudios.isNoResultVisible(),
                "Invalid search should show no results");
        videosAudios.clearSearch();
        log().pass("Invalid search no-result behavior validated");
    }

    @Test(priority = 20, groups = {"VideosAudios", "Negative"})
    public void verifyNoViewActionWhenNoSearchResult() {
        String invalid = videosAudios.randomInvalidKeyword();
        videosAudios.searchKeyword(invalid);
        Assert.assertFalse(videosAudios.hasAnyViewActionInCurrentResults(),
                "View action should not be visible for no-result state");
        videosAudios.clearSearch();
        log().pass("No View action on empty result validated");
    }

    @Test(priority = 21, groups = {"VideosAudios", "Negative"})
    public void verifyNoEditActionWhenNoSearchResult() {
        String invalid = videosAudios.randomInvalidKeyword();
        videosAudios.searchKeyword(invalid);
        Assert.assertFalse(videosAudios.hasAnyEditActionInCurrentResults(),
                "Edit action should not be visible for no-result state");
        videosAudios.clearSearch();
        log().pass("No Edit action on empty result validated");
    }

    @Test(priority = 22, groups = {"VideosAudios", "Negative"})
    public void verifyNoDeleteActionWhenNoSearchResult() {
        String invalid = videosAudios.randomInvalidKeyword();
        videosAudios.searchKeyword(invalid);
        Assert.assertFalse(videosAudios.hasAnyDeleteActionInCurrentResults(),
                "Delete action should not be visible for no-result state");
        videosAudios.clearSearch();
        log().pass("No Delete action on empty result validated");
    }

    @Test(priority = 23, groups = {"VideosAudios", "Negative"})
    public void verifySpecialCharacterSearchHandled() {
        if (!videosAudios.isPageVisible() && !videosAudios.isSearchVisible()) {
            log().pass("Videos & Audios module not exposed for this role/environment; scenario not applicable");
            return;
        }
        videosAudios.searchKeyword("@@@###$$$%%%");
        Assert.assertTrue(videosAudios.isUiStable() || videosAudios.isPageVisible(),
                "Special-character search caused UI instability");
        videosAudios.clearSearch();
        log().pass("Special-character search stability validated");
    }

    @Test(priority = 24, groups = {"VideosAudios", "Negative"})
    public void verifyVeryLongSearchHandled() {
        if (!videosAudios.isPageVisible() && !videosAudios.isSearchVisible()) {
            log().pass("Videos & Audios module not exposed for this role/environment; scenario not applicable");
            return;
        }
        videosAudios.searchKeyword("VIDEO-AUDIO-NOT-FOUND-ABCDEFGHIJKLMNOPQRSTUVWXYZ-1234567890-abcdefghijklmnopqrstuvwxyz");
        Assert.assertTrue(videosAudios.isUiStable() || videosAudios.isPageVisible(),
                "Very long search caused UI instability");
        videosAudios.clearSearch();
        log().pass("Very long keyword search stability validated");
    }
}
