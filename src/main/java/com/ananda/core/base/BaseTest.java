package com.ananda.core.base;

import com.ananda.core.drivers.DriverFactory;
import com.ananda.core.listeners.TestListener;
import com.ananda.core.reports.ExtentManager;
import com.ananda.core.reports.ExtentTestManager;
import com.aventstack.extentreports.ExtentTest;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import java.lang.reflect.Method;
import java.time.Duration;

@Listeners(TestListener.class)
public class BaseTest {

    private static final By EMAIL_INPUT = By.name("email");
    private static final By PASSWORD_INPUT = By.name("password");
    private static final By LOGIN_BUTTON = By.xpath("//button[@type='submit']");
    private static final By MASTER_DATA = By.xpath("//*[self::span or self::a][normalize-space()='Master Data']");
    private static final By USER_MANAGEMENT = By.xpath("//*[self::a or self::span][normalize-space()='User Management']");
    private static final By CONTENT_MANAGEMENT = By.xpath("//*[self::a or self::span][normalize-space()='Content Management']");
    private static final String BASE_URL =
            System.getProperty("ananda.baseUrl", "https://dev-admin.anandaspa.com");
    private static final String ADMIN_EMAIL =
            System.getProperty("ananda.admin.email", "ab.sharma@thesynapses.com");
    private static final String ADMIN_PASSWORD =
            System.getProperty("ananda.admin.password", "8#pF7}fF");
    private static final boolean REUSE_DRIVER_ACROSS_CLASSES =
            Boolean.parseBoolean(System.getProperty("ananda.reuseDriverAcrossClasses", "true"));
    private static final boolean FORCE_NAVIGATION_EACH_TEST =
            Boolean.getBoolean("ananda.forceNavigationEachTest");
    private static final long STABILIZE_CACHE_MS =
            Long.getLong("ananda.stabilizeCacheMs", 20000L);

