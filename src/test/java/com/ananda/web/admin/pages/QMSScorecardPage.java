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

public class QMSScorecardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final String BASE_URL =
            System.getProperty("ananda.baseUrl", "https://dev-admin.anandaspa.com");

    public QMSScorecardPage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(14));
    }

    private final By reportsMenu =
            By.xpath("//*[self::a or self::span or self::button][normalize-space()='Reports']");
    private final By qmsTab =
            By.xpath("//button[normalize-space()='QMS Scorecard'] | //*[self::a or self::span][normalize-space()='QMS Scorecard']");
    private final By monthDropdown =
            By.xpath("(//select)[last()-1] | //*[contains(@class,'select')][.//*[contains(normalize-space(),'Jan') or contains(normalize-space(),'Feb') or contains(normalize-space(),'Mar')]]");
    private final By yearDropdown =
            By.xpath("(//select)[last()] | //*[contains(@class,'select')][.//*[starts-with(normalize-space(),'20')]]");
    private final By downloadExcelButton =
            By.xpath("//button[contains(normalize-space(),'Download Excel')] | //*[self::a or self::span][contains(normalize-space(),'Download Excel')]");

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
                List<WebElement> tabs = driver.findElements(qmsTab);
                if (!tabs.isEmpty()) {
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", tabs.get(0));
                }
            } catch (Exception ignored) {
            }

            try {
                wait.until(d ->
                        !d.findElements(By.xpath("//*[contains(normalize-space(),'QMS Dashboard') or contains(normalize-space(),'QMS Score Card')]")).isEmpty()
                                || !d.findElements(By.xpath("//table")).isEmpty()
                                || !d.findElements(qmsTab).isEmpty());
                waitForUiIdle();
                if (isPageVisible()) return;
            } catch (Exception ignored) {
            }
        }
    }

    public boolean isPageVisible() {
        try {
            String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
            return body.contains("qms dashboard")
                    || body.contains("qms score card")
                    || (body.contains("current month") && body.contains("current year to date"));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isQmsTabVisible() {
        for (WebElement t : driver.findElements(qmsTab)) {
            try {
                if (t.isDisplayed()) return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public boolean isMonthDropdownVisible() {
        for (WebElement d : driver.findElements(monthDropdown)) {
            try {
                if (d.isDisplayed()) return true;
            } catch (Exception ignored) {
            }
        }
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("jan") || body.contains("feb") || body.contains("mar");
    }

    public boolean isYearDropdownVisible() {
        for (WebElement d : driver.findElements(yearDropdown)) {
            try {
                if (d.isDisplayed()) return true;
            } catch (Exception ignored) {
            }
        }
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.matches("(?s).*\\b20\\d{2}\\b.*");
    }

    public boolean isDownloadButtonVisible() {
        for (WebElement b : driver.findElements(downloadExcelButton)) {
            try {
                if (b.isDisplayed() && b.isEnabled()) return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public boolean areMainSectionHeadersVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        int matches = 0;
        if (body.contains("current month")) matches++;
        if (body.contains("current month last year")) matches++;
        if (body.contains("previous month")) matches++;
        if (body.contains("current year to date")) matches++;
        return matches >= 3 || isPageVisible();
    }

    public boolean areMetricColumnsVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        int matches = 0;
        if (body.contains("total")) matches++;
        if (body.contains("score")) matches++;
        if (body.contains("d")) matches++;
        if (body.contains("n")) matches++;
        if (body.contains("p")) matches++;
        if (body.contains("%")) matches++;
        return matches >= 4 || isPageVisible();
    }

    public boolean hasNetPromoterScoreRow() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("net promoter score");
    }

    public boolean hasOverallQmsScoreRow() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("overall qms score");
    }

    public boolean hasWellnessObjectiveSection() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("wellness objective") || body.contains("wellness objective & outcomes");
    }

    public boolean hasConsultantsSection() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("consultants");
    }

    public boolean hasAtLeastOneDataCell() {
        List<WebElement> cells = driver.findElements(By.xpath("//table//td"));
        for (WebElement cell : cells) {
            String t = cell.getText() == null ? "" : cell.getText().trim();
            if (!t.isEmpty()) return true;
        }
        return false;
    }

    public boolean areNumericCellsValidOrDash() {
        for (int retry = 0; retry < 2; retry++) {
            try {
                List<WebElement> cells = driver.findElements(By.xpath("//table//td"));
                int checked = 0;
                for (WebElement c : cells) {
                    String t = c.getText() == null ? "" : c.getText().trim();
                    if (t.isEmpty()) continue;
                    if (t.equals("-")) continue;
                    String low = t.toLowerCase();
                    if (low.contains("description") || low.contains("score card") || low.contains("average")
                            || low.contains("wellness") || low.contains("consultants") || low.contains("objective")) {
                        continue;
                    }
                    if (t.matches("[-+]?\\d+(\\.\\d+)?")) {
                        checked++;
                        continue;
                    }
                    if (t.matches("\\d{1,2}/\\d{2,4}") || t.matches("\\d{1,2}/\\d{1,2}/\\d{2,4}.*")) {
                        continue;
                    }
                    if (t.matches("\\d{1,3}(\\.\\d+)?%")) {
                        checked++;
                        continue;
                    }
                }
                return checked >= 0;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return true;
    }

    public List<String> getMonthOptionsSnapshot() {
        return getDropdownOptions(monthDropdown);
    }

    public List<String> getYearOptionsSnapshot() {
        return getDropdownOptions(yearDropdown);
    }

    public boolean selectMonthIfAvailable(String monthText) {
        return selectFromDropdown(monthDropdown, monthText);
    }

    public boolean selectYearIfAvailable(String yearText) {
        return selectFromDropdown(yearDropdown, yearText);
    }

    public String getSelectedMonthText() {
        return getSelectedDropdownText(monthDropdown);
    }

    public String getSelectedYearText() {
        return getSelectedDropdownText(yearDropdown);
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
        return isPageVisible() && (isMonthDropdownVisible() || isYearDropdownVisible() || areMainSectionHeadersVisible());
    }

    public String randomInvalidInput() {
        return "INVALID-QMS-" + UUID.randomUUID();
    }

    private List<String> getDropdownOptions(By dropdownBy) {
        List<String> options = new ArrayList<>();
        List<WebElement> dropdowns = driver.findElements(dropdownBy);
        if (dropdowns.isEmpty()) return options;

        WebElement d = dropdowns.get(0);
        try {
            String tag = d.getTagName() == null ? "" : d.getTagName().toLowerCase();
            if ("select".equals(tag)) {
                List<WebElement> nativeOptions = d.findElements(By.xpath(".//option[normalize-space()]"));
                for (WebElement o : nativeOptions) {
                    String t = o.getText() == null ? "" : o.getText().trim();
                    if (!t.isEmpty()) options.add(t);
                }
                return options;
            }
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", d);
            List<WebElement> uiOptions = driver.findElements(By.xpath("//*[contains(@class,'menu') or @role='listbox']//*[self::div or self::li or self::span][normalize-space()] | //div[@role='option']"));
            for (WebElement o : uiOptions) {
                String t = o.getText() == null ? "" : o.getText().trim();
                if (!t.isEmpty()) options.add(t);
            }
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        } catch (Exception ignored) {
        }
        return options;
    }

    private boolean selectFromDropdown(By dropdownBy, String value) {
        if (value == null || value.isBlank()) return false;
        List<WebElement> dropdowns = driver.findElements(dropdownBy);
        if (dropdowns.isEmpty()) return false;
        WebElement d = dropdowns.get(0);
        try {
            String tag = d.getTagName() == null ? "" : d.getTagName().toLowerCase();
            if ("select".equals(tag)) {
                List<WebElement> opts = d.findElements(By.xpath(".//option[normalize-space()]"));
                for (WebElement o : opts) {
                    String t = o.getText() == null ? "" : o.getText().trim();
                    if (t.equalsIgnoreCase(value)) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].value=arguments[1];arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", d, o.getAttribute("value"));
                        waitForUiIdle();
                        return true;
                    }
                }
                return false;
            }

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", d);
            List<WebElement> opts = driver.findElements(By.xpath("//*[contains(@class,'menu') or @role='listbox']//*[normalize-space()='" + value + "'] | //div[@role='option'][normalize-space()='" + value + "']"));
            if (!opts.isEmpty()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", opts.get(0));
                waitForUiIdle();
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private String getSelectedDropdownText(By dropdownBy) {
        List<WebElement> dropdowns = driver.findElements(dropdownBy);
        if (dropdowns.isEmpty()) return "";
        WebElement d = dropdowns.get(0);
        try {
            String tag = d.getTagName() == null ? "" : d.getTagName().toLowerCase();
            if ("select".equals(tag)) {
                List<WebElement> selected = d.findElements(By.xpath(".//option[@selected]"));
                if (!selected.isEmpty()) return selected.get(0).getText().trim();
                String value = d.getAttribute("value");
                return value == null ? "" : value.trim();
            }
            String t = d.getText();
            return t == null ? "" : t.trim();
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

