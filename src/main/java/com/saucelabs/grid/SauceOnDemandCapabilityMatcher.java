package com.saucelabs.grid;

import org.openqa.grid.internal.utils.DefaultCapabilityMatcher;

import java.util.Map;

/**
 * @author Ross Rowe
 */
public class SauceOnDemandCapabilityMatcher extends DefaultCapabilityMatcher {


    private SauceOnDemandRemoteProxy proxy;

    public SauceOnDemandCapabilityMatcher(SauceOnDemandRemoteProxy proxy) {
        super();
        this.proxy = proxy;
    }

    @Override
    public boolean matches(Map<String, Object> nodeCapability, Map<String, Object> requestedCapability) {
        if (proxy.shouldHandleUnspecifiedCapabilities()) {
            return true;
        } else {
            return super.matches(nodeCapability, requestedCapability);
        }
    }
}