    protected WebDriver driver;
    private boolean authReady = true;
    private String lastStabilizedPath = "";
    private long lastStabilizedAtMs = 0L;
    private static volatile boolean shutdownHookRegistered = false;
    private static final long TEST_PAUSE_MS =
            Long.getLong("ananda.testPauseMs", 0L);

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        registerShutdownHookIfNeeded();
        DriverFactory.initDriver();
        driver = DriverFactory.getDriver();
        authReady = isLoginSuite() || isLikelyAuthenticated() || bootstrapAuthenticatedSession();
    }

    @BeforeMethod(alwaysRun = true)
    public void initExtentTest(Method method) {
        if (!isLoginSuite() && !authReady) {
            authReady = bootstrapAuthenticatedSession();
        }
        if (!isLoginSuite() && !authReady) {
            throw new SkipException("Skipping test: unable to bootstrap authenticated session");
        }

        if (!isLoginSuite()) {
            stabilizeTestStartContext();
        }

        if (ExtentTestManager.getTest() == null) {
            ExtentTestManager.setTest(
                    ExtentManager.getInstance().createTest(
                            getClass().getSimpleName() + "." + method.getName()
                    )
            );
        }
    }

    @org.testng.annotations.AfterMethod(alwaysRun = true)
    public void pauseBetweenTests() {
        if (TEST_PAUSE_MS > 0) {
            try {
                Thread.sleep(TEST_PAUSE_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (!REUSE_DRIVER_ACROSS_CLASSES) {
            DriverFactory.quitDriver();
        }
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        DriverFactory.quitDriver();
    }

    protected ExtentTest log() {
        if (ExtentTestManager.getTest() == null) {
            ExtentTestManager.setTest(
                    ExtentManager.getInstance().createTest(getClass().getSimpleName())
            );
        }
        return ExtentTestManager.getTest();
    }

    private boolean bootstrapAuthenticatedSession() {
        if (isLoginSuite()) {
            return true;
        }

        if (isLikelyAuthenticated()) {
            return true;
        }

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            driver.get(BASE_URL + "/login");

            boolean hasLoginForm = !driver.findElements(EMAIL_INPUT).isEmpty();
            if (hasLoginForm) {
                wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT)).clear();
                driver.findElement(EMAIL_INPUT).sendKeys(ADMIN_EMAIL);
                driver.findElement(PASSWORD_INPUT).clear();
                driver.findElement(PASSWORD_INPUT).sendKeys(ADMIN_PASSWORD);
                driver.findElement(LOGIN_BUTTON).click();
            }

            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(MASTER_DATA),
                    ExpectedConditions.visibilityOfElementLocated(USER_MANAGEMENT)
            ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void stabilizeTestStartContext() {
        String targetPath = getDefaultModulePathForSuite();
        try {
            long now = System.currentTimeMillis();
            if (!FORCE_NAVIGATION_EACH_TEST) {
                if (isTargetContextReady(targetPath)) {
                    closeBlockingOverlays();
                    lastStabilizedPath = targetPath;
                    lastStabilizedAtMs = now;
                    return;
                }
                if (targetPath.equals(lastStabilizedPath) && (now - lastStabilizedAtMs) < STABILIZE_CACHE_MS) {
                    closeBlockingOverlays();
                    return;
                }
            }

            driver.get(BASE_URL + targetPath);
            waitForDocumentReady(8);
            waitForAnyDashboardAnchor(6);
            closeBlockingOverlays();
            waitForDocumentReady(5);
            lastStabilizedPath = targetPath;
            lastStabilizedAtMs = System.currentTimeMillis();
        } catch (Exception e) {
            throw new SkipException("Skipping test: unable to stabilize test start context for " + targetPath);
        }
    }

    private String getDefaultModulePathForSuite() {
        String suite = getClass().getSimpleName().toLowerCase();
        if (suite.contains("guestuser")
                || suite.contains("downloads")
                || suite.contains("teammembers")
                || suite.contains("userrole")
                || suite.contains("accessmanagement")
                || suite.contains("deleterequest")) {
            return "/user-management";
        }
        if (suite.contains("video")
                || suite.contains("audio")
                || suite.contains("article")
                || suite.contains("announcement")
                || suite.contains("content")) {
            return "/content-management";
        }
        return "/master-data";
    }

    private void waitForDocumentReady(int timeoutSec) {
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSec)).until(d ->
                "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    }

    private void waitForAnyDashboardAnchor(int timeoutSec) {
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSec)).until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(MASTER_DATA),
                ExpectedConditions.presenceOfElementLocated(USER_MANAGEMENT),
                ExpectedConditions.presenceOfElementLocated(CONTENT_MANAGEMENT)
        ));
    }

    private void closeBlockingOverlays() {
        try {
            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
        } catch (Exception ignored) {
        }

        By backdrop = By.xpath(
                "//*[contains(@class,'modal-backdrop') or contains(@class,'MuiBackdrop-root') or contains(@class,'overlay')]"
        );
        for (WebElement element : driver.findElements(backdrop)) {
            try {
                if (element.isDisplayed()) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private boolean isLoginSuite() {
        return "AdminLoginTest".equals(getClass().getSimpleName());
    }

    private boolean isLikelyAuthenticated() {
        try {
            return hasAnyDashboardAnchor();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTargetContextReady(String targetPath) {
        if (!isOnTargetPath(targetPath)) {
            return false;
        }
        return hasAnyDashboardAnchor();
    }

    private boolean isOnTargetPath(String targetPath) {
        try {
            String current = driver.getCurrentUrl();
            return current != null && current.toLowerCase().contains(targetPath.toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasAnyDashboardAnchor() {
        return !driver.findElements(MASTER_DATA).isEmpty()
                || !driver.findElements(USER_MANAGEMENT).isEmpty()
                || !driver.findElements(CONTENT_MANAGEMENT).isEmpty();
    }

    private void registerShutdownHookIfNeeded() {
        if (!REUSE_DRIVER_ACROSS_CLASSES || shutdownHookRegistered) {
            return;
        }
        synchronized (BaseTest.class) {
            if (shutdownHookRegistered) {
                return;
            }
            Runtime.getRuntime().addShutdownHook(new Thread(DriverFactory::quitDriver));
            shutdownHookRegistered = true;
        }
    }
}
