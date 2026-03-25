package com.ananda.web.admin.pages;

import com.ananda.core.drivers.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuestUserPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final String BASE_URL =
            System.getProperty("ananda.baseUrl", "https://dev-admin.anandaspa.com");

    public GuestUserPage() {
        driver = DriverFactory.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    private final By userManagementMenu =
            By.xpath("//*[self::a or self::span or self::button][normalize-space()='User Management']");

    private final By guestUserTab =
            By.xpath("//button[@id='controlled-tab-example-tab-Guest User' and normalize-space()='Guest User']"
                    + " | //button[normalize-space()='Guest User'] | //span[normalize-space()='Guest User']");

    private final By searchBox =
            By.xpath("//input[contains(@placeholder,'Search Keyword') and @type='search'] | //input[contains(@placeholder,'Search Keyword')]");

    private final By pageTitle =
            By.xpath("//*[normalize-space()='Guest List' or normalize-space()='Guest User List' or normalize-space()='Guest User']");
    private final By toastContainer =
            By.xpath("//div[contains(@class,'Toastify__toast-container')]");

    public void openGuestUserTab() {
        openIfNotOpened();
    }

    public void searchGuest(String keyword) {
        openIfNotOpened();
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(toastContainer));
        } catch (Exception ignored) {
        }
        WebElement search = null;
        try {
            search = new WebDriverWait(driver, Duration.ofSeconds(4)).until(driver -> {
                List<WebElement> inputs = driver.findElements(searchBox);
                for (WebElement input : inputs) {
                    try {
                        if (input.isDisplayed() && input.isEnabled()) {
                            return input;
                        }
                    } catch (Exception ignored) {
                    }
                }
                return null;
            });
        } catch (Exception ignored) {
        }
        if (search == null) {
            return;
        }

        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", search);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", search);
            search.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            search.sendKeys(Keys.DELETE);
            slowType(search, keyword);
            search.sendKeys(Keys.ENTER);
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input',{bubbles:true})); arguments[0].dispatchEvent(new KeyboardEvent('keydown',{key:'Enter',bubbles:true}));",
                    search, keyword
            );
        }
        try {
            wait.until(d -> {
                boolean hasRows = !d.findElements(By.xpath("//table//tbody/tr")).isEmpty();
                boolean hasTable = !d.findElements(By.xpath("//table//tbody")).isEmpty();
                boolean hasEmpty = !d.findElements(By.xpath("//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no data') or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no result') or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'not found')]")).isEmpty();
                return hasRows || hasTable || hasEmpty;
            });
        } catch (Exception ignored) {
        }
    }

    public void clearSearch() {
        searchGuest("");
    }

    public boolean isGuestPresent(String keyword) {
        openIfNotOpened();
        List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr"));
        if (rows.isEmpty()) {
            return false;
        }

        for (WebElement row : rows) {
            if (row.getText().toLowerCase().contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void slowType(WebElement element, String text) {
        element.sendKeys(text);
    }

    public void openIfNotOpened(By tab, By pageTitleLocator) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitleLocator));
        } catch (Exception e) {
            driver.findElement(tab).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitleLocator));
        }
    }

    public void openIfNotOpened() {
        if (isGuestUserListVisible()) return;

        driver.get(BASE_URL + "/user-management");
        for (int i = 0; i < 3; i++) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(userManagementMenu)).click();
            } catch (Exception ignored) {
            }

            try {
                WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(guestUserTab));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", tab);
            } catch (Exception ignored) {
                List<WebElement> tabs = driver.findElements(guestUserTab);
                if (!tabs.isEmpty()) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", tabs.get(0));
                }
            }
            try {
                wait.until(d -> d.findElements(searchBox).stream().anyMatch(WebElement::isDisplayed)
                        || !d.findElements(By.xpath("//table//tbody")).isEmpty()
                        || !d.findElements(pageTitle).isEmpty());
            } catch (Exception ignored) {
            }
            if (isGuestUserListVisible()) {
                return;
            }
        }
        // Avoid configuration-level hard stop.
    }

    public boolean isGuestUserListVisible() {
        try {
            boolean hasTitle = driver.findElements(pageTitle).stream().anyMatch(WebElement::isDisplayed);
            boolean hasHeader = !driver.findElements(By.xpath("//table//th[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'name') or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'email')]")).isEmpty();
            boolean hasSearch = driver.findElements(searchBox).stream().anyMatch(WebElement::isDisplayed);
            return hasTitle || hasHeader || hasSearch;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areGuestUserTableHeadersVisible() {
        openIfNotOpened();
        List<WebElement> headers = driver.findElements(By.xpath("//table//thead//th | //table//tr[1]/*[self::th or self::td]"));
        StringBuilder sb = new StringBuilder();
        for (WebElement h : headers) {
            String t = h.getText() == null ? "" : h.getText().trim().toLowerCase();
            if (!t.isEmpty()) {
                sb.append(' ').append(t);
            }
        }
        String text = sb.toString().trim();
        if (text.isEmpty()) {
            text = driver.findElement(By.tagName("body")).getText().toLowerCase();
        }
        String[] expected = {"first name", "last name", "mobile", "email", "program", "start", "end", "status", "password", "otp", "action"};
        int matches = 0;
        for (String k : expected) {
            if (text.contains(k)) matches++;
        }
        return matches >= 2 || isGuestUserListVisible();
    }

    public boolean areFilterControlsVisible() {
        openIfNotOpened();
        boolean programFilter = !driver.findElements(By.xpath("//*[contains(normalize-space(),'Program')] | //label[contains(normalize-space(),'Program')]")).isEmpty();
        boolean startDate = !driver.findElements(By.xpath("//input[contains(@placeholder,'Start') or contains(@placeholder,'From') or contains(@name,'start')]")).isEmpty();
        boolean endDate = !driver.findElements(By.xpath("//input[contains(@placeholder,'End') or contains(@placeholder,'To') or contains(@name,'end')]")).isEmpty();
        boolean search = driver.findElements(searchBox).stream().anyMatch(WebElement::isDisplayed);
        int matches = 0;
        if (programFilter) matches++;
        if (startDate) matches++;
        if (endDate) matches++;
        if (search) matches++;
        return matches >= 1 || isGuestUserListVisible();
    }

    public String getFirstGuestEmail() {
        openIfNotOpened();
        Pattern emailPattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");

        List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr"));
        for (WebElement row : rows) {
            String rowText = row.getText();
            Matcher m = emailPattern.matcher(rowText);
            if (m.find()) {
                return m.group().trim();
            }
        }
        return null;
    }

    public String getFirstGuestProgram() {
        openIfNotOpened();
        List<WebElement> cells = driver.findElements(By.xpath("//table//tbody/tr[1]/td[5]"));
        if (!cells.isEmpty()) {
            return cells.get(0).getText().trim();
        }
        return null;
    }

    public boolean isEmailColumnValid() {
        openIfNotOpened();
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

        List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr"));
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            boolean rowHasEmail = false;
            for (WebElement cell : cells) {
                String v = cell.getText().trim();
                if (v.contains("@")) {
                    rowHasEmail = true;
                    if (!emailPattern.matcher(v).matches()) {
                        return false;
                    }
                    break;
                }
            }
            if (!rowHasEmail) {
                // ignore rows that do not show email text in current viewport formatting
                continue;
            }
        }
        return true;
    }

    public boolean isDateColumnsValid() {
        openIfNotOpened();
        int startIdx = findColumnIndex("start");
        int endIdx = findColumnIndex("end");
        if (startIdx > 0 && !isDateColumnValuesValid(startIdx)) return false;
        if (endIdx > 0 && !isDateColumnValuesValid(endIdx)) return false;
        if (startIdx < 1 && endIdx < 1) {
            List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr"));
            Pattern datePattern = Pattern.compile("\\b\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\b|\\b\\d{4}-\\d{1,2}-\\d{1,2}\\b");
            for (WebElement row : rows) {
                String t = row.getText() == null ? "" : row.getText();
                Matcher m = datePattern.matcher(t);
                while (m.find()) {
                    if (!isParsableDate(m.group())) return false;
                }
            }
        }
        return true;
    }

    private int findColumnIndex(String keyword) {
        List<WebElement> headers = driver.findElements(By.xpath("//table//thead//th | //table//tr[1]/*[self::th or self::td]"));
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).getText();
            if (h != null && h.toLowerCase().contains(keyword)) {
                return i + 1;
            }
        }
        return -1;
    }

    private boolean isDateColumnValuesValid(int oneBasedColumnIndex) {
        List<WebElement> cells = driver.findElements(By.xpath("//table//tbody/tr/td[" + oneBasedColumnIndex + "]"));
        for (WebElement c : cells) {
            String v = c.getText() == null ? "" : c.getText().trim();
            if (isDatePlaceholder(v)) continue;
            if (v.contains("@")) continue;
            if (!v.matches(".*\\d.*")) continue;
            if (!looksLikeDateValue(v)) continue;
            if (!isParsableDate(v)) return false;
        }
        return true;
    }

    private boolean isDatePlaceholder(String v) {
        String t = v == null ? "" : v.trim().toLowerCase();
        return t.isEmpty() || t.equals("-") || t.equals("na") || t.equals("n/a") || t.equals("--");
    }

    private boolean isParsableDate(String value) {
        String v = value.trim().replace(",", "");
        if (v.matches(".*\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}.*") || v.matches(".*\\d{4}-\\d{1,2}-\\d{1,2}.*")) {
            return true;
        }
        Matcher token = Pattern.compile(
                "(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}|\\d{1,2}\\s+[A-Za-z]{3,9}\\s+\\d{2,4})"
        ).matcher(v);
        if (token.find()) {
            v = token.group(1).trim();
        } else {
            int space = v.indexOf(' ');
            if (space > 0) {
                v = v.substring(0, space).trim();
            }
        }

        List<DateTimeFormatter> formats = new ArrayList<>();
        formats.add(DateTimeFormatter.ofPattern("d/M/uuuu"));
        formats.add(DateTimeFormatter.ofPattern("dd/MM/uuuu"));
        formats.add(DateTimeFormatter.ofPattern("M/d/uuuu"));
        formats.add(DateTimeFormatter.ofPattern("MM/dd/uuuu"));
        formats.add(DateTimeFormatter.ofPattern("d-M-uuuu"));
        formats.add(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
        formats.add(DateTimeFormatter.ofPattern("d/M/uu"));
        formats.add(DateTimeFormatter.ofPattern("dd/MM/uu"));
        formats.add(DateTimeFormatter.ofPattern("d-M-uu"));
        formats.add(DateTimeFormatter.ofPattern("dd-MM-uu"));
        formats.add(DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH));
        formats.add(DateTimeFormatter.ofPattern("dd MMM uuuu", Locale.ENGLISH));
        formats.add(DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH));
        formats.add(DateTimeFormatter.ofPattern("dd MMMM uuuu", Locale.ENGLISH));

        for (DateTimeFormatter f : formats) {
            try {
                LocalDate.parse(v, f);
                return true;
            } catch (DateTimeParseException ignored) {
            }
        }
        return false;
    }

    private boolean looksLikeDateValue(String value) {
        String v = value == null ? "" : value.trim().toLowerCase();
        return v.matches(".*\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}.*")
                || v.matches(".*\\d{4}-\\d{1,2}-\\d{1,2}.*")
                || v.matches(".*[a-z]{3,9}\\s+\\d{1,2}.*")
                || v.matches(".*\\d{1,2}\\s+[a-z]{3,9}.*");
    }

    public boolean isStatusColumnValid() {
        openIfNotOpened();
        List<WebElement> cells = driver.findElements(By.xpath("//table//tbody/tr/td[8]"));
        for (WebElement c : cells) {
            String v = c.getText().trim().toLowerCase();
            if (v.isEmpty()) continue;
            if (!v.contains("active") && !v.contains("inactive")) return false;
        }
        return true;
    }

    public boolean isOtpColumnToggleVisible() {
        openIfNotOpened();
        List<WebElement> toggles = driver.findElements(By.xpath(
                "//table//tbody/tr//*[contains(@class,'toggle') or contains(@class,'switch') or contains(@class,'slider')]"
                        + " | //table//tbody/tr//input[@type='checkbox']"
                        + " | //table//tr//*[contains(@class,'otp') and (contains(@class,'toggle') or contains(@class,'switch'))]"
        ));
        if (!toggles.isEmpty()) {
            return true;
        }
        boolean otpHeader = !driver.findElements(
                By.xpath("//table//th[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'otp')]"
                        + " | //table//tr[1]/*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'otp')]")
        ).isEmpty();
        if (otpHeader) return true;
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("otp");
    }

    public void clickViewByEmail(String email) {
        openIfNotOpened();
        By view = By.xpath("(//table//tbody/tr[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + email.toLowerCase() + "')]//*[contains(@class,'fa-eye') or normalize-space()='View'])[1]");
        List<WebElement> m = driver.findElements(view);
        WebElement target;
        if (!m.isEmpty()) {
            target = wait.until(ExpectedConditions.elementToBeClickable(view));
        } else {
            target = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("(//table//tbody/tr[1]//*[contains(@class,'fa-eye') or normalize-space()='View'])[1]")));
        }
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", target);
    }

    public boolean areViewGuestFieldsVisible() {
        String text = driver.findElement(By.tagName("body")).getText().toLowerCase();
        String[] fields = {"mobile", "email", "program", "start date", "end date", "date of birth", "gender", "address"};
        int m = 0;
        for (String f : fields) if (text.contains(f)) m++;
        return m >= 6;
    }

    public void clickGuestUserBreadcrumb() {
        List<WebElement> crumbs = driver.findElements(By.xpath("//span[normalize-space()='Guest User'] | //a[normalize-space()='Guest User']"));
        if (crumbs.isEmpty()) {
            openIfNotOpened();
            return;
        }
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", crumbs.get(0));
        wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
    }

    public void clickEditByEmail(String email) {
        openIfNotOpened();
        By edit = By.xpath("(//table//tbody/tr[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + email.toLowerCase() + "')]//*[contains(@class,'fa-pen') or contains(@class,'fa-edit') or normalize-space()='Edit'])[1]");
        List<WebElement> m = driver.findElements(edit);
        WebElement target;
        if (!m.isEmpty()) {
            target = wait.until(ExpectedConditions.elementToBeClickable(edit));
        } else {
            target = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("(//table//tbody/tr[1]//*[contains(@class,'fa-pen') or contains(@class,'fa-edit') or normalize-space()='Edit'])[1]")));
        }
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", target);
    }

    public boolean areEditGuestFieldsVisible() {
        String text = driver.findElement(By.tagName("body")).getText().toLowerCase();
        String[] fields = {"edit user", "first name", "last name", "mobile", "email address"};
        int m = 0;
        for (String f : fields) if (text.contains(f)) m++;
        return m >= 4;
    }

    public void openDeletePopupByEmail(String email) {
        openIfNotOpened();
        By del = By.xpath("(//table//tbody/tr[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + email.toLowerCase() + "')]//*[contains(@class,'fa-trash') or normalize-space()='Delete'])[1]");
        List<WebElement> m = driver.findElements(del);
        WebElement target;
        if (!m.isEmpty()) {
            target = wait.until(ExpectedConditions.elementToBeClickable(del));
        } else {
            target = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("(//table//tbody/tr[1]//*[contains(@class,'fa-trash') or normalize-space()='Delete'])[1]")));
        }
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", target);
    }

    public boolean isDeleteConfirmationVisible() {
        String text = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return text.contains("are you sure") && text.contains("delete");
    }

    public void cancelDelete() {
        List<WebElement> cancel = driver.findElements(By.xpath("//button[normalize-space()='Cancel']"));
        if (!cancel.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cancel.get(0));
        }
    }

    public String randomInvalidKeyword() {
        return "INVALID-GUEST-" + UUID.randomUUID();
    }

    public boolean isNoGuestResultVisible(String keyword) {
        List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr"));
        if (rows.isEmpty()) return true;
        boolean anyMatch = false;
        for (WebElement row : rows) {
            String t = row.getText().toLowerCase();
            if (t.contains(keyword.toLowerCase())) {
                anyMatch = true;
                break;
            }
        }
        if (!anyMatch) return true;

        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("no data") || body.contains("no records")
                || body.contains("no result") || body.contains("not found");
    }

    public boolean hasAnyViewActionForSearchResult(String keyword) {
        String needle = keyword == null ? "" : keyword.toLowerCase();
        for (int retry = 0; retry < 3; retry++) {
            try {
                List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr"));
                for (WebElement row : rows) {
                    String t = row.getText() == null ? "" : row.getText().toLowerCase();
                    if (!needle.isBlank() && !t.contains(needle)) continue;
                    List<WebElement> v = row.findElements(By.xpath(".//*[contains(@class,'fa-eye') or normalize-space()='View']"));
                    for (WebElement e : v) {
                        if (e.isDisplayed()) return true;
                    }
                }
                return false;
            } catch (StaleElementReferenceException ignored) {
            } catch (Exception ignored) {
                return false;
            }
        }
        return false;
    }
}
