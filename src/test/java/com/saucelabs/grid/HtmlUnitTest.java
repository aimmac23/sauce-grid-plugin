package com.saucelabs.grid;

import static junit.framework.Assert.assertEquals;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.utils.SelfRegisteringRemote;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class HtmlUnitTest extends AbstractSeleniumGridTest {

    private RemoteWebDriver driver;
	private SelfRegisteringRemote node;

    @Before
    public void setUp() throws Exception {
    	int nodePort = PortProber.findFreePort();
    	RegistrationRequest request = buildRegistrationRequest(nodePort);
    	request.addDesiredCapability(DesiredCapabilities.htmlUnit());
    	
    	node = new SelfRegisteringRemote(request);
    	node.startRemoteServer();
    	
    	node.startRegistrationProcess();
    	
    	DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
        this.driver = new RemoteWebDriver(
                new URL(String.format("http://localhost:%s/wd/hub", 
                		AbstractSeleniumGridTest.HUB_PORT)),
                capabilities);    	
    }
    
    @Test
    public void amazonTest() {
    	driver.get("http://www.amazon.com/");
    	
        assertEquals(
                "Amazon.com: Online Shopping for Electronics, Apparel, Computers, Books, DVDs & more",
                driver.getTitle());
        
        String sessionKey = driver.getSessionId().toString();
        
    }
    
    
    @Override
	@After
    public void tearDown() throws Exception {
    	
    	if(node != null) {
    		node.stopRemoteServer();
    	}
    	
    	if(driver != null) {
    		driver.quit();	
    	}
    	super.tearDown();
    }
    
    private RegistrationRequest buildRegistrationRequest(int nodePort) {
        RegistrationRequest req = RegistrationRequest.build("-role", "node" , "-host", "localhost");

        req.getConfiguration().put(RegistrationRequest.PORT, nodePort);

        req.getConfiguration().put(RegistrationRequest.HUB_HOST, hub.getHost());
        req.getConfiguration().put(RegistrationRequest.HUB_PORT, hub.getPort());
        
        String url =
                "http://" + req.getConfiguration().get(RegistrationRequest.HOST) + ":"
                    + req.getConfiguration().get(RegistrationRequest.PORT);
            req.getConfiguration().put(RegistrationRequest.REMOTE_HOST, url);

        
        // we don't want silly defaults
        req.getCapabilities().clear();

        
        return req;

    }
}
