package com.ananda.web.admin.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String LOGIN_URL = "https://dev-admin.anandaspa.com/login";

    // --- Locators ---
    private By emailInput   = By.name("email");
    private By passwordInput = By.name("password");
    private By loginButton   = By.xpath("//button[@type='submit']");

    // Success page check
    private By masterDataHeading = By.xpath("//span[text()='Master Data']");

    // Invalid password (field validation)
    private By passwordError = By.xpath("//div[@id='pswrdError']");

    // Incorrect credentials (toast popup)
    private By toastError = By.xpath("//div[contains(@class,'Toastify__toast--error')]");

    // Forgot Password
    private By forgotPasswordLink = By.xpath("//div[contains(@class,'forgotPassword')]//a[contains(@href,'forgotPassword') or normalize-space()='Forgot Password?']");
    private By forgotPasswordTitle = By.xpath("//h3[contains(@class,'Heading') and normalize-space()='Forgot Password'] | //h3[normalize-space()='Forgot Password']");
    private By forgotEmailInput = By.xpath("//input[@id='inputEmail' or @name='email' or @placeholder='Email Address']");
    private By forgotSubmitButton = By.xpath("//button[normalize-space()='Submit']");
    private By backToLoginLink = By.xpath("//div[contains(@class,'BackToLogin')]//a[contains(@href,'/login') or normalize-space()='Back to Login'] | //a[normalize-space()='Back to Login']");
    private By loginTitle = By.xpath("//*[contains(normalize-space(),'Login')]");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    public void open() {
        driver.get(LOGIN_URL);
        ensureLoginForm();
    }

    // Generic login action
    public void login(String email, String password) {
        ensureLoginForm();
        WebElement emailEl = wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        safeClearAndType(emailEl, email);

        WebElement passEl = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        safeClearAndType(passEl, password);

        clickWithFallback(loginButton);
    }

    private void ensureLoginForm() {
        if (driver.findElements(emailInput).isEmpty()) {
            driver.get(LOGIN_URL);
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
    }
    public void logout() {

        // open profile dropdown (top right)
        WebElement profileMenu = driver.findElement(By.cssSelector("a[aria-haspopup='true']"));
        profileMenu.click();

        // click logout button text
        WebElement logoutBtn = driver.findElement(
                By.xpath("//span[normalize-space()='Log out']")
        );
        logoutBtn.click();
    }
    // ---------- VALID LOGIN ----------
    public boolean isMasterDataVisible() {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(masterDataHeading),
                    ExpectedConditions.urlContains("/master-data")
            ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ---------- INVALID LOGIN: password too short ----------
    public boolean isPasswordErrorVisible() {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(passwordError),
                    ExpectedConditions.visibilityOfElementLocated(By.xpath(
                            "//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'password')"
                                    + " and (contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'invalid')"
                                    + " or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'required')"
                                    + " or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'minimum'))]"
                    )),
                    ExpectedConditions.visibilityOfElementLocated(toastError)
            ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ---------- INVALID LOGIN: incorrect credentials ----------
    public boolean isToastErrorVisible() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(toastError));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isOnLoginPage() {
        try {
            return driver.getCurrentUrl().toLowerCase().contains("/login")
                    || (!driver.findElements(emailInput).isEmpty() && !driver.findElements(loginButton).isEmpty());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isLoginFormVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).isDisplayed()
                    && wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput)).isDisplayed()
                    && wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(loginButton),
                    ExpectedConditions.visibilityOfElementLocated(loginTitle)
            )) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public void openForgotPasswordPage() {
        ensureLoginForm();
        try {
            clickWithFallback(forgotPasswordLink);
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("forgotPassword"),
                    ExpectedConditions.visibilityOfElementLocated(forgotPasswordTitle)
            ));
            wait.until(ExpectedConditions.visibilityOfElementLocated(forgotEmailInput));
            wait.until(ExpectedConditions.visibilityOfElementLocated(forgotSubmitButton));
        } catch (Exception ignored) {
            try {
                driver.get("https://dev-admin.anandaspa.com/forgotPassword");
                wait.until(ExpectedConditions.visibilityOfElementLocated(forgotEmailInput));
            } catch (Exception ignoredAgain) {
            }
        }
    }

    public boolean isForgotPasswordPageVisible() {
        try {
            boolean onForgotUrl = driver.getCurrentUrl().toLowerCase().contains("forgotpassword");
            return (onForgotUrl || !driver.findElements(forgotPasswordTitle).isEmpty())
                    && !driver.findElements(forgotEmailInput).isEmpty()
                    && !driver.findElements(forgotSubmitButton).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public void submitForgotPassword(String email) {
        try {
            WebElement emailBox = wait.until(ExpectedConditions.visibilityOfElementLocated(forgotEmailInput));
            safeClearAndType(emailBox, email == null ? "" : email);
            clickWithFallback(forgotSubmitButton);
        } catch (Exception ignored) {
        }
    }

    public void backToLoginFromForgotPassword() {
        try {
            clickWithFallback(backToLoginLink);
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/login"),
                    ExpectedConditions.visibilityOfElementLocated(emailInput)
            ));
        } catch (Exception ignored) {
        }
    }

    public boolean isForgotPasswordErrorOrStillOnPage() {
        try {
            boolean stillOnForgot = isForgotPasswordPageVisible();
            boolean hasErrorText = !driver.findElements(By.xpath(
                    "//*[contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'invalid')"
                            + " or contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'required')"
                            + " or contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'error')]"
            )).isEmpty();
            return stillOnForgot || hasErrorText;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPasswordMasked() {
        try {
            WebElement pwd = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
            String type = pwd.getAttribute("type");
            return type != null && type.equalsIgnoreCase("password");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isForgotPasswordFormVisible() {
        try {
            return isForgotPasswordPageVisible()
                    && !driver.findElements(backToLoginLink).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private void clickWithFallback(By locator) {
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        try {
            wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
            return;
        } catch (Exception ignored) {
        }
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
            return;
        } catch (Exception ignored) {
        }
        new Actions(driver).moveToElement(el).click().perform();
    }

    private void safeClearAndType(WebElement el, String value) {
        String text = value == null ? "" : value;
        try {
            el.clear();
        } catch (Exception ignored) {
        }
        try {
            el.sendKeys(org.openqa.selenium.Keys.chord(org.openqa.selenium.Keys.CONTROL, "a"));
            el.sendKeys(org.openqa.selenium.Keys.DELETE);
            el.sendKeys(text);
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].value=arguments[1];", el, text);
        }
    }
}
