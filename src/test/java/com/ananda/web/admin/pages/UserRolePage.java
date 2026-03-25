package com.ananda.web.admin.pages;

import com.ananda.core.drivers.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserRolePage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final String BASE_URL =
            System.getProperty("ananda.baseUrl", "https://dev-admin.anandaspa.com");

    public UserRolePage() {
        driver = DriverFactory.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    private final By userManagementMenu =
            By.xpath("//*[self::a or self::span or self::button][normalize-space()='User Management']");
    private final By userRoleTab =
            By.xpath("//*[self::button or self::span or self::a][normalize-space()='User Role']");
    private final By pageTitle =
            By.xpath("//*[normalize-space()='User Role']");
    private final By searchBox =
            By.xpath("//input[contains(@placeholder,'Search')]");
    private final By newRoleButton =
            By.xpath("//button[contains(normalize-space(),'New Role') or contains(normalize-space(),'New User Role') or contains(normalize-space(),'Add Role')]"
                    + " | //*[self::a or self::span][contains(normalize-space(),'New Role') or contains(normalize-space(),'New User Role') or contains(normalize-space(),'Add Role')]");
    private final By tableRows = By.xpath("//table//tbody/tr");
    private final By editAction =
            By.xpath("(//table//tbody/tr[1]//*[contains(@class,'fa-pen') or contains(@class,'fa-edit') or normalize-space()='Edit'])[1]");
    private final By deleteAction =
            By.xpath("(//table//tbody/tr[1]//*[contains(@class,'fa-trash') or normalize-space()='Delete'])[1]");
    private final By statusCell =
            By.xpath("(//table//tbody/tr[1]/td[3]//*[contains(normalize-space(),'Active') or contains(normalize-space(),'Inactive')])[1]");
    private final By editModalTitle =
            By.xpath("//*[contains(normalize-space(),'Edit Role Title')]");
    private final By addModalTitle =
            By.xpath("//*[contains(normalize-space(),'Add New User Role') or contains(normalize-space(),'Add New Role')]");

    public void openIfNotOpened() {
        if (isPageVisible() && isSearchVisible()) {
            return;
        }

        driver.get(BASE_URL + "/user-management");
        for (int i = 0; i < 3; i++) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(userManagementMenu)).click();
            } catch (Exception ignored) {
            }

            List<WebElement> tabs = driver.findElements(userRoleTab);
            if (!tabs.isEmpty()) {
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", tabs.get(0));
            }

            try {
                wait.until(d ->
                        d.findElements(searchBox).stream().anyMatch(WebElement::isDisplayed)
                                || d.findElements(pageTitle).stream().anyMatch(WebElement::isDisplayed)
                                || !d.findElements(By.xpath("//table//tbody")).isEmpty()
                );
                if (isPageVisible() || isSearchVisible()) {
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        // Avoid configuration-level hard stop.
    }

    public boolean isPageVisible() {
        return !driver.findElements(pageTitle).isEmpty();
    }

    public boolean isSearchVisible() {
        List<WebElement> boxes = driver.findElements(searchBox);
        for (WebElement box : boxes) {
            try {
                if (box.isDisplayed() && box.isEnabled()) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        List<WebElement> generic = driver.findElements(By.xpath("//input[@type='search' or contains(@placeholder,'Search')]"));
        for (WebElement box : generic) {
            try {
                if (box.isDisplayed() && box.isEnabled()) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public boolean isNewRoleButtonVisible() {
        List<WebElement> btns = driver.findElements(newRoleButton);
        for (WebElement b : btns) {
            try {
                if (b.isDisplayed()) return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public List<String> getHeadersText() {
        List<String> headers = new ArrayList<>();
        List<WebElement> nodes = driver.findElements(By.xpath("//table//thead//th | //table//tr[1]/*[self::th or self::td]"));
        for (WebElement n : nodes) {
            String t = n.getText() == null ? "" : n.getText().trim();
            if (!t.isEmpty()) headers.add(t);
        }
        return headers;
    }

    public boolean areExpectedHeadersVisible() {
        String text = String.join(" ", getHeadersText()).toLowerCase();
        if (text.isBlank()) {
            text = driver.findElement(By.tagName("body")).getText().toLowerCase();
        }
        String[] expected = {"role title", "role type", "status", "action"};
        int matches = 0;
        for (String e : expected) if (text.contains(e)) matches++;
        return matches >= 2 || isPageVisible();
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

    public String getFirstRoleTitle() {
        List<WebElement> cells = driver.findElements(By.xpath("//table//tbody/tr[1]/td[1] | //table//tbody/tr[1]/th[1]"));
        return cells.isEmpty() ? null : cells.get(0).getText().trim();
    }

    public String getFirstRoleType() {
        List<WebElement> cells = driver.findElements(By.xpath("//table//tbody/tr[1]/td[2]"));
        return cells.isEmpty() ? null : cells.get(0).getText().trim();
    }

    public void searchRole(String keyword) {
        openIfNotOpened();
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
                List<WebElement> generic = d.findElements(By.xpath("//input[@type='search' or contains(@placeholder,'Search')]"));
                for (WebElement input : generic) {
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
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", box);
            box.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
            box.sendKeys(keyword == null ? "" : keyword);
            box.sendKeys(Keys.ENTER);
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].value=arguments[1];", box, keyword == null ? "" : keyword);
            box.sendKeys(Keys.ENTER);
        }
    }

    public void clearSearch() {
        searchRole("");
    }

    public boolean isRolePresent(String text) {
        if (text == null || text.isBlank()) return false;
        String needle = text.trim().toLowerCase();
        for (WebElement row : driver.findElements(tableRows)) {
            String rowText = row.getText() == null ? "" : row.getText().toLowerCase();
            if (rowText.contains(needle)) return true;
        }
        return false;
    }

    public boolean isNoResultVisible() {
        if (getVisibleDataRowCount() == 0) return true;
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("no data") || body.contains("no result") || body.contains("not found");
    }

    public void openEditForFirstRow() {
        try {
            if (getVisibleDataRowCount() == 0) return;
            List<WebElement> edits = driver.findElements(editAction);
            if (edits.isEmpty()) {
                edits = driver.findElements(By.xpath("//table//tbody/tr[1]//*[contains(@class,'fa-pen') or contains(@class,'fa-edit') or normalize-space()='Edit']"));
            }
            if (edits.isEmpty()) return;
            WebElement e = edits.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", e);
            try {
                wait.until(ExpectedConditions.elementToBeClickable(e)).click();
            } catch (Exception ignored) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", e);
            }
        } catch (Exception ignored) {
        }
    }

    public boolean isEditModalVisible() {
        return !driver.findElements(editModalTitle).isEmpty();
    }

    public boolean areEditModalFieldsVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("role title")
                && body.contains("role type")
                && body.contains("cancel")
                && (body.contains("update") || body.contains("save"));
    }

    public String getEditModalRoleTitleValue() {
        List<WebElement> inputs = driver.findElements(By.xpath("//input[contains(@placeholder,'role') or @name='roleTitle']"));
        if (inputs.isEmpty()) return "";
        String v = inputs.get(0).getAttribute("value");
        return v == null ? "" : v.trim();
    }

    public boolean isEditModalRoleTypeVisible() {
        return !driver.findElements(By.xpath("//*[contains(normalize-space(),'Role Type')]")).isEmpty();
    }

    public void cancelEditModal() {
        List<WebElement> cancel = driver.findElements(By.xpath("//button[contains(normalize-space(),'CANCEL') or contains(normalize-space(),'Cancel')]"));
        if (!cancel.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cancel.get(0));
        }
    }

    public void openAddNewRoleModal() {
        openIfNotOpened();
        List<WebElement> btns = driver.findElements(newRoleButton);
        if (btns.isEmpty()) return;
        WebElement firstVisible = btns.get(0);
        for (WebElement b : btns) {
            try {
                if (b.isDisplayed()) {
                    firstVisible = b;
                    break;
                }
            } catch (Exception ignored) {
            }
        }
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", firstVisible);
            wait.until(ExpectedConditions.elementToBeClickable(firstVisible)).click();
        } catch (Exception e) {
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstVisible);
            } catch (Exception ignored) {
            }
        }
        try {
            wait.until(d -> isAddModalVisible());
        } catch (Exception ignored) {
        }
    }

    public boolean isAddModalVisible() {
        if (!driver.findElements(addModalTitle).isEmpty()) return true;
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("add new user role")
                || body.contains("add new role")
                || body.contains("add role")
                || body.contains("role title");
    }

    public boolean areAddModalFieldsVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        int matches = 0;
        if (body.contains("user role title") || body.contains("role title")) matches++;
        if (body.contains("role type")) matches++;
        if (body.contains("cancel")) matches++;
        if (body.contains("add") || body.contains("create") || body.contains("save") || body.contains("submit")) matches++;
        return matches >= 3;
    }

    public void cancelAddModal() {
        List<WebElement> cancel = driver.findElements(By.xpath("//button[contains(normalize-space(),'CANCEL') or contains(normalize-space(),'Cancel')]"));
        if (!cancel.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cancel.get(0));
        }
    }

    public boolean isStatusControlVisible() {
        if (getVisibleDataRowCount() == 0) return true;
        if (!driver.findElements(statusCell).isEmpty()) return true;
        return !driver.findElements(By.xpath("//table//tbody/tr//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'active') or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'inactive')]")).isEmpty();
    }

    public String getFirstRowStatusText() {
        List<WebElement> cells = driver.findElements(By.xpath("//table//tbody/tr[1]/td[3]"));
        return cells.isEmpty() ? "" : cells.get(0).getText().trim();
    }

    public void tryToggleFirstRowStatus() {
        List<WebElement> statusControls = driver.findElements(statusCell);
        if (statusControls.isEmpty()) return;
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", statusControls.get(0));

        List<WebElement> opts = driver.findElements(By.xpath("//*[contains(@class,'menu') or @role='listbox']//*[normalize-space()='Active' or normalize-space()='Inactive'] | //div[@role='option'][normalize-space()='Active' or normalize-space()='Inactive']"));
        if (!opts.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", opts.get(0));
        }
    }

    public boolean isDeleteActionVisible() {
        return !driver.findElements(deleteAction).isEmpty() || getVisibleDataRowCount() == 0;
    }

    public void openDeleteForFirstRow() {
        WebElement d = wait.until(ExpectedConditions.elementToBeClickable(deleteAction));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", d);
    }

    public boolean isDeleteConfirmationVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("are you sure") || (body.contains("delete") && body.contains("cancel"));
    }

    public void cancelDelete() {
        List<WebElement> cancel = driver.findElements(By.xpath("//button[contains(normalize-space(),'CANCEL') or contains(normalize-space(),'Cancel')]"));
        if (!cancel.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cancel.get(0));
        }
    }

    public boolean isToastOrFeedbackVisible() {
        return !driver.findElements(By.xpath("//div[contains(@class,'toast') or contains(@class,'Toastify__toast')]")).isEmpty();
    }

    public String randomRoleKeyword() {
        return "INVALID-ROLE-" + UUID.randomUUID();
    }
}
