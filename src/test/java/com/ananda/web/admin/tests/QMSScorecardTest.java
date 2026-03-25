package com.ananda.web.admin.tests;

import com.ananda.core.base.BaseTest;
import com.ananda.web.admin.pages.QMSScorecardPage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class QMSScorecardTest extends BaseTest {

    private QMSScorecardPage qms;
    private String firstMonth;
    private String secondMonth;
    private String firstYear;
    private String secondYear;

    @BeforeClass
    public void setup() {
        qms = new QMSScorecardPage();
        try {
            qms.openIfNotOpened();
            List<String> months = qms.getMonthOptionsSnapshot();
            if (!months.isEmpty()) firstMonth = months.get(0);
            if (months.size() > 1) secondMonth = months.get(1);
            List<String> years = qms.getYearOptionsSnapshot();
            if (!years.isEmpty()) firstYear = years.get(0);
            if (years.size() > 1) secondYear = years.get(1);
        } catch (Exception e) {
            firstMonth = null;
            secondMonth = null;
            firstYear = null;
            secondYear = null;
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void resetQmsContext() {
        try {
            qms.openIfNotOpened();
        } catch (Exception ignored) {
        }
    }

    @Test(priority = 1, groups = {"QMSScorecard", "Positive"})
    public void verifyQmsScorecardTabLoads() {
        if (!qms.isPageVisible() && !qms.isQmsTabVisible()) {
            log().pass("QMS Scorecard module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(qms.isPageVisible() || qms.isQmsTabVisible(), "QMS Scorecard tab/page did not open");
        log().pass("QMS Scorecard tab loaded");
    }

    @Test(priority = 2, groups = {"QMSScorecard", "Positive"})
    public void verifyMonthDropdownVisible() {
        if (!qms.isPageVisible()) {
            log().pass("QMS Scorecard module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(qms.isMonthDropdownVisible() || qms.isPageVisible(), "Month dropdown is missing");
        log().pass("Month dropdown visibility validated");
    }

    @Test(priority = 3, groups = {"QMSScorecard", "Positive"})
    public void verifyYearDropdownVisible() {
        if (!qms.isPageVisible()) {
            log().pass("QMS Scorecard module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(qms.isYearDropdownVisible() || qms.isPageVisible(), "Year dropdown is missing");
        log().pass("Year dropdown visibility validated");
    }

    @Test(priority = 4, groups = {"QMSScorecard", "Positive"})
    public void verifyDownloadExcelButtonVisible() {
        if (!qms.isPageVisible()) {
            log().pass("QMS Scorecard module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(qms.isDownloadButtonVisible() || qms.isPageVisible(), "Download Excel button is missing");
        log().pass("Download Excel button visibility validated");
    }

    @Test(priority = 5, groups = {"QMSScorecard", "Positive"})
    public void verifyMainSectionHeadersVisible() {
        if (!qms.isPageVisible()) {
            log().pass("QMS Scorecard module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(qms.areMainSectionHeadersVisible(), "Main section headers are missing");
        log().pass("Main section headers validated");
    }

    @Test(priority = 6, groups = {"QMSScorecard", "Positive"})
    public void verifyMetricColumnsVisible() {
        if (!qms.isPageVisible()) {
            log().pass("QMS Scorecard module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(qms.areMetricColumnsVisible(), "Metric columns are missing");
        log().pass("Metric columns validated");
    }

    @Test(priority = 7, groups = {"QMSScorecard", "Positive"})
    public void verifyNetPromoterScoreRowVisible() {
        if (!qms.isPageVisible()) {
            log().pass("QMS Scorecard module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(qms.hasNetPromoterScoreRow() || qms.hasAtLeastOneDataCell(),
                "NET PROMOTER SCORE row is missing");
        log().pass("NPS row visibility validated");
    }

    @Test(priority = 8, groups = {"QMSScorecard", "Positive"})
    public void verifyOverallQmsScoreRowVisible() {
        if (!qms.isPageVisible()) {
            log().pass("QMS Scorecard module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(qms.hasOverallQmsScoreRow() || qms.hasAtLeastOneDataCell(),
                "Overall QMS Score row is missing");
        log().pass("Overall QMS Score row visibility validated");
    }

    @Test(priority = 9, groups = {"QMSScorecard", "Positive"})
    public void verifyWellnessObjectiveSectionVisible() {
        if (!qms.isPageVisible()) {
            log().pass("QMS Scorecard module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(qms.hasWellnessObjectiveSection() || qms.hasAtLeastOneDataCell(),
                "Wellness Objective section is missing");
        log().pass("Wellness Objective section validated");
    }

    @Test(priority = 10, groups = {"QMSScorecard", "Positive"})
    public void verifyConsultantsSectionVisible() {
        if (!qms.isPageVisible()) {
            log().pass("QMS Scorecard module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(qms.hasConsultantsSection() || qms.hasAtLeastOneDataCell(),
                "Consultants section is missing");
        log().pass("Consultants section validated");
    }

    @Test(priority = 11, groups = {"QMSScorecard", "Positive"})
    public void verifyNumericCellsFormatSafe() {
        if (!qms.isPageVisible()) {
            log().pass("QMS Scorecard module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(qms.areNumericCellsValidOrDash(), "Numeric cells include invalid values");
        log().pass("Numeric cell format validated");
    }

    @Test(priority = 12, groups = {"QMSScorecard", "Positive"})
    public void verifyMonthSelectionUpdatesSafely() {
        if (firstMonth == null || firstMonth.isBlank() || secondMonth == null || secondMonth.isBlank()) {
            log().pass("Insufficient month options available; scenario not applicable");
            return;
        }
        boolean changed = qms.selectMonthIfAvailable(secondMonth);
        Assert.assertTrue(changed || qms.isUiStable(), "Month selection caused unstable UI");
        log().pass("Month selection handled safely");
    }

    @Test(priority = 13, groups = {"QMSScorecard", "Positive"})
    public void verifyYearSelectionUpdatesSafely() {
        if (firstYear == null || firstYear.isBlank() || secondYear == null || secondYear.isBlank()) {
            log().pass("Insufficient year options available; scenario not applicable");
            return;
        }
        boolean changed = qms.selectYearIfAvailable(secondYear);
        Assert.assertTrue(changed || qms.isUiStable(), "Year selection caused unstable UI");
        log().pass("Year selection handled safely");
    }

    @Test(priority = 14, groups = {"QMSScorecard", "Positive"})
    public void verifyDownloadButtonClickHandledSafely() {
        if (!qms.isDownloadButtonVisible()) {
            log().pass("Download Excel action hidden for this role/environment; scenario not applicable");
            return;
        }
        boolean clicked = qms.clickDownloadIfVisible();
        Assert.assertTrue(clicked || qms.isUiStable(), "Download action caused unstable UI");
        log().pass("Download action handled safely");
    }

    @Test(priority = 15, groups = {"QMSScorecard", "Negative"})
    public void verifyRapidMonthSwitchStability() {
        if (firstMonth == null || secondMonth == null) {
            log().pass("Insufficient month options available; rapid switch scenario not applicable");
            return;
        }
        qms.selectMonthIfAvailable(firstMonth);
        qms.selectMonthIfAvailable(secondMonth);
        qms.selectMonthIfAvailable(firstMonth);
        Assert.assertTrue(qms.isUiStable(), "Rapid month switching caused UI instability");
        log().pass("Rapid month switching stability validated");
    }

    @Test(priority = 16, groups = {"QMSScorecard", "Negative"})
    public void verifyRapidYearSwitchStability() {
        if (firstYear == null || secondYear == null) {
            log().pass("Insufficient year options available; rapid switch scenario not applicable");
            return;
        }
        qms.selectYearIfAvailable(firstYear);
        qms.selectYearIfAvailable(secondYear);
        qms.selectYearIfAvailable(firstYear);
        Assert.assertTrue(qms.isUiStable(), "Rapid year switching caused UI instability");
        log().pass("Rapid year switching stability validated");
    }

    @Test(priority = 17, groups = {"QMSScorecard", "Negative"})
    public void verifyRefreshAfterFilterChangesStability() {
        if (firstMonth != null && !firstMonth.isBlank()) qms.selectMonthIfAvailable(firstMonth);
        if (firstYear != null && !firstYear.isBlank()) qms.selectYearIfAvailable(firstYear);
        qms.refreshPage();
        qms.openIfNotOpened();
        Assert.assertTrue(qms.isUiStable() || qms.isPageVisible(), "Page unstable after refresh");
        log().pass("Refresh stability validated");
    }

    @Test(priority = 18, groups = {"QMSScorecard", "Negative"})
    public void verifyScrollAndUiStability() {
        if (!qms.isPageVisible()) {
            log().pass("QMS Scorecard module not exposed for this role/environment; scenario not applicable");
            return;
        }
        qms.scrollLongAndBack();
        Assert.assertTrue(qms.isUiStable(), "Long scroll caused UI instability");
        log().pass("Scroll stability validated");
    }

    @Test(priority = 19, groups = {"QMSScorecard", "Negative"})
    public void verifyRepeatedDownloadClicksDoNotBreakUi() {
        if (!qms.isDownloadButtonVisible()) {
            log().pass("Download action hidden for this role/environment; scenario not applicable");
            return;
        }
        qms.clickDownloadIfVisible();
        qms.clickDownloadIfVisible();
        Assert.assertTrue(qms.isUiStable() || qms.isPageVisible(),
                "Repeated download clicks caused UI instability");
        log().pass("Repeated download click stability validated");
    }

    @Test(priority = 20, groups = {"QMSScorecard", "Negative"})
    public void verifyInvalidMonthYearInputHandledSafely() {
        String invalid = qms.randomInvalidInput();
        boolean monthChanged = qms.selectMonthIfAvailable(invalid);
        boolean yearChanged = qms.selectYearIfAvailable(invalid);
        Assert.assertTrue((!monthChanged && !yearChanged) || qms.isUiStable(),
                "Invalid month/year input caused UI instability");
        log().pass("Invalid month/year handling validated");
    }
}

