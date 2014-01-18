package com.saucelabs.grid;

import java.util.Map;

import org.openqa.grid.internal.utils.DefaultCapabilityMatcher;

/**
 * Handles the matching of requested capabilities with those handled by the Sauce Node.
 *
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
