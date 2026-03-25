package com.ananda.core.drivers;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.events.EventFiringDecorator;
import org.openqa.selenium.support.events.WebDriverListener;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Set;

public class DriverFactory {

    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static final long SLOW_MO_MS = Long.getLong("ananda.slowMoMs", 0L);
    private static final Set<String> SLOW_METHODS = Set.of(
            "click", "sendKeys", "clear", "submit",
            "get", "back", "forward", "refresh"
    );

    public static void initDriver() {
        if (driver.get() == null) {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized");
            options.addArguments("--disable-background-timer-throttling");
            options.addArguments("--disable-renderer-backgrounding");
            options.addArguments("--disable-backgrounding-occluded-windows");

            WebDriver d = new ChromeDriver(options);
            if (SLOW_MO_MS > 0) {
                d = new EventFiringDecorator<>(new SlowMoListener(SLOW_MO_MS)).decorate(d);
            }
            d.manage().window().maximize();
            d.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
            d.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
            d.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
            driver.set(d);
        }
    }

    public static WebDriver getDriver() {
        return driver.get();
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
        }
    }

    private static class SlowMoListener implements WebDriverListener {
        private final long delayMs;

        private SlowMoListener(long delayMs) {
            this.delayMs = delayMs;
        }

        @Override
        public void afterAnyCall(Object target, Method method, Object[] args, Object result) {
            if (method != null && SLOW_METHODS.contains(method.getName())) {
                sleep();
            }
        }

        private void sleep() {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
