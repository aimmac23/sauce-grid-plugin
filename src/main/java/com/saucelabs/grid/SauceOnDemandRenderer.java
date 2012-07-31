package com.saucelabs.grid;

import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.web.servlet.beta.WebProxyHtmlRendererBeta;
import org.openqa.grid.web.utils.BrowserNameUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.List;
import java.util.Map;

/**
 * Handles rendering the HTML fragment used to display the configuration details of the Sauce Proxy.
 *
 * @author Ross Rowe
 */
public class SauceOnDemandRenderer extends WebProxyHtmlRendererBeta {

    private SauceOnDemandRemoteProxy sauceProxy;

    public SauceOnDemandRenderer(RemoteProxy proxy) {
        super(proxy);
        this.sauceProxy = (SauceOnDemandRemoteProxy) proxy;
    }

    public String renderSummary() {
        StringBuilder builder = new StringBuilder();
        builder.append("<div class='proxy'>");
        builder.append("<fieldset>");
        builder.append("<legend class='proxyname'>").append(sauceProxy.getClass().getSimpleName()).append("</legend>");
        String platform = getPlatform(sauceProxy);

        builder.append("<p class='proxyid'>id : ");
        builder.append(sauceProxy.getId());
        builder.append(", OS : " + platform + "</p>");

        if (sauceProxy.shouldHandleUnspecifiedCapabilities())
            builder.append("Listening on ").append(sauceProxy.getNodeHost());

        if (sauceProxy.shouldProxySauceOnDemand()) {
            builder.append("<br/> Proxying to Sauce On Demand is enabled <br/> ");
        } else {
            builder.append("<br/> Proxying to Sauce On Demand is disabled <br/> ");
        }

        builder.append("<br />");
        if (sauceProxy.getTimeOut() > 0) {
            int inSec = sauceProxy.getTimeOut() / 1000;
            builder.append("Test sessions will time out after ").append(inSec).append(" sec.<br />");
        }

        builder.append("Supports up to <b>").append(sauceProxy.getMaxNumberOfConcurrentTestSessions())
                .append("</b> concurrent tests<br />");

        if (sauceProxy.shouldHandleUnspecifiedCapabilities()) {
            builder.append("Unspecified capabililities will be forwarded to Sauce OnDemand");
        } else {

            int max = sauceProxy.getMaxNumberOfConcurrentTestSessions();
            if (max > 0) {
                builder.append("<ul>");
                List<TestSlot> slots = sauceProxy.getTestSlots();
                for (int i = 0; i < slots.size(); i += max) {
                    TestSlot slot = slots.get(i);
                    Object capability = slot.getCapabilities().get(SauceOnDemandCapabilities.NAME);
                    if (capability != null) {
                        builder.append("<li>" + capability);

                        int used = 0;
                        for (int j = 0; j < i + max; j++) {
                            if (j >= slots.size()) {
                                break;
                            }
                            TestSlot slo = slots.get(j);
                            if (slo.getSession() != null) {
                                used++;
                            }
                        }
                        if (used != 0) {
                            builder.append("(running : " + used + ")");
                        }

                        builder.append("</li>");
                    }

                }
                builder.append("</ul>");
            }
        }

        builder.append("<br/><a href='/grid/admin/SauceOnDemandAdminServlet/admin?id=" + sauceProxy.getId()
                + "' >Configure Proxy</a></br>");

        builder.append("</fieldset>");
        builder.append("</div>");

        return builder.toString();
    }

    private String getIcon(Map<String, Object> capabilities) {
        return BrowserNameUtils.getConsoleIconPath(new DesiredCapabilities(capabilities),
                sauceProxy.getRegistry());
    }


}
