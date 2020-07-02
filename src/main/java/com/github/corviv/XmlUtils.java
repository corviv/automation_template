package com.github.corviv;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@Listeners(LoggerListener.class)
public class XmlUtils {

    private static final Logger logger = LoggerFactory.getLogger("XmlUtils");

    public static class Settings {

        private File xmlFile = null;
        private Document xmlDoc = null;
        private String xmlDefPath = "C:\\defxml.xml";
        public String token = null;

        public Settings() {
        }

        public Settings(String xmlPath) {
            xmlDefPath = xmlPath;
        }

        public void parseSettings() {
            parseSettings(xmlDefPath);
        }

        public void parseSettings(String xmlPath) {
            try {
                xmlFile = new File(xmlPath);
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                xmlDoc = dBuilder.parse(xmlFile);
                xmlDoc.getDocumentElement().normalize();

            } catch (IOException | ParserConfigurationException | SAXException e) {
                logger.debug(e.getMessage());
            }
        }
    }
}