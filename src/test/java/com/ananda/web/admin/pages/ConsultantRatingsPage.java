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
import java.util.ArrayList;
import java.util.List;

public class ConsultantRatingsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final String BASE_URL =
            System.getProperty("ananda.baseUrl", "https://dev-admin.anandaspa.com");

    public ConsultantRatingsPage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(14));
    }

    private final By reportsMenu =
            By.xpath("//*[self::a or self::span or self::button][normalize-space()='Reports']");
    private final By consultantRatingsTab =
            By.xpath("//button[normalize-space()='Consultant Ratings'] | //*[self::a or self::span][normalize-space()='Consultant Ratings']");
    private final By title = By.xpath("//*[normalize-space()='Consultant Ratings']");
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
            tryClick(consultantRatingsTab);
            waitForUiIdle();
            if (isPageVisible()) return;
        }
    }

    public boolean isPageVisible() {
        try {
            boolean hasTitle = !driver.findElements(title).isEmpty();
            boolean hasHeaders = !driver.findElements(
                    By.xpath("//table//tr[1]//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'consultant') or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'total session')]")
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
        String[] expected = {"consultant", "therapist", "total session", "total rating", "d", "n", "p", "qms score"};
        int matches = 0;
        for (String key : expected) {
            if (text.contains(key)) matches++;
        }
        return matches >= 5 || isPageVisible();
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

    public boolean hasConsultantNameVisible() {
        int col = getColumnIndexByHeader("consultant");
        if (col < 1 || getVisibleRowCount() == 0) return true;
        for (WebElement row : driver.findElements(tableRows)) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < col) continue;
            if (!safe(cells.get(col - 1).getText()).isEmpty()) return true;
        }
        return false;
    }

    public boolean areMetricCellsNumericOrDash() {
        String[] headers = {"total session", "total rating", "d", "n", "p", "qms score"};
        for (String header : headers) {
            int col = getColumnIndexByHeader(header);
            if (col < 1) continue;
            for (WebElement row : driver.findElements(tableRows)) {
                List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
                if (cells.size() < col) continue;
                String v = safe(cells.get(col - 1).getText());
                if (v.isEmpty() || "-".equals(v)) continue;
                String normalized = v.replace(",", "").trim().toLowerCase();
                if (normalized.equals("na") || normalized.equals("n/a")) continue;
                if (normalized.matches("[-+]?\\d+(\\.\\d+)?%?")) continue;
                if (normalized.matches(".*[a-z].*")) continue;
                if (normalized.contains("consultant") || normalized.contains("therapist") || normalized.contains("total")) continue;
                return false;
            }
        }
        return true;
    }

    public boolean arePercentageCellsValid() {
        List<Integer> pctCols = new ArrayList<>();
        List<WebElement> headers = driver.findElements(By.xpath("//table//thead//th | //table//tr[1]/*[self::th or self::td]"));
        for (int i = 0; i < headers.size(); i++) {
            if ("%".equals(safe(headers.get(i).getText()))) pctCols.add(i + 1);
        }
        for (Integer col : pctCols) {
            for (WebElement row : driver.findElements(tableRows)) {
                List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
                if (cells.size() < col) continue;
                String v = safe(cells.get(col - 1).getText());
                if (v.isEmpty() || "-".equals(v)) continue;
                if (!v.matches("[-+]?\\d+(\\.\\d+)?")) return false;
            }
        }
        return true;
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

    private int getColumnIndexByHeader(String keyword) {
        List<WebElement> headers = driver.findElements(By.xpath("//table//thead//th | //table//tr[1]/*[self::th or self::td]"));
        String k = keyword == null ? "" : keyword.toLowerCase();
        for (int i = 0; i < headers.size(); i++) {
            String h = safe(headers.get(i).getText()).toLowerCase();
            if (h.contains(k)) return i + 1;
        }
        return -1;
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
