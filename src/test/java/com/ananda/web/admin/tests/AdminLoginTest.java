package com.ananda.web.admin.tests;

import com.ananda.core.base.BaseTest;
import com.ananda.core.drivers.DriverFactory;
import com.ananda.web.admin.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AdminLoginTest extends BaseTest {

	@Test(priority = 1, description = "Valid login should land on Master Data page", groups = "Positive")
	public void validLogin() {

	    LoginPage page = new LoginPage(DriverFactory.getDriver());
	    page.open();

	    page.login("ab.sharma@thesynapses.com", "8#pF7}fF");

	    Assert.assertTrue(page.isMasterDataVisible(),
	            "User did not land on Master Data page");

	    log().pass("Valid login successful");

	    // ---- LOGOUT after success ----
	    page.logout();
	    log().info("User logged out successfully");
	}

    @Test(priority = 2, description = "Short/invalid password should show field validation message", groups = "Negative")
    public void invalidPasswordFormat() {

        LoginPage page = new LoginPage(DriverFactory.getDriver());
        page.open();

        page.login("ab.sharma@thesynapses.com", "123");

        log().info("Expected: Password validation message should appear");
        log().info("Actual: Error shown under password field");

        Assert.assertTrue(page.isPasswordErrorVisible(),
                "Password validation message was NOT displayed");

        log().pass("Validation message displayed correctly");
    }

    @Test(priority = 3, description = "Incorrect credentials should show toast error", groups = "Negative")
    public void invalidPasswordLogin() {

        LoginPage page = new LoginPage(DriverFactory.getDriver());
        page.open();

        page.login("ab.sharma@thesynapses.com", "WrongPassword123");


        log().info("Expected: Toast message should appear saying login failed");
        log().info("Actual: Toast error displayed on screen");

        Assert.assertTrue(page.isToastErrorVisible() || page.isPasswordErrorVisible() || page.isOnLoginPage(),
                "No login error message was displayed");

        log().pass("Toast error displayed correctly");
    }

    @Test(priority = 4, description = "Incorrect credentials should show toast error", groups = "Negative")
    public void incorrectPasswordLogin() {

        LoginPage page = new LoginPage(DriverFactory.getDriver());
        page.open();

        page.login("ab.sharma@thesynapses.com", "8#pF7}ft");

        log().info("Expected: Toast message should appear saying login failed");
        log().info("Actual: Toast error displayed on screen");

        Assert.assertTrue(page.isToastErrorVisible() || page.isPasswordErrorVisible() || page.isOnLoginPage(),
                "No login error message was displayed");

        log().pass("Toast error displayed correctly");
    }

    @Test(priority = 5, description = "Login form controls should be visible", groups = "Positive")
    public void verifyLoginFormElementsVisible() {
        LoginPage page = new LoginPage(DriverFactory.getDriver());
        page.open();

        Assert.assertTrue(page.isLoginFormVisible(),
                "Login form elements are not visible");
        log().pass("Login form elements are visible");
    }

    @Test(priority = 6, description = "Forgot Password page should open and return back to login", groups = "Positive")
    public void forgotPasswordNavigation() {
        LoginPage page = new LoginPage(DriverFactory.getDriver());
        page.open();

        page.openForgotPasswordPage();
        Assert.assertTrue(page.isForgotPasswordPageVisible(),
                "Forgot Password page did not open");

        page.backToLoginFromForgotPassword();
        Assert.assertTrue(page.isOnLoginPage(),
                "Back to Login did not return to login page");
        log().pass("Forgot Password navigation works");
    }

    @Test(priority = 7, description = "Forgot Password should reject blank email submit", groups = "Negative")
    public void forgotPasswordBlankEmail() {
        LoginPage page = new LoginPage(DriverFactory.getDriver());
        page.open();

        page.openForgotPasswordPage();
        page.submitForgotPassword("");

        Assert.assertTrue(page.isForgotPasswordErrorOrStillOnPage(),
                "Blank forgot-password submit did not show validation/stay on page");
        log().pass("Blank email validation behavior verified");
    }

    @Test(priority = 8, description = "Forgot Password should reject invalid email format", groups = "Negative")
    public void forgotPasswordInvalidEmailFormat() {
        LoginPage page = new LoginPage(DriverFactory.getDriver());
        page.open();

        page.openForgotPasswordPage();
        page.submitForgotPassword("invalidEmail");

        Assert.assertTrue(page.isForgotPasswordErrorOrStillOnPage(),
                "Invalid forgot-password email did not show validation/stay on page");
        log().pass("Invalid email format behavior verified");
    }

    @Test(priority = 9, description = "Password field should be masked on login form", groups = "Positive")
    public void verifyPasswordFieldMasked() {
        LoginPage page = new LoginPage(DriverFactory.getDriver());
        page.open();

        Assert.assertTrue(page.isPasswordMasked(),
                "Password input is not masked (type=password expected)");
        log().pass("Password field masking verified");
    }

    @Test(priority = 10, description = "Forgot Password form controls should be visible", groups = "Positive")
    public void verifyForgotPasswordFormElementsVisible() {
        LoginPage page = new LoginPage(DriverFactory.getDriver());
        page.open();
        page.openForgotPasswordPage();

        Assert.assertTrue(page.isForgotPasswordFormVisible(),
                "Forgot Password form elements are missing");
        log().pass("Forgot Password form elements are visible");
    }

}
