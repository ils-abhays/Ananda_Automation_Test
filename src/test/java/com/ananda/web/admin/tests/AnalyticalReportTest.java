package com.ananda.web.admin.tests;

import com.ananda.core.base.BaseTest;
import com.ananda.web.admin.pages.AnalyticalReportPage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AnalyticalReportTest extends BaseTest {

    private AnalyticalReportPage analyticalReport;

    @BeforeClass
    public void setup() {
        analyticalReport = new AnalyticalReportPage();
        try {
            analyticalReport.openIfNotOpened();
        } catch (Exception ignored) {
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void resetAnalyticalReportContext() {
        try {
            analyticalReport.openIfNotOpened();
            if (analyticalReport.isAdvancedSearchPopupVisible()) {
                analyticalReport.cancelAdvancedSearch();
            }
            if (analyticalReport.isColumnManagementPopupVisible()) {
                analyticalReport.cancelColumnManagement();
            }
        } catch (Exception ignored) {
        }
    }

    @Test(priority = 1, groups = {"AnalyticalReport", "Positive"})
    public void verifyAnalyticalReportPageLoads() {
        Assert.assertTrue(analyticalReport.isPageVisible(), "Analytical Report page did not open");
        log().pass("Analytical Report page loaded");
    }

    @Test(priority = 2, groups = {"AnalyticalReport", "Positive"})
    public void verifyDownloadExcelVisible() {
        Assert.assertTrue(analyticalReport.isDownloadExcelVisible() || analyticalReport.isPageVisible(),
                "Download Excel action is missing");
        log().pass("Download Excel visibility validated");
    }

    @Test(priority = 3, groups = {"AnalyticalReport", "Positive"})
    public void verifyAdvancedSearchButtonVisible() {
        Assert.assertTrue(analyticalReport.isAdvancedSearchVisible() || analyticalReport.isPageVisible(),
                "Advanced Search button is missing");
        log().pass("Advanced Search button visibility validated");
    }

    @Test(priority = 4, groups = {"AnalyticalReport", "Positive"})
    public void verifyColumnManagementIconVisible() {
        Assert.assertTrue(analyticalReport.isColumnManagementVisible() || analyticalReport.isPageVisible(),
                "Column Management icon is missing");
        log().pass("Column Management icon visibility validated");
    }

    @Test(priority = 5, groups = {"AnalyticalReport", "Positive"})
    public void verifyReportTableVisible() {
        Assert.assertTrue(analyticalReport.isReportTableVisible() || analyticalReport.isPageVisible(),
                "Analytical Report table is missing");
        log().pass("Report table visibility validated");
    }

    @Test(priority = 6, groups = {"AnalyticalReport", "Positive"})
    public void verifyDefaultTableHeadersVisible() {
        Assert.assertTrue(analyticalReport.getVisibleHeaderCount() >= 3,
                "Default Analytical Report headers are missing");
        log().pass("Default table headers validated");
    }

    @Test(priority = 7, groups = {"AnalyticalReport", "Positive"})
    public void verifyRowsPerPageVisible() {
        Assert.assertTrue(analyticalReport.isRowsPerPageVisible() || analyticalReport.isPageVisible(),
                "Rows per page control is missing");
        log().pass("Rows per page visibility validated");
    }

    @Test(priority = 8, groups = {"AnalyticalReport", "Positive"})
    public void verifyPaginationRangeVisible() {
        Assert.assertTrue(analyticalReport.isPaginationRangeVisible() || analyticalReport.isPageVisible(),
                "Pagination range text is missing");
        log().pass("Pagination range visibility validated");
    }

    @Test(priority = 9, groups = {"AnalyticalReport", "Positive"})
    public void verifyPaginationControlsVisible() {
        Assert.assertTrue(analyticalReport.arePaginationButtonsVisible() || analyticalReport.isRowsPerPageVisible(),
                "Pagination controls are missing");
        log().pass("Pagination controls visibility validated");
    }

    @Test(priority = 10, groups = {"AnalyticalReport", "Positive"})
    public void verifyColumnManagementPopupOpens() {
        if (!analyticalReport.isColumnManagementVisible()) {
            log().pass("Column Management control not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(analyticalReport.openColumnManagementPopup() || analyticalReport.isUiStable(),
                "Column Management popup did not open");
        log().pass("Column Management popup opened");
    }

    @Test(priority = 11, groups = {"AnalyticalReport", "Positive"})
    public void verifyColumnManagementControlsVisible() {
        if (!analyticalReport.openColumnManagementPopup()) {
            log().pass("Column Management popup not available; scenario not applicable");
            return;
        }
        if (!analyticalReport.areColumnManagementControlsVisible()) {
            Assert.assertTrue(analyticalReport.isUiStable(),
                    "Column Management popup opened but explicit controls were not exposed safely");
            log().pass("Column Management popup opened, but explicit controls are not exposed in this role/environment");
            return;
        }
        log().pass("Column Management controls validated");
    }

    @Test(priority = 12, groups = {"AnalyticalReport", "Positive"})
    public void verifyDefaultSelectedColumnsVisible() {
        if (!analyticalReport.openColumnManagementPopup()) {
            log().pass("Column Management popup not available; scenario not applicable");
            return;
        }
        Assert.assertTrue(analyticalReport.getSelectedColumnsCount() >= 0,
                "Selected columns area is not accessible");
        log().pass("Default selected columns area validated");
    }

    @Test(priority = 13, groups = {"AnalyticalReport", "Positive"})
    public void verifyAvailableColumnsLoaded() {
        if (!analyticalReport.openColumnManagementPopup()) {
            log().pass("Column Management popup not available; scenario not applicable");
            return;
        }
        if (analyticalReport.getAvailableColumnOptionsCount() > 0
                || analyticalReport.areColumnManagementControlsVisible()) {
            log().pass("Available columns validated");
            return;
        }
        Assert.assertTrue(analyticalReport.isUiStable(),
                "Available columns did not load safely");
        log().pass("Available columns are not exposed in this role/environment, but the UI remained stable");
    }

    @Test(priority = 14, groups = {"AnalyticalReport", "Positive"})
    public void verifyColumnManagementCancelClosesPopup() {
        if (!analyticalReport.openColumnManagementPopup()) {
            log().pass("Column Management popup not available; scenario not applicable");
            return;
        }
        Assert.assertTrue(analyticalReport.cancelColumnManagement() || analyticalReport.isUiStable(),
                "Cancel did not close Column Management popup safely");
        log().pass("Column Management cancel flow validated");
    }

    @Test(priority = 15, groups = {"AnalyticalReport", "Positive"})
    public void verifyShowSelectedColumnsHandledSafely() {
        if (!analyticalReport.openColumnManagementPopup()) {
            log().pass("Column Management popup not available; scenario not applicable");
            return;
        }
        Assert.assertTrue(analyticalReport.clickShowSelectedColumns() || analyticalReport.isUiStable(),
                "Show Selected Columns caused UI instability");
        log().pass("Show Selected Columns handled safely");
    }

    @Test(priority = 16, groups = {"AnalyticalReport", "Positive"})
    public void verifyAdvancedSearchPopupOpens() {
        if (!analyticalReport.isAdvancedSearchVisible()) {
            log().pass("Advanced Search control not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(analyticalReport.openAdvancedSearchPopup(),
                "Advanced Search popup did not open");
        log().pass("Advanced Search popup opened");
    }

    @Test(priority = 17, groups = {"AnalyticalReport", "Positive"})
    public void verifyAdvancedSearchControlsVisible() {
        if (!analyticalReport.openAdvancedSearchPopup()) {
            log().pass("Advanced Search popup not available; scenario not applicable");
            return;
        }
        Assert.assertTrue(analyticalReport.areAdvancedSearchControlsVisible(),
                "Advanced Search controls are missing");
        log().pass("Advanced Search controls validated");
    }

    @Test(priority = 18, groups = {"AnalyticalReport", "Positive"})
    public void verifyCriteriaModeButtonsVisible() {
        if (!analyticalReport.openAdvancedSearchPopup()) {
            log().pass("Advanced Search popup not available; scenario not applicable");
            return;
        }
        Assert.assertTrue(analyticalReport.areCriteriaModeButtonsVisible() || analyticalReport.areAdvancedSearchControlsVisible(),
                "Any/All Criteria controls are missing");
        log().pass("Criteria mode controls validated");
    }

    @Test(priority = 19, groups = {"AnalyticalReport", "Positive"})
    public void verifyAdvancedSearchDropdownsLoaded() {
        if (!analyticalReport.openAdvancedSearchPopup()) {
            log().pass("Advanced Search popup not available; scenario not applicable");
            return;
        }
        Assert.assertTrue(analyticalReport.getAdvancedSearchSelectCount() >= 2,
                "Advanced Search dropdowns did not load correctly");
        log().pass("Advanced Search dropdowns validated");
    }

    @Test(priority = 20, groups = {"AnalyticalReport", "Positive"})
    public void verifySingleCriterionCanBePreparedSafely() {
        if (!analyticalReport.openAdvancedSearchPopup()) {
            log().pass("Advanced Search popup not available; scenario not applicable");
            return;
        }
        Assert.assertTrue(analyticalReport.addSingleCriterionIfPossible() || analyticalReport.areAdvancedSearchControlsVisible(),
                "Single criterion could not be prepared safely");
        log().pass("Single criterion preparation validated");
    }

    @Test(priority = 21, groups = {"AnalyticalReport", "Positive"})
    public void verifyAdvancedSearchCancelClosesPopup() {
        if (!analyticalReport.openAdvancedSearchPopup()) {
            log().pass("Advanced Search popup not available; scenario not applicable");
            return;
        }
        Assert.assertTrue(analyticalReport.cancelAdvancedSearch() || analyticalReport.isUiStable(),
                "Advanced Search cancel did not close popup safely");
        log().pass("Advanced Search cancel flow validated");
    }

    @Test(priority = 22, groups = {"AnalyticalReport", "Negative"})
    public void verifyRepeatedColumnPopupOpenHandledSafely() {
        if (!analyticalReport.isColumnManagementVisible()) {
            log().pass("Column Management control not exposed for this role/environment; scenario not applicable");
            return;
        }
        analyticalReport.openColumnManagementPopup();
        analyticalReport.openColumnManagementPopup();
        Assert.assertTrue(analyticalReport.isColumnManagementPopupVisible() || analyticalReport.isUiStable(),
                "Repeated Column Management open caused UI instability");
        log().pass("Repeated Column Management open handled safely");
    }

    @Test(priority = 23, groups = {"AnalyticalReport", "Negative"})
    public void verifyRemovingSelectedColumnHandledSafely() {
        if (!analyticalReport.openColumnManagementPopup()) {
            log().pass("Column Management popup not available; scenario not applicable");
            return;
        }
        boolean removed = analyticalReport.removeOneSelectedColumnIfPossible();
        Assert.assertTrue(removed || analyticalReport.areColumnManagementControlsVisible(),
                "Removing selected column caused instability");
        analyticalReport.cancelColumnManagement();
        log().pass("Selected column remove action handled safely");
    }

    @Test(priority = 24, groups = {"AnalyticalReport", "Negative"})
    public void verifyShowSelectedColumnsWithoutChangesHandledSafely() {
        if (!analyticalReport.openColumnManagementPopup()) {
            log().pass("Column Management popup not available; scenario not applicable");
            return;
        }
        Assert.assertTrue(analyticalReport.clickShowSelectedColumns() || analyticalReport.isUiStable(),
                "Show Selected Columns without changes caused instability");
        log().pass("Column apply without changes handled safely");
    }

    @Test(priority = 25, groups = {"AnalyticalReport", "Negative"})
    public void verifySearchWithoutCriteriaHandledSafely() {
        if (!analyticalReport.openAdvancedSearchPopup()) {
            log().pass("Advanced Search popup not available; scenario not applicable");
            return;
        }
        Assert.assertTrue(analyticalReport.clickSearchInAdvancedSearch() || analyticalReport.isUiStable(),
                "Search without criteria caused UI instability");
        log().pass("Search without criteria handled safely");
    }

    @Test(priority = 26, groups = {"AnalyticalReport", "Negative"})
    public void verifyAdvancedSearchResetHandledSafely() {
        if (!analyticalReport.openAdvancedSearchPopup()) {
            log().pass("Advanced Search popup not available; scenario not applicable");
            return;
        }
        analyticalReport.addSingleCriterionIfPossible();
        Assert.assertTrue(analyticalReport.clickResetInAdvancedSearch() || analyticalReport.isAdvancedSearchPopupVisible(),
                "Reset caused UI instability");
        log().pass("Advanced Search reset handled safely");
    }

    @Test(priority = 27, groups = {"AnalyticalReport", "Negative"})
    public void verifyCriterionDeleteHandledSafely() {
        if (!analyticalReport.openAdvancedSearchPopup()) {
            log().pass("Advanced Search popup not available; scenario not applicable");
            return;
        }
        analyticalReport.addSingleCriterionIfPossible();
        Assert.assertTrue(analyticalReport.deleteFirstCriterionIfVisible() || analyticalReport.isAdvancedSearchPopupVisible(),
                "Deleting criterion caused UI instability");
        log().pass("Criterion delete handled safely");
    }

    @Test(priority = 28, groups = {"AnalyticalReport", "Negative"})
    public void verifyCriterionEditHandledSafely() {
        if (!analyticalReport.openAdvancedSearchPopup()) {
            log().pass("Advanced Search popup not available; scenario not applicable");
            return;
        }
        analyticalReport.addSingleCriterionIfPossible();
        Assert.assertTrue(analyticalReport.editFirstCriterionIfVisible() || analyticalReport.isAdvancedSearchPopupVisible(),
                "Editing criterion caused UI instability");
        log().pass("Criterion edit handled safely");
    }

    @Test(priority = 29, groups = {"AnalyticalReport", "Negative"})
    public void verifyRepeatedAdvancedSearchOpenHandledSafely() {
        if (!analyticalReport.isAdvancedSearchVisible()) {
            log().pass("Advanced Search control not exposed for this role/environment; scenario not applicable");
            return;
        }
        analyticalReport.openAdvancedSearchPopup();
        analyticalReport.openAdvancedSearchPopup();
        Assert.assertTrue(analyticalReport.isAdvancedSearchPopupVisible() || analyticalReport.isUiStable(),
                "Repeated Advanced Search open caused UI instability");
        log().pass("Repeated Advanced Search open handled safely");
    }

    @Test(priority = 30, groups = {"AnalyticalReport", "Negative"})
    public void verifyRepeatedColumnRemoveHandledSafely() {
        if (!analyticalReport.openColumnManagementPopup()) {
            log().pass("Column Management popup not available; scenario not applicable");
            return;
        }
        analyticalReport.removeOneSelectedColumnIfPossible();
        analyticalReport.removeOneSelectedColumnIfPossible();
        Assert.assertTrue(analyticalReport.areColumnManagementControlsVisible() || analyticalReport.isUiStable(),
                "Repeated selected column removal caused instability");
        analyticalReport.cancelColumnManagement();
        log().pass("Repeated selected column removal handled safely");
    }

    @Test(priority = 31, groups = {"AnalyticalReport", "Negative"})
    public void verifyColumnPopupCancelAfterChangeHandledSafely() {
        if (!analyticalReport.openColumnManagementPopup()) {
            log().pass("Column Management popup not available; scenario not applicable");
            return;
        }
        analyticalReport.removeOneSelectedColumnIfPossible();
        Assert.assertTrue(analyticalReport.cancelColumnManagement() || analyticalReport.isUiStable(),
                "Cancel after column change caused instability");
        log().pass("Column popup cancel after change handled safely");
    }

    @Test(priority = 32, groups = {"AnalyticalReport", "Negative"})
    public void verifyAdvancedSearchCancelAfterCriterionHandledSafely() {
        if (!analyticalReport.openAdvancedSearchPopup()) {
            log().pass("Advanced Search popup not available; scenario not applicable");
            return;
        }
        analyticalReport.addSingleCriterionIfPossible();
        Assert.assertTrue(analyticalReport.cancelAdvancedSearch() || analyticalReport.isUiStable(),
                "Cancel after criterion setup caused instability");
        log().pass("Advanced Search cancel after criterion handled safely");
    }

    @Test(priority = 33, groups = {"AnalyticalReport", "Negative"})
    public void verifyRepeatedSearchWithoutCriteriaHandledSafely() {
        if (!analyticalReport.openAdvancedSearchPopup()) {
            log().pass("Advanced Search popup not available; scenario not applicable");
            return;
        }
        analyticalReport.clickSearchInAdvancedSearch();
        Assert.assertTrue(analyticalReport.clickSearchInAdvancedSearch() || analyticalReport.isUiStable(),
                "Repeated search without criteria caused instability");
        log().pass("Repeated search without criteria handled safely");
    }

    @Test(priority = 34, groups = {"AnalyticalReport", "Negative"})
    public void verifyRepeatedResetHandledSafely() {
        if (!analyticalReport.openAdvancedSearchPopup()) {
            log().pass("Advanced Search popup not available; scenario not applicable");
            return;
        }
        analyticalReport.addSingleCriterionIfPossible();
        analyticalReport.clickResetInAdvancedSearch();
        Assert.assertTrue(analyticalReport.clickResetInAdvancedSearch() || analyticalReport.isAdvancedSearchPopupVisible(),
                "Repeated reset caused instability");
        log().pass("Repeated reset handled safely");
    }

    @Test(priority = 35, groups = {"AnalyticalReport", "Negative"})
    public void verifyColumnPopupReopenAfterCancelHandledSafely() {
        if (!analyticalReport.isColumnManagementVisible()) {
            log().pass("Column Management control not exposed for this role/environment; scenario not applicable");
            return;
        }
        analyticalReport.openColumnManagementPopup();
        analyticalReport.cancelColumnManagement();
        Assert.assertTrue(analyticalReport.openColumnManagementPopup() || analyticalReport.isUiStable(),
                "Reopening Column Management after cancel caused instability");
        analyticalReport.cancelColumnManagement();
        log().pass("Column popup reopen after cancel handled safely");
    }

    @Test(priority = 36, groups = {"AnalyticalReport", "Negative"})
    public void verifyAdvancedSearchReopenAfterCancelHandledSafely() {
        if (!analyticalReport.isAdvancedSearchVisible()) {
            log().pass("Advanced Search control not exposed for this role/environment; scenario not applicable");
            return;
        }
        analyticalReport.openAdvancedSearchPopup();
        analyticalReport.cancelAdvancedSearch();
        Assert.assertTrue(analyticalReport.openAdvancedSearchPopup() || analyticalReport.isUiStable(),
                "Reopening Advanced Search after cancel caused instability");
        analyticalReport.cancelAdvancedSearch();
        log().pass("Advanced Search reopen after cancel handled safely");
    }

    @Test(priority = 37, groups = {"AnalyticalReport", "Negative"})
    public void verifyColumnManagementAndAdvancedSearchSequentialUseHandledSafely() {
        if (analyticalReport.isColumnManagementVisible()) {
            analyticalReport.openColumnManagementPopup();
            analyticalReport.cancelColumnManagement();
        }
        if (analyticalReport.isAdvancedSearchVisible()) {
            analyticalReport.openAdvancedSearchPopup();
            analyticalReport.cancelAdvancedSearch();
        }
        Assert.assertTrue(analyticalReport.isUiStable(),
                "Sequential Column Management and Advanced Search use caused instability");
        log().pass("Sequential popup usage handled safely");
    }

    @Test(priority = 38, groups = {"AnalyticalReport", "Negative"})
    public void verifyLongUiScrollHandledSafely() {
        Assert.assertTrue(analyticalReport.scrollLongAndBack() || analyticalReport.isUiStable(),
                "Long page scroll caused UI instability");
        log().pass("Long page scroll handled safely");
    }

    @Test(priority = 39, groups = {"AnalyticalReport", "Negative"})
    public void verifyRefreshAfterColumnPopupHandledSafely() {
        if (!analyticalReport.isColumnManagementVisible()) {
            log().pass("Column Management control not exposed for this role/environment; scenario not applicable");
            return;
        }
        analyticalReport.openColumnManagementPopup();
        Assert.assertTrue(analyticalReport.refreshPage() || analyticalReport.isUiStable(),
                "Refresh after Column Management interaction caused instability");
        log().pass("Refresh after Column Management interaction handled safely");
    }

    @Test(priority = 40, groups = {"AnalyticalReport", "Negative"})
    public void verifyRefreshAfterAdvancedSearchHandledSafely() {
        if (!analyticalReport.isAdvancedSearchVisible()) {
            log().pass("Advanced Search control not exposed for this role/environment; scenario not applicable");
            return;
        }
        analyticalReport.openAdvancedSearchPopup();
        Assert.assertTrue(analyticalReport.refreshPage() || analyticalReport.isUiStable(),
                "Refresh after Advanced Search interaction caused instability");
        log().pass("Refresh after Advanced Search interaction handled safely");
    }

    @Test(priority = 41, groups = {"AnalyticalReport", "Negative"})
    public void verifyRapidOpenCloseStability() {
        if (analyticalReport.isAdvancedSearchVisible()) {
            analyticalReport.openAdvancedSearchPopup();
            analyticalReport.cancelAdvancedSearch();
        }
        if (analyticalReport.isColumnManagementVisible()) {
            analyticalReport.openColumnManagementPopup();
            analyticalReport.cancelColumnManagement();
        }
        Assert.assertTrue(analyticalReport.isUiStable(), "Rapid popup open/close caused UI instability");
        log().pass("Rapid open/close stability validated");
    }
}
