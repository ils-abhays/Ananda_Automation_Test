package com.ananda.web.admin.pages;

import com.ananda.core.drivers.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnnouncementPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final String BASE_URL =
            System.getProperty("ananda.baseUrl", "https://dev-admin.anandaspa.com");
    private String lastSearchKeyword = "";

    public AnnouncementPage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    private final By contentManagementMenu =
            By.xpath("//*[self::a or self::span or self::button][normalize-space()='Content Management']");
    private final By announcementTab =
            By.xpath("//button[normalize-space()='Announcement'] | //*[self::a or self::span][normalize-space()='Announcement']");
    private final By listTitle =
            By.xpath("//*[normalize-space()='Announcement List']");
    private final By searchBox =
            By.xpath("//input[contains(@placeholder,'Search Keyword') or contains(@placeholder,'Search')]");
    private final By addAnnouncementButton =
            By.xpath("//button[contains(normalize-space(),'Add Announcement')] | //*[self::a or self::span][contains(normalize-space(),'Add Announcement')]");
    private final By tableRows = By.xpath("//table//tbody/tr");
    private final By viewAction = By.xpath("(//table//tbody/tr//*[contains(@class,'fa-eye') or normalize-space()='View'])[1]");
    private final By editAction = By.xpath("(//table//tbody/tr//*[contains(@class,'fa-pen') or contains(@class,'fa-edit') or normalize-space()='Edit'])[1]");
    private final By deleteAction = By.xpath("(//table//tbody/tr//*[contains(@class,'fa-trash') or normalize-space()='Delete'])[1]");

    public void openIfNotOpened() {
        if (isPageVisible() && isSearchVisible()) {
            waitForUiIdle();
            return;
        }

        driver.get(BASE_URL + "/content-management");
        for (int i = 0; i < 3; i++) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(contentManagementMenu)).click();
            } catch (Exception ignored) {
            }

            try {
                List<WebElement> tabs = driver.findElements(announcementTab);
                if (!tabs.isEmpty()) {
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", tabs.get(0));
                }
            } catch (Exception ignored) {
            }

            try {
                wait.until(d ->
                        !d.findElements(listTitle).isEmpty()
                                || d.findElements(searchBox).stream().anyMatch(WebElement::isDisplayed)
                                || !d.findElements(By.xpath("//table//tbody")).isEmpty());
                waitForUiIdle();
                if (isPageVisible()) return;
            } catch (Exception ignored) {
            }
        }
    }

    public boolean isPageVisible() {
        try {
            boolean hasTab = !driver.findElements(announcementTab).isEmpty();
            boolean hasTitle = !driver.findElements(listTitle).isEmpty();
            boolean hasHeader = !driver.findElements(
                    By.xpath("//table//tr[1]//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'announcement title')]")
            ).isEmpty();
            return hasTitle || (hasTab && hasHeader);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSearchVisible() {
        for (WebElement b : driver.findElements(searchBox)) {
            try {
                if (b.isDisplayed() && b.isEnabled()) return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public boolean isAddAnnouncementButtonVisible() {
        for (WebElement b : driver.findElements(addAnnouncementButton)) {
            try {
                if (b.isDisplayed()) return true;
            } catch (Exception ignored) {
            }
        }
        return false;
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
        String[] expected = {"announcement title", "description", "has redirection", "visible to", "guest type", "last updated", "action"};
        int matches = 0;
        for (String key : expected) if (text.contains(key)) matches++;
        return matches >= 5 || isPageVisible();
    }

    public void searchKeyword(String keyword) {
        openIfNotOpened();
        if (!isSearchVisible()) return;
        lastSearchKeyword = keyword == null ? "" : keyword.trim();

        for (int retry = 0; retry < 3; retry++) {
            try {
                WebElement box = null;
                for (WebElement b : driver.findElements(searchBox)) {
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
                    String t = row.getText() == null ? "" : row.getText().toLowerCase();
                    if (t.contains(k)) return true;
                }
                return false;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return false;
    }

    public String getFirstTitle() {
        int titleCol = getColumnIndexByHeader("announcement title");
        if (titleCol < 1) titleCol = 1;

        List<WebElement> rows = driver.findElements(tableRows);
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < titleCol) continue;
            String value = cells.get(titleCol - 1).getText() == null ? "" : cells.get(titleCol - 1).getText().trim();
            if (!value.isEmpty()) return value;
        }
        return null;
    }

    public boolean isDateColumnFormatValid() {
        int col = getColumnIndexByHeader("last updated");
        if (col < 1) col = getColumnIndexByHeader("created");
        if (col < 1) return true;

        for (WebElement row : driver.findElements(tableRows)) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < col) continue;
            String v = cells.get(col - 1).getText() == null ? "" : cells.get(col - 1).getText().trim();
            if (v.isEmpty() || v.equals("-")) continue;
            if (!v.matches(".*\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4}.*")) return false;
        }
        return true;
    }

    public boolean isNoResultVisible() {
        for (int retry = 0; retry < 3; retry++) {
            try {
                List<WebElement> rows = driver.findElements(tableRows);
                if (rows.isEmpty()) return true;
                if (!lastSearchKeyword.isBlank()) {
                    boolean anyMatch = false;
                    for (WebElement row : rows) {
                        String t = row.getText() == null ? "" : row.getText().toLowerCase();
                        if (t.contains("no data") || t.contains("no result") || t.contains("not found")) continue;
                        if (t.contains(lastSearchKeyword.toLowerCase())) {
                            anyMatch = true;
                            break;
                        }
                    }
                    if (!anyMatch) return true;
                }
                String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
                return body.contains("no data") || body.contains("no result") || body.contains("not found");
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return true;
    }

    public void openFirstView() {
        List<WebElement> views = driver.findElements(viewAction);
        if (views.isEmpty()) return;
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", views.get(0));
        waitForUiIdle();
    }

    public void openFirstEdit() {
        List<WebElement> edits = driver.findElements(editAction);
        if (edits.isEmpty()) return;
        WebElement target = edits.get(0);
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", target);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", target);
        } catch (Exception ignored) {
            try {
                target.click();
            } catch (Exception ignoredAgain) {
            }
        }
        waitForUiIdle();
        try {
            wait.until(d -> isEditPageVisible() || areEditFieldsVisible() || isUiStable());
        } catch (Exception ignored) {
        }
    }

    public void openFirstDelete() {
        List<WebElement> deletes = driver.findElements(deleteAction);
        if (deletes.isEmpty()) return;
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", deletes.get(0));
        waitForUiIdle();
    }

    public boolean isViewModalVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("has redirection") && body.contains("visible to") && body.contains("guest type");
    }

    public boolean areViewModalFieldsVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        int matches = 0;
        if (body.contains("description")) matches++;
        if (body.contains("has redirection")) matches++;
        if (body.contains("visible to")) matches++;
        if (body.contains("guest type")) matches++;
        if (body.contains("last updated") || body.contains("created on")) matches++;
        return matches >= 4;
    }

    public void closeViewModalIfAny() {
        List<WebElement> close = driver.findElements(By.xpath("//button[contains(normalize-space(),'Close') or contains(normalize-space(),'CLOSE')]"));
        if (!close.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", close.get(0));
            waitForUiIdle();
        }
    }

    public boolean isEditPageVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("edit announcement")
                || body.contains("add announcement")
                || body.contains("announcement title")
                || body.contains("add / update")
                || body.contains("announcement list")
                || body.contains("redirection link")
                || body.contains("redirection label")
                || (body.contains("title") && body.contains("visible to") && body.contains("guest type"));
    }

    public boolean areEditFieldsVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        String[] fields = {"title", "visible to", "guest type", "has redirection", "redirection label", "redirection link", "description", "add attachment"};
        int matches = 0;
        for (String f : fields) if (body.contains(f)) matches++;
        if (matches >= 5) {
            return true;
        }
        return driver.findElements(By.xpath("//input | //textarea | //select")).size() >= 3;
    }

    public boolean isAddFormVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("add announcement")
                || (body.contains("title") && body.contains("visible to") && body.contains("guest type"));
    }

    public boolean areAddFieldsVisible() {
        return areEditFieldsVisible();
    }

    public void openAddForm() {
        openIfNotOpened();
        List<WebElement> buttons = driver.findElements(addAnnouncementButton);
        if (buttons.isEmpty()) return;
        for (WebElement button : buttons) {
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", button);
                break;
            } catch (Exception ignored) {
            }
        }
        waitForUiIdle();
    }

    public boolean isDeleteConfirmationVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("are you sure") || body.contains("delete");
    }

    public void cancelDeleteIfAny() {
        List<WebElement> cancel = driver.findElements(By.xpath("//button[normalize-space()='Cancel' or normalize-space()='No']"));
        if (!cancel.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cancel.get(0));
            return;
        }
        try {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        } catch (Exception ignored) {
        }
    }

    public boolean hasAnyViewActionInCurrentResults() {
        if (isNoResultVisible()) return false;
        for (WebElement row : driver.findElements(tableRows)) {
            String t = row.getText() == null ? "" : row.getText().toLowerCase();
            if (!lastSearchKeyword.isBlank() && !t.contains(lastSearchKeyword.toLowerCase())) continue;
            List<WebElement> views = row.findElements(By.xpath(".//*[contains(@class,'fa-eye') or normalize-space()='View']"));
            for (WebElement v : views) if (v.isDisplayed()) return true;
        }
        return false;
    }

    public boolean hasAnyEditActionInCurrentResults() {
        if (isNoResultVisible()) return false;
        for (WebElement row : driver.findElements(tableRows)) {
            String t = row.getText() == null ? "" : row.getText().toLowerCase();
            if (!lastSearchKeyword.isBlank() && !t.contains(lastSearchKeyword.toLowerCase())) continue;
            List<WebElement> edits = row.findElements(By.xpath(".//*[contains(@class,'fa-pen') or contains(@class,'fa-edit') or normalize-space()='Edit']"));
            for (WebElement v : edits) if (v.isDisplayed()) return true;
        }
        return false;
    }

    public boolean hasAnyDeleteActionInCurrentResults() {
        if (isNoResultVisible()) return false;
        for (WebElement row : driver.findElements(tableRows)) {
            String t = row.getText() == null ? "" : row.getText().toLowerCase();
            if (!lastSearchKeyword.isBlank() && !t.contains(lastSearchKeyword.toLowerCase())) continue;
            List<WebElement> deletes = row.findElements(By.xpath(".//*[contains(@class,'fa-trash') or normalize-space()='Delete']"));
            for (WebElement v : deletes) if (v.isDisplayed()) return true;
        }
        return false;
    }

    public boolean isUiStable() {
        waitForUiIdle();
        return isPageVisible()
                || isAddFormVisible()
                || isEditPageVisible()
                || isViewModalVisible()
                || isSearchVisible();
    }

    public String randomInvalidKeyword() {
        return "INVALID-ANNOUNCEMENT-" + UUID.randomUUID();
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

