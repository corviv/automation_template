package com.github.corviv.pages;

import com.github.corviv.LoggerListener;
import com.github.corviv.WiniumSession;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(LoggerListener.class)
public class CalculatorPage {

    private static WiniumSession session;
    private static final Logger logger = LoggerFactory.getLogger("CalculatorPage");

    public static String strAppWindowName = "Калькулятор";

    private static final By btnOne = By.name("Один");
    private static final By btnSeven = By.name("Семь");
    private static final By btnEight = By.name("Восемь");
    private static final By btnNine = By.name("Девять");

    private static final By btnPlus = By.name("Плюс");
    private static final By btnDivine = By.name("Разделить на");
    private static final By btnMultiplication = By.name("Умножить на");
    private static final By btnSubtraction = By.name("Минус");
    private static final By btnEquals = By.name("Равно");
    private static final By btnClear = By.name("Очистить");
    private static final String lineResultPrefix = "На экране показано";

    public CalculatorPage(WiniumSession session) {
        this.session = session;
    }

    public void Clear() {
        session.findElement(btnClear).click();
        Assert.assertEquals("0", _getCalcResultText());
    }

    public void Addition() {
        Clear();
        session.findElement(btnOne).click();
        session.findElement(btnPlus).click();
        session.findElement(btnSeven).click();
        session.findElement(btnEquals).click();
        Assert.assertEquals("8", _getCalcResultText());
    }

    public void Subtraction() {
        Clear();
        session.findElement(btnNine).click();
        session.findElement(btnSubtraction).click();
        session.findElement(btnOne).click();
        session.findElement(btnEquals).click();
        Assert.assertEquals("8", _getCalcResultText());
    }

    @Test
    public void Division() {
        Clear();
        session.findElement(btnEight).click();
        session.findElement(btnEight).click();
        session.findElement(btnDivine).click();
        session.findElement(btnOne).click();
        session.findElement(btnOne).click();
        session.findElement(btnEquals).click();
        Assert.assertEquals("8", _getCalcResultText());
    }

    public void Multiplication() {
        Clear();
        session.findElement(btnNine).click();
        session.findElement(btnMultiplication).click();
        session.findElement(btnNine).click();
        session.findElement(btnEquals).click();
        Assert.assertEquals("81", _getCalcResultText());
    }

    @Test
    public void Combination() {
        Clear();
        session.findElement(btnSeven).click();
        session.findElement(btnMultiplication).click();
        session.findElement(btnNine).click();
        session.findElement(btnPlus).click();
        session.findElement(btnOne).click();
        session.findElement(btnEquals).click();
        session.findElement(btnDivine).click();
        session.findElement(btnEight).click();
        session.findElement(btnEquals).click();
        Assert.assertEquals("8", _getCalcResultText());
    }

    protected String _getCalcResultText() {
        WebElement lineCalcResult = session.findElement(By.name("CalculatorResults"));
        // trim extra text and whitespace off of the display value
        return lineCalcResult.getText().replace(lineResultPrefix, "").trim();
    }
}
