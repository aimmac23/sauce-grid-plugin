package com.saucelabs.grid.internal;

import java.util.HashMap;

import org.openqa.grid.common.SeleniumProtocol;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.TestSlot;

/**
 * This class probably isn't necessary, but it reduces the number of parameters
 * CGLib has to pass to the constructor
 * 
 * @author Alasdair Macmillan
 *
 */
public class TestSlotWithMinimalConstructor extends TestSlot {

	public TestSlotWithMinimalConstructor(RemoteProxy proxy) {
		super(proxy, SeleniumProtocol.WebDriver, "", new HashMap<String, Object>());
	}
}