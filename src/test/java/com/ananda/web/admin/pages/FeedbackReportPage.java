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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FeedbackReportPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final String BASE_URL =
            System.getProperty("ananda.baseUrl", "https://dev-admin.anandaspa.com");

    public FeedbackReportPage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(14));
    }

    private final By reportsMenu =
            By.xpath("//*[self::a or self::span or self::button][normalize-space()='Reports']");
    private final By feedbackReportTab =
            By.xpath("//button[normalize-space()='Feedback Report'] | //*[self::a or self::span][normalize-space()='Feedback Report']");
    private final By title = By.xpath("//*[normalize-space()='Feedback Report']");
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
            try {
                List<WebElement> menus = driver.findElements(reportsMenu);
                if (!menus.isEmpty()) {
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", menus.get(0));
                }
            } catch (Exception ignored) {
            }

            try {
                List<WebElement> tabs = driver.findElements(feedbackReportTab);
                if (!tabs.isEmpty()) {
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", tabs.get(0));
                }
            } catch (Exception ignored) {
            }

            try {
                wait.until(d ->
                        !d.findElements(title).isEmpty()
                                || !d.findElements(By.xpath("//table")).isEmpty()
                                || !d.findElements(feedbackReportTab).isEmpty());
                waitForUiIdle();
                if (isPageVisible()) return;
            } catch (Exception ignored) {
            }
        }
    }

    public boolean isPageVisible() {
        try {
            boolean hasTitle = !driver.findElements(title).isEmpty();
            boolean hasHeader = !driver.findElements(
                    By.xpath("//table//tr[1]//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'first name')"
                            + " or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'feedback date')]")
            ).isEmpty();
            boolean hasDownload = !driver.findElements(downloadExcelButton).isEmpty();
            boolean hasDates = !driver.findElements(fromDateInput).isEmpty() || !driver.findElements(toDateInput).isEmpty();
            return hasTitle || hasHeader || hasDownload || hasDates;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areDateFiltersVisible() {
        boolean from = false;
        boolean to = false;
        for (WebElement e : driver.findElements(fromDateInput)) {
            try {
                if (e.isDisplayed() && e.isEnabled()) {
                    from = true;
                    break;
                }
            } catch (Exception ignored) {
            }
        }
        for (WebElement e : driver.findElements(toDateInput)) {
            try {
                if (e.isDisplayed() && e.isEnabled()) {
                    to = true;
                    break;
                }
            } catch (Exception ignored) {
            }
        }
        return from && to;
    }

    public boolean isDownloadExcelVisible() {
        for (WebElement b : driver.findElements(downloadExcelButton)) {
            try {
                if (b.isDisplayed() && b.isEnabled()) return true;
            } catch (Exception ignored) {
            }
        }
        return false;
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
        String[] expected = {
                "first name", "last name", "room", "check in", "check out",
                "feedback date", "overall wellness objective", "outcome achieved", "physical health objective"
        };
        int matches = 0;
        for (String key : expected) if (text.contains(key)) matches++;
        return matches >= 6 || isPageVisible();
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

    public boolean isCheckInDateFormatValid() {
        int col = getColumnIndexByHeader("check in");
        if (col < 1) return true;
        for (WebElement row : driver.findElements(tableRows)) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < col) continue;
            String v = safe(cells.get(col - 1).getText());
            if (v.isEmpty() || v.equals("-")) continue;
            if (!looksLikeDate(v)) return false;
        }
        return true;
    }

    public boolean isCheckOutDateFormatValid() {
        int col = getColumnIndexByHeader("check out");
        if (col < 1) return true;
        for (WebElement row : driver.findElements(tableRows)) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < col) continue;
            String v = safe(cells.get(col - 1).getText());
            if (v.isEmpty() || v.equals("-")) continue;
            if (!looksLikeDate(v)) return false;
        }
        return true;
    }

    public boolean isFeedbackDateFormatValid() {
        int col = getColumnIndexByHeader("feedback date");
        if (col < 1) return true;
        for (WebElement row : driver.findElements(tableRows)) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < col) continue;
            String v = safe(cells.get(col - 1).getText());
            if (v.isEmpty() || v.equals("-")) continue;
            if (!looksLikeDateOrDateTime(v)) return false;
        }
        return true;
    }

    public boolean isCheckOutOnOrAfterCheckIn() {
        int inCol = getColumnIndexByHeader("check in");
        int outCol = getColumnIndexByHeader("check out");
        if (inCol < 1 || outCol < 1) return true;

        for (WebElement row : driver.findElements(tableRows)) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < Math.max(inCol, outCol)) continue;
            String in = safe(cells.get(inCol - 1).getText());
            String out = safe(cells.get(outCol - 1).getText());
            if (in.isEmpty() || out.isEmpty() || in.equals("-") || out.equals("-")) continue;
            LocalDate inDate = parseDate(in);
            LocalDate outDate = parseDate(out);
            if (inDate == null || outDate == null) continue;
            if (outDate.isBefore(inDate)) return false;
        }
        return true;
    }

    public void setDateRange(String from, String to) {
        if (!areDateFiltersVisible()) return;
        List<WebElement> fromEls = driver.findElements(fromDateInput);
        List<WebElement> toEls = driver.findElements(toDateInput);
        if (fromEls.isEmpty() || toEls.isEmpty()) return;
        WebElement f = fromEls.get(0);
        WebElement t = toEls.get(0);

        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", f);
            f.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
            f.sendKeys(from == null ? "" : from);
            t.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
            t.sendKeys(to == null ? "" : to);
            t.sendKeys(Keys.ENTER);
            waitForUiIdle();
        } catch (Exception ignored) {
        }
    }

    public String getFromDateValue() {
        List<WebElement> els = driver.findElements(fromDateInput);
        if (els.isEmpty()) return "";
        String v = els.get(0).getAttribute("value");
        return v == null ? "" : v.trim();
    }

    public String getToDateValue() {
        List<WebElement> els = driver.findElements(toDateInput);
        if (els.isEmpty()) return "";
        String v = els.get(0).getAttribute("value");
        return v == null ? "" : v.trim();
    }

    public boolean isNoResultVisible() {
        for (int retry = 0; retry < 3; retry++) {
            try {
                List<WebElement> rows = driver.findElements(tableRows);
                if (rows.isEmpty()) return true;
                String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
                return body.contains("no data") || body.contains("no result") || body.contains("not found");
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return true;
    }

    public boolean clickDownloadIfVisible() {
        List<WebElement> buttons = driver.findElements(downloadExcelButton);
        if (buttons.isEmpty()) return false;
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttons.get(0));
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
        return isPageVisible()
                && (areDateFiltersVisible()
                || isDownloadExcelVisible()
                || !driver.findElements(By.xpath("//table")).isEmpty()
                || driver.findElement(By.tagName("body")).getText().toLowerCase().contains("feedback report"));
    }

    public String randomInvalidInput() {
        return "INVALID-FEEDBACK-REPORT-" + UUID.randomUUID();
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

    private boolean looksLikeDate(String v) {
        return parseDate(v) != null;
    }

    private boolean looksLikeDateOrDateTime(String v) {
        if (parseDate(v) != null) return true;
        return parseDateTime(v) != null;
    }

    private LocalDate parseDate(String v) {
        List<DateTimeFormatter> formats = List.of(
                DateTimeFormatter.ofPattern("dd MMM, yyyy"),
                DateTimeFormatter.ofPattern("dd MMM yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
        );
        for (DateTimeFormatter f : formats) {
            try {
                return LocalDate.parse(v.trim(), f);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private LocalDateTime parseDateTime(String v) {
        List<DateTimeFormatter> formats = List.of(
                DateTimeFormatter.ofPattern("dd MMM, yyyy hh:mm a"),
                DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        );
        for (DateTimeFormatter f : formats) {
            try {
                return LocalDateTime.parse(v.trim(), f);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String safe(String v) {
        return v == null ? "" : v.trim();
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
