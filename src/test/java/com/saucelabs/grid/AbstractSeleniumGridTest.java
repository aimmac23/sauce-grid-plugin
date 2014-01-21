package com.saucelabs.grid;

import org.junit.After;
import org.junit.Before;
import org.openqa.grid.web.Hub;

import com.saucelabs.grid.utils.TestHelper;

public abstract class AbstractSeleniumGridTest {
	
	public static final int HUB_PORT = 7777;
	protected Hub hub; 
	
	@Before
	public void setup() throws Exception {
		// we'll always need a hub
		hub = TestHelper.getHub(HUB_PORT);
	}
	
	@After
	public void tearDown() throws Exception {
		hub.stop();
	}
	

}
