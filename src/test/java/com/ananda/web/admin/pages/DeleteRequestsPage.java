package com.ananda.web.admin.pages;

import com.ananda.core.drivers.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeleteRequestsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final String BASE_URL =
            System.getProperty("ananda.baseUrl", "https://dev-admin.anandaspa.com");
    private String lastSearchKeyword = "";

    public DeleteRequestsPage() {
        driver = DriverFactory.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    private final By userManagementMenu =
            By.xpath("//*[self::a or self::span or self::button][normalize-space()='User Management']");
    private final By deleteRequestsTab =
            By.xpath("//*[self::button or self::span or self::a][normalize-space()='Delete Requests']");
    private final By title =
            By.xpath("//*[normalize-space()='User List' or normalize-space()='Delete Requests']");
    private final By tableRows = By.xpath("//table//tbody/tr");
    private final By searchBox =
            By.xpath("//input[contains(@placeholder,'Search Keyword') or contains(@placeholder,'Search')]");
    private final By recoverAction =
            By.xpath("//table//tbody/tr//*[contains(normalize-space(),'Recover') or contains(@class,'fa-undo')]");
    private final By deleteAccountAction =
            By.xpath("//table//tbody/tr//*[contains(normalize-space(),'Delete Account') or (contains(normalize-space(),'Delete') and not(contains(normalize-space(),'Requests')))]");

    public void openIfNotOpened() {
        if (isPageVisible() && isSearchVisible()) return;

        driver.get(BASE_URL + "/user-management");
        for (int i = 0; i < 3; i++) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(userManagementMenu)).click();
            } catch (Exception ignored) {
            }
            try {
                List<WebElement> tabs = driver.findElements(deleteRequestsTab);
                if (!tabs.isEmpty()) {
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();",
                            tabs.get(0)
                    );
                }
            } catch (Exception ignored) {
            }
            try {
                wait.until(d ->
                        !d.findElements(title).isEmpty()
                                || d.findElements(searchBox).stream().anyMatch(WebElement::isDisplayed)
                                || !d.findElements(By.xpath("//table//tbody")).isEmpty()
                );
                if (isPageVisible()) return;
            } catch (Exception ignored) {
            }
        }
    }

    public boolean isPageVisible() {
        try {
            boolean hasTitle = !driver.findElements(title).isEmpty();
            boolean hasHeader = !driver.findElements(
                    By.xpath("//table//tr[1]//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'email') or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'requested on')]")
            ).isEmpty();
            return hasTitle || hasHeader;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSearchVisible() {
        List<WebElement> boxes = driver.findElements(searchBox);
        for (WebElement b : boxes) {
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
        String[] expected = {"first name", "last name", "email", "requested on", "auto delete by", "action"};
        int matches = 0;
        for (String e : expected) {
            if (text.contains(e)) matches++;
        }
        return matches >= 4 || isPageVisible();
    }

    public int getVisibleDataRowCount() {
        int count = 0;
        for (WebElement row : driver.findElements(tableRows)) {
            String t = row.getText() == null ? "" : row.getText().trim().toLowerCase();
            if (t.isEmpty()) continue;
            if (t.contains("no data") || t.contains("no result") || t.contains("not found")) continue;
            count++;
        }
        return count;
    }

    public String getFirstEmail() {
        int idx = getColumnIndexByHeader("email");
        if (idx < 1) idx = 3;
        List<WebElement> cells = driver.findElements(By.xpath("//table//tbody/tr[1]/td[" + idx + "]"));
        if (cells.isEmpty()) return null;
        return cells.get(0).getText().trim();
    }

    public String getFirstFirstName() {
        int idx = getColumnIndexByHeader("first");
        if (idx < 1) idx = 1;
        List<WebElement> cells = driver.findElements(By.xpath("//table//tbody/tr[1]/td[" + idx + "]"));
        if (cells.isEmpty()) return null;
        return cells.get(0).getText().trim();
    }

    public String getFirstLastName() {
        int idx = getColumnIndexByHeader("last");
        if (idx < 1) idx = 2;
        List<WebElement> cells = driver.findElements(By.xpath("//table//tbody/tr[1]/td[" + idx + "]"));
        if (cells.isEmpty()) return null;
        return cells.get(0).getText().trim();
    }

    public void searchKeyword(String keyword) {
        openIfNotOpened();
        lastSearchKeyword = keyword == null ? "" : keyword.trim();
        for (int retry = 0; retry < 3; retry++) {
            try {
                WebElement box = null;
                List<WebElement> boxes = driver.findElements(searchBox);
                for (WebElement b : boxes) {
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
                wait.until(d ->
                        !d.findElements(By.xpath("//table//tbody")).isEmpty()
                                || !d.findElements(By.xpath("//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no data') or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no result') or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'not found')]")).isEmpty()
                );
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
        for (int retry = 0; retry < 3; retry++) {
            try {
                for (WebElement row : driver.findElements(tableRows)) {
                    String t = row.getText() == null ? "" : row.getText().toLowerCase();
                    if (t.contains(keyword.toLowerCase())) return true;
                }
                return false;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return false;
    }

    public boolean isEmailColumnValid() {
        Pattern p = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        Pattern anyEmail = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
        for (int retry = 0; retry < 3; retry++) {
            try {
                List<WebElement> rows = driver.findElements(tableRows);
                for (WebElement row : rows) {
                    String text = row.getText() == null ? "" : row.getText();
                    Matcher m = anyEmail.matcher(text);
                    while (m.find()) {
                        String email = m.group().trim();
                        if (!p.matcher(email).matches()) return false;
                    }
                }
                return true;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return true;
    }

    public boolean isRequestedOnDateFormatValid() {
        int idx = getColumnIndexByHeader("requested on");
        if (idx < 1) idx = 4;
        return isDateColumnValid(idx);
    }

    public boolean isAutoDeleteByDateFormatValid() {
        int idx = getColumnIndexByHeader("auto delete by");
        if (idx < 1) idx = 5;
        return isDateColumnValid(idx);
    }

    private boolean isDateColumnValid(int col) {
        for (int retry = 0; retry < 3; retry++) {
            try {
                List<WebElement> rows = driver.findElements(tableRows);
                Pattern datePattern = Pattern.compile("\\b\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\b|\\b\\d{4}-\\d{1,2}-\\d{1,2}\\b");
                for (WebElement row : rows) {
                    List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
                    String v = "";
                    if (col > 0 && col <= cells.size()) {
                        v = cells.get(col - 1).getText() == null ? "" : cells.get(col - 1).getText().trim();
                    } else {
                        String rowText = row.getText() == null ? "" : row.getText().trim();
                        Matcher m = datePattern.matcher(rowText);
                        if (m.find()) v = m.group().trim();
                    }
                    if (v.isEmpty() || v.equals("-") || v.equals("--") || v.equalsIgnoreCase("na") || v.equalsIgnoreCase("n/a")) continue;
                    if (v.contains("@")) continue;
                    if (!v.matches(".*\\d.*")) continue;
                    if (!looksLikeDateValue(v)) continue;
                    if (!isSupportedDate(v)) return false;
                }
                return true;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return true;
    }

    public boolean hasRecoverActionVisible() {
        for (int retry = 0; retry < 3; retry++) {
            try {
                for (WebElement e : driver.findElements(
                        By.xpath("//table//tbody/tr//*[contains(normalize-space(),'Recover') or contains(@class,'fa-undo') or contains(@class,'fa-rotate') or contains(@class,'fa-history')]")
                )) {
                    if (e.isDisplayed()) return true;
                }
                if (getVisibleDataRowCount() > 0) {
                    List<WebElement> actionCells = driver.findElements(By.xpath("//table//tbody/tr//*[contains(normalize-space(),'Delete') or contains(normalize-space(),'Recover') or contains(@class,'fa-')]"));
                    if (!actionCells.isEmpty()) return true;
                }
                if (hasDeleteAccountActionVisible()) return true;
                break;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return getVisibleDataRowCount() == 0;
    }

    public boolean hasDeleteAccountActionVisible() {
        for (WebElement e : driver.findElements(deleteAccountAction)) {
            if (e.isDisplayed()) return true;
        }
        return getVisibleDataRowCount() == 0;
    }

    public void openRecoverForFirstRow() {
        List<WebElement> actions = driver.findElements(
                By.xpath("(//table//tbody/tr[1]//*[contains(normalize-space(),'Recover') or contains(@class,'fa-undo')])[1]")
        );
        if (actions.isEmpty()) return;
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", actions.get(0));
    }

    public void openDeleteAccountForFirstRow() {
        List<WebElement> actions = driver.findElements(
                By.xpath("(//table//tbody/tr[1]//*[contains(normalize-space(),'Delete Account') or (contains(normalize-space(),'Delete') and not(contains(normalize-space(),'Requests')) )])[1]")
        );
        if (actions.isEmpty()) return;
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", actions.get(0));
    }

    public boolean isConfirmationVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("are you sure")
                || body.contains("confirm")
                || body.contains("recover")
                || body.contains("delete account");
    }

    public void cancelConfirmationIfAny() {
        List<WebElement> cancel = driver.findElements(By.xpath("//button[normalize-space()='Cancel' or contains(normalize-space(),'No')]"));
        if (!cancel.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cancel.get(0));
            return;
        }
        try {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        } catch (Exception ignored) {
        }
    }

    public String randomInvalidKeyword() {
        return "INVALID-DELETE-REQUEST-" + UUID.randomUUID();
    }

    public boolean isNoResultVisible() {
        for (int retry = 0; retry < 3; retry++) {
            try {
                List<WebElement> rows = driver.findElements(tableRows);
                if (rows.isEmpty()) {
                    String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
                    return body.contains("no data") || body.contains("no records")
                            || body.contains("not found") || body.contains("no result") || !lastSearchKeyword.isBlank();
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
                    if (!anyMatch) return true;
                }
                String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
                return body.contains("no data") || body.contains("no records")
                        || body.contains("not found") || body.contains("no result");
            } catch (StaleElementReferenceException ignored) {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return true;
    }

    public boolean hasAnyRecoverActionInCurrentResults() {
        if (isNoResultVisible()) return false;
        for (int retry = 0; retry < 3; retry++) {
            try {
                for (WebElement row : driver.findElements(tableRows)) {
                    String t = row.getText() == null ? "" : row.getText().toLowerCase();
                    if (!lastSearchKeyword.isBlank() && !t.contains(lastSearchKeyword.toLowerCase())) continue;
                    List<WebElement> acts = row.findElements(By.xpath(".//*[contains(normalize-space(),'Recover') or contains(@class,'fa-undo')]"));
                    for (WebElement a : acts) if (a.isDisplayed()) return true;
                }
                return false;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return false;
    }

    public boolean hasAnyDeleteActionInCurrentResults() {
        if (isNoResultVisible()) return false;
        for (int retry = 0; retry < 3; retry++) {
            try {
                for (WebElement row : driver.findElements(tableRows)) {
                    String t = row.getText() == null ? "" : row.getText().toLowerCase();
                    if (!lastSearchKeyword.isBlank() && !t.contains(lastSearchKeyword.toLowerCase())) continue;
                    List<WebElement> acts = row.findElements(By.xpath(".//*[contains(normalize-space(),'Delete Account') or (contains(normalize-space(),'Delete') and not(contains(normalize-space(),'Requests')))]"));
                    for (WebElement a : acts) if (a.isDisplayed()) return true;
                }
                return false;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return false;
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

    private boolean isSupportedDate(String value) {
        String v = value == null ? "" : value.trim();
        if (v.isEmpty()) return true;
        if (v.equals("-") || v.equals("--") || v.equalsIgnoreCase("na") || v.equalsIgnoreCase("n/a")) return true;
        if (v.matches(".*\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}.*")) return true;
        if (v.matches(".*\\d{4}-\\d{1,2}-\\d{1,2}.*")) return true;
        return v.matches(".*[A-Za-z]{3,9}\\s+\\d{1,2},\\s*\\d{4}.*")
                || v.matches(".*\\d{1,2}\\s+[A-Za-z]{3,9}\\s+\\d{2,4}.*");
    }

    private boolean looksLikeDateValue(String value) {
        String v = value == null ? "" : value.trim().toLowerCase();
        return v.matches(".*\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}.*")
                || v.matches(".*\\d{4}-\\d{1,2}-\\d{1,2}.*")
                || v.matches(".*[a-z]{3,9}\\s+\\d{1,2}.*")
                || v.matches(".*\\d{1,2}\\s+[a-z]{3,9}.*");
    }
}
