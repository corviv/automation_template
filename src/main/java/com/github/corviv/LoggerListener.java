package com.github.corviv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class LoggerListener implements ITestListener {
    private static final Logger logger = LoggerFactory.getLogger("LoggerListener");

    @Override
    public void onStart(ITestContext iTestContext) {
        AllureUtils.setAllureEnvInfo();
    }

    @Override
    public void onFinish(ITestContext iTestContext) {
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
    }

    @Override
    public void onTestStart(ITestResult result) {
        logger.info("Starting '{}.{}' with parameters {}...\n", result.getTestClass().getName(), result.getName(), result.getParameters());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("Test '{}' PASSED\n", result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("Test '{}' FAILED!\n", result.getName(), result.getThrowable());

        String testClassName = getTestClassName(result.getInstanceName()).trim();
        String testMethodName = result.getName().trim();
        String screenshotName = testClassName + "_" + testMethodName;
        SessionUtils.takeScreenshot(screenshotName);
        AllureUtils.appendLogToAllure();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.error("Test '{}' SKIPPED!\n", result.getName(), result.getThrowable());
    }

    public String getTestClassName(String testName) {
        String[] reqTestClassName = testName.split("\\.");
        int i = reqTestClassName.length - 1;
        logger.debug("Required Test Name : " + reqTestClassName[i]);
        return reqTestClassName[i];
    }
}