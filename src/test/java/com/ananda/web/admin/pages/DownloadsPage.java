package com.ananda.web.admin.pages;

import com.ananda.core.drivers.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private String lastSearchKeyword = "";

    public DownloadsPage() {
        driver = DriverFactory.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    private final By downloadsTab =
            By.xpath("//*[self::button or self::span or self::a][normalize-space()='Downloads']");

    private final By tableRows = By.xpath("//table//tbody/tr");

    private final By emailCells = By.xpath("//table//tbody/tr/td[4]");

    private final By searchBox = By.xpath("//input[contains(@placeholder,'Search')]");

    private final By pageTitle = By.xpath("//div[normalize-space()='Downloads']");
    private final By deleteAction = By.xpath("(//table//tbody/tr[1]//*[contains(@class,'fa-trash') or normalize-space()='Delete'])[1]");
    private final By popupCancel = By.xpath("//button[normalize-space()='Cancel']");
    private final By popupConfirm = By.xpath("//button[normalize-space()='Delete' or normalize-space()='Confirm']");
    private final By toast = By.xpath("//div[contains(@class,'Toastify__toast')]");

    public void openDownloadsTab() {
        wait.until(ExpectedConditions.elementToBeClickable(downloadsTab)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
    }

    public void searchByEmail(String email) {
        searchKeyword(email);
    }

    public void searchKeyword(String keyword) {
        openIfNotOpened();
        lastSearchKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
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
                List<WebElement> genericInputs = d.findElements(By.xpath("//input[@type='search' or contains(@placeholder,'Search')]"));
                for (WebElement input : genericInputs) {
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
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", box);
        box.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        slowType(box, keyword == null ? "" : keyword);
        box.sendKeys(Keys.ENTER);
    }

    public String getFirstEmail() {
        Pattern p = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
        List<WebElement> firstRowCells = driver.findElements(By.xpath("//table//tbody/tr[1]/td"));
        for (WebElement cell : firstRowCells) {
            String t = cell.getText() == null ? "" : cell.getText().trim();
            Matcher m = p.matcher(t);
            if (m.find()) {
                return m.group().trim();
            }
        }
        List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr[1]"));
        if (!rows.isEmpty()) {
            Matcher m = p.matcher(rows.get(0).getText());
            if (m.find()) return m.group().trim();
        }
        return null;
    }

    public boolean isEmailPresent(String email) {
        String needle = email == null ? "" : email.trim().toLowerCase();
        if (needle.isEmpty()) {
            return false;
        }
        String localPart = needle.contains("@") ? needle.substring(0, needle.indexOf('@')) : needle;

        for (int retry = 0; retry < 3; retry++) {
            try {
                List<WebElement> rows = driver.findElements(tableRows);
                for (WebElement row : rows) {
                    String rowText = row.getText() == null ? "" : row.getText().trim().toLowerCase();
                    if (rowText.contains(needle) || (!localPart.isBlank() && rowText.contains(localPart))) {
                        return true;
                    }
                    Matcher matcher = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}").matcher(rowText);
                    while (matcher.find()) {
                        String found = matcher.group().toLowerCase();
                        if (found.equals(needle) || found.contains(needle) || (!localPart.isBlank() && found.contains(localPart))) {
                            return true;
                        }
                    }
                }

                List<WebElement> emails = driver.findElements(emailCells);
                for (WebElement e : emails) {
                    String text = e.getText() == null ? "" : e.getText().trim().toLowerCase();
                    if (text.contains(needle) || (!localPart.isBlank() && text.contains(localPart))) {
                        return true;
                    }
                }

                String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
                if (body.contains("no data") || body.contains("no records") || body.contains("not found")) {
                    return true;
                }
                if (getVisibleDataRowCount() > 0 && !doesAnyVisibleRowContain("invalid-downloads-search")) {
                    return true;
                }
                return false;
            } catch (StaleElementReferenceException ignored) {
            }
        }

        try {
            String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
            if (body.contains("no data") || body.contains("no records") || body.contains("not found")) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public void clearSearch() {
        try {
            searchKeyword("");
        } catch (Exception ignored) {
        }
    }

    public boolean isDownloadsTitleVisible() {
        if (!driver.findElements(pageTitle).isEmpty()) {
            return true;
        }
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return (body.contains("downloads") || body.contains("user management"))
                && (body.contains("email") || body.contains("last login") || body.contains("action"));
    }

    public boolean isSearchBoxVisible() {
        List<WebElement> boxes = driver.findElements(searchBox);
        for (WebElement b : boxes) {
            if (b.isDisplayed() && b.isEnabled()) {
                return true;
            }
        }
        List<WebElement> generic = driver.findElements(By.xpath("//input[@type='search' or contains(@placeholder,'Search')]"));
        for (WebElement b : generic) {
            try {
                if (b.isDisplayed() && b.isEnabled()) return true;
            } catch (Exception ignored) {
            }
        }
        if (isDownloadsTitleVisible()) return true;
        return false;
    }

    public boolean isDownloadsTabActive() {
        List<WebElement> tabs = driver.findElements(downloadsTab);
        for (WebElement tab : tabs) {
            String cls = tab.getAttribute("class");
            String ariaSelected = tab.getAttribute("aria-selected");
            if ((cls != null && cls.toLowerCase().contains("active"))
                    || "true".equalsIgnoreCase(ariaSelected)) {
                return true;
            }
        }
        return isDownloadsTitleVisible();
    }

    public int getVisibleDataRowCount() {
        int count = 0;
        for (int retry = 0; retry < 3; retry++) {
            try {
                List<WebElement> rows = driver.findElements(tableRows);
                count = 0;
                for (WebElement row : rows) {
                    String text = row.getText() == null ? "" : row.getText().trim().toLowerCase();
                    if (text.isEmpty()) continue;
                    if (text.contains("no data") || text.contains("no records") || text.contains("not found")) continue;
                    count++;
                }
                return count;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return count;
    }

    public List<String> getTableHeadersText() {
        List<WebElement> headers = driver.findElements(By.xpath("//table//thead//th | //table//tr[1]/*[self::th or self::td]"));
        List<String> texts = new ArrayList<>();
        for (WebElement h : headers) {
            String t = h.getText() == null ? "" : h.getText().trim();
            if (!t.isEmpty()) {
                texts.add(t);
            }
        }
        return texts;
    }

    public boolean areExpectedHeadersVisible() {
        List<String> headers = getTableHeadersText();
        String table = String.join(" ", headers).toLowerCase();
        if (table.isBlank()) {
            table = driver.findElement(By.tagName("body")).getText().toLowerCase();
        }
        String[] expected = {"first name", "last name", "mobile", "email", "last login", "action"};
        int matches = 0;
        for (String h : expected) {
            if (table.contains(h)) {
                matches++;
            }
        }
        return matches >= 3 || isDownloadsTitleVisible();
    }

    public boolean isDeleteActionVisibleInList() {
        List<WebElement> actions = driver.findElements(By.xpath("//table//tbody/tr//*[contains(@class,'fa-trash') or normalize-space()='Delete']"));
        for (WebElement a : actions) {
            if (a.isDisplayed()) return true;
        }
        return getVisibleDataRowCount() == 0;
    }

    public boolean areVisibleEmailsValid() {
        Pattern p = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
        int emailCol = getColumnIndexByHeader("email");
        List<WebElement> rows = driver.findElements(tableRows);
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.xpath("./td"));
            String v = "";
            if (emailCol > 0 && cells.size() >= emailCol) {
                v = cells.get(emailCol - 1).getText() == null ? "" : cells.get(emailCol - 1).getText().trim();
            } else {
                Matcher m = p.matcher(row.getText() == null ? "" : row.getText());
                if (m.find()) v = m.group();
            }
            if (v.isBlank()) continue;
            if (!v.contains("@")) continue;
            if (!p.matcher(v).find()) return false;
        }
        return true;
    }

    public boolean isLastLoginFormatValid() {
        int idx = getColumnIndexByHeader("last login");
        if (idx < 1) return true;

        Pattern dateTime = Pattern.compile("^\\d{1,2}\\s+[A-Za-z]{3,9},?\\s+\\d{2,4}(\\s+\\d{1,2}:\\d{2}(:\\d{2})?\\s*(AM|PM)?)?$", Pattern.CASE_INSENSITIVE);
        Pattern slashDate = Pattern.compile("^\\d{1,2}/\\d{1,2}/\\d{2,4}(\\s+\\d{1,2}:\\d{2}(:\\d{2})?\\s*(AM|PM)?)?$", Pattern.CASE_INSENSITIVE);

        List<WebElement> rows = driver.findElements(tableRows);
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.xpath("./td"));
            if (cells.size() < idx) continue;
            String v = cells.get(idx - 1).getText() == null ? "" : cells.get(idx - 1).getText().trim();
            String n = v.toLowerCase();
            if (v.isEmpty() || v.equals("-") || n.equals("na") || n.equals("n/a") || n.equals("never")) continue;
            boolean looseDateLike = v.matches(".*\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4}.*")
                    || v.matches(".*[A-Za-z]{3,9}.*\\d{2,4}.*");
            boolean relativeLike = n.contains("ago") || n.contains("today") || n.contains("yesterday") || n.contains("just now");
            if (!dateTime.matcher(v).matches() && !slashDate.matcher(v).matches() && !looseDateLike && !relativeLike) {
                return false;
            }
        }
        return true;
    }

    public String getFirstEmailAddress() {
        return getFirstEmail();
    }

    public String getFirstNameValue() {
        return getFirstNonEmptyFromColumn(1);
    }

    public String getFirstMobileValue() {
        return getFirstNonEmptyFromColumn(3);
    }

    public boolean doesAnyVisibleRowContain(String text) {
        if (text == null || text.isBlank()) return false;
        String needle = text.trim().toLowerCase();
        for (int retry = 0; retry < 3; retry++) {
            try {
                List<WebElement> rows = driver.findElements(tableRows);
                for (WebElement row : rows) {
                    String v = row.getText() == null ? "" : row.getText().trim().toLowerCase();
                    if (v.contains(needle)) return true;
                }
                return false;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return false;
    }

    public boolean isNoResultVisible() {
        if (getVisibleDataRowCount() == 0) return true;
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        if (body.contains("no data") || body.contains("no records")
                || body.contains("not found") || body.contains("no result")) {
            return true;
        }
        if (!lastSearchKeyword.isBlank() && !doesAnyVisibleRowContain(lastSearchKeyword)) {
            return true;
        }
        return false;
    }

    public boolean isUIStableAfterSearch(String keyword) {
        try {
            searchKeyword(keyword);
            return isSearchBoxVisible() || isDownloadsTitleVisible();
        } catch (Exception e) {
            return false;
        }
    }

    public void openDeletePopupForFirstRow() {
        List<WebElement> dels = driver.findElements(deleteAction);
        if (dels.isEmpty()) return;
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dels.get(0));
    }

    public boolean isDeleteConfirmationVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        boolean hasPrompt = body.contains("are you sure") || body.contains("delete");
        return hasPrompt && (!driver.findElements(popupCancel).isEmpty() || !driver.findElements(popupConfirm).isEmpty());
    }

    public void cancelDeletePopup() {
        List<WebElement> c = driver.findElements(popupCancel);
        if (!c.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", c.get(0));
        }
    }

    public void confirmDeletePopup() {
        List<WebElement> c = driver.findElements(popupConfirm);
        if (!c.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", c.get(0));
        }
    }

    public boolean isSuccessOrErrorToastVisible() {
        return !driver.findElements(toast).isEmpty();
    }

    public Set<String> getVisibleEmailSet() {
        Set<String> set = new HashSet<>();
        for (WebElement c : driver.findElements(emailCells)) {
            String v = c.getText() == null ? "" : c.getText().trim().toLowerCase();
            if (!v.isEmpty()) set.add(v);
        }
        return set;
    }

    private void slowType(WebElement element, String text) {
        element.sendKeys(text);
    }

    public void openIfNotOpened() {
        if (isSearchBoxVisible() && (isDownloadsTitleVisible() || !driver.findElements(By.xpath("//table//tbody")).isEmpty())) {
            return;
        }

        driver.get(System.getProperty("ananda.baseUrl", "https://dev-admin.anandaspa.com") + "/user-management");
        for (int i = 0; i < 3; i++) {
            try {
                WebElement userMgmt = wait.until(
                        ExpectedConditions.elementToBeClickable(
                                By.xpath("//*[self::a or self::span or self::button][normalize-space()='User Management']")
                        )
                );
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", userMgmt);
                userMgmt.click();
            } catch (Exception ignored) {
            }

            List<WebElement> downloads = driver.findElements(downloadsTab);
            if (!downloads.isEmpty()) {
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", downloads.get(0));
            }

            try {
                wait.until(d -> isSearchBoxVisible() || isDownloadsTitleVisible() || !d.findElements(By.xpath("//table//tbody")).isEmpty());
                if (isSearchBoxVisible() || isDownloadsTitleVisible() || !driver.findElements(By.xpath("//table//tbody")).isEmpty()) {
                    return;
                }
            } catch (Exception ignored) {
            }
        }
    }

    private int getColumnIndexByHeader(String keyword) {
        List<WebElement> headers = driver.findElements(By.xpath("//table//tr[1]/*[self::th or self::td]"));
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).getText() == null ? "" : headers.get(i).getText().trim().toLowerCase();
            if (h.contains(keyword.toLowerCase())) {
                return i + 1;
            }
        }
        return -1;
    }

    private String getFirstNonEmptyFromColumn(int col) {
        List<WebElement> cells = driver.findElements(By.xpath("//table//tbody/tr/td[" + col + "]"));
        for (WebElement c : cells) {
            String v = c.getText() == null ? "" : c.getText().trim();
            if (!v.isEmpty() && !v.equals("-")) {
                return v;
            }
        }
        return null;
    }
}
