package com.saucelabs.grid.internal;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.openqa.grid.internal.TestSlot;

/**
 * A CGLib interceptor which makes the Selenium Console web page believe that
 * test slots in use have exactly the capabilities requested, to keep track
 * of what the Hub/Sauce is up to.
 * 
 * Previously the actual test slot allocated could claim to be a completely
 * different browser, which is confusing.
 * 
 * @author Alasdair Macmillan
 *
 */
public class TestSlotCapabilityInterceptor implements MethodInterceptor {
	
	private TestSlot originalObject;

	public TestSlotCapabilityInterceptor(TestSlot originalObject) {
		this.originalObject = originalObject;
	}

	@Override
	public Object intercept(Object arg0, Method method, Object[] arg2,
			MethodProxy arg3) throws Throwable {
		
		if(method.getName().equals("getCapabilities") && originalObject.getSession() != null) {
			// Note: This is under the assumption that SauceLabs is giving us what we asked for
			return originalObject.getSession().getRequestedCapabilities();
		}
		
		return arg3.invoke(originalObject, arg2);
	}
}