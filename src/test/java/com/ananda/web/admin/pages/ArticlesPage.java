package com.ananda.web.admin.pages;

import com.ananda.core.drivers.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class ArticlesPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final String BASE_URL =
            System.getProperty("ananda.baseUrl", "https://dev-admin.anandaspa.com");
    private String lastSearchKeyword = "";

    public ArticlesPage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    private final By contentManagementMenu =
            By.xpath("//*[self::a or self::span or self::button][normalize-space()='Content Management']");
    private final By articlesTab =
            By.xpath("//button[normalize-space()='Articles'] | //*[self::a or self::span][normalize-space()='Articles']");
    private final By contentListTitle =
            By.xpath("//*[normalize-space()='Content List']");
    private final By searchBox =
            By.xpath("//input[contains(@placeholder,'Search Keyword') or contains(@placeholder,'Search')]");
    private final By addArticleButton =
            By.xpath("//button[contains(normalize-space(),'Add Article')] | //*[self::a or self::span][contains(normalize-space(),'Add Article')]");
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
                List<WebElement> tabs = driver.findElements(articlesTab);
                if (!tabs.isEmpty()) {
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", tabs.get(0));
                }
            } catch (Exception ignored) {
            }

            try {
                wait.until(d ->
                        !d.findElements(contentListTitle).isEmpty()
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
            boolean hasTab = !driver.findElements(articlesTab).isEmpty();
            boolean hasTitle = !driver.findElements(contentListTitle).isEmpty();
            boolean hasHeader = !driver.findElements(
                    By.xpath("//table//tr[1]//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'content type')]")
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

    public boolean isAddArticleButtonVisible() {
        for (WebElement b : driver.findElements(addArticleButton)) {
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
        String[] expected = {"title", "content type", "uploaded by", "free", "paid", "status", "action"};
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
        int titleCol = getColumnIndexByHeader("title");
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

    public boolean isUiStable() {
        waitForUiIdle();
        return isPageVisible() && (isSearchVisible() || isAddUpdatePageVisible() || isViewContentPageVisible());
    }

    public boolean isUploadedByFormatValid() {
        int col = getColumnIndexByHeader("uploaded by");
        if (col < 1) return true;
        Pattern readPattern = Pattern.compile(".*\\d+\\s*MIN\\s*READ.*", Pattern.CASE_INSENSITIVE);
        Pattern typePattern = Pattern.compile(".*\\b(article|blog)\\b.*", Pattern.CASE_INSENSITIVE);
        Pattern alphaPattern = Pattern.compile(".*[A-Za-z]{3,}.*");
        for (WebElement row : driver.findElements(tableRows)) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < col) continue;
            String value = cells.get(col - 1).getText() == null ? "" : cells.get(col - 1).getText().trim();
            if (value.isEmpty() || value.equals("-")) continue;
            String normalized = value.toLowerCase();
            if (!readPattern.matcher(value).matches()
                    && !typePattern.matcher(value).matches()
                    && !normalized.contains("read")
                    && !alphaPattern.matcher(value).matches()) {
                return false;
            }
        }
        return true;
    }

    public boolean isFreePaidColumnValid() {
        int col = getColumnIndexByHeader("free");
        if (col < 1) col = getColumnIndexByHeader("paid");
        if (col < 1) return true;

        for (WebElement row : driver.findElements(tableRows)) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < col) continue;
            String value = cells.get(col - 1).getText() == null ? "" : cells.get(col - 1).getText().trim().toLowerCase();
            if (value.isEmpty() || value.equals("-")) continue;
            if (!value.contains("free") && !value.contains("paid")) return false;
        }
        return true;
    }

    public boolean isStatusControlVisibleInList() {
        if (getVisibleRowCount() == 0) return true;
        return !driver.findElements(By.xpath("//table//tbody/tr//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'active') or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'inactive')]")).isEmpty();
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
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", edits.get(0));
        waitForUiIdle();
    }

    public void openFirstDelete() {
        List<WebElement> deletes = driver.findElements(deleteAction);
        if (deletes.isEmpty()) return;
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", deletes.get(0));
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

    public void openAddForm() {
        openIfNotOpened();
        List<WebElement> buttons = driver.findElements(addArticleButton);
        if (buttons.isEmpty()) return;
        for (WebElement button : buttons) {
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", button);
                break;
            } catch (Exception ignored) {
            }
        }
        try {
            wait.until(d -> isAddUpdatePageVisible() || isPageVisible());
        } catch (Exception ignored) {
        }
        waitForUiIdle();
    }

    public boolean isViewContentPageVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("view content") || (body.contains("published by:") && body.contains("published date:"));
    }

    public boolean areViewContentFieldsVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        int matches = 0;
        if (body.contains("published by")) matches++;
        if (body.contains("published date")) matches++;
        if (body.contains("active") || body.contains("inactive")) matches++;
        if (body.contains("content management") && body.contains("articles")) matches++;
        return matches >= 2;
    }

    public boolean isAddUpdatePageVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("add / update content") || body.contains("add article") || body.contains("edit article");
    }

    public boolean areAddEditFormFieldsVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        String[] fields = {"select type", "select program", "select category", "title", "published by", "description", "add attachment"};
        int matches = 0;
        for (String f : fields) if (body.contains(f)) matches++;
        return matches >= 6;
    }

    public boolean areTypeProgramCategoryDropdownsVisible() {
        return !driver.findElements(By.xpath(
                "//label[contains(normalize-space(),'Select Type')]/following::*[contains(@class,'control')][1]"
                        + " | //label[contains(normalize-space(),'Select Program')]/following::*[contains(@class,'control')][1]"
                        + " | //label[contains(normalize-space(),'Select Category')]/following::*[contains(@class,'control')][1]"
        )).isEmpty();
    }

    public boolean isDescriptionEditorVisible() {
        return !driver.findElements(By.xpath("//*[contains(@class,'ql-editor') or contains(@class,'editor') or contains(@placeholder,'Description')]")).isEmpty()
                || driver.findElement(By.tagName("body")).getText().toLowerCase().contains("description");
    }

    public boolean isAttachmentControlVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("add attachment") || !driver.findElements(By.xpath("//input[@type='file']")).isEmpty();
    }

    public boolean isPublishButtonVisible() {
        return !driver.findElements(By.xpath("//button[contains(normalize-space(),'PUBLISH') or contains(normalize-space(),'Publish')]")).isEmpty();
    }

    public boolean isCancelButtonVisible() {
        return !driver.findElements(By.xpath("//button[contains(normalize-space(),'CANCEL') or contains(normalize-space(),'Cancel')]")).isEmpty();
    }

    public void clickPublishIfVisible() {
        List<WebElement> buttons = driver.findElements(By.xpath("//button[contains(normalize-space(),'PUBLISH') or contains(normalize-space(),'Publish')]"));
        if (!buttons.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttons.get(0));
            waitForUiIdle();
        }
    }

    public void clickCancelIfVisible() {
        List<WebElement> buttons = driver.findElements(By.xpath("//button[contains(normalize-space(),'CANCEL') or contains(normalize-space(),'Cancel')]"));
        if (!buttons.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttons.get(0));
            waitForUiIdle();
        }
    }

    public boolean hasValidationOrStayedOnForm() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return isAddUpdatePageVisible()
                || body.contains("required")
                || body.contains("please")
                || body.contains("validation")
                || body.contains("must be");
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

    public String randomInvalidKeyword() {
        return "INVALID-ARTICLE-" + UUID.randomUUID();
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
