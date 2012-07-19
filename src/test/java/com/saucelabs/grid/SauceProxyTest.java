package com.saucelabs.grid;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
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
public class SauceProxyTest {

    private WebDriver driver;

    @BeforeMethod
    public void setUp() throws Exception {

        //start hub

        //start Node with Sauce Proxy, enable pass-through to SoD for non-handled capabilities


        //start Node for Firefox


        //this.driver = new FirefoxDriver();
        DesiredCapabilities capabillities = DesiredCapabilities.firefox();
        capabillities.setCapability("version", "12");
        capabillities.setCapability("platform", Platform.XP);
        capabillities.setCapability("sauceUser", "rossco_9_9");
        capabillities.setCapability("username", "rossco_9_9");
        capabillities.setCapability("access-key", "44f0744c-1689-4418-af63-560303cbb37b");
        capabillities.setCapability("sauceKey", "44f0744c-1689-4418-af63-560303cbb37b");
        this.driver = new RemoteWebDriver(
                new URL("http://localhost:4444/wd/hub"),
                capabillities);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        driver.quit();

        //stop nodes

        //stop hub
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
