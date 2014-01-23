package com.saucelabs.grid;

import static junit.framework.Assert.assertEquals;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.grid.internal.utils.SelfRegisteringRemote;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * @author Ross Rowe
 */
public class SauceWebDriverTest extends AbstractSeleniumGridTest {

    private WebDriver driver;
	private SelfRegisteringRemote sauceNode;

    @Before
    public void setUp() throws Exception {
    	
    	// XXX: deliberately specifying IE here. Should fix the configuration later. 
    	sauceNode = createSauceNode(PortProber.findFreePort(), DesiredCapabilities.internetExplorer());
    	sauceNode.getConfiguration().put(SauceOnDemandRemoteProxy.SAUCE_HANDLE_UNSPECIFIED_CAPABILITIES, "true");
    	startNode(sauceNode);

        DesiredCapabilities capabillities = DesiredCapabilities.firefox();
        capabillities.setCapability("version", "24");
        capabillities.setCapability("platform", Platform.WIN8);
        capabillities.setCapability("name", "Amazon Grid Test");
        this.driver = new RemoteWebDriver(
                new URL(String.format("http://localhost:%s/wd/hub", HUB_PORT)),
                capabillities);
    }

    @Override
	@After
    public void tearDown() throws Exception {
    	if(driver != null) {
    		driver.quit();	
    	}
        
    	if(sauceNode != null) {
    		sauceNode.stopRemoteServer();	
    	}
        
        super.tearDown();
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
