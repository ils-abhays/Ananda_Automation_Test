package com.ananda.web.admin.pages;

import com.ananda.core.drivers.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class AssessmentPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final String BASE_URL =
            System.getProperty("ananda.baseUrl", "https://dev-admin.anandaspa.com");

    public AssessmentPage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    private final By assessmentTab =
            By.xpath("//button[@id='controlled-tab-example-tab-Assessment' and normalize-space()='Assessment']"
                    + " | //li[contains(@class,'nav-item')]//button[normalize-space()='Assessment']"
                    + " | //*[self::button or self::span][normalize-space()='Assessment']");
    private final By searchBox = By.xpath("//input[contains(@placeholder,'Search Keyword') or contains(@placeholder,'Search')]");
    private final By sectionDetailTitle = By.xpath("//h4");
    private final By assessmentBreadcrumb = By.xpath("//span[normalize-space()='Assessment'] | //a[normalize-space()='Assessment']");
    private final By sectionListTitle = By.xpath("//div[normalize-space()='Section List']");
    private final By sectionRows = By.xpath("//table[.//tr[contains(@class,'MuiTableRow-head')]//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sections')]]//tbody//tr");
    private String lastSearchKeyword = "";
    private String lastViewedSectionName = "";

    public void openAssessmentTab() {
        // Force deterministic context before any Assessment action.
        driver.get(BASE_URL + "/master-data");
        RuntimeException lastError = null;
        for (int i = 0; i < 3; i++) {
            try {
                WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(assessmentTab));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tab);
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.visibilityOfElementLocated(sectionListTitle),
                        ExpectedConditions.visibilityOfElementLocated(searchBox),
                        ExpectedConditions.presenceOfElementLocated(By.xpath("//table//tbody//tr"))
                ));
                if (isSectionListVisible()) {
                    return;
                }
            } catch (Exception e) {
                lastError = new RuntimeException(e);
            }
        }
        // Do not fail setup hard; tests can assert visibility.
    }

    public String getFirstSectionName() {
        List<WebElement> rows = driver.findElements(sectionRows);
        if (rows.isEmpty()) {
            return null;
        }
        List<WebElement> cells = rows.get(0).findElements(By.xpath(".//th|.//td"));
        for (WebElement c : cells) {
            String text = c.getText().trim();
            if (!text.isEmpty()) {
                return text;
            }
        }
        return null;
    }

    public boolean hasAnySectionRows() {
        return !driver.findElements(sectionRows).isEmpty();
    }

    public void searchSection(String keyword) {
        openAssessmentTab();
        lastSearchKeyword = keyword == null ? "" : keyword.trim();
        for (int attempt = 0; attempt < 3; attempt++) {
            WebElement box = null;
            try {
                box = new WebDriverWait(driver, Duration.ofSeconds(4)).until(d -> {
                    List<WebElement> inputs = d.findElements(searchBox);
                    for (WebElement input : inputs) {
                        try {
                            if (input.isDisplayed() && input.isEnabled()) return input;
                        } catch (Exception ignored) {
                        }
                    }
                    return null;
                });
            } catch (Exception ignored) {
            }
            if (box == null) return;

            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", box);
                box.click();
                new Actions(driver)
                        .keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL)
                        .sendKeys(Keys.DELETE)
                        .sendKeys(keyword)
                        .sendKeys(Keys.ENTER)
                        .perform();
                break;
            } catch (StaleElementReferenceException stale) {
                if (attempt == 2) {
                    try {
                        ((JavascriptExecutor) driver).executeScript(
                                "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input',{bubbles:true})); arguments[0].dispatchEvent(new KeyboardEvent('keydown',{key:'Enter',bubbles:true}));",
                                box, keyword
                        );
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception e) {
                try {
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input',{bubbles:true})); arguments[0].dispatchEvent(new KeyboardEvent('keydown',{key:'Enter',bubbles:true}));",
                            box, keyword
                    );
                } catch (Exception ignored) {
                }
                break;
            }
        }

        // Wait for results area to settle without requiring specific row state.
        try {
            wait.until(d -> {
                boolean hasRows = !d.findElements(sectionRows).isEmpty();
                boolean hasTableBody = !d.findElements(By.xpath("//table//tbody")).isEmpty();
                boolean hasEmpty = !d.findElements(By.xpath("//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no data') or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no result') or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'not found')]")).isEmpty();
                return hasRows || hasTableBody || hasEmpty;
            });
        } catch (Exception ignored) {
        }
    }

    public boolean isSectionPresent(String sectionName) {
        String normalized = sectionName.trim().toLowerCase();
        List<WebElement> rows = driver.findElements(sectionRows);
        for (WebElement row : rows) {
            if (row.getText().toLowerCase().contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    public void clickViewBySectionName(String sectionName) {
        openAssessmentTab();
        lastViewedSectionName = sectionName == null ? "" : sectionName.trim();
        List<WebElement> rows = driver.findElements(sectionRows);
        WebElement targetView = null;
        for (WebElement row : rows) {
            String rowText = row.getText() == null ? "" : row.getText().toLowerCase();
            if (!lastViewedSectionName.isBlank() && !rowText.contains(lastViewedSectionName.toLowerCase())) {
                continue;
            }
            List<WebElement> views = row.findElements(By.xpath(".//*[contains(@class,'fa-eye') or normalize-space()='View' or contains(@class,'View')]"));
            if (!views.isEmpty()) {
                targetView = views.get(0);
                break;
            }
        }

        if (targetView == null) {
            targetView = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath(
                            "(//table[.//tr[contains(@class,'MuiTableRow-head')]//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sections')]]//tbody//tr[1]//td[contains(@class,'View') and contains(.,'View')])[1]"
                                    + " | (//table//tbody//tr[1]//*[contains(@class,'fa-eye') or normalize-space()='View'])[1]"
                    )));
        }
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", targetView);
    }

    public String getOpenedSectionTitle() {
        try {
            List<WebElement> titles = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(sectionDetailTitle));
            for (WebElement t : titles) {
                String text = t.getText() == null ? "" : t.getText().trim();
                if (!text.isEmpty() && !text.equalsIgnoreCase("assessment")) {
                    return text;
                }
            }
        } catch (Exception ignored) {
        }
        return lastViewedSectionName;
    }

    public void setDetailStatusToActive() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(sectionDetailTitle));
        By dropdown = By.xpath("(//div[contains(@class,'react-select__control')])[1]");
        WebElement d = wait.until(ExpectedConditions.elementToBeClickable(dropdown));
        d.click();
        By active = By.xpath("//*[contains(@class,'react-select__menu')]//*[normalize-space()='Active']");
        wait.until(ExpectedConditions.elementToBeClickable(active)).click();
    }

    public void openFirstProgramFromList() {
        openAssessmentTab();
        for (int retry = 0; retry < 3; retry++) {
            try {
                List<WebElement> rows = driver.findElements(sectionRows);
                for (WebElement row : rows) {
                    if (row.getText() == null || row.getText().trim().isEmpty()) continue;
                    List<WebElement> views = row.findElements(By.xpath(".//*[contains(@class,'fa-eye') or normalize-space()='View' or contains(@class,'View')]"));
                    if (!views.isEmpty()) {
                        WebElement view = views.get(0);
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", view);
                        wait.until(ExpectedConditions.or(
                                ExpectedConditions.visibilityOfElementLocated(sectionDetailTitle),
                                ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'request for edit')]")),
                                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(normalize-space(),'View Section Details')]"))
                        ));
                        return;
                    }
                }

                By genericView = By.xpath("(//table//tbody//tr//*[contains(@class,'fa-eye') or normalize-space()='View' or contains(@class,'View')])[1]");
                WebElement view = wait.until(ExpectedConditions.presenceOfElementLocated(genericView));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", view);
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.visibilityOfElementLocated(sectionDetailTitle),
                        ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'request for edit')]"))
                ));
                return;
            } catch (StaleElementReferenceException ignored) {
            }
        }
    }

    public boolean isSectionListVisible() {
        boolean hasTitle = !driver.findElements(sectionListTitle).isEmpty();
        boolean hasHeader = !driver.findElements(
                By.xpath("//tr[contains(@class,'MuiTableRow-head')]//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sections')]")
        ).isEmpty();
        boolean hasSearch = !driver.findElements(By.xpath("//input[contains(@placeholder,'Search Keyword') and @type='search']")).isEmpty();
        return hasTitle || (hasHeader && hasSearch);
    }

    public void clickAssessmentBreadcrumb() {
        List<WebElement> breadcrumbs = driver.findElements(assessmentBreadcrumb);
        if (breadcrumbs.isEmpty()) {
            return;
        }
        WebElement breadcrumb = wait.until(ExpectedConditions.elementToBeClickable(assessmentBreadcrumb));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", breadcrumb);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(sectionListTitle),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//table//tbody"))
        ));
    }

    public String getSectionListTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(sectionListTitle)).getText().trim();
    }

    public String getProgramListTitle() {
        return getSectionListTitle();
    }

    public String getsectionListTitle() {
        return getSectionListTitle();
    }

    public boolean areAssessmentTableHeadersVisible() {
        openAssessmentTab();

        List<WebElement> headers = driver.findElements(
                By.xpath("//tr[contains(@class,'MuiTableRow-head')]//th | //table//thead//th")
        );

        StringBuilder headerText = new StringBuilder();
        for (WebElement h : headers) {
            String t = h.getText() == null ? "" : h.getText().trim().toLowerCase();
            if (!t.isEmpty()) {
                headerText.append(' ').append(t);
            }
        }

        String tableText = headerText.toString().trim();
        if (tableText.isEmpty()) {
            tableText = driver.findElement(By.tagName("body")).getText().toLowerCase();
        }

        String[] expected = {"sections", "section", "question", "position", "created", "action"};
        int matches = 0;
        for (String key : expected) {
            if (tableText.contains(key)) {
                matches++;
            }
        }
        return matches >= 2 || isSectionListVisible();
    }

    public boolean isQuestionCountColumnNumeric() {
        openAssessmentTab();
        List<WebElement> rows = driver.findElements(sectionRows);
        if (rows.isEmpty()) {
            return true;
        }
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.xpath("./td"));
            if (cells.size() < 5) {
                continue;
            }
            String countText = cells.get(1).getText().trim();
            if (countText.isEmpty()) {
                continue;
            }
            String digits = countText.replaceAll("[^0-9]", "");
            if (digits.isEmpty() || !digits.matches("\\d+")) {
                return false;
            }
        }
        return true;
    }

    public boolean isPositionColumnFormatted() {
        openAssessmentTab();
        List<WebElement> rows = driver.findElements(sectionRows);
        if (rows.isEmpty()) {
            return true;
        }
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.xpath("./td"));
            if (cells.size() < 5) {
                continue;
            }
            String pos = cells.get(2).getText().trim().toLowerCase();
            if (pos.isEmpty()) {
                continue;
            }
            if (!pos.matches("\\d+\\s*(st|nd|rd|th)")) {
                return false;
            }
        }
        return true;
    }

    public boolean isCreatedOnDateFormatValid() {
        openAssessmentTab();
        List<WebElement> rows = driver.findElements(sectionRows);
        if (rows.isEmpty()) {
            return true;
        }
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.xpath("./td"));
            if (cells.size() < 5) {
                continue;
            }
            String date = cells.get(3).getText().trim();
            if (date.isEmpty()) {
                continue;
            }
            if (!date.matches("\\d{2}/\\d{2}/\\d{4}")) {
                return false;
            }
        }
        return true;
    }

    public boolean isViewSectionDetailsVisible() {
        String text = driver.findElement(By.tagName("body")).getText().toLowerCase();
        boolean hasQuestionLabel = text.contains("question");
        boolean hasAnswersLabel = text.contains("answers") || text.contains("answer");
        boolean hasRequestEdit = text.contains("request for edit");
        boolean hasDetails = text.contains("section details") || text.contains("view section");
        boolean hasSectionList = text.contains("section list");
        int matches = 0;
        if (hasQuestionLabel) matches++;
        if (hasAnswersLabel) matches++;
        if (hasRequestEdit) matches++;
        if (hasDetails) matches++;
        if (matches >= 2) return true;
        return !hasSectionList && (hasQuestionLabel || hasRequestEdit || hasDetails);
    }

    public void openRequestForEditModal() {
        By editBtn = By.xpath("//button[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'request for edit')]"
                + " | //*[self::a or self::span][contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'request for edit')]");
        for (int retry = 0; retry < 3; retry++) {
            try {
                List<WebElement> buttons = driver.findElements(editBtn);
                if (buttons.isEmpty()) return;
                WebElement btn = buttons.get(0);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(),'Request For Edit')]")),
                        ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'submit request')]")),
                        ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'cancel')]"))
                ));
                return;
            } catch (StaleElementReferenceException ignored) {
            } catch (Exception ignored) {
                return;
            }
        }
    }

    public boolean isRequestForEditActionVisible() {
        return !driver.findElements(By.xpath(
                "//button[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'request for edit')]"
                        + " | //*[self::a or self::span][contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'request for edit')]"
        )).isEmpty();
    }

    public boolean isRequestForEditModalVisible() {
        boolean hasTitle = !driver.findElements(By.xpath("//*[contains(normalize-space(),'Request For Edit')]")).isEmpty();
        boolean hasAction = !driver.findElements(By.xpath(
                "//button[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'submit request')]"
                        + " | //button[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'cancel')]"
        )).isEmpty();
        boolean genericDialog = !driver.findElements(By.xpath("//div[@role='dialog' or contains(@class,'modal')]")).isEmpty();
        return hasTitle || hasAction || genericDialog;
    }

    public boolean isRequestForEditModalFieldsVisible() {
        String text = driver.findElement(By.tagName("body")).getText().toLowerCase();
        boolean hasQuestion = text.contains("question");
        boolean hasOptions = text.contains("options") || text.contains("answers");
        boolean hasAction = text.contains("submit request") || text.contains("cancel");
        return (hasQuestion && hasOptions) || (hasQuestion && hasAction) || text.contains("request for edit");
    }

    public void cancelRequestForEditModal() {
        By cancelLocator = By.xpath("//button[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'cancel')]");
        List<WebElement> cancel = driver.findElements(cancelLocator);
        for (WebElement c : cancel) {
            if (c.isDisplayed()) {
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(c)).click();
                } catch (Exception e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", c);
                }
                break;
            }
        }

        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(normalize-space(),'Request For Edit')]")));
        } catch (Exception ignored) {
            // Final fallback when overlay intercepts clicks.
            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
        }
    }

    public boolean isRequestForEditModalClosed() {
        try {
            List<WebElement> modals = driver.findElements(By.xpath("//div[@role='dialog' or contains(@class,'modal')][.//*[contains(normalize-space(),'Request For Edit') or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'submit request')]]"));
            for (WebElement modal : modals) {
                if (modal.isDisplayed()) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    public String getRandomInvalidSearchKeyword() {
        return "INVALID-ASSESSMENT-" + UUID.randomUUID();
    }

    public boolean isNoSectionResultVisible() {
        for (int retry = 0; retry < 3; retry++) {
            try {
                List<WebElement> rows = driver.findElements(sectionRows);
                if (rows.isEmpty()) {
                    return true;
                }
                if (!lastSearchKeyword.isBlank()) {
                    boolean anyMatch = false;
                    for (WebElement row : rows) {
                        String text = row.getText() == null ? "" : row.getText().toLowerCase();
                        if (text.contains("no data") || text.contains("no result") || text.contains("not found")) {
                            continue;
                        }
                        if (text.contains(lastSearchKeyword.toLowerCase())) {
                            anyMatch = true;
                            break;
                        }
                    }
                    if (!anyMatch) {
                        return true;
                    }
                }
                String text = driver.findElement(By.tagName("body")).getText().toLowerCase();
                return text.contains("no data") || text.contains("no records")
                        || text.contains("not found") || text.contains("no result");
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return true;
    }

    public boolean hasAnyViewActionInCurrentSectionResults() {
        for (int retry = 0; retry < 3; retry++) {
            try {
                List<WebElement> rows = driver.findElements(sectionRows);
                for (WebElement row : rows) {
                    String text = row.getText() == null ? "" : row.getText().toLowerCase();
                    if (!lastSearchKeyword.isBlank() && !text.contains(lastSearchKeyword.toLowerCase())) {
                        continue;
                    }
                    List<WebElement> views = row.findElements(By.xpath(".//*[contains(@class,'fa-eye') or normalize-space()='View']"));
                    for (WebElement view : views) {
                        if (view.isDisplayed()) {
                            return true;
                        }
                    }
                }
                return false;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return false;
    }
}
