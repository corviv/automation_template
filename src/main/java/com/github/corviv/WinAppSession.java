package com.github.corviv;

import static java.lang.Thread.sleep;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import io.appium.java_client.windows.WindowsDriver;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;

@Listeners(LoggerListener.class)
public class WinAppSession {

    // Default path to WinAppDriver
    private static final String DRIVER_PATH = "C:\\Program Files (x86)\\Windows Application Driver\\WinAppDriver.exe";
    // Default WinAppDriver URL
    private static final String DRIVER_URL = "http://127.0.0.1:4723";
    // Default path to testing application
    private static final String DEF_APP_PATH = "C:\\Windows\\System32\\calc.exe";
    private String def_appArgs = null;
    // Default timeout for implicitly wait (sec)
    private int def_timeout = 3;
    private long startTaskTime = 0;

    private static ProcessBuilder processBuilder = null;
    private static Process process = null;
    private static final Logger logger = LoggerFactory.getLogger("Session");
    private static SessionUtils.CustomWait waiter = null;
    static WindowsDriver session = null;

    public static WindowsDriver getDriverSession() {
        return session;
    }

    public void createSession() {
        createSession(DEF_APP_PATH, def_appArgs, def_timeout);
    }

    public void createSession(String appPath, String appArgs, int timeout) {
        try {
            //processBuilder = new ProcessBuilder(driverPath);
            //process = processBuilder.start();

            DesiredCapabilities capabilities = new DesiredCapabilities();

            capabilities.setCapability("platformName", "Windows");
            capabilities.setCapability("deviceName", "WindowsPC");
            capabilities.setCapability("app", appPath);
            if (appArgs != null) {
                capabilities.setCapability("appArguments", appArgs);
            }

            try {
                session = new WindowsDriver<>(new URL(DRIVER_URL), capabilities);
            } catch (SessionNotCreatedException e) {
                sleep(1000);
                session = new WindowsDriver<>(new URL(DRIVER_URL), capabilities);
            }

            session.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
            //session.launchApp();

        } catch (IOException | InterruptedException e) {
            logger.error("Catch 'createSession' exception!\n");
            logger.info(e.getMessage());
            throw new RuntimeException("'createSession' exception!");
        }
    }

    public String getAppHex() {
        Pointer foundWindowPointer = new Memory(4096);
        String handle = null;
        if (foundWindowPointer.getPointer(0) != null) {
            WinDef.HWND foundWindow = new WinDef.HWND(foundWindowPointer.getPointer(0));
            handle = String.valueOf(foundWindow);
        }
        return handle.substring(7);
    }

    public void createSessionWithAppWindowHandle() {
        createSessionWithAppWindowHandle(DEF_APP_PATH, def_appArgs, def_timeout);
    }

    public void createSessionWithAppWindowHandle(String appPath, String appArgs, int timeout) {
        try {
            processBuilder = new ProcessBuilder(appPath);
            process = processBuilder.start();

            sleep(2000);

            String windowHandle = getAppHex();

            sleep(2000);

            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setCapability("appTopLevelWindow", windowHandle);
            capabilities.setCapability("platformName", "Windows");
            capabilities.setCapability("deviceName", "WindowsPC");
            capabilities.setCapability("app", appPath);
            if (appArgs != null) {
                capabilities.setCapability("appArguments", appArgs);
            }

            session = new WindowsDriver<>(new URL(DRIVER_URL), capabilities);
            session.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);

        } catch (IOException | InterruptedException e) {
            logger.error("Catch 'createSession' exception!\n");
            logger.info(e.getMessage());
            throw new RuntimeException("'createSession' exception!");
        }
    }

    public void connectSession(String appName) {
        connectSession(DEF_APP_PATH, appName, def_timeout);
    }

    public void connectSession(String appPath, String appName, int timeout) {
        try {
            processBuilder = new ProcessBuilder(appPath);
            process = processBuilder.start();

            sleep(2000);

            DesiredCapabilities desktopCapabilities = new DesiredCapabilities();
            desktopCapabilities.setCapability("platformName", "Windows");
            desktopCapabilities.setCapability("deviceName", "WindowsPC");
            desktopCapabilities.setCapability("app", "Root");

            session = new WindowsDriver<>(new URL(DRIVER_URL), desktopCapabilities);

            WebElement BHWebElement = session.findElement(By.name(appName));
            String WinHandleStr = BHWebElement.getAttribute("NativeWindowHandle");
            int WinHandleInt = Integer.parseInt(WinHandleStr);
            String WinHandleHex = Integer.toHexString(WinHandleInt);

            DesiredCapabilities MACapabilities = new DesiredCapabilities();
            MACapabilities.setCapability("platformName", "Windows");
            MACapabilities.setCapability("deviceName", "WindowsPC");
            MACapabilities.setCapability("appTopLevelWindow", WinHandleHex);

            session = new WindowsDriver<>(new URL(DRIVER_URL), MACapabilities);

        } catch (IOException | InterruptedException e) {
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
            session.close();
            session.quit();
            session = null;
            process.destroy();
            if (!process.waitFor(3, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        } catch (InterruptedException e) {
            logger.error("Catch 'stopSession' exception!\n");
            logger.info(e.getMessage());
        } finally {
            logger.info("Elapsed time: " + ((System.nanoTime() - startTaskTime) / 1000000) + " ms");
        }
    }
}
