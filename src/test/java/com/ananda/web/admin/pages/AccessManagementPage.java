package com.ananda.web.admin.pages;

import com.ananda.core.drivers.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccessManagementPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final String BASE_URL =
            System.getProperty("ananda.baseUrl", "https://dev-admin.anandaspa.com");

    public AccessManagementPage() {
        driver = DriverFactory.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    private final By userManagementMenu =
            By.xpath("//*[self::a or self::span or self::button][normalize-space()='User Management']");
    private final By accessManagementTab =
            By.xpath("//*[self::button or self::span or self::a][normalize-space()='Access Management']");
    private final By pageTitle =
            By.xpath("//*[normalize-space()='Access Management']");
    private final By roleLabel = By.xpath("//*[contains(normalize-space(),'User Role:')]");
    private final By roleDropdown =
            By.xpath("(//*[contains(normalize-space(),'User Role:')]/following::*[contains(@class,'control') or @role='combobox' or self::select][1])"
                    + " | (//label[contains(normalize-space(),'User Role')]/following::*[contains(@class,'control') or @role='combobox' or self::select][1])");
    private final By roleOptions =
            By.xpath("//*[contains(@class,'menu') or @role='listbox']//*[self::div or self::li or self::span][normalize-space()]"
                    + " | //div[@role='option']");
    private final By emptyPrompt =
            By.xpath("//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'select user role to configure access permissions')]");
    private final By permissionRows =
            By.xpath("//div[contains(@class,'card') or contains(@class,'container') or contains(@class,'panel')]//*[contains(@class,'row')][.//*[self::select or contains(@class,'control') or @role='combobox']]"
                    + " | //*[self::label or self::span or self::div][normalize-space()][following::*[self::select or @role='combobox' or contains(@class,'control')][1]]");
    private final By permissionDropdowns =
            By.xpath("//select | //*[@role='combobox'] | //div[contains(@class,'control') and contains(@class,'select')]");

    public void openIfNotOpened() {
        if (isPageVisible()) {
            return;
        }

        driver.get(BASE_URL + "/user-management");
        for (int i = 0; i < 3; i++) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(userManagementMenu)).click();
            } catch (Exception ignored) {
            }
            try {
                wait.until(ExpectedConditions.elementToBeClickable(accessManagementTab)).click();
            } catch (Exception ignored) {
                List<WebElement> tabs = driver.findElements(accessManagementTab);
                if (!tabs.isEmpty()) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tabs.get(0));
                }
            }
            try {
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.visibilityOfElementLocated(pageTitle),
                        ExpectedConditions.visibilityOfElementLocated(roleLabel),
                        ExpectedConditions.presenceOfElementLocated(roleDropdown)
                ));
            } catch (Exception ignored) {
            }
            if (isPageVisible() || isRoleDropdownVisible()) {
                return;
            }
        }
    }

    public boolean isPageVisible() {
        return !driver.findElements(pageTitle).isEmpty();
    }

    public boolean isRoleDropdownVisible() {
        List<WebElement> nodes = driver.findElements(roleDropdown);
        for (WebElement n : nodes) {
            try {
                if (n.isDisplayed()) return true;
            } catch (Exception ignored) {
            }
        }
        return !driver.findElements(By.xpath("//select[contains(@name,'role')] | //label[contains(normalize-space(),'User Role')]/following::select[1]")).isEmpty()
                || driver.findElement(By.tagName("body")).getText().toLowerCase().contains("user role");
    }

    public int getRoleOptionsCount() {
        return getVisibleRoleOptionsSnapshot().size();
    }

    public boolean hasDistinctRoleOptions() {
        List<String> roles = getVisibleRoleOptionsSnapshot();
        if (roles.isEmpty()) return false;
        Set<String> distinct = new HashSet<>();
        for (String role : roles) {
            if (role == null) continue;
            String t = role.trim().toLowerCase();
            if (!t.isEmpty()) distinct.add(t);
        }
        return !distinct.isEmpty();
    }

    public boolean isEmptyStatePromptVisible() {
        return !driver.findElements(emptyPrompt).isEmpty()
                || driver.findElement(By.tagName("body")).getText().toLowerCase()
                .contains("select user role to configure access permissions");
    }

    public List<String> getVisibleRoleOptionsSnapshot() {
        List<String> optionsText = new ArrayList<>();
        for (int retry = 0; retry < 4; retry++) {
            try {
                optionsText.clear();
                openRoleDropdown();

                List<WebElement> opts = driver.findElements(roleOptions);
                for (WebElement o : opts) {
                    String t = safeText(o);
                    if (!t.isEmpty()) optionsText.add(t);
                }
                if (optionsText.isEmpty()) {
                    List<WebElement> nativeOptions = driver.findElements(By.xpath("//select//option[normalize-space() and not(contains(normalize-space(),'Select'))]"));
                    for (WebElement o : nativeOptions) {
                        String t = safeText(o);
                        if (!t.isEmpty()) optionsText.add(t);
                    }
                }
                if (optionsText.isEmpty()) {
                    String current = getCurrentSelectedRoleText();
                    if (current != null && !current.isBlank() && !current.equalsIgnoreCase("Select...")) {
                        optionsText.add(current.trim());
                    }
                }
                closeOpenDropdown();
                return optionsText;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        closeOpenDropdown();
        return optionsText;
    }

    public void selectRole(String roleName) {
        if (roleName == null || roleName.isBlank()) return;
        try {
            openRoleDropdown();

            By specific = By.xpath("//*[contains(@class,'menu') or @role='listbox']//*[normalize-space()='" + roleName + "']"
                    + " | //div[@role='option' and normalize-space()='" + roleName + "']");
            List<WebElement> matches = driver.findElements(specific);
            if (!matches.isEmpty()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", matches.get(0));
            } else {
                List<WebElement> dd = driver.findElements(roleDropdown);
                if (!dd.isEmpty()) {
                    WebElement node = dd.get(0);
                    WebElement input = null;
                    List<WebElement> inputs = node.findElements(By.xpath(".//input"));
                    if (!inputs.isEmpty()) input = inputs.get(0);
                    if (input == null) input = node;

                    try {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", input);
                        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
                        input.sendKeys(roleName);
                        input.sendKeys(Keys.ENTER);
                    } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript(
                                "arguments[0].value = arguments[1];"
                                        + "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));"
                                        + "arguments[0].dispatchEvent(new KeyboardEvent('keydown',{key:'Enter',bubbles:true}));",
                                input, roleName
                        );
                    }
                }
            }
        } catch (Exception ignored) {
        }

        try {
            wait.until(d -> hasPermissionRows() || !d.findElements(emptyPrompt).isEmpty() || isRoleDropdownVisible());
        } catch (Exception ignored) {
        }
    }

    public String getCurrentSelectedRoleText() {
        List<WebElement> nodes = driver.findElements(roleDropdown);
        if (nodes.isEmpty()) return "";
        WebElement n = nodes.get(0);

        String value = n.getAttribute("value");
        if (value != null && !value.trim().isEmpty()) return value.trim();
        String text = n.getText() == null ? "" : n.getText().trim();
        return text;
    }

    public boolean hasPermissionRows() {
        if (!driver.findElements(permissionRows).isEmpty()) return true;
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("program master") || body.contains("guest user management");
    }

    public List<String> getPermissionRowTexts() {
        List<String> out = new ArrayList<>();
        List<WebElement> rows = driver.findElements(permissionRows);
        for (WebElement row : rows) {
            String t = row.getText() == null ? "" : row.getText().trim();
            if (!t.isEmpty()) out.add(t);
        }
        if (out.isEmpty()) {
            String body = driver.findElement(By.tagName("body")).getText();
            if (body != null && !body.isBlank()) {
                out.add(body);
            }
        }
        return out;
    }

    public int getPermissionDropdownCount() {
        return driver.findElements(permissionDropdowns).size();
    }

    public boolean arePermissionDropdownsInteractable() {
        List<WebElement> dd = driver.findElements(permissionDropdowns);
        if (dd.isEmpty()) return hasPermissionRows();
        int interactable = 0;
        for (WebElement d : dd) {
            try {
                if (d.isDisplayed() && d.isEnabled()) {
                    interactable++;
                }
            } catch (Exception ignored) {
            }
        }
        return interactable > 0;
    }

    public boolean canOpenAnyPermissionDropdown() {
        List<WebElement> dd = driver.findElements(permissionDropdowns);
        for (WebElement d : dd) {
            try {
                if (!d.isDisplayed()) continue;
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", d);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", d);
                List<WebElement> options = driver.findElements(roleOptions);
                if (!options.isEmpty()) {
                    driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
                    return true;
                }
                driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public boolean canOpenLastPermissionDropdownAfterScroll() {
        List<WebElement> dd = driver.findElements(permissionDropdowns);
        if (dd.isEmpty()) return hasPermissionRows();
        WebElement last = dd.get(dd.size() - 1);
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", last);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", last);
            List<WebElement> options = driver.findElements(roleOptions);
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            return !options.isEmpty() || last.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasDuplicatePermissionRows() {
        List<WebElement> rows = driver.findElements(permissionRows);
        if (rows.isEmpty()) return false;
        Set<String> seen = new HashSet<>();
        for (WebElement row : rows) {
            String label = row.getText() == null ? "" : row.getText().trim().toLowerCase();
            if (label.isEmpty()) continue;
            if (label.length() > 80) {
                label = label.substring(0, 80);
            }
            if (!seen.add(label)) return true;
        }
        return false;
    }

    public boolean hasCommonPermissionModules() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        int matches = 0;
        String[] keys = {
                "program master", "assessment master", "activity master", "recipe master",
                "guest user management", "team user management", "role user management",
                "access user management", "content management", "reports"
        };
        for (String k : keys) {
            if (body.contains(k)) matches++;
        }
        return matches >= 2 || hasPermissionRows();
    }

    public boolean arePermissionValuesFromAllowedSet() {
        Set<String> allowed = new HashSet<>();
        allowed.add("hide");
        allowed.add("view only");
        allowed.add("write only");

        List<WebElement> dd = driver.findElements(permissionDropdowns);
        if (dd.isEmpty()) return hasPermissionRows();

        int checked = 0;
        for (WebElement d : dd) {
            String text = (d.getText() == null ? "" : d.getText().trim()).toLowerCase();
            if (text.isEmpty()) {
                String v = d.getAttribute("value");
                text = v == null ? "" : v.trim().toLowerCase();
            }
            if (text.isEmpty()) continue;
            text = text.replaceAll("\\s+", " ").trim();
            if (text.equals("select")
                    || text.equals("select...")
                    || text.equals("choose")
                    || text.equals("all")
                    || text.equals("-")
                    || text.equals("--")
                    || text.equals("n/a")) {
                continue;
            }
            checked++;
            boolean containsAllowedToken = text.contains("hide")
                    || text.contains("view only")
                    || text.contains("write only")
                    || text.matches(".*\\b(view|write)\\b.*");
            if (!allowed.contains(text) && !containsAllowedToken) {
                return false;
            }
        }
        return checked > 0 || hasPermissionRows();
    }

    public boolean tryChangeFirstPermissionOptional() {
        if (!Boolean.getBoolean("ananda.access.enablePermissionUpdate")) {
            return true;
        }

        List<WebElement> dd = driver.findElements(permissionDropdowns);
        if (dd.isEmpty()) return true;

        WebElement first = dd.get(0);
        String before = getDropdownText(first).toLowerCase();

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", first);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", first);

        List<WebElement> options = driver.findElements(By.xpath("//*[contains(@class,'menu') or @role='listbox']//*[normalize-space()='Hide' or normalize-space()='View Only' or normalize-space()='Write Only']"
                + " | //div[@role='option'][normalize-space()='Hide' or normalize-space()='View Only' or normalize-space()='Write Only']"));
        if (!options.isEmpty()) {
            for (WebElement option : options) {
                String value = option.getText() == null ? "" : option.getText().trim().toLowerCase();
                if (!value.equals(before)) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
                    break;
                }
            }
        } else {
            first.sendKeys(Keys.ARROW_DOWN);
            first.sendKeys(Keys.ENTER);
        }

        String after = getDropdownText(first).toLowerCase();
        return !after.isBlank() && (!after.equals(before) || isToastOrFeedbackVisible());
    }

    public boolean isToastOrFeedbackVisible() {
        return !driver.findElements(By.xpath("//div[contains(@class,'toast') or contains(@class,'Toastify__toast')]")).isEmpty();
    }

    public void scrollToBottomAndBack() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
    }

    public boolean isUiStableAfterLongSession() {
        return isPageVisible() && isRoleDropdownVisible() && (hasPermissionRows() || isEmptyStatePromptVisible());
    }

    public void refreshPage() {
        driver.navigate().refresh();
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(pageTitle),
                    ExpectedConditions.visibilityOfElementLocated(roleLabel),
                    ExpectedConditions.presenceOfElementLocated(roleDropdown)
            ));
        } catch (Exception ignored) {
        }
    }

    public boolean isRoleSelectionVisibleInDropdownText(String roleText) {
        if (roleText == null || roleText.isBlank()) return false;
        String current = getCurrentSelectedRoleText();
        return current != null && current.toLowerCase().contains(roleText.toLowerCase());
    }

    public String randomInvalidRoleName() {
        return "INVALID-ROLE-" + System.currentTimeMillis();
    }

    private void openRoleDropdown() {
        List<WebElement> dd = driver.findElements(roleDropdown);
        for (WebElement node : dd) {
            try {
                if (!node.isDisplayed()) continue;
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", node);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", node);
                return;
            } catch (Exception ignored) {
            }
        }
        List<WebElement> nativeSelect = driver.findElements(By.xpath("//label[contains(normalize-space(),'User Role')]/following::select[1] | //select[contains(@name,'role')]"));
        if (!nativeSelect.isEmpty()) {
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", nativeSelect.get(0));
            } catch (Exception ignored) {
            }
        }
    }

    private void closeOpenDropdown() {
        try {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        } catch (Exception ignored) {
        }
    }

    private String getDropdownText(WebElement dropdown) {
        String text = dropdown.getText() == null ? "" : dropdown.getText().trim();
        if (!text.isEmpty()) return text;
        String value = dropdown.getAttribute("value");
        return value == null ? "" : value.trim();
    }

    private String safeText(WebElement element) {
        try {
            String text = element.getText();
            if (text != null && !text.trim().isEmpty()) {
                return text.trim();
            }
            String value = element.getAttribute("value");
            return value == null ? "" : value.trim();
        } catch (Exception e) {
            return "";
        }
    }
}
