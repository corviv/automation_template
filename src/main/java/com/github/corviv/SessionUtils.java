package com.github.corviv;

import static java.lang.Thread.sleep;

import io.qameta.allure.Allure;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.winium.WiniumDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;

@Listeners(LoggerListener.class)
public class SessionUtils {

    public static final int DEF_TIMEOUT_S = 10;
    public static final int DEF_POLL_PERIOD_S = 2;
    private static final int DEF_SCROLL_DELAY_MS = 100;

    private static final String PATH_SCREENSHOTS = "allure-results\\screenshots\\";

    private static final Logger logger = LoggerFactory.getLogger("SessionUtils");

    public static class CustomWait {

        private WiniumDriver session = null;
        private static ScheduledExecutorService ses = null;
        private static WebElement res = null;
        private static int iters;
        private long startSearchTime = 0;

        public CustomWait(WiniumDriver session) {
            this.session = session;
        }

        WebElement findElementCustom(By by, int timeout, int period, boolean isThrowable) {
            ses = Executors.newSingleThreadScheduledExecutor();

            if (period < DEF_POLL_PERIOD_S) {
                period = DEF_POLL_PERIOD_S;
            }

            if (timeout < DEF_POLL_PERIOD_S) {
                timeout = DEF_POLL_PERIOD_S;
            }

            iters = (int) (timeout / period) + 1;
            startSearchTime = System.nanoTime();

            ScheduledFuture<?> handler = ses.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() throws TimeoutException {
                    checkElement(by);
                }
            }, 0, period, TimeUnit.SECONDS);

