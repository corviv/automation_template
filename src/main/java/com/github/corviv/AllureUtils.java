package com.github.corviv;

import static com.github.automatedowl.tools.AllureEnvironmentWriter.allureEnvironmentWriter;

import com.google.common.collect.ImmutableMap;
import io.qameta.allure.Allure;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;

@Listeners(LoggerListener.class)
public class AllureUtils {

    private static final Logger logger = LoggerFactory.getLogger("AllureUtils");

    public static void setAllureEnvInfo() {
        try {
            Properties properties = new Properties();
            properties.load(Main.class.getResourceAsStream("/pom.properties"));
            //String artifactId = properties.getProperty("artifactId");
            //String groupId = properties.getProperty("groupId");
            logger.info("Project version: {}", properties.getProperty("version"));

            com.github.corviv.FileUtils.getAppVersion();

            allureEnvironmentWriter(
                ImmutableMap.<String, String>builder()
                    .put("OS", System.getProperty("os.name"))
                    .put("OS version", System.getProperty("os.version"))
                    .put("Hostname", InetAddress.getLocalHost().getHostName())
                    .put("User", System.getProperty("user.name"))
                    .put("Product version", WiniumSession.appVersion)
                    .put("Project version", properties.getProperty("version"))
                    .build(), System.getProperty("user.dir")
                    + "/allure-results/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void appendLogToAllure() {
        try {
            Allure.addAttachment("Log file", FileUtils.openInputStream(new File("epp_test.log")));
        } catch (IOException e) {
            logger.error("An exception occurred while taking log file");
            e.printStackTrace();
        }
    }

    public static void addEpicLabel(final String epic) {
        Allure.epic(epic);
        logger.info("Added epic label: {}", epic);
    }

    public static void addFeatureLabel(final String feature) {
        Allure.feature(feature);
        logger.info("Added feature label: {}", feature);
    }

    public static void addStoryLabel(final String story) {
        Allure.story(story);
        logger.info("Added story label: {}", story);
    }

    public static void addSuiteLabel(final String suite) {
        Allure.suite(suite);
        logger.info("Added suite label: {}", suite);
    }

    public static void addDescription(final String desc) {
        Allure.description(desc);
        logger.info("Add description: {}", desc);
    }

    public static void setTestMethodName(final String testMethod) {
        Allure.getLifecycle().updateTestCase(testResult -> testResult.setName(testMethod));
        logger.info("Changed name of test method : {}", testMethod);
    }

    public static void setParameter(final String paramName, final String paramValue) {
        Allure.parameter(paramName, paramValue);
        logger.debug("Create new parameter '{}' with '{}' value", paramName, paramValue);
    }

    public static void setAgentName() {
        try {
            setAgentName(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void setAgentName(String agentName) {
        setParameter("Agent", agentName);
    }
}
