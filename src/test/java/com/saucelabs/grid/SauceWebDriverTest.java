package com.saucelabs.grid;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URL;

import static junit.framework.Assert.assertEquals;

/**
 * @author Ross Rowe
 */
public class SauceWebDriverTest {

    private WebDriver driver;

    @BeforeMethod
    public void setUp() throws Exception {

        DesiredCapabilities capabillities = DesiredCapabilities.firefox();
        capabillities.setCapability("version", "12");
        capabillities.setCapability("platform", Platform.XP);
        this.driver = new RemoteWebDriver(
                new URL("http://localhost:4444/wd/hub"),
                capabillities);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        driver.quit();
    }

    @Test
    public void amazon() throws Exception {
        driver.get("http://www.amazon.com/");
        assertEquals(
                "Amazon.com: Online Shopping for Electronics, Apparel, Computers, Books, DVDs & more",
                driver.getTitle());

        //verify test was executed against SoD
    }
}
