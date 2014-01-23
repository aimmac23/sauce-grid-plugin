package com.saucelabs.grid;

import static junit.framework.Assert.assertEquals;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * @author Ross Rowe
 */
public class SauceWebDriverTest {

    private WebDriver driver;

    @Before
    public void setUp() throws Exception {

        DesiredCapabilities capabillities = DesiredCapabilities.firefox();
        capabillities.setCapability("version", "24");
        capabillities.setCapability("platform", Platform.WIN8);
        capabillities.setCapability("name", "Amazon Grid Test");
        this.driver = new RemoteWebDriver(
                new URL("http://localhost:4444/wd/hub"),
                capabillities);
    }

    @After
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
