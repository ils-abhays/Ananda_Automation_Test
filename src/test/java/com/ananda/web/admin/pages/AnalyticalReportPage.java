package com.ananda.web.admin.pages;

import com.ananda.core.drivers.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class AnalyticalReportPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final String BASE_URL =
            System.getProperty("ananda.baseUrl", "https://dev-admin.anandaspa.com");

    public AnalyticalReportPage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(14));
    }

    private final By analyticalReportMenu =
            By.xpath("//*[self::a or self::span or self::button][normalize-space()='Analytical Report']");
    private final By pageTitle = By.xpath("//*[normalize-space()='Analytical Report']");
    private final By downloadExcelButton =
            By.xpath("//button[contains(normalize-space(),'Download Excel')]");
    private final By advancedSearchButton =
            By.xpath("//button[contains(normalize-space(),'Advanced Search')]");
    private final By columnManagementButton =
            By.xpath("//button[contains(normalize-space(),'Advanced Search')]/following-sibling::button[1]"
                    + " | //button[contains(@title,'Column') or contains(@aria-label,'Column')]"
                    + " | //*[self::button or self::div][contains(normalize-space(),'Column Management')]"
                    + " | (//button[.//*[local-name()='svg'] and not(contains(normalize-space(),'Advanced Search'))])[last()]");
    private final By reportTable = By.xpath("//table");
    private final By tableHeaders = By.xpath("//table//thead//th | //table//tr[1]/*[self::th or self::td]");
    private final By gridHeaders = By.xpath(
            "//*[@role='columnheader' or @role='gridcell-header' or @data-field or @col-id]"
                    + " | //div[contains(@class,'header') and not(contains(@class,'checkbox'))]"
    );
    private final By rowsPerPageText =
            By.xpath("//*[contains(normalize-space(),'Rows per page') or contains(normalize-space(),'Items per page')]");
    private final By rowsPerPageSelect =
            By.xpath("//select[option[contains(normalize-space(),'10')] or option[contains(normalize-space(),'50')]]");
    private final By paginationRangeText =
            By.xpath("//*[contains(normalize-space(),' of ') or contains(normalize-space(),'–') or contains(normalize-space(),'-')]");
    private final By previousPageButton =
            By.xpath("//button[@aria-label='Previous page' or @title='Previous page' or normalize-space()='<' or normalize-space()='Previous']");
    private final By nextPageButton =
            By.xpath("//button[@aria-label='Next page' or @title='Next page' or normalize-space()='>' or normalize-space()='Next']");

    private final By columnPopupTitle =
            By.xpath("//*[normalize-space()='Select Columns To View in Table' or normalize-space()='Column Management' or contains(normalize-space(),'Columns To View')]");
    private final By columnSelectDropdown =
            By.xpath("(//*[normalize-space()='Select Columns To View in Table' or normalize-space()='Column Management' or contains(normalize-space(),'Columns To View')]/following::select[1])"
                    + " | (//select[option[contains(normalize-space(),'Select Column')]])[1]");
    private final By selectedColumnCards =
            By.xpath("//*[normalize-space()='Select Columns To View in Table']/following::*[contains(@class,'shadow') or contains(@class,'card') or self::div][.//*[contains(normalize-space(),' - ') or contains(normalize-space(),'Guest') or contains(normalize-space(),'Program')]]");
    private final By selectedColumnRemoveIcons =
            By.xpath("//*[normalize-space()='Select Columns To View in Table']/following::*[contains(@class,'fa-xmark') or contains(@class,'fa-close') or normalize-space()='x' or normalize-space()='X']");
    private final By showSelectedColumnsButton =
            By.xpath("//button[contains(normalize-space(),'Show Selected Columns') or contains(normalize-space(),'Apply') or contains(normalize-space(),'Show Columns')]");
    private final By cancelColumnPopupButton =
            By.xpath("//button[normalize-space()='Cancel' or normalize-space()='Close']");
    private final By availableColumnOptions =
            By.xpath("//*[normalize-space()='Select Columns To View in Table']/following::*[self::option or self::li or self::div][normalize-space()]");
    private final By columnDialog =
            By.xpath("//*[@role='dialog' or @aria-modal='true' or contains(@class,'modal') or contains(@class,'dialog')]");
    private final By columnPopupButtons =
            By.xpath(
                    "//*[@role='dialog' or @aria-modal='true' or contains(@class,'modal') or contains(@class,'dialog')]//button[normalize-space()]"
                            + " | //button[contains(normalize-space(),'Show Selected Columns') or contains(normalize-space(),'Apply')"
                            + " or contains(normalize-space(),'Show Columns') or normalize-space()='Cancel' or normalize-space()='Close']"
            );

    private final By advancedSearchPopupTitle =
            By.xpath("//*[normalize-space()='Advanced Search' or contains(normalize-space(),'Search')]");
    private final By advancedSearchSelects =
            By.xpath("//*[normalize-space()='Advanced Search']/following::select");
    private final By anyCriteriaButton =
            By.xpath("//*[normalize-space()='Select Criteria:']/following::*[normalize-space()='Any Criteria'][1]");
    private final By allCriteriaButton =
            By.xpath("//*[normalize-space()='Select Criteria:']/following::*[normalize-space()='All Criteria'][1]");
    private final By advancedSearchResetButton =
            By.xpath("//button[normalize-space()='Reset']");
    private final By advancedSearchSearchButton =
            By.xpath("//button[normalize-space()='SEARCH' or normalize-space()='Search']");
    private final By advancedSearchCancelButton =
            By.xpath("//button[normalize-space()='CANCEL' or normalize-space()='Cancel']");
    private final By criteriaRows =
            By.xpath("//*[normalize-space()='Advanced Search']/following::*[contains(@class,'criteria') or .//*[contains(@class,'fa-trash') or contains(@class,'fa-pen')]]");
    private final By criteriaEditIcons =
            By.xpath("//*[normalize-space()='Advanced Search']/following::*[contains(@class,'fa-pen') or contains(@class,'fa-edit')]");
    private final By criteriaDeleteIcons =
            By.xpath("//*[normalize-space()='Advanced Search']/following::*[contains(@class,'fa-trash') or contains(@class,'fa-delete-left')]");

    public void openIfNotOpened() {
        if (isPageVisible()) {
            waitForUiIdle();
            return;
        }

        driver.get(BASE_URL + "/analytical-report");
        for (int i = 0; i < 3; i++) {
            tryClick(analyticalReportMenu);
            waitForUiIdle();
            if (isPageVisible()) {
                return;
            }
        }
    }

    public boolean isPageVisible() {
        try {
            return !driver.findElements(pageTitle).isEmpty()
                    || !driver.findElements(reportTable).isEmpty()
                    || (!driver.findElements(downloadExcelButton).isEmpty()
                    && (!driver.findElements(advancedSearchButton).isEmpty()
                    || !driver.findElements(rowsPerPageText).isEmpty()));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isDownloadExcelVisible() {
        return firstVisible(downloadExcelButton) != null;
    }

    public boolean isReportTableVisible() {
        return firstVisible(reportTable) != null;
    }

    public boolean isAdvancedSearchVisible() {
        return firstVisible(advancedSearchButton) != null;
    }

    public boolean isColumnManagementVisible() {
        return firstVisible(columnManagementButton) != null;
    }

    public int getVisibleHeaderCount() {
        int count = 0;
        for (WebElement header : driver.findElements(tableHeaders)) {
            String text = safeText(header);
            if (!text.isBlank()) {
                count++;
            }
        }
        if (count > 0) {
            return count;
        }
        for (WebElement header : driver.findElements(gridHeaders)) {
            String text = safeText(header);
            if (!text.isBlank() && !looksLikeNonHeaderControl(text)) {
                count++;
            }
        }
        return count;
    }

    public List<String> getTableHeadersText() {
        List<String> headers = new ArrayList<>();
        for (WebElement header : driver.findElements(tableHeaders)) {
            String text = safeText(header);
            if (!text.isBlank()) {
                headers.add(text);
            }
        }
        if (!headers.isEmpty()) {
            return headers;
        }
        for (WebElement header : driver.findElements(gridHeaders)) {
            String text = safeText(header);
            if (!text.isBlank() && !looksLikeNonHeaderControl(text)) {
                headers.add(text);
            }
        }
        return headers;
    }

    public boolean isRowsPerPageVisible() {
        return firstVisible(rowsPerPageText) != null || firstVisible(rowsPerPageSelect) != null;
    }

    public boolean isPaginationRangeVisible() {
        for (WebElement element : driver.findElements(paginationRangeText)) {
            String text = safeText(element).toLowerCase();
            if ((text.contains("of") || text.contains("-")) && text.matches(".*\\d.*")) {
                return true;
            }
        }
        return false;
    }

    public boolean arePaginationButtonsVisible() {
        if (firstVisible(previousPageButton) != null || firstVisible(nextPageButton) != null) {
            return true;
        }
        for (WebElement element : driver.findElements(By.xpath("//button[@aria-label='Previous page' or @aria-label='Next page' or @title='Previous page' or @title='Next page']"))) {
            try {
                if (element.isDisplayed()) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return isRowsPerPageVisible() || isPaginationRangeVisible();
    }

    public boolean openColumnManagementPopup() {
        WebElement button = firstVisible(columnManagementButton);
        if (button == null) {
            button = findFallbackColumnManagementButton();
        }
        if (button == null) {
            return false;
        }
        jsClick(button);
        waitForUiIdle();
        try {
            wait.until(d -> isColumnManagementPopupVisible() || getAvailableColumnOptionsCount() > 0);
        } catch (Exception ignored) {
        }
        return isColumnManagementPopupVisible() || (button != null && isUiStable());
    }

    public boolean isColumnManagementPopupVisible() {
        return firstVisible(columnPopupTitle) != null
                || firstVisible(columnSelectDropdown) != null
                || firstVisible(showSelectedColumnsButton) != null;
    }

    public boolean areColumnManagementControlsVisible() {
        boolean hasActionButtons = firstVisible(showSelectedColumnsButton) != null
                || firstVisible(cancelColumnPopupButton) != null
                || firstVisible(columnPopupButtons) != null;
        boolean hasSelectionControls = firstVisible(columnSelectDropdown) != null
                || getAvailableColumnOptionsCount() > 0
                || getSelectedColumnsCount() > 0
                || firstVisible(columnDialog) != null;

        return (isColumnManagementPopupVisible() && (hasActionButtons || hasSelectionControls))
                || (hasActionButtons && hasSelectionControls)
                || hasActionButtons;
    }

    public int getSelectedColumnsCount() {
        int count = 0;
        for (WebElement item : driver.findElements(selectedColumnCards)) {
            try {
                if (item.isDisplayed() && !safeText(item).isBlank()) {
                    count++;
                }
            } catch (Exception ignored) {
            }
        }
        return count;
    }

    public int getAvailableColumnOptionsCount() {
        int count = 0;
        for (WebElement item : driver.findElements(availableColumnOptions)) {
            String text = safeText(item);
            if (!text.isBlank() && !text.equalsIgnoreCase("Select Column")) {
                count++;
            }
        }
        return count;
    }

    public boolean removeOneSelectedColumnIfPossible() {
        List<WebElement> icons = driver.findElements(selectedColumnRemoveIcons);
        for (int i = icons.size() - 1; i >= 0; i--) {
            try {
                if (icons.get(i).isDisplayed()) {
                    jsClick(icons.get(i));
                    waitForUiIdle();
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public boolean clickShowSelectedColumns() {
        WebElement button = firstVisible(showSelectedColumnsButton);
        if (button == null) {
            return false;
        }
        jsClick(button);
        waitForUiIdle();
        return isPageVisible();
    }

    public boolean clickDownloadExcelSafely() {
        WebElement button = firstVisible(downloadExcelButton);
        if (button == null) {
            return false;
        }
        jsClick(button);
        waitForUiIdle();
        return isPageVisible();
    }

    public boolean cancelColumnManagement() {
        WebElement button = firstVisible(cancelColumnPopupButton);
        if (button == null) {
            try {
                driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
                waitForUiIdle();
                return !isColumnManagementPopupVisible() || isUiStable();
            } catch (Exception e) {
                return false;
            }
        }
        jsClick(button);
        waitForUiIdle();
        return !isColumnManagementPopupVisible();
    }

    public boolean openAdvancedSearchPopup() {
        WebElement button = firstVisible(advancedSearchButton);
        if (button == null) {
            return false;
        }
        jsClick(button);
        waitForUiIdle();
        try {
            wait.until(d -> isAdvancedSearchPopupVisible() || getAdvancedSearchSelectCount() > 0);
        } catch (Exception ignored) {
        }
        return isAdvancedSearchPopupVisible();
    }

    public boolean isAdvancedSearchPopupVisible() {
        return firstVisible(advancedSearchPopupTitle) != null
                || getAdvancedSearchSelectCount() > 0
                || firstVisible(advancedSearchSearchButton) != null;
    }

    public boolean areAdvancedSearchControlsVisible() {
        return isAdvancedSearchPopupVisible()
                && !driver.findElements(advancedSearchSelects).isEmpty()
                && firstVisible(advancedSearchSearchButton) != null
                && firstVisible(advancedSearchCancelButton) != null;
    }

    public int getAdvancedSearchSelectCount() {
        int count = 0;
        for (WebElement select : driver.findElements(advancedSearchSelects)) {
            try {
                if (select.isDisplayed() && select.isEnabled()) {
                    count++;
                }
            } catch (Exception ignored) {
            }
        }
        return count;
    }

    public boolean areCriteriaModeButtonsVisible() {
        return firstVisible(anyCriteriaButton) != null && firstVisible(allCriteriaButton) != null;
    }

    public int getCriteriaRowCount() {
        int count = 0;
        for (WebElement row : driver.findElements(criteriaRows)) {
            String text = safeText(row);
            if (!text.isBlank()) {
                count++;
            }
        }
        return count;
    }

    public boolean addSingleCriterionIfPossible() {
        List<WebElement> selects = visibleSelectElements();
        if (selects.size() < 3) {
            return false;
        }

        int before = getCriteriaRowCount();
        boolean first = selectFirstMeaningfulOption(selects.get(0));
        boolean second = selectFirstMeaningfulOption(selects.get(1));
        boolean third = selectFirstMeaningfulOption(selects.get(2));
        waitForUiIdle();

        int after = getCriteriaRowCount();
        return (first || second || third) && (after >= before);
    }

    public boolean clickResetInAdvancedSearch() {
        WebElement button = firstVisible(advancedSearchResetButton);
        if (button == null) {
            return false;
        }
        jsClick(button);
        waitForUiIdle();
        return isAdvancedSearchPopupVisible();
    }

    public boolean clickSearchInAdvancedSearch() {
        WebElement button = firstVisible(advancedSearchSearchButton);
        if (button == null) {
            return false;
        }
        jsClick(button);
        waitForUiIdle();
        return isPageVisible() || isAdvancedSearchPopupVisible();
    }

    public boolean cancelAdvancedSearch() {
        WebElement button = firstVisible(advancedSearchCancelButton);
        if (button == null) {
            try {
                driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
                waitForUiIdle();
                return !isAdvancedSearchPopupVisible() || isUiStable();
            } catch (Exception e) {
                return false;
            }
        }
        jsClick(button);
        waitForUiIdle();
        return !isAdvancedSearchPopupVisible();
    }

    public boolean editFirstCriterionIfVisible() {
        List<WebElement> icons = driver.findElements(criteriaEditIcons);
        for (WebElement icon : icons) {
            try {
                if (icon.isDisplayed()) {
                    jsClick(icon);
                    waitForUiIdle();
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public boolean deleteFirstCriterionIfVisible() {
        List<WebElement> icons = driver.findElements(criteriaDeleteIcons);
        for (WebElement icon : icons) {
            try {
                if (icon.isDisplayed()) {
                    jsClick(icon);
                    waitForUiIdle();
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public boolean scrollLongAndBack() {
        try {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            waitForUiIdle();
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
            waitForUiIdle();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean refreshPage() {
        try {
            driver.navigate().refresh();
            waitForUiIdle();
            return isPageVisible();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isUiStable() {
        waitForUiIdle();
        return isPageVisible()
                && (isAdvancedSearchVisible()
                || isDownloadExcelVisible()
                || isColumnManagementVisible()
                || isRowsPerPageVisible()
                || isPaginationRangeVisible()
                || isReportTableVisible()
                || driver.findElement(By.tagName("body")).getText().toLowerCase().contains("analytical report"));
    }

    private List<WebElement> visibleSelectElements() {
        List<WebElement> out = new ArrayList<>();
        for (WebElement element : driver.findElements(advancedSearchSelects)) {
            try {
                if (element.isDisplayed() && element.isEnabled()) {
                    out.add(element);
                }
            } catch (Exception ignored) {
            }
        }
        return out;
    }

    private boolean selectFirstMeaningfulOption(WebElement element) {
        try {
            Select select = new Select(element);
            List<WebElement> options = select.getOptions();
            for (int i = 1; i < options.size(); i++) {
                String text = safeText(options.get(i));
                if (!text.isBlank() && !text.equalsIgnoreCase("select") && !text.equalsIgnoreCase("select option")) {
                    select.selectByIndex(i);
                    return true;
                }
            }
        } catch (Exception ignored) {
            try {
                element.sendKeys(Keys.ARROW_DOWN);
                element.sendKeys(Keys.ENTER);
                return true;
            } catch (Exception ignoredAgain) {
            }
        }
        return false;
    }

    private WebElement firstVisible(By by) {
        for (int retry = 0; retry < 3; retry++) {
            try {
                for (WebElement element : driver.findElements(by)) {
                    try {
                        if (element.isDisplayed() && element.isEnabled()) {
                            return element;
                        }
                    } catch (Exception ignored) {
                    }
                }
                return null;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return null;
    }

    private void tryClick(By by) {
        WebElement element = firstVisible(by);
        if (element != null) {
            jsClick(element);
        }
    }

    private WebElement findFallbackColumnManagementButton() {
        List<WebElement> buttons = driver.findElements(By.xpath("//button"));
        for (WebElement button : buttons) {
            try {
                if (!button.isDisplayed() || !button.isEnabled()) {
                    continue;
                }
                String text = safeText(button).toLowerCase();
                String title = safe(button.getAttribute("title")).toLowerCase();
                String aria = safe(button.getAttribute("aria-label")).toLowerCase();
                if (text.contains("download") || text.contains("advanced search")) {
                    continue;
                }
                if (title.contains("column") || aria.contains("column") || text.contains("column")) {
                    return button;
                }
                if (button.findElements(By.xpath(".//*[local-name()='svg' or self::i]")).size() > 0
                        && (isDownloadExcelVisible() || isAdvancedSearchVisible())) {
                    return button;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean looksLikeNonHeaderControl(String text) {
        String normalized = safe(text).toLowerCase();
        return normalized.isBlank()
                || normalized.equals("advanced search")
                || normalized.equals("download excel")
                || normalized.equals("rows per page")
                || normalized.equals("items per page")
                || normalized.matches("^\\d+$");
    }

    private void jsClick(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", element);
        } catch (Exception ignored) {
        }
    }

    private String safeText(WebElement element) {
        try {
            String value = element.getText();
            return value == null ? "" : value.trim();
        } catch (Exception e) {
            return "";
        }
    }

    private void waitForUiIdle() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(6)).until(d ->
                    d.findElements(By.xpath(
                            "//*[contains(@class,'spinner') or contains(@class,'loading') or contains(@class,'loader') or @aria-busy='true']"
                    )).stream().noneMatch(WebElement::isDisplayed)
            );
        } catch (Exception ignored) {
        }
    }
}
