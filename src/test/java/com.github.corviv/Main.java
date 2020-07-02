package com.github.corviv;

import java.util.List;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.TestNG;
import org.testng.annotations.Listeners;
import org.testng.collections.Lists;

@Listeners(LoggerListener.class)
public class Main {

    public final static String PATH_XML_WIN = "C:\\test";
    public final static String PATH_XML_LIN = "/opt/test";

    public static void main(String[] args) {

        final Logger log = LoggerFactory.getLogger("Main");

        if (args.length < 1) {
            log.error(
                "No argument specified!\n\n Usage : java -jar automation_template.jar <testng_xml>\n Where :\n\t testng_xml: Name of TestNG suite xml-file in \"{}\"",
                getPathToXml());
            return;
        }

        // Run all the tests in selected .xml
        TestNG testng = new TestNG();
        List<String> suites = Lists.newArrayList();
        suites.add(getPathToXml() + args[0]);
        testng.setTestSuites(suites);
        testng.run();

        log.info("Return code {}\n\n", testng.getStatus());
        //if (testng.getStatus() != 0)
        System.exit(testng.getStatus());
    }

    public static String getPathToXml() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return PATH_XML_WIN;
        } else {
            return PATH_XML_LIN;
        }
    }
}