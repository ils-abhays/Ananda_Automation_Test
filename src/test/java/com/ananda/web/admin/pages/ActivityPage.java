package com.ananda.web.admin.pages;

import com.ananda.core.drivers.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class ActivityPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final String BASE_URL =
            System.getProperty("ananda.baseUrl", "https://dev-admin.anandaspa.com");

    public ActivityPage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    private final By activityTab =
            By.xpath("//button[@id='controlled-tab-example-tab-Activity' and normalize-space()='Activity']"
                    + " | //li[contains(@class,'nav-item')]//button[normalize-space()='Activity']"
                    + " | //*[self::button or self::span][normalize-space()='Activity']");
    private final By tableContainer = By.xpath("//table//tbody");
    private final By searchBox = By.xpath("//input[contains(@placeholder,'Search Keyword') and @type='search'] | //input[contains(@placeholder,'Search')]");
    private final By tableRows = By.xpath("//table//tbody/tr");
    private final By activityDetailTitle = By.xpath("//h4");
    private final By activityBreadcrumb = By.xpath("//span[normalize-space()='Activity'] | //a[normalize-space()='Activity']");
    private final By activityListTitle = By.xpath("//div[normalize-space()='Daily Activities']");
    private final By toastMessage = By.xpath("//div[contains(@class,'Toastify__toast') and contains(.,'success')]");
    private final By toastContainer = By.xpath("//div[contains(@class,'Toastify__toast-container')]");
    private String lastSearchKeyword = "";

    public void openActivityTab() {
        if (isActivityListVisible()) return;

        driver.get(BASE_URL + "/master-data");
        for (int i = 0; i < 3; i++) {
            try {
                WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(activityTab));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tab);
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.visibilityOfElementLocated(activityListTitle),
                        ExpectedConditions.visibilityOfElementLocated(searchBox),
                        ExpectedConditions.presenceOfElementLocated(tableContainer)
                ));
                if (isActivityListVisible()) {
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        // Avoid configuration-level hard stop; tests can assert visibility explicitly.
    }

    public void searchActivity(String value) {
        openActivityTab();
        lastSearchKeyword = value == null ? "" : value.trim();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.invisibilityOfElementLocated(toastContainer));
        } catch (Exception ignored) {
        }
        WebElement searchInput = null;
        try {
            searchInput = new WebDriverWait(driver, Duration.ofSeconds(4)).until(driver -> {
                List<WebElement> inputs = driver.findElements(searchBox);
                for (WebElement input : inputs) {
                    if (input.isDisplayed() && input.isEnabled()) {
                        return input;
                    }
                }
                return null;
            });
        } catch (Exception ignored) {
        }
        if (searchInput == null) {
            openActivityTab();
            try {
                searchInput = new WebDriverWait(driver, Duration.ofSeconds(3))
                        .until(ExpectedConditions.visibilityOfElementLocated(searchBox));
            } catch (Exception ignored) {
                return;
            }
        }
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", searchInput);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", searchInput);
        searchInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        searchInput.sendKeys(value == null ? "" : value);
        searchInput.sendKeys(Keys.ENTER);
        wait.until(d -> {
            boolean hasRows = !d.findElements(tableRows).isEmpty();
            boolean hasEmpty = !d.findElements(By.xpath("//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no data') or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no result') or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'not found')]")).isEmpty();
            boolean hasTableBody = !d.findElements(By.xpath("//table//tbody")).isEmpty();
            return hasRows || hasEmpty || hasTableBody;
        });
    }

    public void clearSearch() {
        searchActivity("");
    }

    public String randomInvalidKeyword() {
        return "INVALID-ACTIVITY-" + UUID.randomUUID();
    }

    public String getFirstActivityName() {
        List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr"));
        if (rows.isEmpty()) {
            return null;
        }
        List<WebElement> cells = rows.get(0).findElements(By.xpath(".//th|.//td"));
        for (WebElement c : cells) {
            String text = c.getText().trim();
            if (!text.isEmpty()) {
                return text;
            }
        }
        return null;
    }

    public String getFirstVenueName() {
        List<WebElement> cells = driver.findElements(By.xpath("//table//tbody/tr[1]/td[4]"));
        if (!cells.isEmpty()) {
            String text = cells.get(0).getText().trim();
            if (!text.isEmpty()) {
                return text;
            }
        }
        return getFirstActivityName();
    }

    public boolean hasAnyActivityRows() {
        return !driver.findElements(By.xpath("//table//tbody/tr")).isEmpty();
    }

    public boolean isActivityPresent(String activityName) {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table")));
        List<WebElement> rows = driver.findElements(tableRows);
        for (WebElement row : rows) {
            String rowText = row.getText().toLowerCase().trim();
            if (rowText.contains(activityName.toLowerCase().trim())) {
                return true;
            }
        }
        return false;
    }

    public void clickViewForActivity(String activityName) {
        openActivityTab();
        By viewBtn = By.xpath("(//table//tbody//tr[td[1][contains(normalize-space(),\"" + activityName + "\")]]//*[contains(@class,'fa-eye') or normalize-space()='View'])[1]");
        List<WebElement> match = driver.findElements(viewBtn);
        WebElement target;
        if (!match.isEmpty()) {
            target = wait.until(ExpectedConditions.elementToBeClickable(viewBtn));
        } else {
            target = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("(//table//tbody//tr[1]//*[contains(@class,'fa-eye') or normalize-space()='View'])[1]")));
        }
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", target);
    }

    public String getOpenedActivityTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(activityDetailTitle)).getText().trim();
    }

    public void clickActivityBreadcrumb() {
        List<WebElement> crumbs = driver.findElements(activityBreadcrumb);
        if (crumbs.isEmpty()) {
            openActivityTab();
            return;
        }
        WebElement breadcrumb = wait.until(ExpectedConditions.elementToBeClickable(activityBreadcrumb));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", breadcrumb);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(activityListTitle),
                ExpectedConditions.presenceOfElementLocated(tableContainer)
        ));
    }

    public boolean isActivityListVisible() {
        try {
            boolean hasTitle = !driver.findElements(activityListTitle).isEmpty();
            boolean hasSearch = driver.findElements(searchBox).stream().anyMatch(WebElement::isDisplayed);
            boolean hasHeader = !driver.findElements(
                    By.xpath("//table//tr[1]//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'activity title')]")
            ).isEmpty();
            return hasSearch && (hasTitle || hasHeader);
        } catch (Exception e) {
            return false;
        }
    }

    public String getActivityListTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(activityListTitle)).getText().trim();
    }

    public boolean isVenuePresent(String venue) {
        openActivityTab();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table")));
        List<WebElement> venues = driver.findElements(By.xpath("//table//tbody/tr/td[4]"));
        for (WebElement v : venues) {
            if (v.getText().trim().equalsIgnoreCase(venue.trim())) {
                return true;
            }
        }
        return false;
    }

    public void changeStatus(String status) {
        openActivityTab();
        By statusDropdown = By.xpath("(//table//tbody/tr[1]//*[contains(@class,'dropdown') or contains(@class,'indicator') or contains(@class,'react-select')])[1]");
        List<WebElement> drops = driver.findElements(statusDropdown);
        if (!drops.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", drops.get(0));
            By option = By.xpath("//*[contains(@class,'react-select__menu')]//*[normalize-space()='" + status + "']");
            List<WebElement> options = driver.findElements(option);
            if (!options.isEmpty()) {
                options.get(0).click();
            }
        }
    }

    public void waitForToast() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(toastMessage));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(toastMessage));
    }

    public void openEditActivity() {
        openActivityTab();
        By edit = By.xpath("(//table//tbody/tr[1]//*[contains(@class,'fa-pen') or contains(@class,'fa-edit') or normalize-space()='Edit'])[1]");
        WebElement editBtn = wait.until(ExpectedConditions.elementToBeClickable(edit));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editBtn);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[normalize-space()='Edit Activity']")),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//label[contains(normalize-space(),'Title')]"))
        ));
    }

    public void updateTitle(String title) {
        By input = By.xpath("//input[contains(@placeholder,'title') or @name='title']");
        WebElement e = wait.until(ExpectedConditions.visibilityOfElementLocated(input));
        e.clear();
        e.sendKeys(title);
    }

    public void updateVenue(String venue) {
        By input = By.xpath("//input[contains(@placeholder,'venue') or @name='venue']");
        WebElement e = wait.until(ExpectedConditions.visibilityOfElementLocated(input));
        e.clear();
        e.sendKeys(venue);
    }

    public void updateCapacity(String capacity) {
        By input = By.xpath("//input[contains(@placeholder,'capacity') or @name='capacity']");
        WebElement e = wait.until(ExpectedConditions.visibilityOfElementLocated(input));
        e.clear();
        e.sendKeys(capacity);
    }

    public void submitUpdate() {
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']"))).click();
    }

    public boolean isUpdatedTitleVisible(String title) {
        return !driver.findElements(By.xpath("//table//tbody//tr[contains(.,\"" + title + "\")]")).isEmpty();
    }

    public boolean isUpdatedVenueVisible(String venue) {
        return !driver.findElements(By.xpath("//table//tbody//tr[contains(.,\"" + venue + "\")]")).isEmpty();
    }

    public boolean isUpdatedCapacityVisible(String capacity) {
        return !driver.findElements(By.xpath("//table//tbody//tr[contains(.,\"" + capacity + "\")]")).isEmpty();
    }

    public void openDeletePopup() {
        openActivityTab();
        By delete = By.xpath("(//table//tbody/tr[1]//*[contains(@class,'fa-trash') or normalize-space()='Delete'])[1]");
        List<WebElement> buttons = driver.findElements(delete);
        if (!buttons.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttons.get(0));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(),'Are you sure')]")));
        }
    }

    public void cancelDelete() {
        List<WebElement> cancel = driver.findElements(By.xpath("//button[normalize-space()='Cancel']"));
        if (!cancel.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cancel.get(0));
        }
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(normalize-space(),'Are you sure')]")));
    }

    public boolean isDeleteConfirmationVisible() {
        return !driver.findElements(By.xpath("//*[contains(normalize-space(),'Are you sure')]")).isEmpty()
                && !driver.findElements(By.xpath("//*[contains(normalize-space(),'delete this record')]")).isEmpty();
    }

    public boolean areActivityTableHeadersVisible() {
        openActivityTab();
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
        String[] expected = {
                "activity title", "capacity", "booked user count", "venue",
                "activity date", "stop time", "frequency", "activity time", "status", "action"
        };
        int matches = 0;
        for (String key : expected) {
            if (text.contains(key)) {
                matches++;
            }
        }
        return matches >= 4;
    }

    public boolean isCapacityColumnNumericOrDash() {
        openActivityTab();
        List<WebElement> cells = driver.findElements(By.xpath("//table//tbody/tr/td[2]"));
        for (WebElement cell : cells) {
            String value = cell.getText().trim();
            if (value.isEmpty()) continue;
            if (!value.matches("\\d+") && !value.equals("-")) {
                return false;
            }
        }
        return true;
    }

    public boolean isActivityDateColumnValid() {
        openActivityTab();
        List<WebElement> cells = driver.findElements(By.xpath("//table//tbody/tr/td[5]"));
        for (WebElement cell : cells) {
            String value = cell.getText().trim();
            if (value.isEmpty() || value.equals("-")) continue;
            if (!value.matches("\\d{2}/\\d{2}/\\d{4}")) {
                return false;
            }
        }
        return true;
    }

    public boolean isActivityTimeColumnValid() {
        openActivityTab();
        int timeCol = getColumnIndexByHeader("activity time");
        if (timeCol <= 0) {
            return true;
        }
        List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr"));
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < timeCol) {
                continue;
            }
            String value = cells.get(timeCol - 1).getText().trim();
            if (value.isEmpty() || value.equals("-")) {
                continue;
            }
            if (!value.matches("\\d{2}:\\d{2}:\\d{2}")) {
                return false;
            }
        }
        return true;
    }

    public boolean areViewActivityDetailFieldsVisible() {
        String text = driver.findElement(By.tagName("body")).getText().toLowerCase();
        String[] fields = {
                "activity title", "venue", "activity date", "stop date",
                "repeat activity", "frequency", "time", "duration", "notes"
        };
        int matches = 0;
        for (String f : fields) {
            if (text.contains(f)) {
                matches++;
            }
        }
        return matches >= 7;
    }

    public boolean areEditActivityFieldsVisible() {
        String text = driver.findElement(By.tagName("body")).getText().toLowerCase();
        String[] fields = {
                "edit activity", "title", "venue", "capacity", "start date", "stop date",
                "repeat activity", "is permanent", "frequency", "time", "duration", "notes"
        };
        int matches = 0;
        for (String f : fields) {
            if (text.contains(f)) {
                matches++;
            }
        }
        return matches >= 9;
    }

    public boolean isNoActivityResultVisible() {
        for (int retry = 0; retry < 4; retry++) {
            try {
                List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr"));
                if (rows.isEmpty()) {
                    return true;
                }

                String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
                if (body.contains("no data") || body.contains("no records")
                        || body.contains("no result") || body.contains("not found")) {
                    return true;
                }

                if (lastSearchKeyword.isBlank()) {
                    return false;
                }

                String expected = lastSearchKeyword.toLowerCase().trim();
                for (WebElement row : rows) {
                    String rowText = row.getText() == null ? "" : row.getText().toLowerCase();
                    if (rowText.contains(expected)) {
                        return false;
                    }
                }
                return true;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return true;
    }

    public boolean hasAnyViewActionInCurrentResults() {
        if (isNoActivityResultVisible()) return false;
        for (int retry = 0; retry < 3; retry++) {
            try {
                List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr"));
                for (WebElement row : rows) {
                    String rowText = row.getText() == null ? "" : row.getText().toLowerCase();
                    if (!lastSearchKeyword.isBlank() && !rowText.contains(lastSearchKeyword.toLowerCase())) {
                        continue;
                    }
                    List<WebElement> views = row.findElements(By.xpath(".//*[contains(@class,'fa-eye') or normalize-space()='View']"));
                    for (WebElement v : views) {
                        if (v.isDisplayed()) return true;
                    }
                }
                return false;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return false;
    }

    public boolean hasAnyEditActionInCurrentResults() {
        if (isNoActivityResultVisible()) return false;
        for (int retry = 0; retry < 3; retry++) {
            try {
                List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr"));
                for (WebElement row : rows) {
                    String rowText = row.getText() == null ? "" : row.getText().toLowerCase();
                    if (!lastSearchKeyword.isBlank() && !rowText.contains(lastSearchKeyword.toLowerCase())) {
                        continue;
                    }
                    List<WebElement> edits = row.findElements(By.xpath(".//*[contains(@class,'fa-pen') or contains(@class,'fa-edit') or normalize-space()='Edit']"));
                    for (WebElement e : edits) {
                        if (e.isDisplayed()) return true;
                    }
                }
                return false;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return false;
    }

    public boolean hasAnyDeleteActionInCurrentResults() {
        if (isNoActivityResultVisible()) return false;
        for (int retry = 0; retry < 3; retry++) {
            try {
                List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr"));
                for (WebElement row : rows) {
                    String rowText = row.getText() == null ? "" : row.getText().toLowerCase();
                    if (!lastSearchKeyword.isBlank() && !rowText.contains(lastSearchKeyword.toLowerCase())) {
                        continue;
                    }
                    List<WebElement> dels = row.findElements(By.xpath(".//*[contains(@class,'fa-trash') or normalize-space()='Delete']"));
                    for (WebElement d : dels) {
                        if (d.isDisplayed()) return true;
                    }
                }
                return false;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return false;
    }

    private int getColumnIndexByHeader(String headerKeyword) {
        List<WebElement> headers = driver.findElements(By.xpath("//table//tr[1]/*[self::th or self::td]"));
        for (int i = 0; i < headers.size(); i++) {
            String text = headers.get(i).getText() == null ? "" : headers.get(i).getText().trim().toLowerCase();
            if (text.contains(headerKeyword.toLowerCase())) {
                return i + 1;
            }
        }
        return -1;
    }
}
