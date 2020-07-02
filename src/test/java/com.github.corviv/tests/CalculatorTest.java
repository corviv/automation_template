package com.github.corviv.tests;

import com.github.corviv.LoggerListener;
import com.github.corviv.SessionUtils;
import com.github.corviv.WinAppSession;
import com.github.corviv.WiniumSession;
import com.github.corviv.pages.CalculatorPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Story;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(LoggerListener.class)
public class CalculatorTest {

    CalculatorPage calcPage = null;
    WiniumSession testWinium;
    WinAppSession testWinApp;
    private static final Logger logger = LoggerFactory.getLogger("CalculatorTest");

    @BeforeTest
    public void startDriver() {
        testWinium = new WiniumSession();
        //testWinApp = new WinAppSession();
        testWinium.createSession();
        //testWinApp.createSession();
        //testWinApp.connectSession(CalculatorPage.strAppWindowName);

        calcPage = new CalculatorPage(testWinium);
        //calcPage = new CalculatorPage(testWinApp);
    }

    @AfterTest
    public void stop() {
        testWinium.stopSession();
        testWinApp.stopSession();
    }

    @Epic(value = "CalculatorTests")
    @Story(value = "Test1")
    @Test
    public void test() {
        calcPage.Addition();
        calcPage.Subtraction();
        calcPage.Division();
        calcPage.Multiplication();
        calcPage.Combination();
        logger.info("random debug: {}", Math.random());
        SessionUtils.takeScreenshot("calcTest_screen");
    }
}
