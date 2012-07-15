package com.saucelabs.grid;

import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.internal.utils.HtmlRenderer;
import org.openqa.grid.web.utils.BrowserNameUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.List;
import java.util.Map;

public class SauceOnDemandRenderer implements HtmlRenderer {

  private SauceOnDemandRemoteProxy proxy;

  public SauceOnDemandRenderer() {}

  public SauceOnDemandRenderer(SauceOnDemandRemoteProxy proxy) {
    this.proxy = proxy;
  }

  public String renderSummary() {
    StringBuilder builder = new StringBuilder();
    builder.append("<fieldset>");
    builder.append("<legend>").append(proxy.getClass().getSimpleName()).append("</legend>");
    builder.append("listening on ").append(proxy.getRemoteHost());

    if (proxy.shouldProxySauceOnDemand()) {
      builder.append("<br/> I'm the sauce lab one!<br/> ");
      builder.append("<br/> I'm ");
      if (proxy.isMarkUp()) {
        builder.append("up <a href='/grid/admin/SauceOnDemandAdminServlet/test?state=down&id="
            + proxy.getId() + "'>mark down</a> ");
      } else {
        builder.append("down <a href='/grid/admin/SauceOnDemandAdminServlet/test?state=up&id="
            + proxy.getId() + "'>mark up</a> ");
      }
    }
    builder.append("<a href='/grid/admin/SauceOnDemandAdminServlet/admin?id=" + proxy.getId()
        + "' >admin</a></br>");

    builder.append("<br />");
    if (proxy.getTimeOut() > 0) {
      int inSec = proxy.getTimeOut() / 1000;
      builder.append("test session time out after ").append(inSec).append(" sec.<br />");
    }

    builder.append("Supports up to <b>").append(proxy.getMaxNumberOfConcurrentTestSessions())
        .append("</b> concurrent tests from: <br />");

    builder.append("<ul>");
    List<TestSlot> slots = proxy.getTestSlots();
    int max = proxy.getMaxNumberOfConcurrentTestSessions();
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
        proxy.getRegistry());
  }



}
