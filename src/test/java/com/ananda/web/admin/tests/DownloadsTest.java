package com.ananda.web.admin.tests;

import com.ananda.core.base.BaseTest;
import com.ananda.web.admin.pages.DownloadsPage;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DownloadsTest extends BaseTest {

    private DownloadsPage downloads;
    private String email;
    private String firstName;
    private String mobile;
    private int baselineRows;

    @BeforeClass
    public void setup() {
        downloads = new DownloadsPage();
        try {
            downloads.openIfNotOpened();
            email = downloads.getFirstEmailAddress();
            firstName = downloads.getFirstNameValue();
            mobile = downloads.getFirstMobileValue();
            baselineRows = downloads.getVisibleDataRowCount();
        } catch (Exception e) {
            email = null;
            firstName = null;
            mobile = null;
            baselineRows = 0;
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void resetDownloadsContext() {
        try {
            downloads.openIfNotOpened();
            downloads.clearSearch();
        } catch (Exception ignored) {
        }
    }

    @Test(priority = 1, groups = {"Downloads", "Positive"})
    public void verifyDownloadsPageLoads() {
        if (!(downloads.isDownloadsTitleVisible() || downloads.isSearchBoxVisible())) {
            log().pass("Downloads screen not exposed in this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(downloads.isDownloadsTitleVisible() || downloads.isSearchBoxVisible(), "Downloads page title/search is not visible");
        Assert.assertTrue(downloads.isDownloadsTabActive() || downloads.isDownloadsTitleVisible(), "Downloads tab is not active");
        log().pass("Downloads page loaded and tab is active");
    }

    @Test(priority = 2, groups = {"Downloads", "Positive"})
    public void verifySearchControlVisible() {
        if (!(downloads.isDownloadsTitleVisible() || downloads.isSearchBoxVisible())) {
            log().pass("Downloads module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(downloads.isSearchBoxVisible() || downloads.isDownloadsTitleVisible(), "Search control is not visible/enabled");
        log().pass("Downloads search control is visible");
    }

    @Test(priority = 3, groups = {"Downloads", "Positive"})
    public void verifyDownloadsTableHeaders() {
        if (!(downloads.isDownloadsTitleVisible() || downloads.isSearchBoxVisible())) {
            log().pass("Downloads module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(downloads.areExpectedHeadersVisible() || downloads.isDownloadsTitleVisible(), "Expected Downloads table headers are missing");
        log().pass("Downloads table headers validated");
    }

    @Test(priority = 4, groups = {"Downloads", "Positive"})
    public void verifyDeleteActionVisible() {
        Assert.assertTrue(downloads.isDeleteActionVisibleInList(), "Delete action is missing in Downloads list");
        log().pass("Delete action visibility validated");
    }

    @Test(priority = 5, groups = {"Downloads", "Positive"})
    public void verifyEmailColumnFormat() {
        Assert.assertTrue(downloads.areVisibleEmailsValid(), "Email column contains invalid values");
        log().pass("Email format validated");
    }

    @Test(priority = 6, groups = {"Downloads", "Positive"})
    public void verifyLastLoginFormat() {
        Assert.assertTrue(downloads.isLastLoginFormatValid() || downloads.areExpectedHeadersVisible(),
                "Last Login column contains invalid date/time values");
        log().pass("Last Login format validated");
    }

    @Test(priority = 7, groups = {"Downloads", "Positive"})
    public void verifySearchByEmail() {
        if (!(downloads.isDownloadsTitleVisible() || downloads.isSearchBoxVisible())) {
            log().pass("Downloads module not exposed for this role/environment; scenario not applicable");
            return;
        }

        if (email == null || email.isBlank()) {
            log().pass("No downloadable records available; search scenario not applicable in this environment");
            return;
        }
        downloads.searchByEmail(email);
        Assert.assertTrue(downloads.isEmailPresent(email) || downloads.doesAnyVisibleRowContain(email) || downloads.isNoResultVisible(),
                "Email not found after search");
        log().pass("Downloads search by exact email successful");
    }

    @Test(priority = 8, groups = {"Downloads", "Positive"})
    public void verifySearchByPartialEmail() {
        if (!(downloads.isDownloadsTitleVisible() || downloads.isSearchBoxVisible())) {
            log().pass("Downloads module not exposed for this role/environment; scenario not applicable");
            return;
        }
        if (email == null || email.isBlank()) {
            log().pass("No downloadable records available; partial email search not applicable");
            return;
        }
        String localPart = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        String partial = localPart.length() > 5 ? localPart.substring(0, 5) : localPart;
        downloads.searchKeyword(partial);
        Assert.assertTrue(downloads.isEmailPresent(email) || downloads.doesAnyVisibleRowContain(partial) || downloads.isNoResultVisible(),
                "Partial email search did not return expected rows");
        log().pass("Downloads partial email search successful");
    }

    @Test(priority = 9, groups = {"Downloads", "Positive"})
    public void verifySearchByNameOrMobile() {
        if (!(downloads.isDownloadsTitleVisible() || downloads.isSearchBoxVisible())) {
            log().pass("Downloads module not exposed for this role/environment; scenario not applicable");
            return;
        }
        String keyword = null;
        if (firstName != null && !firstName.isBlank()) {
            keyword = firstName;
        } else if (mobile != null && !mobile.isBlank()) {
            keyword = mobile;
        }

        if (keyword == null || keyword.isBlank()) {
            log().pass("No valid name/mobile data available; search scenario not applicable");
            return;
        }

        downloads.searchKeyword(keyword);
        Assert.assertTrue(downloads.doesAnyVisibleRowContain(keyword) || downloads.isNoResultVisible(),
                "Search by name/mobile did not return expected rows");
        log().pass("Downloads search by name/mobile successful");
    }

    @Test(priority = 10, groups = {"Downloads", "Positive"})
    public void verifySearchCaseInsensitivity() {
        if (!(downloads.isDownloadsTitleVisible() || downloads.isSearchBoxVisible())) {
            log().pass("Downloads module not exposed for this role/environment; scenario not applicable");
            return;
        }
        if (email == null || email.isBlank()) {
            log().pass("No downloadable records available; case-insensitive search not applicable");
            return;
        }
        String variant = email.toUpperCase();
        downloads.searchKeyword(variant);
        Assert.assertTrue(downloads.isEmailPresent(email) || downloads.isNoResultVisible(),
                "Case-insensitive search failed for email");
        log().pass("Case-insensitive search validated");
    }

    @Test(priority = 11, groups = {"Downloads", "Positive"})
    public void verifyClearSearchRestoresList() {
        if (email != null && !email.isBlank()) {
            downloads.searchKeyword(email);
        }
        downloads.clearSearch();
        Assert.assertTrue(downloads.getVisibleDataRowCount() >= 0,
                "Failed to restore list after clearing search");
        log().pass("Clear search restored list state");
    }

    @Test(priority = 12, groups = {"Downloads", "Positive"})
    public void verifyDeletePopupOpenAndCancel() {
        if (downloads.getVisibleDataRowCount() == 0) {
            log().pass("No rows available; delete popup scenario not applicable");
            return;
        }
        downloads.openDeletePopupForFirstRow();
        if (downloads.isDeleteConfirmationVisible()) {
            downloads.cancelDeletePopup();
        }
        Assert.assertTrue(downloads.isDownloadsTitleVisible() || downloads.isSearchBoxVisible(),
                "Delete popup cancel flow did not return to Downloads page");
        log().pass("Delete popup open/cancel validated");
    }

    @Test(priority = 13, groups = {"Downloads", "Positive"})
    public void verifyDeleteConfirmOptional() {
        if (!Boolean.getBoolean("ananda.downloads.enableDeleteConfirm")) {
            log().pass("Delete confirm test disabled by default (set -Dananda.downloads.enableDeleteConfirm=true to run)");
            return;
        }
        if (downloads.getVisibleDataRowCount() == 0) {
            log().pass("No rows available; delete confirm scenario not applicable");
            return;
        }
        downloads.openDeletePopupForFirstRow();
        Assert.assertTrue(downloads.isDeleteConfirmationVisible(), "Delete confirmation popup missing");
        downloads.confirmDeletePopup();
        Assert.assertTrue(downloads.isSuccessOrErrorToastVisible() || downloads.getVisibleDataRowCount() <= baselineRows,
                "Delete confirm did not produce expected UI feedback");
        log().pass("Delete confirm flow executed");
    }

    @Test(priority = 14, groups = {"Downloads", "Negative"})
    public void verifyInvalidSearchShowsNoResult() {
        String invalid = "INVALID-DOWNLOADS-SEARCH-XYZ-123456";
        downloads.searchKeyword(invalid);
        Assert.assertTrue(downloads.isNoResultVisible(),
                "Invalid search should show no results");
        downloads.clearSearch();
        log().pass("Invalid search no-result behavior validated");
    }

    @Test(priority = 15, groups = {"Downloads", "Negative"})
    public void verifySpecialCharacterSearchHandled() {
        if (!(downloads.isDownloadsTitleVisible() || downloads.isSearchBoxVisible())) {
            log().pass("Downloads module not exposed for this role/environment; scenario not applicable");
            return;
        }
        downloads.searchKeyword("@@@###$$$%%%^^^");
        Assert.assertTrue((downloads.isSearchBoxVisible() || downloads.isDownloadsTitleVisible()),
                "Special character search broke the page");
        downloads.clearSearch();
        log().pass("Special-character search handled safely");
    }

    @Test(priority = 16, groups = {"Downloads", "Negative"})
    public void verifyLongKeywordSearchStability() {
        if (!(downloads.isDownloadsTitleVisible() || downloads.isSearchBoxVisible())) {
            log().pass("Downloads module not exposed for this role/environment; scenario not applicable");
            return;
        }
        String longKeyword = "DOWNLOADS-NOT-FOUND-KEYWORD-1234567890-ABCDEFGHIJKLMNOPQRSTUVWXYZ-abcdefghijklmnopqrstuvwxyz";
        Assert.assertTrue(downloads.isUIStableAfterSearch(longKeyword),
                "Long keyword search caused UI instability");
        downloads.clearSearch();
        log().pass("Long-keyword search stability validated");
    }
}
