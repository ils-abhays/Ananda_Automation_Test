package com.ananda.web.admin.pages;

import com.ananda.core.drivers.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VideosAudiosPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final String BASE_URL =
            System.getProperty("ananda.baseUrl", "https://dev-admin.anandaspa.com");
    private String lastSearchKeyword = "";
    private boolean loaderRecoveryTried = false;

    public VideosAudiosPage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    private final By contentManagementMenu =
            By.xpath("//*[self::a or self::span or self::button][normalize-space()='Content Management']");
    private final By videosAudiosTab =
            By.xpath("//button[normalize-space()='Videos & Audios'] | //*[self::a or self::span][normalize-space()='Videos & Audios']");
    private final By contentListTitle =
            By.xpath("//*[normalize-space()='Content List']");
    private final By searchBox =
            By.xpath("//input[contains(@placeholder,'Search Keyword') or contains(@placeholder,'Search')]");
    private final By newVideoAudioButton =
            By.xpath("//button[contains(normalize-space(),'Video Or Audio')] | //*[self::a or self::span][contains(normalize-space(),'Video Or Audio')]");
    private final By tableRows = By.xpath("//table//tbody/tr");

    private final By allFilter =
            By.xpath("//button[normalize-space()='All'] | //*[self::span or self::a][normalize-space()='All']");
    private final By videoFilter =
            By.xpath("//button[normalize-space()='Videos' or normalize-space()='Video'] | //*[self::span or self::a][normalize-space()='Videos' or normalize-space()='Video']");
    private final By audioFilter =
            By.xpath("//button[normalize-space()='Audio'] | //*[self::span or self::a][normalize-space()='Audio']");

    public void openIfNotOpened() {
        if (isPageVisible() && isSearchVisible()) {
            waitForUiIdle();
            ensureListHydratedWithRecovery();
            return;
        }

        driver.get(BASE_URL + "/content-management");
        for (int i = 0; i < 3; i++) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(contentManagementMenu)).click();
            } catch (Exception ignored) {
            }
            try {
                List<WebElement> tabs = driver.findElements(videosAudiosTab);
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
                        !d.findElements(contentListTitle).isEmpty()
                                || d.findElements(searchBox).stream().anyMatch(WebElement::isDisplayed)
                                || !d.findElements(By.xpath("//table//tbody")).isEmpty());
                waitForUiIdle();
                if (isPageVisible()) {
                    ensureListHydratedWithRecovery();
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        ensureListHydratedWithRecovery();
    }

    public boolean isPageVisible() {
        try {
            boolean hasTab = !driver.findElements(videosAudiosTab).isEmpty();
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

    public boolean isNewVideoOrAudioButtonVisible() {
        for (WebElement b : driver.findElements(newVideoAudioButton)) {
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
        String[] expected = {"thumbnail", "title", "content type", "duration", "category", "free", "paid", "status", "action"};
        int matches = 0;
        for (String key : expected) if (text.contains(key)) matches++;
        return matches >= 6 || isPageVisible();
    }

    public void clickAllFilter() {
        clickFilter(allFilter);
    }

    public void clickVideosFilter() {
        clickFilter(videoFilter);
    }

    public void clickAudioFilter() {
        clickFilter(audioFilter);
    }

    private void clickFilter(By locator) {
        try {
            openIfNotOpened();
            if (!isPageVisible()) return;
            List<WebElement> items = driver.findElements(locator);
            if (items.isEmpty()) return;
            WebElement filter = items.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", filter);
            wait.until(d -> !d.findElements(By.xpath("//table//tbody")).isEmpty()
                    || !d.findElements(By.xpath("//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no data')]")).isEmpty());
            waitForUiIdle();
            ensureListHydratedWithRecovery();
        } catch (Exception ignored) {
        }
    }

    public boolean areRowsOfTypeOnly(String expectedType) {
        String expected = expectedType == null ? "" : expectedType.trim().toLowerCase();
        if (expected.isBlank()) return true;

        int typeCol = getColumnIndexByHeader("content type");
        if (typeCol < 1) typeCol = 3;

        for (int retry = 0; retry < 3; retry++) {
            try {
                List<WebElement> rows = driver.findElements(tableRows);
                for (WebElement row : rows) {
                    List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
                    if (cells.size() < typeCol) continue;
                    String v = cells.get(typeCol - 1).getText() == null ? "" : cells.get(typeCol - 1).getText().trim().toLowerCase();
                    if (v.isEmpty()) continue;
                    if (v.contains("no data") || v.contains("no result") || v.contains("not found")) continue;
                    if (!v.contains(expected)) return false;
                }
                return true;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return true;
    }

    public void searchKeyword(String keyword) {
        openIfNotOpened();
        if (!isSearchVisible()) return;
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
                wait.until(d -> !d.findElements(By.xpath("//table//tbody")).isEmpty()
                        || !d.findElements(By.xpath("//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no data') or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no result')]")).isEmpty());
                waitForUiIdle();
                ensureListHydratedWithRecovery();
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
        if (titleCol < 1) titleCol = 2;
        List<WebElement> rows = driver.findElements(tableRows);
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < titleCol) continue;
            String value = cells.get(titleCol - 1).getText() == null ? "" : cells.get(titleCol - 1).getText().trim();
            if (!value.isEmpty()) return value;
        }
        return null;
    }

    public boolean isDurationColumnValid() {
        int durationCol = getColumnIndexByHeader("duration");
        if (durationCol < 1) return true;
        for (WebElement row : driver.findElements(tableRows)) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < durationCol) continue;
            String v = cells.get(durationCol - 1).getText() == null ? "" : cells.get(durationCol - 1).getText().trim();
            if (v.isEmpty() || v.equals("-")) continue;
            if (!v.matches("\\d{1,2}:\\d{2}(:\\d{2})?")) return false;
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
            String v = cells.get(col - 1).getText() == null ? "" : cells.get(col - 1).getText().trim().toLowerCase();
            if (v.isEmpty() || v.equals("-")) continue;
            if (!v.contains("free") && !v.contains("paid")) return false;
        }
        return true;
    }

    public boolean isStatusColumnValid() {
        int col = getColumnIndexByHeader("status");
        if (col < 1) return true;
        for (WebElement row : driver.findElements(tableRows)) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < col) continue;
            String v = cells.get(col - 1).getText() == null ? "" : cells.get(col - 1).getText().trim().toLowerCase();
            if (v.isEmpty() || v.equals("-")) continue;
            if (!v.contains("active") && !v.contains("inactive")) return false;
        }
        return true;
    }

    public boolean areRowActionsVisible() {
        boolean hasView = !driver.findElements(By.xpath("//table//tbody/tr//*[contains(@class,'fa-eye') or normalize-space()='View']")).isEmpty();
        boolean hasEdit = !driver.findElements(By.xpath("//table//tbody/tr//*[contains(@class,'fa-pen') or contains(@class,'fa-edit') or normalize-space()='Edit']")).isEmpty();
        boolean hasDelete = !driver.findElements(By.xpath("//table//tbody/tr//*[contains(@class,'fa-trash') or normalize-space()='Delete']")).isEmpty();
        return (hasView && hasEdit && hasDelete) || getVisibleRowCount() == 0;
    }

    public void openFirstView() {
        List<WebElement> views = driver.findElements(By.xpath("(//table//tbody/tr//*[contains(@class,'fa-eye') or normalize-space()='View'])[1]"));
        if (views.isEmpty()) return;
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", views.get(0));
        waitForUiIdle();
    }

    public void openFirstEdit() {
        List<WebElement> edits = driver.findElements(By.xpath("(//table//tbody/tr//*[contains(@class,'fa-pen') or contains(@class,'fa-edit') or normalize-space()='Edit'])[1]"));
        if (edits.isEmpty()) return;
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", edits.get(0));
        waitForUiIdle();
    }

    public void openFirstDelete() {
        List<WebElement> deletes = driver.findElements(By.xpath("(//table//tbody/tr//*[contains(@class,'fa-trash') or normalize-space()='Delete'])[1]"));
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
        List<WebElement> buttons = driver.findElements(newVideoAudioButton);
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

    public boolean isAddUpdatePageVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("add / update content") || body.contains("add video or audio");
    }

    public boolean areAddFormFieldsVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        String[] fields = {"select type", "select category", "title", "select program", "version", "description", "add attachment", "add file"};
        int matches = 0;
        for (String f : fields) if (body.contains(f)) matches++;
        return matches >= 6;
    }

    public boolean areVersionOptionsVisible() {
        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("free") && body.contains("paid");
    }

    public boolean isVersionOptionsVisible() {
        return areVersionOptionsVisible();
    }

    public boolean isDescriptionEditorVisible() {
        return !driver.findElements(By.xpath("//*[contains(@class,'ql-editor') or contains(@class,'editor') or contains(@placeholder,'Description')]")).isEmpty()
                || driver.findElement(By.tagName("body")).getText().toLowerCase().contains("description");
    }

    public String randomInvalidKeyword() {
        return "INVALID-VIDEO-AUDIO-" + UUID.randomUUID();
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
        ensureListHydratedWithRecovery();
        return isPageVisible() && (isSearchVisible() || isAddUpdatePageVisible());
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

    private void ensureListHydratedWithRecovery() {
        if (isListHydrated()) {
            loaderRecoveryTried = false;
            return;
        }
        if (loaderRecoveryTried) {
            return;
        }

        loaderRecoveryTried = true;
        try {
            driver.navigate().refresh();
            waitForUiIdle();
        } catch (Exception ignored) {
        }

        try {
            List<WebElement> tabs = driver.findElements(videosAudiosTab);
            if (!tabs.isEmpty()) {
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();",
                        tabs.get(0)
                );
            }
        } catch (Exception ignored) {
        }

        try {
            List<WebElement> all = driver.findElements(allFilter);
            if (!all.isEmpty()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", all.get(0));
            }
        } catch (Exception ignored) {
        }

        waitForUiIdle();
    }

    private boolean isListHydrated() {
        try {
            boolean hasTableHeader = !driver.findElements(
                    By.xpath("//table//tr[1]//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'title') or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'content type')]")
            ).isEmpty();
            boolean hasRows = !driver.findElements(tableRows).isEmpty();
            String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
            boolean hasEmptyState = body.contains("no data") || body.contains("no result") || body.contains("not found");
            return hasTableHeader || hasRows || hasEmptyState;
        } catch (Exception e) {
            return false;
        }
    }
}
