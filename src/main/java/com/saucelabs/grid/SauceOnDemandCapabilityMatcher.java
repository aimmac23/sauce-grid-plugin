package com.saucelabs.grid;

import org.openqa.grid.internal.utils.DefaultCapabilityMatcher;
import org.openqa.selenium.Platform;

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
//            if (!result) {
//                result = true;
//                //the node capability will be created using the node OS as the platform
//                //compare the capabilities, ignoring the platform
//                for (String key : requestedCapability.keySet()) {
//                    if (requestedCapability.get(key) != null && !(key.equals("platform"))) {
//                        if (!requestedCapability.get(key).equals(nodeCapability.get(key))) {
//                            result = false;
//                        }
//                    }
//                }
//            }
//            return result;
        }
    }
}
