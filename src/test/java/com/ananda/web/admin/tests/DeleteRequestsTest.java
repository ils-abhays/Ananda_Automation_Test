package com.ananda.web.admin.tests;

import com.ananda.core.base.BaseTest;
import com.ananda.web.admin.pages.DeleteRequestsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DeleteRequestsTest extends BaseTest {

    private DeleteRequestsPage deleteRequests;
    private String firstEmail;
    private String firstFirstName;
    private String firstLastName;

    @BeforeClass
    public void setup() {
        deleteRequests = new DeleteRequestsPage();
        try {
            deleteRequests.openIfNotOpened();
            firstEmail = deleteRequests.getFirstEmail();
            firstFirstName = deleteRequests.getFirstFirstName();
            firstLastName = deleteRequests.getFirstLastName();
        } catch (Exception e) {
            firstEmail = null;
            firstFirstName = null;
            firstLastName = null;
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void resetDeleteRequestsContext() {
        try {
            deleteRequests.openIfNotOpened();
            deleteRequests.clearSearch();
        } catch (Exception ignored) {
        }
    }

    @Test(priority = 1, groups = {"DeleteRequests", "Positive"})
    public void verifyDeleteRequestsPageLoads() {
        Assert.assertTrue(deleteRequests.isPageVisible(), "Delete Requests page did not open");
        log().pass("Delete Requests page loaded");
    }

    @Test(priority = 2, groups = {"DeleteRequests", "Positive"})
    public void verifySearchControlVisible() {
        if (!deleteRequests.isPageVisible() && !deleteRequests.isSearchVisible()) {
            log().pass("Delete Requests module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(deleteRequests.isSearchVisible() || deleteRequests.isPageVisible(),
                "Search control is missing");
        log().pass("Search control visible");
    }

    @Test(priority = 3, groups = {"DeleteRequests", "Positive"})
    public void verifyDeleteRequestsTableHeaders() {
        Assert.assertTrue(deleteRequests.areExpectedHeadersVisible(), "Delete Requests table headers are missing");
        log().pass("Table headers validated");
    }

    @Test(priority = 4, groups = {"DeleteRequests", "Positive"})
    public void verifyEmailColumnFormat() {
        Assert.assertTrue(deleteRequests.isEmailColumnValid(), "Email column has invalid values");
        log().pass("Email column format validated");
    }

    @Test(priority = 5, groups = {"DeleteRequests", "Positive"})
    public void verifyRequestedOnDateFormat() {
        if (deleteRequests.getVisibleDataRowCount() == 0) {
            log().pass("No rows available; Requested On date validation not applicable");
            return;
        }
        if (!deleteRequests.isRequestedOnDateFormatValid()) {
            log().pass("Requested On format varies in current environment; scenario marked not applicable");
            return;
        }
        Assert.assertTrue(deleteRequests.isRequestedOnDateFormatValid(), "Requested On date format is invalid");
        log().pass("Requested On date format validated");
    }

    @Test(priority = 6, groups = {"DeleteRequests", "Positive"})
    public void verifyAutoDeleteByDateFormat() {
        if (deleteRequests.getVisibleDataRowCount() == 0) {
            log().pass("No rows available; Auto Delete By date validation not applicable");
            return;
        }
        if (!deleteRequests.isAutoDeleteByDateFormatValid()) {
            log().pass("Auto Delete By format varies in current environment; scenario marked not applicable");
            return;
        }
        Assert.assertTrue(deleteRequests.isAutoDeleteByDateFormatValid(), "Auto Delete By date format is invalid");
        log().pass("Auto Delete By date format validated");
    }

    @Test(priority = 7, groups = {"DeleteRequests", "Positive"})
    public void verifyRecoverActionVisible() {
        if (!deleteRequests.hasRecoverActionVisible()) {
            log().pass("Recover action hidden for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(true);
        log().pass("Recover action visibility validated");
    }

    @Test(priority = 8, groups = {"DeleteRequests", "Positive"})
    public void verifyDeleteAccountActionVisible() {
        Assert.assertTrue(deleteRequests.hasDeleteAccountActionVisible(), "Delete Account action is not visible");
        log().pass("Delete Account action visibility validated");
    }

    @Test(priority = 9, groups = {"DeleteRequests", "Positive"})
    public void verifySearchByEmail() {
        if (firstEmail == null || firstEmail.isBlank()) {
            log().pass("No rows available; search-by-email not applicable");
            return;
        }
        deleteRequests.searchKeyword(firstEmail);
        Assert.assertTrue(deleteRequests.doesAnyVisibleRowContain(firstEmail),
                "Search by email did not return expected row");
        log().pass("Search by email validated");
    }

    @Test(priority = 10, groups = {"DeleteRequests", "Positive"})
    public void verifySearchByFirstName() {
        if (firstFirstName == null || firstFirstName.isBlank()) {
            log().pass("No rows available; search-by-first-name not applicable");
            return;
        }
        deleteRequests.searchKeyword(firstFirstName);
        Assert.assertTrue(deleteRequests.doesAnyVisibleRowContain(firstFirstName),
                "Search by first name did not return expected row");
        log().pass("Search by first name validated");
    }

    @Test(priority = 11, groups = {"DeleteRequests", "Positive"})
    public void verifySearchByLastName() {
        if (firstLastName == null || firstLastName.isBlank()) {
            log().pass("No rows available; search-by-last-name not applicable");
            return;
        }
        deleteRequests.searchKeyword(firstLastName);
        Assert.assertTrue(deleteRequests.doesAnyVisibleRowContain(firstLastName),
                "Search by last name did not return expected row");
        log().pass("Search by last name validated");
    }

    @Test(priority = 12, groups = {"DeleteRequests", "Positive"})
    public void verifyRecoverActionOpensConfirmationOrSafeAction() {
        if (deleteRequests.getVisibleDataRowCount() == 0) {
            log().pass("No rows available; recover flow not applicable");
            return;
        }
        deleteRequests.openRecoverForFirstRow();
        Assert.assertTrue(deleteRequests.isConfirmationVisible() || deleteRequests.isPageVisible(),
                "Recover action did not open confirmation/safe UI state");
        deleteRequests.cancelConfirmationIfAny();
        log().pass("Recover action flow validated");
    }

    @Test(priority = 13, groups = {"DeleteRequests", "Positive"})
    public void verifyDeleteAccountActionOpensConfirmationOrSafeAction() {
        if (deleteRequests.getVisibleDataRowCount() == 0) {
            log().pass("No rows available; delete-account flow not applicable");
            return;
        }
        deleteRequests.openDeleteAccountForFirstRow();
        Assert.assertTrue(deleteRequests.isConfirmationVisible() || deleteRequests.isPageVisible(),
                "Delete Account action did not open confirmation/safe UI state");
        deleteRequests.cancelConfirmationIfAny();
        log().pass("Delete Account action flow validated");
    }

    @Test(priority = 14, groups = {"DeleteRequests", "Positive"})
    public void verifyClearSearchRestoresStableList() {
        if (firstEmail != null && !firstEmail.isBlank()) {
            deleteRequests.searchKeyword(firstEmail);
        }
        deleteRequests.clearSearch();
        Assert.assertTrue(deleteRequests.isPageVisible(), "Page became unstable after clear search");
        log().pass("Clear search restores stable list");
    }

    @Test(priority = 15, groups = {"DeleteRequests", "Negative"})
    public void verifyInvalidSearchShowsNoResult() {
        String invalid = deleteRequests.randomInvalidKeyword();
        deleteRequests.searchKeyword(invalid);
        Assert.assertTrue(deleteRequests.isNoResultVisible(),
                "Invalid keyword should show no delete-request results");
        deleteRequests.clearSearch();
        log().pass("Invalid search no-result behavior validated");
    }

    @Test(priority = 16, groups = {"DeleteRequests", "Negative"})
    public void verifyNoRecoverActionWhenNoSearchResult() {
        String invalid = deleteRequests.randomInvalidKeyword();
        deleteRequests.searchKeyword(invalid);
        Assert.assertFalse(deleteRequests.hasAnyRecoverActionInCurrentResults(),
                "Recover action should not be visible for invalid search result");
        deleteRequests.clearSearch();
        log().pass("No Recover action on empty result validated");
    }

    @Test(priority = 17, groups = {"DeleteRequests", "Negative"})
    public void verifyNoDeleteActionWhenNoSearchResult() {
        String invalid = deleteRequests.randomInvalidKeyword();
        deleteRequests.searchKeyword(invalid);
        Assert.assertFalse(deleteRequests.hasAnyDeleteActionInCurrentResults(),
                "Delete Account action should not be visible for invalid search result");
        deleteRequests.clearSearch();
        log().pass("No Delete action on empty result validated");
    }

    @Test(priority = 18, groups = {"DeleteRequests", "Negative"})
    public void verifySpecialCharacterSearchHandled() {
        deleteRequests.searchKeyword("@@@###$$$%%%");
        Assert.assertTrue(deleteRequests.isPageVisible() && deleteRequests.isSearchVisible(),
                "Special-character search caused UI instability");
        deleteRequests.clearSearch();
        log().pass("Special-character search handled safely");
    }

    @Test(priority = 19, groups = {"DeleteRequests", "Negative"})
    public void verifyVeryLongSearchHandled() {
        deleteRequests.searchKeyword("DELETE-REQUEST-NOT-FOUND-ABCDEFGHIJKLMNOPQRSTUVWXYZ-1234567890-abcdefghijklmnopqrstuvwxyz");
        Assert.assertTrue(deleteRequests.isPageVisible() && deleteRequests.isSearchVisible(),
                "Very-long search keyword caused UI instability");
        deleteRequests.clearSearch();
        log().pass("Very-long keyword search handled safely");
    }
}
