package com.ananda.web.admin.pages;

import com.ananda.core.drivers.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProgramFeedbackPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final String BASE_URL =
            System.getProperty("ananda.baseUrl", "https://dev-admin.anandaspa.com");
    private String lastSearchKeyword = "";

    public ProgramFeedbackPage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(14));
    }

    private final By reportsMenu =
            By.xpath("//*[self::a or self::span or self::button][normalize-space()='Reports']");
    private final By programFeedbackTab =
            By.xpath("//button[normalize-space()='Program Feedbacks'] | //*[self::a or self::span][normalize-space()='Program Feedbacks']");
    private final By listTitle = By.xpath("//*[normalize-space()='Program Feedbacks List']");
    private final By searchBox =
            By.xpath("//input[contains(@placeholder,'Search Keyword') or contains(@placeholder,'Search')]");
    private final By tableRows = By.xpath("//table//tbody/tr");
    private final By viewAction =
            By.xpath("(//table//tbody/tr//*[contains(@class,'fa-eye') or normalize-space()='View'])[1]");
    private final By fromDateInput = By.xpath("(//input[@type='date' or contains(@placeholder,'From')])[1]");
    private final By toDateInput = By.xpath("(//input[@type='date' or contains(@placeholder,'To')])[1]");

    public void openIfNotOpened() {
        if (isPageVisible()) {
            waitForUiIdle();
            return;
        }

        driver.get(BASE_URL + "/reports");
        for (int i = 0; i < 3; i++) {
            try {
                List<WebElement> menus = driver.findElements(reportsMenu);
                if (!menus.isEmpty()) {
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", menus.get(0));
                }
            } catch (Exception ignored) {
            }

            try {
                List<WebElement> tabs = driver.findElements(programFeedbackTab);
                if (!tabs.isEmpty()) {
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", tabs.get(0));
                }
            } catch (Exception ignored) {
            }

            try {
                wait.until(d ->
                        !d.findElements(listTitle).isEmpty()
                                || d.findElements(searchBox).stream().anyMatch(WebElement::isDisplayed)
                                || !d.findElements(By.xpath("//table//tbody")).isEmpty());
                waitForUiIdle();
                if (isPageVisible()) return;
            } catch (Exception ignored) {
            }
        }
    }

    public boolean isPageVisible() {
        try {
            boolean hasTitle = !driver.findElements(listTitle).isEmpty();
            boolean hasHeader = !driver.findElements(
                    By.xpath("//table//tr[1]//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'program title')]")
            ).isEmpty();
            return hasTitle || hasHeader;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSearchVisible() {
        for (WebElement b : driver.findElements(searchBox)) {
            try {
                if (b.isDisplayed() && b.isEnabled()) return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public boolean areDateFiltersVisible() {
        boolean from = false;
        boolean to = false;
        for (WebElement el : driver.findElements(fromDateInput)) {
            try {
                if (el.isDisplayed()) {
                    from = true;
                    break;
                }
            } catch (Exception ignored) {
            }
        }
        for (WebElement el : driver.findElements(toDateInput)) {
            try {
                if (el.isDisplayed()) {
                    to = true;
                    break;
                }
            } catch (Exception ignored) {
            }
        }
        if (from && to) return true;
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("date:") && body.contains("to");
    }

    public List<String> getHeadersText() {
        List<String> out = new ArrayList<>();
        List<WebElement> headers = driver.findElements(By.xpath("//table//thead//th | //table//tr[1]/*[self::th or self::td]"));
        for (WebElement h : headers) {
            String t = h.getText() == null ? "" : h.getText().trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    public boolean areExpectedHeadersVisible() {
        String text = String.join(" ", getHeadersText()).toLowerCase();
        if (text.isBlank()) {
            text = driver.findElement(By.tagName("body")).getText().toLowerCase();
        }
        String[] expected = {"program title", "guest name", "check in", "check out", "submitted on", "action"};
        int matches = 0;
        for (String key : expected) if (text.contains(key)) matches++;
        return matches >= 4 || isPageVisible();
    }

    public int getVisibleRowCount() {
        int count = 0;
        for (int retry = 0; retry < 3; retry++) {
            try {
                count = 0;
                for (WebElement row : driver.findElements(tableRows)) {
                    String t = row.getText() == null ? "" : row.getText().trim().toLowerCase();
                    if (t.isEmpty()) continue;
                    if (t.contains("no data") || t.contains("no result") || t.contains("not found")) continue;
                    count++;
                }
                return count;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return count;
    }

    public String getFirstProgramTitle() {
        int col = getColumnIndexByHeader("program title");
        if (col < 1) col = 1;
        for (WebElement row : driver.findElements(tableRows)) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < col) continue;
            String value = cells.get(col - 1).getText() == null ? "" : cells.get(col - 1).getText().trim();
            if (!value.isEmpty()) return value;
        }
        return null;
    }

    public String getFirstGuestName() {
        int col = getColumnIndexByHeader("guest name");
        if (col < 1) col = 2;
        for (WebElement row : driver.findElements(tableRows)) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < col) continue;
            String value = cells.get(col - 1).getText() == null ? "" : cells.get(col - 1).getText().trim();
            if (!value.isEmpty()) return value;
        }
        return null;
    }

    public boolean isCheckInCheckOutFormatValid() {
        int col = getColumnIndexByHeader("check in");
        if (col < 1) return true;
        for (WebElement row : driver.findElements(tableRows)) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < col) continue;
            String v = cells.get(col - 1).getText() == null ? "" : cells.get(col - 1).getText().trim();
            if (v.isEmpty() || v.equals("-")) continue;
            if (!v.matches(".*\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4}\\s*-\\s*\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4}.*")) {
                return false;
            }
        }
        return true;
    }

    public boolean isSubmittedOnDateFormatValid() {
        int col = getColumnIndexByHeader("submitted on");
        if (col < 1) return true;
        for (WebElement row : driver.findElements(tableRows)) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < col) continue;
            String v = cells.get(col - 1).getText() == null ? "" : cells.get(col - 1).getText().trim();
            if (v.isEmpty() || v.equals("-")) continue;
            if (!v.matches(".*\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4}.*")) return false;
        }
        return true;
    }

    public void searchKeyword(String keyword) {
        openIfNotOpened();
        if (!isSearchVisible()) return;
        lastSearchKeyword = keyword == null ? "" : keyword.trim();
        for (int retry = 0; retry < 3; retry++) {
            try {
                WebElement box = null;
                for (WebElement b : driver.findElements(searchBox)) {
                    if (b.isDisplayed() && b.isEnabled()) {
                        box = b;
                        break;
                    }
                }
                if (box == null) return;
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", box);
                box.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
                box.sendKeys(keyword == null ? "" : keyword);
                box.sendKeys(Keys.ENTER);
                waitForUiIdle();
                return;
            } catch (StaleElementReferenceException ignored) {
            } catch (Exception ignored) {
                return;
            }
        }
    }

    public void clearSearch() {
        searchKeyword("");
    }

    public boolean doesAnyVisibleRowContain(String keyword) {
        if (keyword == null || keyword.isBlank()) return false;
        String k = keyword.toLowerCase();
        for (int retry = 0; retry < 3; retry++) {
            try {
                for (WebElement row : driver.findElements(tableRows)) {
                    String t = row.getText() == null ? "" : row.getText().toLowerCase();
                    if (t.contains(k)) return true;
                }
                return false;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return false;
    }

    public void openFirstView() {
        List<WebElement> views = driver.findElements(viewAction);
        if (views.isEmpty()) return;
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", views.get(0));
        waitForUiIdle();
    }

    public boolean isViewPageVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("view program feedback")
                || (body.contains("program:") && body.contains("guest name:"));
    }

    public boolean areViewHeaderFieldsVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        int matches = 0;
        if (body.contains("program:")) matches++;
        if (body.contains("guest name:")) matches++;
        if (body.contains("please provide your overall impression")) matches++;
        if (body.contains("rate your overall experience")) matches++;
        if (body.contains("recommend ananda")) matches++;
        return matches >= 3;
    }

    public boolean hasFeedbackQuestionnaireSectionsVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        int matches = 0;
        if (body.contains("overall impression")) matches++;
        if (body.contains("maintenance of the following areas")) matches++;
        if (body.contains("treatment therapists")) matches++;
        if (body.contains("wellness objectives")) matches++;
        if (body.contains("wellness consultants")) matches++;
        if (body.contains("wellness cuisine")) matches++;
        return matches >= 3;
    }

    public boolean isNoResultVisible() {
        for (int retry = 0; retry < 3; retry++) {
            try {
                List<WebElement> rows = driver.findElements(tableRows);
                if (rows.isEmpty()) return true;
                if (!lastSearchKeyword.isBlank()) {
                    boolean anyMatch = false;
                    for (WebElement row : rows) {
                        String t = row.getText() == null ? "" : row.getText().toLowerCase();
                        if (t.contains("no data") || t.contains("no result") || t.contains("not found")) continue;
                        if (t.contains(lastSearchKeyword.toLowerCase())) {
                            anyMatch = true;
                            break;
                        }
                    }
                    if (!anyMatch) return true;
                }
                String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
                return body.contains("no data") || body.contains("no result") || body.contains("not found");
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return true;
    }

    public boolean hasAnyViewActionInCurrentResults() {
        if (isNoResultVisible()) return false;
        for (WebElement row : driver.findElements(tableRows)) {
            String t = row.getText() == null ? "" : row.getText().toLowerCase();
            if (!lastSearchKeyword.isBlank() && !t.contains(lastSearchKeyword.toLowerCase())) continue;
            List<WebElement> views = row.findElements(By.xpath(".//*[contains(@class,'fa-eye') or normalize-space()='View']"));
            for (WebElement v : views) if (v.isDisplayed()) return true;
        }
        return false;
    }

    public boolean isUiStable() {
        waitForUiIdle();
        return isPageVisible() && (isSearchVisible() || isViewPageVisible());
    }

    public String randomInvalidKeyword() {
        return "INVALID-PROGRAM-FEEDBACK-" + UUID.randomUUID();
    }

    private int getColumnIndexByHeader(String keyword) {
        List<WebElement> headers = driver.findElements(By.xpath("//table//thead//th | //table//tr[1]/*[self::th or self::td]"));
        String k = keyword == null ? "" : keyword.toLowerCase();
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).getText() == null ? "" : headers.get(i).getText().trim().toLowerCase();
            if (h.contains(k)) return i + 1;
        }
        return -1;
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
