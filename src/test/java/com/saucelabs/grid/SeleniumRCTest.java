package com.saucelabs.grid;

import com.thoughtworks.selenium.DefaultSelenium;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.grid.internal.utils.SelfRegisteringRemote;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.testng.Assert.assertEquals;

/**
 * @author Ross Rowe
 */
public class SeleniumRCTest extends AbstractSeleniumGridTest {

    private DefaultSelenium selenium;
	private int firefoxNodePort;
	private SelfRegisteringRemote firefoxNode;
	private SelfRegisteringRemote sauceNode;

    @Before
    public void setUp() throws Exception {
    	firefoxNodePort = PortProber.findFreePort();
    	
    	DesiredCapabilities capabilities = DesiredCapabilities.firefox();
    	// We need to signal that this is Selenium RC
    	capabilities.setCapability("seleniumProtocol", "Selenium");
    	
    	// Unfortunately, Selenium RC doesn't recognise htmlunit nodes. so
    	// we must create a firefox node instead
    	firefoxNode = createSeleniumNode(firefoxNodePort, capabilities);
    	startNode(firefoxNode);
    }

    @Test
    public void testSelenumRCWorksLocally() throws Exception {
    	
        selenium = new DefaultSelenium(
                "localhost",
                HUB_PORT,
                "firefox",
                "http://www.amazon.com/");
        selenium.start();
        
        this.selenium.open("http://www.amazon.com");
        assertEquals(AMAZON_TITLE, this.selenium.getTitle());
    }
    
    /**
     * The test is somewhat conclusive - it does not. Selenium seems to expect the browser
     * string to contain a blob of JSON which contains things like username/password information.
     * 
     * Obviously this would probably make the grid not-work, so something needs fixing in the SauceLabs proxy.
     * 
     * @throws Exception
     */
    @Test
    @Ignore
    public void testSeleniumRCWorksWithSauce() throws Exception {
    	int sauceNodePort = PortProber.findFreePort();
    	
    	DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
    	// We need to signal that this is Selenium RC
    	capabilities.setCapability("seleniumProtocol", "Selenium");
    	
    	sauceNode = createSauceNode(sauceNodePort, capabilities);
    	
    	startNode(sauceNode);
    	
    	
        selenium = new DefaultSelenium(
                "localhost",
                HUB_PORT,
                "*chrome",
                "http://www.amazon.com/");
        selenium.start();
        

        this.selenium.open("http://www.amazon.com");
        assertEquals(AMAZON_TITLE, this.selenium.getTitle());
    }

    @Override
	@After
    public void tearDown() throws Exception {
        if(selenium != null) {
        	this.selenium.stop();
        }
        
        if(sauceNode != null) {
        	sauceNode.stopRemoteServer();
        }
        
        firefoxNode.stopRemoteServer();
        
        super.tearDown();
    }

}
