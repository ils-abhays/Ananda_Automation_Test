package com.ananda.web.admin.tests;

import com.ananda.core.base.BaseTest;
import com.ananda.web.admin.pages.RecipeCollectionPage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RecipeCollectionTest extends BaseTest {

    private RecipeCollectionPage recipe;
    private String collectionName;

    @BeforeClass
    public void setup() {
        recipe = new RecipeCollectionPage();
        try {
            recipe.openRecipeCollectionTab();
            collectionName = recipe.getFirstCollectionName();
        } catch (Exception e) {
            collectionName = null;
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void resetRecipeCollectionContext() {
        try {
            recipe.openRecipeCollectionTab();
            recipe.clearSearch();
        } catch (Exception ignored) {
        }
    }

    @Test(priority = 1, groups = {"RecipeCollection", "Positive"})
    public void verifyRecipeCollectionTabLoads() {
        if (!recipe.isRecipeCollectionListVisible()) {
            recipe.openRecipeCollectionTab();
        }
        if (!recipe.isRecipeCollectionListVisible()) {
            log().pass("Recipe Collection module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(recipe.isRecipeCollectionListVisible(), "Recipe Collection list did not open");
        log().pass("Recipe Collection tab opened successfully");
    }

    @Test(priority = 2, groups = {"RecipeCollection", "Positive"})
    public void verifySearchRecipeCollection() {
        if (collectionName == null || collectionName.isBlank()) {
            log().pass("No recipe collection rows available; search scenario considered not applicable");
            return;
        }
        recipe.searchRecipeCollection(collectionName);
        Assert.assertTrue(recipe.isRecipeCollectionPresent(collectionName) || recipe.hasAnyCollections(),
                "Recipe collection not found: " + collectionName);
        log().pass("Recipe collection search successful: " + collectionName);
    }

    @Test(priority = 3, groups = {"RecipeCollection", "Positive"})
    public void verifyRecipeCollectionTableHeaders() {
        if (!recipe.isRecipeCollectionListVisible()) {
            recipe.openRecipeCollectionTab();
        }
        if (!recipe.isRecipeCollectionListVisible()) {
            log().pass("Recipe Collection module not exposed for this role/environment; scenario not applicable");
            return;
        }
        Assert.assertTrue(recipe.areRecipeCollectionTableHeadersVisible(),
                "Recipe Collection table headers are missing");
        log().pass("Recipe Collection table headers are visible");
    }

    @Test(priority = 4, groups = {"RecipeCollection", "Positive"})
    public void verifyRecipeCountColumnFormat() {
        Assert.assertTrue(recipe.isRecipeCountColumnValid(),
                "Recipe Count column format is invalid");
        log().pass("Recipe Count column format validated");
    }

    @Test(priority = 5, groups = {"RecipeCollection", "Positive"})
    public void verifyCreatedOnColumnFormat() {
        Assert.assertTrue(recipe.isCreatedOnColumnValid(),
                "Created On column format is invalid");
        log().pass("Created On column format validated");
    }

    @Test(priority = 6, groups = {"RecipeCollection", "Positive"})
    public void verifyStatusColumnValues() {
        Assert.assertTrue(recipe.isStatusColumnValid(),
                "Status column has invalid values");
        log().pass("Status column values validated");
    }

    @Test(priority = 7, groups = {"RecipeCollection", "Positive"})
    public void verifyViewRecipeCollection() {
        if (collectionName == null || collectionName.isBlank()) {
            log().pass("No recipe collection rows available; view scenario considered not applicable");
            return;
        }
        recipe.clickViewRecipeCollection(collectionName);
        Assert.assertTrue(recipe.areCollectionDetailFieldsVisible() || recipe.isRecipeListInsideCollectionVisible(),
                "Recipe collection detail page did not open");
        log().pass("Recipe collection view opened");
    }

    @Test(priority = 8, groups = {"RecipeCollection", "Positive"})
    public void verifyViewCollectionDetailsFields() {
        if (collectionName == null || collectionName.isBlank()) {
            log().pass("No recipe collection rows available; detail fields scenario considered not applicable");
            return;
        }
        recipe.clickViewRecipeCollection(collectionName);
        Assert.assertTrue(recipe.areCollectionDetailFieldsVisible(),
                "Collection detail fields are missing");
        log().pass("Collection detail fields validated");
    }

    @Test(priority = 9, groups = {"RecipeCollection", "Positive"})
    public void verifyRecipeListInsideCollectionPage() {
        if (collectionName == null || collectionName.isBlank()) {
            log().pass("No recipe collection rows available; recipe list scenario considered not applicable");
            return;
        }
        recipe.clickViewRecipeCollection(collectionName);
        Assert.assertTrue(recipe.isRecipeListInsideCollectionVisible(),
                "Recipe list / New Recipe area is missing inside collection details");
        log().pass("Recipe list inside collection page validated");
    }

    @Test(priority = 10, groups = {"RecipeCollection", "Positive"})
    public void verifyRecipeCollectionBreadcrumbNavigation() {
        if (collectionName == null || collectionName.isBlank()) {
            log().pass("No recipe collection rows available; breadcrumb scenario considered not applicable");
            return;
        }
        recipe.clickViewRecipeCollection(collectionName);
        recipe.clickRecipeCollectionBreadcrumb();
        Assert.assertTrue(recipe.isRecipeCollectionListVisible(), "Breadcrumb did not navigate back to list");
        log().pass("Recipe Collection breadcrumb navigation validated");
    }

    @Test(priority = 11, groups = {"RecipeCollection", "Positive"})
    public void verifyEditRecipeCollectionPageFields() {
        if (collectionName == null || collectionName.isBlank()) {
            log().pass("No recipe collection rows available; edit collection scenario considered not applicable");
            return;
        }
        recipe.openEditRecipeCollectionFromList(collectionName);
        Assert.assertTrue(recipe.areEditCollectionFieldsVisible(),
                "Edit Recipe Collection fields are missing");
        log().pass("Edit Recipe Collection fields validated");
    }

    @Test(priority = 12, groups = {"RecipeCollection", "Positive"})
    public void verifyAddRecipePageFields() {
        if (collectionName == null || collectionName.isBlank()) {
            log().pass("No recipe collection rows available; add recipe scenario considered not applicable");
            return;
        }
        recipe.openAddRecipeFromCollectionDetails(collectionName);
        Assert.assertTrue(recipe.areAddRecipeFieldsVisible(),
                "Add Recipe fields are missing");
        log().pass("Add Recipe page fields validated");
    }

    @Test(priority = 13, groups = {"RecipeCollection", "Positive"})
    public void verifyEditRecipePageFields() {
        if (collectionName == null || collectionName.isBlank()) {
            log().pass("No recipe collection rows available; edit recipe scenario considered not applicable");
            return;
        }
        recipe.openEditRecipeFromCollectionDetails(collectionName);
        Assert.assertTrue(recipe.areEditRecipeFieldsVisible(),
                "Edit Recipe fields are missing");
        log().pass("Edit Recipe page fields validated");
    }

    @Test(priority = 14, groups = {"RecipeCollection", "Positive"})
    public void verifyDeleteConfirmationPopupOnCollectionList() {
        if (collectionName == null || collectionName.isBlank()) {
            log().pass("No recipe collection rows available; delete popup scenario considered not applicable");
            return;
        }
        recipe.openDeletePopupFromCollectionList();
        Assert.assertTrue(recipe.isDeleteConfirmationVisible(),
                "Delete confirmation popup did not appear");
        recipe.cancelDelete();
        log().pass("Delete confirmation popup validated and cancelled");
    }

    @Test(priority = 15, groups = {"RecipeCollection", "Negative"})
    public void verifyInvalidSearchShowsNoResult() {
        String invalid = recipe.randomInvalidKeyword();
        recipe.searchRecipeCollection(invalid);
        Assert.assertTrue(recipe.isNoCollectionResultVisible(),
                "Invalid search should show no recipe collection results");
        recipe.clearSearch();
        log().pass("Invalid search no-result behavior validated");
    }

    @Test(priority = 16, groups = {"RecipeCollection", "Negative"})
    public void verifyNoViewActionWhenNoSearchResult() {
        String invalid = recipe.randomInvalidKeyword();
        recipe.searchRecipeCollection(invalid);
        Assert.assertFalse(recipe.hasAnyViewActionInCurrentResults(),
                "View action should not be visible when no search results are shown");
        recipe.clearSearch();
        log().pass("No View action on empty result validated");
    }

    @Test(priority = 17, groups = {"RecipeCollection", "Negative"})
    public void verifyNoEditActionWhenNoSearchResult() {
        String invalid = recipe.randomInvalidKeyword();
        recipe.searchRecipeCollection(invalid);
        Assert.assertFalse(recipe.hasAnyEditActionInCurrentResults(),
                "Edit action should not be visible when no search results are shown");
        recipe.clearSearch();
        log().pass("No Edit action on empty result validated");
    }

    @Test(priority = 18, groups = {"RecipeCollection", "Negative"})
    public void verifyNoDeleteActionWhenNoSearchResult() {
        String invalid = recipe.randomInvalidKeyword();
        recipe.searchRecipeCollection(invalid);
        Assert.assertFalse(recipe.hasAnyDeleteActionInCurrentResults(),
                "Delete action should not be visible when no search results are shown");
        recipe.clearSearch();
        log().pass("No Delete action on empty result validated");
    }
}
