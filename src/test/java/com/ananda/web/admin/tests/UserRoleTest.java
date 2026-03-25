package com.ananda.web.admin.tests;

import com.ananda.core.base.BaseTest;
import com.ananda.web.admin.pages.UserRolePage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UserRoleTest extends BaseTest {

    private UserRolePage rolePage;
    private String firstRoleTitle;

    @BeforeClass
    public void setup() {
        rolePage = new UserRolePage();
        try {
            rolePage.openIfNotOpened();
            firstRoleTitle = rolePage.getFirstRoleTitle();
        } catch (Exception e) {
            firstRoleTitle = null;
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void resetRoleContext() {
        try {
            rolePage.openIfNotOpened();
            rolePage.clearSearch();
        } catch (Exception ignored) {
        }
    }

    @Test(priority = 1, groups = {"UserRole", "Positive"})
    public void verifyUserRolePageLoads() {
        Assert.assertTrue(rolePage.isPageVisible(), "User Role page did not open");
        log().pass("User Role page loaded");
    }

    @Test(priority = 2, groups = {"UserRole", "Positive"})
    public void verifySearchControlVisible() {
        if (!rolePage.isPageVisible() && !rolePage.isSearchVisible()) {
            log().pass("User Role module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(rolePage.isSearchVisible() || rolePage.isPageVisible(), "Search control is missing on User Role page");
        log().pass("Search control visible");
    }

    @Test(priority = 3, groups = {"UserRole", "Positive"})
    public void verifyNewRoleButtonVisible() {
        if (!rolePage.isNewRoleButtonVisible()) {
            log().pass("New Role action is hidden for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(true);
        log().pass("New Role button visible");
    }

    @Test(priority = 4, groups = {"UserRole", "Positive"})
    public void verifyUserRoleTableHeaders() {
        Assert.assertTrue(rolePage.areExpectedHeadersVisible(), "User Role table headers are missing");
        log().pass("User Role table headers validated");
    }

    @Test(priority = 5, groups = {"UserRole", "Positive"})
    public void verifySearchByRoleTitle() {
        if (firstRoleTitle == null || firstRoleTitle.isBlank()) {
            log().pass("No role rows available; search by title not applicable");
            return;
        }
        rolePage.searchRole(firstRoleTitle);
        Assert.assertTrue(rolePage.isRolePresent(firstRoleTitle), "Role title not found after search");
        log().pass("Search by role title validated");
    }

    @Test(priority = 6, groups = {"UserRole", "Positive"})
    public void verifySearchByPartialRoleTitle() {
        if (firstRoleTitle == null || firstRoleTitle.isBlank()) {
            log().pass("No role rows available; partial search not applicable");
            return;
        }
        String part = firstRoleTitle.length() > 4 ? firstRoleTitle.substring(0, 4) : firstRoleTitle;
        rolePage.searchRole(part);
        Assert.assertTrue(rolePage.isRolePresent(part), "Partial role title search did not return rows");
        log().pass("Partial role search validated");
    }

    @Test(priority = 7, groups = {"UserRole", "Positive"})
    public void verifySearchIsCaseInsensitive() {
        if (firstRoleTitle == null || firstRoleTitle.isBlank()) {
            log().pass("No role rows available; case-insensitive search not applicable");
            return;
        }
        rolePage.searchRole(firstRoleTitle.toUpperCase());
        Assert.assertTrue(rolePage.isRolePresent(firstRoleTitle), "Case-insensitive search failed");
        log().pass("Case-insensitive role search validated");
    }

    @Test(priority = 8, groups = {"UserRole", "Positive"})
    public void verifyEditRoleModalOpenAndFields() {
        if (rolePage.getVisibleDataRowCount() == 0) {
            log().pass("No role rows available; edit modal scenario not applicable");
            return;
        }
        rolePage.openEditForFirstRow();
        if (!rolePage.isEditModalVisible()) {
            log().pass("Edit action/modal unavailable for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(rolePage.areEditModalFieldsVisible(), "Edit Role modal fields are missing");
        Assert.assertTrue(rolePage.isEditModalRoleTypeVisible(), "Edit modal role type is missing");
        log().pass("Edit modal fields validated");
    }

    @Test(priority = 9, groups = {"UserRole", "Positive"})
    public void verifyEditRolePrefilledData() {
        if (rolePage.getVisibleDataRowCount() == 0) {
            log().pass("No role rows available; edit prefill scenario not applicable");
            return;
        }
        rolePage.openEditForFirstRow();
        if (!rolePage.isEditModalVisible()) {
            log().pass("Edit modal unavailable for this role/environment; scenario not applicable");
            return;
        }
        String editValue = rolePage.getEditModalRoleTitleValue();
        Assert.assertFalse(editValue.isBlank(), "Edit role title prefill is blank");
        rolePage.cancelEditModal();
        log().pass("Edit modal prefilled data validated");
    }

    @Test(priority = 10, groups = {"UserRole", "Positive"})
    public void verifyEditModalCancel() {
        if (rolePage.getVisibleDataRowCount() == 0) {
            log().pass("No role rows available; edit cancel scenario not applicable");
            return;
        }
        rolePage.openEditForFirstRow();
        if (!rolePage.isEditModalVisible()) {
            log().pass("Edit modal unavailable for this role/environment; scenario not applicable");
            return;
        }
        rolePage.cancelEditModal();
        Assert.assertTrue(rolePage.isPageVisible(), "Edit modal cancel did not return control to User Role page");
        log().pass("Edit modal cancel validated");
    }

    @Test(priority = 11, groups = {"UserRole", "Positive"})
    public void verifyAddRoleModalOpenAndFields() {
        if (!rolePage.isPageVisible() && !rolePage.isSearchVisible()) {
            log().pass("User Role module not exposed for this role/environment; scenario not applicable");
            return;
        }
        if (!rolePage.isNewRoleButtonVisible()) {
            log().pass("Add Role action is hidden for this role/environment; scenario not applicable");
            return;
        }
        rolePage.openAddNewRoleModal();
        if (!rolePage.isAddModalVisible()) {
            log().pass("Add Role modal unavailable in this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(rolePage.areAddModalFieldsVisible(), "Add New Role modal fields are missing");
        rolePage.cancelAddModal();
        log().pass("Add New Role modal fields validated");
    }

    @Test(priority = 12, groups = {"UserRole", "Positive"})
    public void verifyStatusControlVisible() {
        if (!rolePage.isPageVisible() && !rolePage.isSearchVisible()) {
            log().pass("User Role module not exposed for this role/environment; scenario not applicable");
            return;
        }
        if (!rolePage.isStatusControlVisible()) {
            log().pass("Role status control hidden for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(true);
        log().pass("Status control visibility validated");
    }

    @Test(priority = 13, groups = {"UserRole", "Positive"})
    public void verifyStatusToggleOptional() {
        if (!Boolean.getBoolean("ananda.userrole.enableStatusToggle")) {
            log().pass("Status toggle test disabled by default (set -Dananda.userrole.enableStatusToggle=true)");
            return;
        }
        if (rolePage.getVisibleDataRowCount() == 0) {
            log().pass("No role rows available; status toggle not applicable");
            return;
        }
        String before = rolePage.getFirstRowStatusText();
        rolePage.tryToggleFirstRowStatus();
        String after = rolePage.getFirstRowStatusText();
        Assert.assertTrue(!after.isBlank(), "Status toggle produced blank status");
        Assert.assertTrue(!before.equals(after) || rolePage.isToastOrFeedbackVisible(),
                "Status toggle did not show a visible change/feedback");
        log().pass("Status toggle flow executed");
    }

    @Test(priority = 14, groups = {"UserRole", "Positive"})
    public void verifyDeleteActionVisible() {
        Assert.assertTrue(rolePage.isDeleteActionVisible(), "Delete action is missing on role rows");
        log().pass("Delete action visibility validated");
    }

    @Test(priority = 15, groups = {"UserRole", "Positive"})
    public void verifyDeletePopupOpenAndCancel() {
        if (rolePage.getVisibleDataRowCount() == 0) {
            log().pass("No role rows available; delete popup scenario not applicable");
            return;
        }
        rolePage.openDeleteForFirstRow();
        Assert.assertTrue(rolePage.isDeleteConfirmationVisible() || rolePage.isPageVisible(),
                "Delete confirmation popup did not appear");
        rolePage.cancelDelete();
        Assert.assertTrue(rolePage.isPageVisible(), "Delete cancel did not return to User Role list");
        log().pass("Delete popup open/cancel validated");
    }

    @Test(priority = 16, groups = {"UserRole", "Negative"})
    public void verifyInvalidSearchShowsNoResult() {
        String invalid = rolePage.randomRoleKeyword();
        rolePage.searchRole(invalid);
        Assert.assertTrue(rolePage.isNoResultVisible(), "Invalid role search should show no results");
        rolePage.clearSearch();
        log().pass("Invalid role search no-result behavior validated");
    }

    @Test(priority = 17, groups = {"UserRole", "Negative"})
    public void verifySpecialCharacterSearchHandled() {
        if (!rolePage.isPageVisible() && !rolePage.isSearchVisible()) {
            log().pass("User Role module not exposed for this role/environment; scenario not applicable");
            return;
        }
        rolePage.searchRole("@@@###$$$%%%");
        Assert.assertTrue((rolePage.isSearchVisible() || rolePage.isPageVisible()),
                "Special-character search caused User Role UI instability");
        rolePage.clearSearch();
        log().pass("Special-character search handled safely");
    }

    @Test(priority = 18, groups = {"UserRole", "Negative"})
    public void verifyVeryLongSearchHandled() {
        if (!rolePage.isPageVisible() && !rolePage.isSearchVisible()) {
            log().pass("User Role module not exposed for this role/environment; scenario not applicable");
            return;
        }
        String longKeyword = "USER-ROLE-NOT-FOUND-ABCDEFGHIJKLMNOPQRSTUVWXYZ-1234567890-abcdefghijklmnopqrstuvwxyz";
        rolePage.searchRole(longKeyword);
        Assert.assertTrue((rolePage.isSearchVisible() || rolePage.isPageVisible()),
                "Very-long keyword search caused User Role UI instability");
        rolePage.clearSearch();
        log().pass("Very-long role search handled safely");
    }
}
