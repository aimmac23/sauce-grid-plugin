package com.saucelabs.grid;

import static junit.framework.Assert.assertEquals;

import java.net.URL;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.grid.internal.utils.SelfRegisteringRemote;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class HtmlUnitTest extends AbstractSeleniumGridTest {
	
	private static final String AMAZON_TITLE = 
			"Amazon.com: Online Shopping for Electronics, Apparel, Computers, Books, DVDs & more";

    private RemoteWebDriver driver;
	private SelfRegisteringRemote node;
	private int nodePort;

    @Before
    public void setUp() throws Exception {
    	nodePort = PortProber.findFreePort();
    	
    	node = createSeleniumNode(nodePort, DesiredCapabilities.htmlUnit());
    	
    	DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
        this.driver = new RemoteWebDriver(
                new URL(String.format("http://localhost:%s/wd/hub", 
                		AbstractSeleniumGridTest.HUB_PORT)),
                capabilities);    	
    }
    
    @Test
    public void amazonTest() throws JSONException {
    	driver.get("http://www.amazon.com/");
    	
        assertEquals(AMAZON_TITLE, driver.getTitle());
        
        assertNodeHandlingSession(driver, nodePort);
    }
    
    
    @Override
	@After
    public void tearDown() throws Exception {

    	if(driver != null) {
    		driver.quit();	
    	}
    	
    	if(node != null) {
    		node.stopRemoteServer();
    	}
    	
    	super.tearDown();
    }
}
