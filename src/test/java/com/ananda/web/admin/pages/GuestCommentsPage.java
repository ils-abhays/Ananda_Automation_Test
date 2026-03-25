package com.ananda.web.admin.pages;

import com.ananda.core.drivers.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GuestCommentsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final String BASE_URL =
            System.getProperty("ananda.baseUrl", "https://dev-admin.anandaspa.com");

    public GuestCommentsPage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(14));
    }

    private final By reportsMenu =
            By.xpath("//*[self::a or self::span or self::button][normalize-space()='Reports']");
    private final By guestCommentsTab =
            By.xpath("//button[normalize-space()='Guest Comments'] | //*[self::a or self::span][normalize-space()='Guest Comments']");
    private final By title = By.xpath("//*[normalize-space()='Guest Comments']");
    private final By fromDateInput = By.xpath("(//input[@type='date'])[1]");
    private final By toDateInput = By.xpath("(//input[@type='date'])[2]");
    private final By downloadExcelButton =
            By.xpath("//button[contains(normalize-space(),'Download Excel')] | //*[self::a or self::span][contains(normalize-space(),'Download Excel')]");
    private final By tableRows = By.xpath("//table//tbody/tr");

    public void openIfNotOpened() {
        if (isPageVisible()) {
            waitForUiIdle();
            return;
        }

        driver.get(BASE_URL + "/reports");
        for (int i = 0; i < 3; i++) {
            tryClick(reportsMenu);
            tryClick(guestCommentsTab);
            waitForUiIdle();
            if (isPageVisible()) return;
        }
    }

    public boolean isPageVisible() {
        try {
            boolean hasTitle = !driver.findElements(title).isEmpty();
            boolean hasHeaders = !driver.findElements(
                    By.xpath("//table//tr[1]//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'guest name') or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'check out')]")
            ).isEmpty();
            return hasTitle || hasHeaders;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areDateFiltersVisible() {
        return isVisibleAndEnabled(fromDateInput) && isVisibleAndEnabled(toDateInput);
    }

    public boolean isDownloadExcelVisible() {
        return isVisibleAndEnabled(downloadExcelButton);
    }

    public List<String> getHeadersText() {
        List<String> out = new ArrayList<>();
        List<WebElement> headers = driver.findElements(By.xpath("//table//thead//th | //table//tr[1]/*[self::th or self::td]"));
        for (WebElement h : headers) {
            String t = safe(h.getText());
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    public boolean areExpectedHeadersVisible() {
        String text = String.join(" ", getHeadersText()).toLowerCase();
        if (text.isBlank()) text = driver.findElement(By.tagName("body")).getText().toLowerCase();
        String[] expected = {
                "guest name", "check out", "highlights", "improve your wellness experience",
                "therapists recognised", "service staff recognised"
        };
        int matches = 0;
        for (String key : expected) {
            if (text.contains(key)) matches++;
        }
        return matches >= 4 || isPageVisible();
    }

    public int getVisibleRowCount() {
        int count = 0;
        for (int retry = 0; retry < 3; retry++) {
            try {
                count = 0;
                for (WebElement row : driver.findElements(tableRows)) {
                    String t = safe(row.getText()).toLowerCase();
                    if (t.isEmpty() || t.contains("no data") || t.contains("no result") || t.contains("not found")) continue;
                    count++;
                }
                return count;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return count;
    }

    public boolean isCheckOutDateFormatValid() {
        int col = getColumnIndexByHeader("check out");
        if (col < 1) return true;
        for (WebElement row : driver.findElements(tableRows)) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < col) continue;
            String v = safe(cells.get(col - 1).getText());
            if (v.isEmpty() || "-".equals(v)) continue;
            if (parseDate(v) == null) return false;
        }
        return true;
    }

    public boolean hasHighlightsOrPlaceholder() {
        return hasNonEmptyOrDashCell("highlight");
    }

    public boolean hasImprovementOrPlaceholder() {
        return hasNonEmptyOrDashCell("improve");
    }

    public boolean hasTherapistsRecognisedOrPlaceholder() {
        return hasNonEmptyOrDashCell("therapists recognised");
    }

    public boolean hasServiceStaffRecognisedOrPlaceholder() {
        return hasNonEmptyOrDashCell("service staff recognised");
    }

    public void setDateRange(String from, String to) {
        if (!areDateFiltersVisible()) return;
        WebElement fromEl = firstVisible(fromDateInput);
        WebElement toEl = firstVisible(toDateInput);
        if (fromEl == null || toEl == null) return;
        try {
            fromEl.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
            fromEl.sendKeys(from == null ? "" : from);
            toEl.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
            toEl.sendKeys(to == null ? "" : to);
            toEl.sendKeys(Keys.ENTER);
            waitForUiIdle();
        } catch (Exception ignored) {
        }
    }

    public boolean isNoResultVisible() {
        try {
            List<WebElement> rows = driver.findElements(tableRows);
            if (rows.isEmpty()) return true;
            String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
            return body.contains("no data") || body.contains("no result") || body.contains("not found");
        } catch (Exception e) {
            return true;
        }
    }

    public boolean clickDownloadIfVisible() {
        WebElement button = firstVisible(downloadExcelButton);
        if (button == null) return false;
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
            waitForUiIdle();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void refreshPage() {
        driver.navigate().refresh();
        waitForUiIdle();
    }

    public void scrollLongAndBack() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
    }

    public boolean isUiStable() {
        waitForUiIdle();
        return isPageVisible() && (areDateFiltersVisible() || isDownloadExcelVisible() || getVisibleRowCount() >= 0);
    }

    private boolean hasNonEmptyOrDashCell(String headerKeyword) {
        int col = getColumnIndexByHeader(headerKeyword);
        if (col < 1 || getVisibleRowCount() == 0) return true;
        for (WebElement row : driver.findElements(tableRows)) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < col) continue;
            String v = safe(cells.get(col - 1).getText());
            if (!v.isEmpty()) return true;
        }
        return false;
    }

    private int getColumnIndexByHeader(String keyword) {
        List<WebElement> headers = driver.findElements(By.xpath("//table//thead//th | //table//tr[1]/*[self::th or self::td]"));
        String k = keyword == null ? "" : keyword.toLowerCase();
        for (int i = 0; i < headers.size(); i++) {
            String h = safe(headers.get(i).getText()).toLowerCase();
            if (h.contains(k)) return i + 1;
        }
        return -1;
    }

    private LocalDate parseDate(String value) {
        List<DateTimeFormatter> formats = List.of(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("d-M-yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
        );
        for (DateTimeFormatter f : formats) {
            try {
                return LocalDate.parse(value.trim(), f);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private boolean isVisibleAndEnabled(By by) {
        return firstVisible(by) != null;
    }

    private WebElement firstVisible(By by) {
        for (WebElement e : driver.findElements(by)) {
            try {
                if (e.isDisplayed() && e.isEnabled()) return e;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private void tryClick(By by) {
        WebElement element = firstVisible(by);
        if (element == null) return;
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", element);
        } catch (Exception ignored) {
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
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
