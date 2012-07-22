package com.saucelabs.grid;

import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.web.servlet.beta.WebProxyHtmlRendererBeta;
import org.openqa.grid.web.utils.BrowserNameUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.List;
import java.util.Map;

public class SauceOnDemandRenderer extends WebProxyHtmlRendererBeta {


    private SauceOnDemandRemoteProxy sauceProxy;

    public SauceOnDemandRenderer(RemoteProxy proxy) {
        super(proxy);
        this.sauceProxy = (SauceOnDemandRemoteProxy) proxy;
    }

    public String renderSummary() {
        StringBuilder builder = new StringBuilder();
        builder.append("<fieldset>");
        builder.append("<legend>").append(sauceProxy.getClass().getSimpleName()).append("</legend>");
        builder.append("listening on ").append(sauceProxy.getRemoteHost());

        if (sauceProxy.shouldProxySauceOnDemand()) {
            builder.append("<br/> I'm the sauce lab one!<br/> ");
            builder.append("<br/> I'm ");
            if (sauceProxy.isMarkUp()) {
                builder.append("up <a href='/grid/admin/SauceOnDemandAdminServlet/test?state=down&id="
                        + sauceProxy.getId() + "'>mark down</a> ");
            } else {
                builder.append("down <a href='/grid/admin/SauceOnDemandAdminServlet/test?state=up&id="
                        + sauceProxy.getId() + "'>mark up</a> ");
            }
        }
        builder.append("<a href='/grid/admin/SauceOnDemandAdminServlet/admin?id=" + sauceProxy.getId()
                + "' >admin</a></br>");

        builder.append("<br />");
        if (sauceProxy.getTimeOut() > 0) {
            int inSec = sauceProxy.getTimeOut() / 1000;
            builder.append("test session time out after ").append(inSec).append(" sec.<br />");
        }

        builder.append("Supports up to <b>").append(sauceProxy.getMaxNumberOfConcurrentTestSessions())
                .append("</b> concurrent tests from: <br />");

        builder.append("<ul>");
        List<TestSlot> slots = sauceProxy.getTestSlots();
        int max = sauceProxy.getMaxNumberOfConcurrentTestSessions();
        for (int i = 0; i < slots.size(); i += max) {
            TestSlot slot = slots.get(i);
            builder.append("<li>" + slot.getCapabilities().get(SauceOnDemandCapabilities.NAME));

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
        builder.append("</ul>");

        builder.append("</fieldset>");

        return builder.toString();
    }

    private String getIcon(Map<String, Object> capabilities) {
        return BrowserNameUtils.getConsoleIconPath(new DesiredCapabilities(capabilities),
                sauceProxy.getRegistry());
    }


}
