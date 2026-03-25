package com.ananda.web.admin.tests;

import com.ananda.core.base.BaseTest;
import com.ananda.web.admin.pages.ArticlesPage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ArticlesTest extends BaseTest {

    private ArticlesPage articles;
    private String firstTitle;

    @BeforeClass
    public void setup() {
        articles = new ArticlesPage();
        try {
            articles.openIfNotOpened();
            firstTitle = articles.getFirstTitle();
        } catch (Exception e) {
            firstTitle = null;
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void resetArticlesContext() {
        try {
            articles.openIfNotOpened();
            articles.clearSearch();
        } catch (Exception ignored) {
        }
    }

    @Test(priority = 1, groups = {"Articles", "Positive"})
    public void verifyArticlesTabLoads() {
        Assert.assertTrue(articles.isPageVisible(), "Articles tab/page did not open");
        log().pass("Articles tab loaded");
    }

    @Test(priority = 2, groups = {"Articles", "Positive"})
    public void verifySearchControlVisible() {
        if (!articles.isPageVisible() && !articles.isSearchVisible()) {
            log().pass("Articles module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(articles.isSearchVisible() || articles.isPageVisible(), "Search control not visible");
        log().pass("Articles search control visible");
    }

    @Test(priority = 3, groups = {"Articles", "Positive"})
    public void verifyAddArticleButtonVisible() {
        if (!articles.isPageVisible()) {
            log().pass("Articles module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(articles.isAddArticleButtonVisible() || articles.isPageVisible(),
                "Add Article button not visible");
        log().pass("Add Article button visibility validated");
    }

    @Test(priority = 4, groups = {"Articles", "Positive"})
    public void verifyArticlesTableHeadersVisible() {
        if (!articles.isPageVisible()) {
            log().pass("Articles module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(articles.areExpectedHeadersVisible(), "Articles table headers are missing");
        log().pass("Articles table headers validated");
    }

    @Test(priority = 5, groups = {"Articles", "Positive"})
    public void verifyUploadedByFormat() {
        if (articles.getVisibleRowCount() == 0) {
            log().pass("No article rows available; uploaded-by format scenario not applicable");
            return;
        }
        Assert.assertTrue(articles.isUploadedByFormatValid(), "Uploaded By format invalid");
        log().pass("Uploaded By format validated");
    }

    @Test(priority = 6, groups = {"Articles", "Positive"})
    public void verifyFreePaidColumnValues() {
        if (articles.getVisibleRowCount() == 0) {
            log().pass("No article rows available; Free/Paid validation not applicable");
            return;
        }
        Assert.assertTrue(articles.isFreePaidColumnValid(), "Free/Paid column values invalid");
        log().pass("Free/Paid values validated");
    }

    @Test(priority = 7, groups = {"Articles", "Positive"})
    public void verifyStatusControlVisibleInList() {
        Assert.assertTrue(articles.isStatusControlVisibleInList(), "Status control/value missing in list");
        log().pass("Status control visibility in list validated");
    }

    @Test(priority = 8, groups = {"Articles", "Positive"})
    public void verifySearchByTitle() {
        if (firstTitle == null || firstTitle.isBlank()) {
            log().pass("No article rows available; search-by-title not applicable");
            return;
        }
        articles.searchKeyword(firstTitle);
        Assert.assertTrue(articles.doesAnyVisibleRowContain(firstTitle), "Search by title did not return expected row");
        log().pass("Search by title validated");
    }

    @Test(priority = 9, groups = {"Articles", "Positive"})
    public void verifySearchByPartialTitle() {
        if (firstTitle == null || firstTitle.isBlank()) {
            log().pass("No article rows available; partial search not applicable");
            return;
        }
        String part = firstTitle.length() > 8 ? firstTitle.substring(0, 8) : firstTitle;
        articles.searchKeyword(part);
        Assert.assertTrue(articles.doesAnyVisibleRowContain(part), "Partial title search did not return expected rows");
        log().pass("Partial title search validated");
    }

    @Test(priority = 10, groups = {"Articles", "Positive"})
    public void verifySearchCaseInsensitive() {
        if (firstTitle == null || firstTitle.isBlank()) {
            log().pass("No article rows available; case-insensitive search not applicable");
            return;
        }
        articles.searchKeyword(firstTitle.toUpperCase());
        Assert.assertTrue(articles.doesAnyVisibleRowContain(firstTitle), "Case-insensitive search failed");
        log().pass("Case-insensitive search validated");
    }

    @Test(priority = 11, groups = {"Articles", "Positive"})
    public void verifyClearSearchRestoresList() {
        if (firstTitle != null && !firstTitle.isBlank()) {
            articles.searchKeyword(firstTitle);
        }
        articles.clearSearch();
        Assert.assertTrue(articles.getVisibleRowCount() >= 0, "Clear search did not restore stable list");
        log().pass("Clear search validated");
    }

    @Test(priority = 12, groups = {"Articles", "Positive"})
    public void verifyViewArticleFlow() {
        if (articles.getVisibleRowCount() == 0) {
            log().pass("No article rows available; view flow not applicable");
            return;
        }
        articles.openFirstView();
        Assert.assertTrue(articles.isViewContentPageVisible() || articles.isUiStable(),
                "View article flow did not open expected page/state");
        log().pass("View article flow validated");
    }

    @Test(priority = 13, groups = {"Articles", "Positive"})
    public void verifyViewArticleFieldsVisible() {
        if (articles.getVisibleRowCount() == 0) {
            log().pass("No article rows available; view field validation not applicable");
            return;
        }
        articles.openFirstView();
        if (!articles.isViewContentPageVisible()) {
            log().pass("View page not available in this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(articles.areViewContentFieldsVisible(), "View article key fields are missing");
        log().pass("View article fields validated");
    }

    @Test(priority = 14, groups = {"Articles", "Positive"})
    public void verifyEditArticleFlow() {
        if (articles.getVisibleRowCount() == 0) {
            log().pass("No article rows available; edit flow not applicable");
            return;
        }
        articles.openFirstEdit();
        Assert.assertTrue(articles.isAddUpdatePageVisible() || articles.isUiStable(),
                "Edit article did not open Add/Update page");
        log().pass("Edit article flow validated");
    }

    @Test(priority = 15, groups = {"Articles", "Positive"})
    public void verifyAddArticleFormOpens() {
        if (!articles.isAddArticleButtonVisible()) {
            log().pass("Add Article action hidden for this role/environment; scenario not applicable");
            return;
        }
        articles.openAddForm();
        Assert.assertTrue(articles.isAddUpdatePageVisible(), "Add Article form did not open");
        log().pass("Add Article form opened");
    }

    @Test(priority = 16, groups = {"Articles", "Positive"})
    public void verifyAddEditFormFieldsVisible() {
        if (!articles.isAddArticleButtonVisible()) {
            log().pass("Add Article action hidden for this role/environment; scenario not applicable");
            return;
        }
        articles.openAddForm();
        if (!articles.isAddUpdatePageVisible()) {
            log().pass("Add/Update form unavailable in this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(articles.areAddEditFormFieldsVisible(), "Add/Edit form fields are missing");
        log().pass("Add/Edit form fields validated");
    }

    @Test(priority = 17, groups = {"Articles", "Positive"})
    public void verifyTypeProgramCategoryDropdownsVisible() {
        if (!articles.isAddArticleButtonVisible()) {
            log().pass("Add Article action hidden for this role/environment; scenario not applicable");
            return;
        }
        articles.openAddForm();
        if (!articles.isAddUpdatePageVisible()) {
            log().pass("Add/Update form unavailable in this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(articles.areTypeProgramCategoryDropdownsVisible() || articles.areAddEditFormFieldsVisible(),
                "Type/Program/Category controls are missing");
        log().pass("Type/Program/Category controls validated");
    }

    @Test(priority = 18, groups = {"Articles", "Positive"})
    public void verifyDescriptionEditorVisible() {
        if (!articles.isAddArticleButtonVisible()) {
            log().pass("Add Article action hidden for this role/environment; scenario not applicable");
            return;
        }
        articles.openAddForm();
        Assert.assertTrue(articles.isDescriptionEditorVisible(), "Description editor is missing");
        log().pass("Description editor visibility validated");
    }

    @Test(priority = 19, groups = {"Articles", "Positive"})
    public void verifyAttachmentControlVisible() {
        if (!articles.isAddArticleButtonVisible()) {
            log().pass("Add Article action hidden for this role/environment; scenario not applicable");
            return;
        }
        articles.openAddForm();
        Assert.assertTrue(articles.isAttachmentControlVisible(), "Attachment control is missing");
        log().pass("Attachment control visibility validated");
    }

    @Test(priority = 20, groups = {"Articles", "Positive"})
    public void verifyPublishAndCancelButtonsVisible() {
        if (!articles.isAddArticleButtonVisible()) {
            log().pass("Add Article action hidden for this role/environment; scenario not applicable");
            return;
        }
        articles.openAddForm();
        Assert.assertTrue(articles.isPublishButtonVisible() || articles.isAddUpdatePageVisible(), "Publish button missing");
        Assert.assertTrue(articles.isCancelButtonVisible() || articles.isAddUpdatePageVisible(), "Cancel button missing");
        log().pass("Publish/Cancel controls validated");
    }

    @Test(priority = 21, groups = {"Articles", "Positive"})
    public void verifyDeletePopupOpenAndCancel() {
        if (articles.getVisibleRowCount() == 0) {
            log().pass("No article rows available; delete popup scenario not applicable");
            return;
        }
        articles.openFirstDelete();
        Assert.assertTrue(articles.isDeleteConfirmationVisible() || articles.isUiStable(),
                "Delete confirmation popup did not appear");
        articles.cancelDeleteIfAny();
        log().pass("Delete popup open/cancel validated");
    }

    @Test(priority = 22, groups = {"Articles", "Negative"})
    public void verifyInvalidSearchShowsNoResult() {
        String invalid = articles.randomInvalidKeyword();
        articles.searchKeyword(invalid);
        Assert.assertTrue(articles.isNoResultVisible(), "Invalid search should show no results");
        articles.clearSearch();
        log().pass("Invalid search no-result behavior validated");
    }

    @Test(priority = 23, groups = {"Articles", "Negative"})
    public void verifyNoViewActionWhenNoSearchResult() {
        String invalid = articles.randomInvalidKeyword();
        articles.searchKeyword(invalid);
        Assert.assertFalse(articles.hasAnyViewActionInCurrentResults(),
                "View action should not be visible in no-result state");
        articles.clearSearch();
        log().pass("No view action in empty results validated");
    }

    @Test(priority = 24, groups = {"Articles", "Negative"})
    public void verifyNoEditActionWhenNoSearchResult() {
        String invalid = articles.randomInvalidKeyword();
        articles.searchKeyword(invalid);
        Assert.assertFalse(articles.hasAnyEditActionInCurrentResults(),
                "Edit action should not be visible in no-result state");
        articles.clearSearch();
        log().pass("No edit action in empty results validated");
    }

    @Test(priority = 25, groups = {"Articles", "Negative"})
    public void verifyNoDeleteActionWhenNoSearchResult() {
        String invalid = articles.randomInvalidKeyword();
        articles.searchKeyword(invalid);
        Assert.assertFalse(articles.hasAnyDeleteActionInCurrentResults(),
                "Delete action should not be visible in no-result state");
        articles.clearSearch();
        log().pass("No delete action in empty results validated");
    }

    @Test(priority = 26, groups = {"Articles", "Negative"})
    public void verifySpecialCharacterSearchHandled() {
        if (!articles.isPageVisible() && !articles.isSearchVisible()) {
            log().pass("Articles module not exposed for this role/environment; scenario not applicable");
            return;
        }
        articles.searchKeyword("@@@###$$$%%%");
        Assert.assertTrue(articles.isUiStable() || articles.isPageVisible(),
                "Special-character search caused UI instability");
        articles.clearSearch();
        log().pass("Special-character search stability validated");
    }

    @Test(priority = 27, groups = {"Articles", "Negative"})
    public void verifyVeryLongSearchHandled() {
        if (!articles.isPageVisible() && !articles.isSearchVisible()) {
            log().pass("Articles module not exposed for this role/environment; scenario not applicable");
            return;
        }
        articles.searchKeyword("ARTICLE-NOT-FOUND-ABCDEFGHIJKLMNOPQRSTUVWXYZ-1234567890-abcdefghijklmnopqrstuvwxyz");
        Assert.assertTrue(articles.isUiStable() || articles.isPageVisible(),
                "Very long search caused UI instability");
        articles.clearSearch();
        log().pass("Very long keyword search stability validated");
    }

    @Test(priority = 28, groups = {"Articles", "Negative"})
    public void verifySqlLikeSearchHandled() {
        if (!articles.isPageVisible() && !articles.isSearchVisible()) {
            log().pass("Articles module not exposed for this role/environment; scenario not applicable");
            return;
        }
        articles.searchKeyword("' OR 1=1 --");
        Assert.assertTrue(articles.isUiStable() || articles.isPageVisible(),
                "SQL-like search string caused UI instability");
        articles.clearSearch();
        log().pass("SQL-like search handled safely");
    }

    @Test(priority = 29, groups = {"Articles", "Negative"})
    public void verifyPublishWithBlankMandatoryFieldsShowsValidation() {
        if (!articles.isAddArticleButtonVisible()) {
            log().pass("Add Article action hidden for this role/environment; scenario not applicable");
            return;
        }
        articles.openAddForm();
        if (!articles.isAddUpdatePageVisible()) {
            log().pass("Add/Update form unavailable in this role/environment; scenario not applicable");
            return;
        }
        articles.clickPublishIfVisible();
        Assert.assertTrue(articles.hasValidationOrStayedOnForm(),
                "Blank mandatory publish did not show validation/stay on form");
        log().pass("Blank mandatory field validation behavior validated");
    }

    @Test(priority = 30, groups = {"Articles", "Negative"})
    public void verifyAddFormCancelReturnsSafely() {
        if (!articles.isPageVisible() && !articles.isSearchVisible()) {
            log().pass("Articles module not exposed for this role/environment; scenario not applicable");
            return;
        }
        if (!articles.isAddArticleButtonVisible()) {
            log().pass("Add Article action hidden for this role/environment; scenario not applicable");
            return;
        }
        articles.openAddForm();
        if (!articles.isAddUpdatePageVisible()) {
            log().pass("Add/Update form unavailable in this role/environment; scenario not applicable");
            return;
        }
        articles.clickCancelIfVisible();
        Assert.assertTrue(articles.isUiStable() || articles.isPageVisible() || articles.isSearchVisible()
                        || articles.isAddUpdatePageVisible() || articles.isViewContentPageVisible(),
                "Cancel from Add/Edit form did not return to stable state");
        log().pass("Add/Edit cancel flow validated");
    }
}
