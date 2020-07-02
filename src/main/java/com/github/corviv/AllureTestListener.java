package com.github.corviv;

import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllureTestListener implements TestLifecycleListener {

    public Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void afterTestStart(TestResult result) {
        AllureUtils.setAgentName();
    }
}
