package com.saucelabs.grid;

import com.saucelabs.grid.services.SauceOnDemandRestAPIException;
import com.saucelabs.grid.services.SauceOnDemandService;
import com.saucelabs.grid.services.SauceOnDemandServiceImpl;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.common.SeleniumProtocol;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.web.servlet.RegistryBasedServlet;
import org.openqa.selenium.remote.DesiredCapabilities;

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
    public static final String WEB_DRIVER_CAPABILITIES = "webDriverCapabilities";
    public static final String SELENIUM_CAPABILITIES = "seleniumCapabilities";
    private SauceOnDemandService service = new SauceOnDemandServiceImpl();
    private final BrowsersCache webDriverBrowsers;
    private final BrowsersCache seleniumBrowsers;
    private static final String SAUCE_USER_NAME = "sauceUserName";
    private static final String SAUCE_ACCESS_KEY = "sauceAccessKey";
    private static final String SAUCE_HANDLE_UNSPECIFIED = "sauceHandleUnspecified";

    public SauceOnDemandAdminServlet() throws SauceOnDemandRestAPIException {
        this(null);
    }


    public SauceOnDemandAdminServlet(Registry registry) throws SauceOnDemandRestAPIException {
        super(registry);
        webDriverBrowsers = new BrowsersCache(service.getWebDriverBrowsers());
        seleniumBrowsers = new BrowsersCache(service.getSeleniumBrowsers());
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

//            RequestDispatcher view = req.getRequestDispatcher("/jsp/sauceAdmin.jsp");
//            view.forward(req, resp);
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
        String[] supported = req.getParameterValues(WEB_DRIVER_CAPABILITIES);
        List<SauceOnDemandCapabilities> caps = new ArrayList<SauceOnDemandCapabilities>();
        if (supported != null) {
            for (String md5 : supported) {
                caps.add(webDriverBrowsers.get(md5));
            }
        }
        getRegistry().removeIfPresent(proxy);

        RegistrationRequest sauceRequest = proxy.getOriginalRegistrationRequest();
        // re-create the test slots with the new capabilities.
        sauceRequest.getCapabilities().clear();

        String userName = req.getParameter(SAUCE_USER_NAME);
        String accessKey = req.getParameter(SAUCE_ACCESS_KEY);
        String max = req.getParameter(RegistrationRequest.MAX_SESSION);
        boolean handleUnspecified = req.getParameter(SAUCE_HANDLE_UNSPECIFIED) != null || !(req.getParameter(SAUCE_HANDLE_UNSPECIFIED).equals(""));
        int m = Integer.parseInt(max);

        sauceRequest.getConfiguration().put(RegistrationRequest.MAX_SESSION, m);
        for (SauceOnDemandCapabilities cap : caps) {
            DesiredCapabilities c = new DesiredCapabilities(cap.asMap());
            c.setCapability(RegistrationRequest.MAX_INSTANCES, m);
            c.setCapability("user-name", userName);
            c.setCapability("access-key", accessKey);
            sauceRequest.getCapabilities().add(c);
        }

        //write selected browsers/auth details to sauce-ondemand.json
        proxy.setUserName(userName);
        proxy.setAccessKey(accessKey);
        proxy.setShouldHandleUnspecifiedCapabilities(handleUnspecified);
        proxy.writeConfigurationToFile();
        SauceOnDemandRemoteProxy newProxy = new SauceOnDemandRemoteProxy(sauceRequest, getRegistry());
        getRegistry().add(newProxy);
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
            b.append("<head>");
            b.append("<title>Sauce OnDemand Grid Configuration</title>");
            b.append("</head>");
            b.append("<form action='/grid/admin/SauceOnDemandAdminServlet/").append(UPDATE_BROWSERS).append("' method='POST'>");
            b.append("<fieldset>");
            b.append("<legend accesskey=c>Sauce OnDemand Configuration</legend>");
            b.append("<label for=\"maxSessions\">Max parallel sessions</label> : <input type='text' name='").
                    append(RegistrationRequest.MAX_SESSION).
                    append("' id='").append(RegistrationRequest.MAX_SESSION).
                    append("' value='").append(p.getMaxNumberOfConcurrentTestSessions()).
                    append("' />");

            b.append("<label for='").append(SAUCE_USER_NAME).append("'>User Name</label> : <input type='text' name='").
                    append(SAUCE_USER_NAME).
                    append("' id='").append(SAUCE_USER_NAME).
                    append("' value='").append(p.getUserName()).
                    append("' />");

            b.append("<label for='").append(SAUCE_ACCESS_KEY).append("'>Access Key</label> : <input type='text' name='").
                    append(SAUCE_ACCESS_KEY).append("' id='").append(SAUCE_ACCESS_KEY).
                    append("' size='50' value='").append(p.getAccessKey()).append("' />");

            b.append("<label for='").append(SAUCE_HANDLE_UNSPECIFIED).append("'>Handle All Unspecified Capabilities?</label>");
            b.append("<input type='checkbox' name='").append(SAUCE_HANDLE_UNSPECIFIED).
                    append("' id='").append(SAUCE_HANDLE_UNSPECIFIED).append("'");
            if (p.shouldHandleUnspecifiedCapabilities()) {
                b.append(" checked='checked' ");
            }
            b.append("value='Handle All Unspecified Capabilities?'/>");
            b.append("</fieldset>");


            b.append("<fieldset>");
            b.append("<legend accesskey=c>Sauce OnDemand Browsers (WebDriver)</legend>");
            b.append("<select name='").append(WEB_DRIVER_CAPABILITIES).append("' multiple='multiple'>");
            for (SauceOnDemandCapabilities cap : webDriverBrowsers.getAllBrowsers()) {

                b.append("<option value='").append(cap.getMD5()).append("'>");
                b.append(cap);
                b.append("</option>");
            }
            b.append("</select>");
            b.append("</fieldset>");

            b.append("<fieldset>");
            b.append("<legend accesskey=c>Sauce OnDemand Browsers (Selenium RC)</legend>");
            b.append("<select name='").append(SELENIUM_CAPABILITIES).append("' multiple='multiple'>");
            for (SauceOnDemandCapabilities cap : seleniumBrowsers.getAllBrowsers()) {

                b.append("<option value='").append(cap.getMD5()).append("'>");
                b.append(cap);
                b.append("</option>");
            }
            b.append("</select>");
            b.append("</fieldset>");
            b.append("<input type='hidden' name='id' value='").append(p.getId()).append("' />");
            b.append("<input type='submit' value='save' />");

            b.append("</form>");

            b.append("</html>");
        } catch (Exception e) {
            b.append("Error : " + e.getMessage());
        }

        return b.toString();

    }

}