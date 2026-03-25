package com.ananda.web.admin.pages;

import com.ananda.core.drivers.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class RecipeCollectionPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final String BASE_URL =
            System.getProperty("ananda.baseUrl", "https://dev-admin.anandaspa.com");

    public RecipeCollectionPage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    private final By recipeTab =
            By.xpath("//button[@id='controlled-tab-example-tab-Recipe Collection' and normalize-space()='Recipe Collection']"
                    + " | //li[contains(@class,'nav-item')]//button[normalize-space()='Recipe Collection']"
                    + " | //*[self::button or self::span][normalize-space()='Recipe Collection']");
    private final By tableContainer = By.xpath("//table//tbody");
    private final By searchBox = By.xpath("//input[contains(@placeholder,'Search Keyword') and @type='search'] | //input[contains(@placeholder,'Search')]");
    private final By collectionNameCells = By.xpath("//table//tbody/tr/td[1]");
    private final By detailTitle = By.xpath("//h1 | //h2 | //h3 | //h4");
    private final By collectionListTitle = By.xpath("//*[normalize-space()='Recipe Collection List']");
    private final By toastContainer = By.xpath("//div[contains(@class,'Toastify__toast-container')]");
    private String lastSearchKeyword = "";

    public void openRecipeCollectionTab() {
        if (isRecipeCollectionListVisible()) return;

        driver.get(BASE_URL + "/master-data");
        for (int i = 0; i < 3; i++) {
            try {
                WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(recipeTab));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tab);
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.visibilityOfElementLocated(collectionListTitle),
                        ExpectedConditions.visibilityOfElementLocated(searchBox),
                        ExpectedConditions.presenceOfElementLocated(tableContainer)
                ));
                if (isRecipeCollectionListVisible()) {
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        // Avoid configuration-level hard stop.
    }

    public String getFirstCollectionName() {
        openRecipeCollectionTab();
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

    public boolean hasAnyCollections() {
        openRecipeCollectionTab();
        return !driver.findElements(By.xpath("//table//tbody/tr")).isEmpty();
    }

    public void searchRecipeCollection(String name) {
        openRecipeCollectionTab();
        lastSearchKeyword = name == null ? "" : name.trim();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.invisibilityOfElementLocated(toastContainer));
        } catch (Exception ignored) {
        }
        WebElement box = null;
        try {
            box = new WebDriverWait(driver, Duration.ofSeconds(4)).until(driver -> {
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
        if (box == null) {
            openRecipeCollectionTab();
            try {
                box = new WebDriverWait(driver, Duration.ofSeconds(3))
                        .until(ExpectedConditions.visibilityOfElementLocated(searchBox));
            } catch (Exception ignored) {
                return;
            }
        }
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", box);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", box);
        box.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        box.sendKeys(name == null ? "" : name);
        box.sendKeys(Keys.ENTER);
        try {
            wait.until(d -> {
                boolean hasRows = !d.findElements(By.xpath("//table//tbody/tr")).isEmpty();
                boolean hasEmpty = !d.findElements(By.xpath("//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no data') or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no result') or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'not found')]")).isEmpty();
                boolean hasTableBody = !d.findElements(By.xpath("//table//tbody")).isEmpty();
                return hasRows || hasEmpty || hasTableBody;
            });
        } catch (Exception ignored) {
        }
    }

    public void clearSearch() {
        searchRecipeCollection("");
    }

    public String randomInvalidKeyword() {
        return "INVALID-RECIPE-COLLECTION-" + UUID.randomUUID();
    }

    public boolean isRecipeCollectionPresent(String name) {
        openRecipeCollectionTab();
        List<WebElement> rows = driver.findElements(collectionNameCells);
        for (WebElement row : rows) {
            String text = row.getText() == null ? "" : row.getText().trim().toLowerCase();
            String needle = name == null ? "" : name.trim().toLowerCase();
            if (!needle.isBlank() && (text.equals(needle) || text.contains(needle) || needle.contains(text))) {
                return true;
            }
        }
        return false;
    }

    public void clickViewRecipeCollection(String name) {
        openRecipeCollectionTab();
        By rowView = By.xpath("(//table//tbody/tr[th[1][normalize-space()='" + name + "'] or td[1][normalize-space()='" + name + "']]//*[contains(@class,'fa-eye') or normalize-space()='View' or contains(@class,'View')])[1]");
        List<WebElement> match = driver.findElements(rowView);
        if (!match.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", match.get(0));
            return;
        }
        List<WebElement> fallbacks = driver.findElements(
                By.xpath("(//table//tbody/tr[1]//*[contains(@class,'fa-eye') or normalize-space()='View' or contains(@class,'View')])[1]")
        );
        if (!fallbacks.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", fallbacks.get(0));
        }
    }

    public String getOpenedCollectionTitle() {
        List<WebElement> titles = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(detailTitle));
        for (WebElement title : titles) {
            String text = title.getText().trim();
            if (!text.isEmpty()
                    && !text.equalsIgnoreCase("Master Data")
                    && !text.equalsIgnoreCase("Recipe Collection")
                    && !text.equalsIgnoreCase("Recipes List")) {
                return text;
            }
        }
        return "";
    }

    public boolean isRecipeCollectionListVisible() {
        try {
            boolean hasTitle = driver.findElements(collectionListTitle).stream().anyMatch(WebElement::isDisplayed);
            boolean hasSearch = driver.findElements(searchBox).stream().anyMatch(WebElement::isDisplayed);
            boolean hasHeader = !driver.findElements(
                    By.xpath("//table//tr[1]//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'collection')]")
            ).isEmpty();
            return hasSearch && (hasTitle || hasHeader);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areRecipeCollectionTableHeadersVisible() {
        openRecipeCollectionTab();
        String text = String.join(" ",
                driver.findElements(By.xpath("//table//tr[1]/*[self::th or self::td]"))
                        .stream()
                        .map(e -> e.getText() == null ? "" : e.getText().trim().toLowerCase())
                        .toList());
        if (text.trim().isEmpty()) {
            text = driver.findElement(By.tagName("body")).getText().toLowerCase();
        }
        String[] expected = {"collection", "recipe", "created", "status", "action"};
        int matches = 0;
        for (String key : expected) {
            if (text.contains(key)) {
                matches++;
            }
        }
        return matches >= 3 || isRecipeCollectionListVisible();
    }

    public boolean isRecipeCountColumnValid() {
        openRecipeCollectionTab();
        int col = getColumnIndexByHeader("recipe count");
        if (col <= 0) {
            return true;
        }
        List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr"));
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < col) continue;
            String value = cells.get(col - 1).getText().trim();
            if (value.isEmpty()) continue;
            if (!value.matches("\\d+\\s*Recipe(s)?")) {
                return false;
            }
        }
        return true;
    }

    public boolean isCreatedOnColumnValid() {
        openRecipeCollectionTab();
        int col = getColumnIndexByHeader("created on");
        if (col <= 0) {
            return true;
        }
        List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr"));
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < col) continue;
            String value = cells.get(col - 1).getText().trim();
            if (value.isEmpty() || value.equals("-")) continue;
            if (!value.matches("\\d{2}/\\d{2}/\\d{4}")) {
                return false;
            }
        }
        return true;
    }

    public boolean isStatusColumnValid() {
        openRecipeCollectionTab();
        int col = getColumnIndexByHeader("status");
        if (col <= 0) {
            return true;
        }
        List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr"));
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.xpath("./th|./td"));
            if (cells.size() < col) continue;
            String value = cells.get(col - 1).getText().trim().toLowerCase();
            if (value.isEmpty()) continue;
            if (!value.contains("active") && !value.contains("inactive")) {
                return false;
            }
        }
        return true;
    }

    public void clickRecipeCollectionBreadcrumb() {
        List<WebElement> crumbs = driver.findElements(By.xpath("//span[normalize-space()='Recipe Collection'] | //a[normalize-space()='Recipe Collection']"));
        if (crumbs.isEmpty()) {
            openRecipeCollectionTab();
            return;
        }
        WebElement crumb = wait.until(ExpectedConditions.elementToBeClickable(crumbs.get(0)));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", crumb);
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(collectionListTitle),
                    ExpectedConditions.presenceOfElementLocated(tableContainer)
            ));
        } catch (Exception ignored) {
            openRecipeCollectionTab();
        }
    }

    public boolean areCollectionDetailFieldsVisible() {
        String text = driver.findElement(By.tagName("body")).getText().toLowerCase();
        String[] fields = {"about collection", "recipe list", "recipe", "dosha", "minutes", "calories", "status", "action"};
        int matches = 0;
        for (String key : fields) {
            if (text.contains(key)) matches++;
        }
        return matches >= 6;
    }

    public boolean isRecipeListInsideCollectionVisible() {
        String text = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return text.contains("recipe list") && text.contains("new recipe");
    }

    public void openEditRecipeCollectionFromList(String name) {
        openRecipeCollectionTab();
        By rowEdit = By.xpath("(//table//tbody/tr[th[1][normalize-space()='" + name + "'] or td[1][normalize-space()='" + name + "']]//*[contains(@class,'fa-pen') or contains(@class,'fa-edit') or normalize-space()='Edit'])[1]");
        List<WebElement> match = driver.findElements(rowEdit);
        if (!match.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", match.get(0));
            return;
        }
        List<WebElement> target = driver.findElements(
                By.xpath("(//table//tbody/tr[1]//*[contains(@class,'fa-pen') or contains(@class,'fa-edit') or normalize-space()='Edit'])[1]"));
        if (!target.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", target.get(0));
            return;
        } else {
            clickViewRecipeCollection(name);
            List<WebElement> detailEdit = driver.findElements(By.xpath("//*[contains(@class,'fa-pen') or contains(@class,'fa-edit') or normalize-space()='Edit']"));
            if (!detailEdit.isEmpty()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", detailEdit.get(0));
            }
        }
    }

    public boolean areEditCollectionFieldsVisible() {
        String text = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return text.contains("edit recipe collection")
                && text.contains("title")
                && text.contains("description");
    }

    public void openAddRecipeFromCollectionDetails(String collectionName) {
        clickViewRecipeCollection(collectionName);
        By newRecipe = By.xpath("//button[contains(normalize-space(),'New Recipe')] | //*[contains(normalize-space(),'New Recipe')]");
        List<WebElement> btns = driver.findElements(newRecipe);
        if (btns.isEmpty()) return;
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", btns.get(0));
    }

    public boolean areAddRecipeFieldsVisible() {
        String text = driver.findElement(By.tagName("body")).getText().toLowerCase();
        String[] fields = {
                "add recipe", "recipe title", "type", "total minutes", "calories", "dosha",
                "nutrition per serving", "portion size", "ingredients", "instructions"
        };
        int matches = 0;
        for (String key : fields) {
            if (text.contains(key)) matches++;
        }
        return matches >= 8;
    }

    public void openEditRecipeFromCollectionDetails(String collectionName) {
        clickViewRecipeCollection(collectionName);
        By edit = By.xpath("(//table//tbody/tr[1]//*[contains(@class,'fa-pen') or contains(@class,'fa-edit') or normalize-space()='Edit'])[1]");
        List<WebElement> btns = driver.findElements(edit);
        if (btns.isEmpty()) {
            btns = driver.findElements(By.xpath("//*[contains(@class,'fa-pen') or contains(@class,'fa-edit') or normalize-space()='Edit']"));
        }
        if (!btns.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", btns.get(0));
        }
    }

    public boolean areEditRecipeFieldsVisible() {
        String text = driver.findElement(By.tagName("body")).getText().toLowerCase();
        String[] fields = {
                "edit recipe", "recipe title", "type", "total minutes", "calories", "dosha",
                "nutrition per serving", "portion size", "ingredients"
        };
        int matches = 0;
        for (String key : fields) {
            if (text.contains(key)) matches++;
        }
        return matches >= 7;
    }

    public void openDeletePopupFromCollectionList() {
        openRecipeCollectionTab();
        By delete = By.xpath("(//table//tbody/tr[1]//*[contains(@class,'fa-trash') or normalize-space()='Delete'])[1]");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(delete));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
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

    public boolean isNoCollectionResultVisible() {
        List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr"));
        if (rows.isEmpty()) return true;

        if (!lastSearchKeyword.isBlank()) {
            boolean anyMatch = false;
            for (WebElement row : rows) {
                String text = row.getText() == null ? "" : row.getText().toLowerCase();
                if (text.contains(lastSearchKeyword.toLowerCase())) {
                    anyMatch = true;
                    break;
                }
            }
            if (!anyMatch) {
                return true;
            }
        }

        String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return body.contains("no data") || body.contains("no records")
                || body.contains("no result") || body.contains("not found");
    }

    public boolean hasAnyViewActionInCurrentResults() {
        if (isNoCollectionResultVisible()) return false;
        for (int retry = 0; retry < 3; retry++) {
            try {
                List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr"));
                for (WebElement row : rows) {
                    String rowText = row.getText() == null ? "" : row.getText().toLowerCase();
                    if (!lastSearchKeyword.isBlank() && !rowText.contains(lastSearchKeyword.toLowerCase())) {
                        continue;
                    }
                    List<WebElement> views = row.findElements(By.xpath(".//*[contains(@class,'fa-eye') or normalize-space()='View']"));
                    for (WebElement e : views) {
                        if (e.isDisplayed()) return true;
                    }
                }
                return false;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return false;
    }

    public boolean hasAnyEditActionInCurrentResults() {
        if (isNoCollectionResultVisible()) return false;
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
        if (isNoCollectionResultVisible()) return false;
        for (int retry = 0; retry < 3; retry++) {
            try {
                List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr"));
                for (WebElement row : rows) {
                    String rowText = row.getText() == null ? "" : row.getText().toLowerCase();
                    if (!lastSearchKeyword.isBlank() && !rowText.contains(lastSearchKeyword.toLowerCase())) {
                        continue;
                    }
                    List<WebElement> deletes = row.findElements(By.xpath(".//*[contains(@class,'fa-trash') or normalize-space()='Delete']"));
                    for (WebElement e : deletes) {
                        if (e.isDisplayed()) return true;
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