            try {
                handler.get();
                while (iters > 0) {
                    sleep(period * 1000);
                }
            } catch (ExecutionException | InterruptedException e) {
                if (isThrowable) {
                    throw new RuntimeException(e.getCause());
                }
                logger.info("Element search completed!");
            } catch (CancellationException e) {
                return res;
            }
            return res;
        }

        void findElementCustom(WebElement element, int timeout, int period, boolean toClick) {
            ses = Executors.newSingleThreadScheduledExecutor();

            if (period < DEF_POLL_PERIOD_S) {
                period = DEF_POLL_PERIOD_S;
            }

            if (timeout < DEF_POLL_PERIOD_S) {
                timeout = DEF_POLL_PERIOD_S;
            }

            iters = (int) (timeout / period) + 1;
            startSearchTime = System.nanoTime();

            ScheduledFuture<?> handler = ses.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() throws TimeoutException {
                    checkElementIsEnabled(element, toClick);
                }
            }, 0, period, TimeUnit.SECONDS);

            try {
                handler.get();
                while (iters > 0) {
                    sleep(period * 1000);
                }
            } catch (ExecutionException | InterruptedException e) {
                if (iters == 0) {
                    throw new RuntimeException(e.getCause());
                }
            } catch (CancellationException expected) {
            }
        }

        public void checkElement(By locator) throws TimeoutException {
            try {
                logger.debug("Trying to find '{}'..", locator.toString());
                res = session.findElement(locator);
                logger.debug("Element found in {} ms",
                    (System.nanoTime() - startSearchTime) / 1000000);
                iters = 0;
                ses.shutdownNow();
            } catch (NoSuchElementException e) {
                logger.info("'{}' was not found!", locator.toString());
                --iters;
                if (iters == 0) {
                    ses.shutdownNow();
                    throw new AssertionError(
                        "\nTimeoutException: element '" + locator.toString() + "' not found!");
                }
            }
        }

        public void checkElementIsEnabled(WebElement element, boolean toClick) {

            logger.debug("Checking if is '{}' enabled..", element.getAttribute("Name"));
            if (element.isEnabled()) {
                if (toClick) {
                    element.click();
                }
                // TODO: add handling WebDriver exceptions
                //ses.shutdownNow();
                throw new TimeoutException("\nIt's OK!");
            }

            logger.info("'{}' is not enabled now!", element.getAttribute("Name"));
            --iters;
            if (iters == 0) {
                throw new TimeoutException(
                    "\nElement '" + element.getAttribute("Name") + "' is not enabled!");
            }
        }

        public FluentWait<WiniumDriver> fluentWait(By locator, int timeout, int period) {

            if (period < DEF_POLL_PERIOD_S) {
                period = DEF_POLL_PERIOD_S;
            }

            if (timeout < DEF_POLL_PERIOD_S) {
                timeout = DEF_POLL_PERIOD_S;
            }

            return new FluentWait<>(session)
                .withTimeout(Duration.ofSeconds(timeout))
                .pollingEvery(Duration.ofSeconds(period))
                .ignoring(NoSuchElementException.class)
                .withMessage("'" + locator.toString() + "' is not displayed");
        }

        public WebElement waitForElement(By locator, int timeout, int period) {
            try {
                logger.debug("Checking the '" + locator + "' having " + timeout + " seconds limit");
                return fluentWait(locator, timeout, period)
                    .until(ExpectedConditions.elementToBeClickable(locator));
            } catch (TimeoutException | NoSuchElementException e) {
                throw new TimeoutException(
                    "\nThe '" + locator.toString() + "' isn't clickable after timeout!");
            }
        }

        public boolean isElementDisplayed(By locator, int timeout, int period) {
            boolean status = true;
            try {
                logger.debug("Checking the '" + locator.toString() + "' having " + timeout
                    + " seconds limit");
                fluentWait(locator, timeout, period)
                    .until(ExpectedConditions.elementToBeClickable(locator));
            } catch (TimeoutException e) {
                status = false;
            }
            return status;
        }
    }

    public static void pasteText(String text) {
        try {
            StringSelection stringSelection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, stringSelection);
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_V);
            sleep(500);
            robot.keyRelease(KeyEvent.VK_CONTROL);
            robot.keyRelease(KeyEvent.VK_V);
        } catch (InterruptedException | AWTException e) {
            logger.error(e.getMessage());
        }
    }

    /*
    public static void moveToElement(By locator) {
        WebElement wb = session.findElementCustom(locator);
        Actions actions = new Actions(WiniumSession.getDriverSession());
        actions.moveToElement(wb).perform();
    }
    */

    public static void scrollDown(int numScrolls) {
        scroll(1, numScrolls, DEF_SCROLL_DELAY_MS);
    }

    public static void scrollDown(int numScrolls, int delayMs) {
        scroll(1, numScrolls, delayMs);
    }

    public static void scrollUp(int numScrolls) {
        scroll(-1, numScrolls, DEF_SCROLL_DELAY_MS);
    }

    public static void scrollUp(int numScrolls, int delayMs) {
        scroll(-1, numScrolls, delayMs);
    }

    public static void scroll(int wheelAmt, int numScrolls, int delayMs) {
        try {
            Robot robot = new Robot();

            for (int index = 0; index < numScrolls; index++) {
                robot.mouseWheel(wheelAmt);
                robot.delay(delayMs);
            }
        } catch (AWTException e) {
            logger.error(e.getMessage());
        }
    }

    public static void doubleClick() {
        try {
            Robot robot = new Robot();
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.delay(100);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            robot.delay(100);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.delay(100);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static void takeScreenshot(String screenshotName) {
        WiniumDriver driver = WiniumSession.getDriverSession();
        if (driver == null) {
            logger.error("Driver is not initialized!");
            return;
        }
        try {
            File screenshotAs = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Allure.addAttachment(screenshotName, FileUtils.openInputStream(screenshotAs));
            File targetFile = new File(PATH_SCREENSHOTS, screenshotName + ".png");
            FileUtils.copyFile(screenshotAs, targetFile);
            logger.info("Screenshot can be found : {}", targetFile.getPath());
        } catch (IOException e) {
            logger.error("An exception occurred while taking screenshot");
            e.printStackTrace();
        }
    }

    /*    NOT WORKED!
    @Attachment(value = "Screenshot of {0}", type = "image/png")
    public static byte[] takeScreenshot(String screenshotName) {
        try {
            WiniumDriver driver = WiniumSession.getDriverSession();
            if (driver == null) {
                logger.error("Driver is not initialized!");
                return null;
            }
            File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File targetFile = new File(PATH_SCREENSHOTS, screenshotName + ".png");
            FileUtils.copyFile(screenshotFile, targetFile);
            logger.info("Screenshot can be found : {}", targetFile.getPath());
            return FileUtils.readFileToByteArray(screenshotFile);
        } catch (IOException e) {
            logger.error("An exception occurred while taking screenshot");
            e.printStackTrace();
            return null;
        }
    }
    */
}
