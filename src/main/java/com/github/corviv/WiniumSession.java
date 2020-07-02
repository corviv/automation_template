package com.github.corviv;

import static java.lang.Thread.sleep;

import java.io.IOException;
import java.net.URL;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.winium.DesktopOptions;
import org.openqa.selenium.winium.WiniumDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;

@Listeners(LoggerListener.class)
public class WiniumSession {

    // Default path to Winium Driver
    private static final String DRIVER_PATH = "C:\\dev\\Selenium\\Winium.Desktop.Driver\\Winium.Desktop.Driver.exe";
    // Default Winium Driver URL
    private static final String DRIVER_URL = "http://localhost:9999";
    // Default path to testing application
    private static final String DEF_APP_PATH = "C:\\Windows\\System32\\calc.exe";
    private static final String PSEXEC_PATH = "C:\\dev\\PsTools\\PsExec.exe";
    private static final String MSIEXEC_PATH = "-d C:\\windows\\system32\\msiexec.exe /i ";

    public static String appName;
    public static String appVersion;
    private String def_appArgs = null;
    private static ProcessBuilder processBuilder = null;
    private static Process process = null;
    private static DesktopOptions app = null;
    //private static WiniumDriverService service = null;
    private static WiniumDriver session = null;
    private static final Logger logger = LoggerFactory.getLogger("WiniumSession");
    private long startTaskTime = 0;
    private static SessionUtils.CustomWait waiter = null;

    public static WiniumDriver getDriverSession() {
        return session;
    }

    public void createSession() {
        createSession(DEF_APP_PATH, def_appArgs);
    }

    public void createSessionFromInstaller(String installerPath, String prefix) {
        appName = FileUtils.getFileNameByPrefix(installerPath, prefix);
        createSession(PSEXEC_PATH, MSIEXEC_PATH + appName);
    }

    public void createSession(String appPath, String appArgs) {
        startTaskTime = System.nanoTime();
        try {
            killDriverSession();

            processBuilder = new ProcessBuilder(DRIVER_PATH);
            process = processBuilder.start();

            app = new DesktopOptions();
            app.setApplicationPath(appPath);
            if (appArgs != null) {
                app.setArguments(appArgs);
            }

             /*
            // #0
            TODO: create DesiredCapabilities variant
            */

            /*
            // #1
            File file = new File(driverPath);
            sleep(1000);
            service = new WiniumDriverService.Builder().usingDriverExecutable(file).usingPort(9999).withVerbose(false).withSilent(false).buildDesktopService();
            service.start();
            session = new WiniumDriver(service, app);
            */

            // #2
            sleep(3000);
            session = new WiniumDriver(new URL(DRIVER_URL), app);
            waiter = new SessionUtils.CustomWait(session);

        } catch (InterruptedException | IOException e) {
            logger.error("Catch 'createSession' exception!\n");
            logger.info(e.getMessage());
            throw new RuntimeException("'createSession' exception!");
        }
    }

    public void connectSession(String appPath) {
        startTaskTime = System.nanoTime();
        try {
            processBuilder = new ProcessBuilder(DRIVER_PATH);
            process = processBuilder.start();

            app = new DesktopOptions();
            app.setApplicationPath(appPath);
            app.setDebugConnectToRunningApp(true);

            session = new WiniumDriver(new URL(DRIVER_URL), app);
            waiter = new SessionUtils.CustomWait(session);

        } catch (IOException e) {
            logger.error("Catch 'connectSession' exception!\n");
            logger.info(e.getMessage());
            throw new RuntimeException("'connectSession' exception!");
        }
    }

    public void stopSession() {

        if (session == null) {
            return;
        }

        try {
            // #1
            //session.close();

            session.quit();
            session = null;
            sleep(5000);
            process.destroy();

            /*
            if (!process.waitFor(3, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }

            killDriverSession();
            */

            // #2
            //service.stop();

        } catch (Exception e) {
            logger.error("Catch 'stopSession' exception!\n");
            logger.info(e.getMessage());
            killDriverSession();
        } finally {
            logger.info("Elapsed time: " + ((System.nanoTime() - startTaskTime) / 1000000) + " ms");
        }
    }

    private boolean killDriverSession() {
        CommandUtils.executeCommand("taskkill /F /IM Winium.Desktop.Driver.exe");
        if (CommandUtils.isSuccessfully) {
            try {
                logger.debug("Driver session destroyed successfully!\n");
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }
        logger.debug("Driver session not found..\n");
        return false;
    }

    public WebElement findElement(By locator) {
        return findElementCustom(locator);
    }

    public WebElement findElementCustom(By locator) {
        return waiter
            .findElementCustom(locator, SessionUtils.DEF_TIMEOUT_S, SessionUtils.DEF_POLL_PERIOD_S,
                true);
    }

    public static WebElement findElementCustom(By locator, int timeout) {
        WebElement abv = null;
        if (timeout > 10) {
            return abv;
        }
        throw new AssertionError(
            new TimeoutException("\nElement '" + locator.toString() + "' not found!"));
        //return waiter.findElementCustom(locator, timeout, SessionUtils.DEF_POLL_PERIOD_S, true);
    }

    public static WebElement findElementCustom(By locator, int timeout, boolean isThrowable) {
        return waiter
            .findElementCustom(locator, timeout, SessionUtils.DEF_POLL_PERIOD_S, isThrowable);
    }

    public static WebElement findElementCustom(By locator, int timeout, int period) {
        return waiter.findElementCustom(locator, timeout, period, true);
    }

    public static WebElement findElementCustom(By locator, int timeout, int period,
        boolean isThrowable) {
        return waiter.findElementCustom(locator, timeout, period, isThrowable);
    }

    public static void checkElementIsEnabled(WebElement element, int timeout) {
        waiter.findElementCustom(element, timeout, SessionUtils.DEF_POLL_PERIOD_S, false);
    }

    public static void checkElementIsEnabled(WebElement element, int timeout, boolean toClick) {
        waiter.findElementCustom(element, timeout, SessionUtils.DEF_POLL_PERIOD_S, toClick);
    }
}