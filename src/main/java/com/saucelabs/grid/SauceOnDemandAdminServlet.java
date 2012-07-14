package com.saucelabs.grid;

import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.common.SeleniumProtocol;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.web.servlet.RegistryBasedServlet;
import org.openqa.selenium.remote.DesiredCapabilities;
import com.saucelabs.grid.services.SauceOnDemandRestAPIException;
import com.saucelabs.grid.services.SauceOnDemandService;
import com.saucelabs.grid.services.SauceOnDemandServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author François Reynaud - Initial version of plugin
 * @author Ross Rowe - Additional functionality
 */
public class SauceOnDemandAdminServlet extends RegistryBasedServlet {

    private static final String UPDATE_BROWSERS = "updateSupportedBrowsers";
    private SauceOnDemandService service = new SauceOnDemandServiceImpl();
    private final BrowsersCache browsers;
    private static final String SAUCE_USER_NAME = "sauceUserName";
    private static final String SAUCE_ACCESS_KEY = "sauceAccessKey";

    public SauceOnDemandAdminServlet() throws SauceOnDemandRestAPIException {
        this(null);
    }


    public SauceOnDemandAdminServlet(Registry registry) throws SauceOnDemandRestAPIException {
        super(registry);
        browsers = new BrowsersCache(service.getBrowsers());
        //todo read selected browsers/auth details from sauce-ondemand.json
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        String id = req.getParameter("id");
        SauceOnDemandRemoteProxy p = getProxy(id);
        if (req.getPathInfo().endsWith(UPDATE_BROWSERS)) {
            updateBrowsers(req, resp, p);
            resp.sendRedirect("/grid/console");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        String id = req.getParameter("id");
        SauceOnDemandRemoteProxy p = getProxy(id);

        if (req.getPathInfo().endsWith("/admin")) {
            String page = renderAdminPage(p);
            resp.getWriter().print(page);
            resp.getWriter().close();
            return;
        }


        String state = req.getParameter("state");
        if ("up".equals(state)) {
            p.setMarkUp(true);
        } else {
            p.setMarkUp(false);
        }
        resp.sendRedirect("/grid/console");
    }

    private void updateBrowsers(HttpServletRequest req, HttpServletResponse resp,
                                SauceOnDemandRemoteProxy proxy) {
        String[] supported = req.getParameterValues("supportedCapabilities");
        List<SauceOnDemandCapabilities> caps = new ArrayList<SauceOnDemandCapabilities>();
        if (supported != null) {
            for (String md5 : supported) {
                caps.add(browsers.get(md5));
            }
        }
        getRegistry().removeIfPresent(proxy);

        RegistrationRequest sauceRequest = proxy.getOriginalRegistrationRequest();
        // re-create the test slots with the new capabilities.
        sauceRequest.getCapabilities().clear();

        String userName = req.getParameter(SAUCE_USER_NAME);
        String accessKey = req.getParameter(SAUCE_ACCESS_KEY);
        String max = req.getParameter(RegistrationRequest.MAX_SESSION);
        int m = Integer.parseInt(max);

        sauceRequest.getConfiguration().put(RegistrationRequest.MAX_SESSION, m);
        for (SauceOnDemandCapabilities cap : caps) {
            DesiredCapabilities c = new DesiredCapabilities(cap.asMap());
            c.setCapability(RegistrationRequest.MAX_INSTANCES, m);
            c.setCapability("user-name", userName);
            c.setCapability("access-key", accessKey);
            sauceRequest.getCapabilities().add(c);
        }

        SauceOnDemandRemoteProxy newProxy = new SauceOnDemandRemoteProxy(sauceRequest, getRegistry());
        getRegistry().add(newProxy);

        //todo write selected browsers/auth details to sauce-ondemand.json


    }


    private List<TestSlot> createSlots(SauceOnDemandRemoteProxy proxy, SauceOnDemandCapabilities cap) {
        List<TestSlot> slots = new ArrayList<TestSlot>();
        for (int i = 0; i < proxy.getMaxNumberOfConcurrentTestSessions(); i++) {
            slots.add(new TestSlot(proxy, SeleniumProtocol.WebDriver,
                    SauceOnDemandRemoteProxy.SAUCE_END_POINT, cap.asMap()));
        }
        return slots;
    }


    private SauceOnDemandRemoteProxy getProxy(String id) {
        return (SauceOnDemandRemoteProxy) getRegistry().getProxyById(id);
    }

    private String renderAdminPage(SauceOnDemandRemoteProxy p) {

        StringBuilder b = new StringBuilder();

        try {
            b.append("<html>");

            b.append("<form action='/grid/admin/SauceLabAdminServlet/" + UPDATE_BROWSERS
                    + "' method='POST'>");

            b.append("max sessions in parallel on sauce : <input type='text' name='"
                    + RegistrationRequest.MAX_SESSION + "' value='"
                    + p.getMaxNumberOfConcurrentTestSessions() + "' />");

            b.append("User Name : <input type='text' name='"
                    + SAUCE_USER_NAME + "' value='"
                    + p.getUserName() + "' />");

            b.append("Access Key : <input type='text' name='"
                    + SAUCE_ACCESS_KEY + "' value='"
                    + p.getAccessKey() + "' />");

            b.append("<ul>");
            for (SauceOnDemandCapabilities cap : browsers.getAllBrowsers()) {

                b.append("<li>");
                b.append("<input type='checkbox' name='supportedCapabilities'");
                if (p.contains(cap)) {
                    b.append(" checked='checked' ");
                }
                b.append("value='" + cap.getMD5() + "'>");
                b.append(cap);
                b.append("</input>");
                b.append("</li>");
            }
            b.append("</ul>");

            b.append("<input type='hidden' name='id' value='" + p.getId() + "' />");
            b.append("<input type='submit' value='save' />");

            b.append("</form>");

            b.append("</html>");
        } catch (Exception e) {
            b.append("Error : " + e.getMessage());
        }

        return b.toString();

    }

}
