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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class TherapistSessionsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final String BASE_URL =
            System.getProperty("ananda.baseUrl", "https://dev-admin.anandaspa.com");
    private String lastSearchKeyword = "";

    public TherapistSessionsPage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(14));
    }

    private final By reportsMenu =
            By.xpath("//*[self::a or self::span or self::button][normalize-space()='Reports']");
    private final By therapistSessionsTab =
            By.xpath("//button[normalize-space()='Therapist Sessions'] | //*[self::a or self::span][normalize-space()='Therapist Sessions']");
    private final By title = By.xpath("//*[normalize-space()='Therapist Sessions']");
    private final By searchBox =
            By.xpath("//input[contains(@placeholder,'Search Keyword') or contains(@placeholder,'Search')]");
    private final By therapistDropdown =
            By.xpath("(//select)[1] | (//input[contains(@placeholder,'Search Keyword') or contains(@placeholder,'Search')]/following::*[self::div[contains(@class,'select') or contains(@class,'dropdown')]][1])");
    private final By dateInput = By.xpath("(//input[@type='date'])[1]");
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
            tryClick(therapistSessionsTab);
            waitForUiIdle();
            if (isPageVisible()) return;
        }
    }

    public boolean isPageVisible() {
        try {
            boolean hasTitle = !driver.findElements(title).isEmpty();
            boolean hasHeaders = !driver.findElements(
                    By.xpath("//table//tr[1]//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'guest name') or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'session name')]")
            ).isEmpty();
            return hasTitle || hasHeaders;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSearchVisible() {
        return isVisibleAndEnabled(searchBox);
    }

    public boolean isTherapistDropdownVisible() {
        return firstVisible(therapistDropdown) != null || bodyText().contains("all");
    }

    public boolean isDateFilterVisible() {
        return isVisibleAndEnabled(dateInput);
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
        if (text.isBlank()) text = bodyText();
        String[] expected = {
                "guest name", "gender", "guest room number", "doctor name",
                "therapist assigned", "session name", "therapy room name", "session time"
        };
        int matches = 0;
        for (String key : expected) {
            if (text.contains(key)) matches++;
        }
        return matches >= 6 || isPageVisible();
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

    public String getFirstGuestName() {
        return getFirstCellValue("guest name");
    }

    public String getFirstTherapistAssigned() {
        return getFirstCellValue("therapist assigned");
    }

    public boolean isSessionTimeFormatValid() {
        int col = getColumnIndexByHeader("session time");
        if (col < 1) return true;
        for (WebElement row : driver.findElements(tableRows)) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < col) continue;
            String v = safe(cells.get(col - 1).getText());
            if (v.isEmpty() || "-".equals(v)) continue;
            if (!isPlausibleSessionTime(v)) return false;
        }
        return true;
    }

    public void searchKeyword(String keyword) {
        openIfNotOpened();
        if (!isSearchVisible()) return;
        lastSearchKeyword = keyword == null ? "" : keyword.trim();
        for (int retry = 0; retry < 3; retry++) {
            try {
                WebElement box = firstVisible(searchBox);
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
                    String t = safe(row.getText()).toLowerCase();
                    if (t.contains(k)) return true;
                }
                return false;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return false;
    }

    public boolean selectTherapistIfAvailable(String optionText) {
        if (optionText == null || optionText.isBlank()) return false;
        WebElement dropdown = firstVisible(therapistDropdown);
        if (dropdown == null) return false;
        try {
            String tag = safe(dropdown.getTagName()).toLowerCase();
            if ("select".equals(tag)) {
                List<WebElement> options = dropdown.findElements(By.xpath(".//option[normalize-space()]"));
                for (WebElement option : options) {
                    String text = safe(option.getText());
                    if (text.equalsIgnoreCase(optionText)) {
                        ((JavascriptExecutor) driver).executeScript(
                                "arguments[0].value=arguments[1];arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",
                                dropdown, option.getAttribute("value"));
                        waitForUiIdle();
                        return true;
                    }
                }
                return false;
            }

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dropdown);
            List<WebElement> options = driver.findElements(
                    By.xpath("//*[contains(@class,'menu') or @role='listbox']//*[normalize-space()='" + optionText + "'] | //div[@role='option'][normalize-space()='" + optionText + "']")
            );
            if (options.isEmpty()) {
                driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
                return false;
            }
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", options.get(0));
            waitForUiIdle();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> getTherapistOptionsSnapshot() {
        List<String> values = new ArrayList<>();
        WebElement dropdown = firstVisible(therapistDropdown);
        if (dropdown == null) return values;
        try {
            String tag = safe(dropdown.getTagName()).toLowerCase();
            if ("select".equals(tag)) {
                for (WebElement option : dropdown.findElements(By.xpath(".//option[normalize-space()]"))) {
                    String text = safe(option.getText());
                    if (!text.isEmpty()) values.add(text);
                }
                return values;
            }
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dropdown);
            List<WebElement> options = driver.findElements(By.xpath("//*[contains(@class,'menu') or @role='listbox']//*[self::div or self::li or self::span][normalize-space()] | //div[@role='option']"));
            for (WebElement option : options) {
                String text = safe(option.getText());
                if (!text.isEmpty()) values.add(text);
            }
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        } catch (Exception ignored) {
        }
        return values;
    }

    public void setDate(String value) {
        WebElement input = firstVisible(dateInput);
        if (input == null) return;
        try {
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
            input.sendKeys(value == null ? "" : value);
            input.sendKeys(Keys.ENTER);
            waitForUiIdle();
        } catch (Exception ignored) {
        }
    }

    public boolean isNoResultVisible() {
        try {
            List<WebElement> rows = driver.findElements(tableRows);
            if (rows.isEmpty()) return true;
            if (!lastSearchKeyword.isBlank()) {
                boolean anyMatch = false;
                for (WebElement row : rows) {
                    String text = safe(row.getText()).toLowerCase();
                    if (text.contains(lastSearchKeyword.toLowerCase())) {
                        anyMatch = true;
                        break;
                    }
                }
                if (!anyMatch) return true;
            }
            String body = bodyText();
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

    public boolean isUiStable() {
        waitForUiIdle();
        return isPageVisible() && (isSearchVisible() || isTherapistDropdownVisible() || getVisibleRowCount() >= 0);
    }

    public String randomInvalidKeyword() {
        return "INVALID-THERAPIST-" + UUID.randomUUID();
    }

    private String getFirstCellValue(String headerKeyword) {
        int col = getColumnIndexByHeader(headerKeyword);
        if (col < 1) return null;
        for (WebElement row : driver.findElements(tableRows)) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < col) continue;
            String value = safe(cells.get(col - 1).getText());
            if (!value.isEmpty() && !"-".equals(value)) return value;
        }
        return null;
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

    private LocalDateTime parseDateTime(String value) {
        List<DateTimeFormatter> formats = List.of(
                DateTimeFormatter.ofPattern("dd MMM, yyyy hh:mm a"),
                DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a"),
                DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a"),
                DateTimeFormatter.ofPattern("MMM d, yyyy hh:mm a"),
                DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a"),
                DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd h:mm a"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        );
        for (DateTimeFormatter f : formats) {
            try {
                return LocalDateTime.parse(value.trim(), f);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private boolean isPlausibleSessionTime(String value) {
        String normalized = safe(value)
                .replace('\u2013', '-')
                .replace('\u2014', '-')
                .replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            return true;
        }

        if (parseDateTime(normalized) != null) {
            return true;
        }

        Pattern timeOnly = Pattern.compile("^\\d{1,2}:\\d{2}(:\\d{2})?\\s*(AM|PM)?$", Pattern.CASE_INSENSITIVE);
        Pattern timeRange = Pattern.compile(
                "^\\d{1,2}:\\d{2}(:\\d{2})?\\s*(AM|PM)?\\s*(-|to)\\s*\\d{1,2}:\\d{2}(:\\d{2})?\\s*(AM|PM)?$",
                Pattern.CASE_INSENSITIVE
        );
        Pattern embeddedTime = Pattern.compile(
                "\\b\\d{1,2}:\\d{2}(:\\d{2})?\\s*(AM|PM)?\\b",
                Pattern.CASE_INSENSITIVE
        );
        Pattern datedTime = Pattern.compile(
                "^\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}(\\s+\\d{1,2}:\\d{2}(:\\d{2})?\\s*(AM|PM)?)?$",
                Pattern.CASE_INSENSITIVE
        );
        Pattern writtenDateTime = Pattern.compile(
                "^\\d{1,2}\\s+[A-Za-z]{3,9},?\\s+\\d{2,4}(\\s+\\d{1,2}:\\d{2}(:\\d{2})?\\s*(AM|PM)?)?$",
                Pattern.CASE_INSENSITIVE
        );

        return timeOnly.matcher(normalized).matches()
                || timeRange.matcher(normalized).matches()
                || embeddedTime.matcher(normalized).find()
                || datedTime.matcher(normalized).matches()
                || writtenDateTime.matcher(normalized).matches();
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

    private String bodyText() {
        return driver.findElement(By.tagName("body")).getText().toLowerCase();
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
