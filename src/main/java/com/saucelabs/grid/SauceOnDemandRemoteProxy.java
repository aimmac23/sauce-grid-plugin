package com.saucelabs.grid;

import com.saucelabs.grid.services.SauceOnDemandRestAPIException;
import com.saucelabs.grid.services.SauceOnDemandService;
import com.saucelabs.grid.services.SauceOnDemandServiceImpl;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.grid.common.JSONConfigurationUtils;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.TestSession;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.internal.utils.CapabilityMatcher;
import org.openqa.grid.internal.utils.HtmlRenderer;
import org.openqa.grid.selenium.proxy.DefaultRemoteProxy;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author François Reynaud - Initial version of plugin
 * @author Ross Rowe - Additional functionality
 */
public class SauceOnDemandRemoteProxy extends DefaultRemoteProxy {

    /**
     * Send all Sauce OnDemand requests to ondemand.saucelabs.com/wd/hub.
     */
    public static String SAUCE_END_POINT = "/wd/hub";
    public static final String SAUCE_ONDEMAND_CONFIG_FILE = "sauce-ondemand.json";
    public static final String SAUCE_USER_NAME = "sauceUserName";
    public static final String SAUCE_ACCESS_KEY = "sauceAccessKey";
    private volatile boolean markUp = false;

    private String userName;
    private String accessKey;
    public static final String SAUCE_ONE = "sauce";
    private boolean shouldProxySauceOnDemand = false;
    private boolean shouldHandleUnspecifiedCapabilities;
    private static final String SAUCE_HANDLE_UNSPECIFIED_CAPABILITIES = "sauceHandleUnspecifiedCapabilities";
    private CapabilityMatcher capabilityHelper;

    private SauceOnDemandService service = new SauceOnDemandServiceImpl();

    public boolean shouldProxySauceOnDemand() {
        return shouldProxySauceOnDemand;
    }

    public SauceOnDemandRemoteProxy(RegistrationRequest req, Registry registry) {
        super(updateDesiredCapabilities(req), registry);

        //read configuration from sauce-ondemand.json
        //TODO include proxy id in json file

        //TODO run unauth Sauce REST API call to verify if Sauce is up?

        JSONObject sauceConfiguration = readConfigurationFromFile();
        try {
            if (sauceConfiguration != null) {
                this.userName = sauceConfiguration.getString(SAUCE_USER_NAME);
                this.accessKey = sauceConfiguration.getString(SAUCE_ACCESS_KEY);
                this.shouldHandleUnspecifiedCapabilities = sauceConfiguration.getBoolean(SAUCE_HANDLE_UNSPECIFIED_CAPABILITIES);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Object b = req.getConfiguration().get(SAUCE_ONE);
        if (b != null) {
            shouldProxySauceOnDemand = Boolean.valueOf(b.toString());
        }
    }

    private static RegistrationRequest updateDesiredCapabilities(RegistrationRequest request) {
        //TODO ensure thread safety
        List<DesiredCapabilities> capabilities = request.getCapabilities();
        for (DesiredCapabilities capability : capabilities) {
            //sauce end point should handle both SeleniumRC and WebDriver requests
            capability.setCapability(RegistrationRequest.PATH, SAUCE_END_POINT);
        }
        return request;
    }

    private JSONObject readConfigurationFromFile() {

        File file = new File(SAUCE_ONDEMAND_CONFIG_FILE);
        if (file.exists()) {
            return JSONConfigurationUtils.loadJSON(file.getName());
        }
        return null;
    }


    @Override
    public boolean hasCapability(Map<String, Object> requestedCapability) {
        if (shouldHandleUnspecifiedCapabilities/* && browser combination is supported by sauce labs*/) {
            return true;
        }
        return super.hasCapability(requestedCapability);
    }

    /**
     * @param requestedCapability
     * @return
     */
    @Override
    public TestSession getNewSession(Map<String, Object> requestedCapability) {

        //if no proxy can handle requested capability, and shouldHandleUnspecifiedCapabilities is set to true
        //(and the browser capabillitiy is supported by Sauce), create new desired capability that runs
        //against sauce
        try {
            this.markUp = service.isSauceLabUp();
            if (!markUp) {
                //log an error message?
            }
        } catch (SauceOnDemandRestAPIException e) {
            //error contacting sauce
            e.printStackTrace();
        }
        if ((shouldProxySauceOnDemand && markUp) || !shouldProxySauceOnDemand) {
            return super.getNewSession(requestedCapability);
        } else {
            return null;
        }
    }

    @Override
    public CapabilityMatcher getCapabilityHelper() {
        if (capabilityHelper == null) {
            capabilityHelper = new SauceOnDemandCapabilityMatcher(this);
        }


        return this.capabilityHelper;

    }

    @Override
    public HtmlRenderer getHtmlRender() {
        return new SauceOnDemandRenderer(this);
    }

    @Override
    public int compareTo(RemoteProxy o) {
        if (!(o instanceof SauceOnDemandRemoteProxy)) {
            throw new RuntimeException("cannot mix saucelab and not saucelab ones");
        } else {
            SauceOnDemandRemoteProxy other = (SauceOnDemandRemoteProxy) o;

            if (this.shouldProxySauceOnDemand) {
                System.out.println("return -1, sslone");
                return 1;
            } else if (other.shouldProxySauceOnDemand) {
                return -1;
            } else {
                int i = super.compareTo(o);
                System.out.println("return normal " + i);
                return i;
            }
        }
    }

    public boolean contains(SauceOnDemandCapabilities capabilities) throws JSONException {
        for (TestSlot slot : getTestSlots()) {
            SauceOnDemandCapabilities slc = new SauceOnDemandCapabilities(slot.getCapabilities());
            if (slc.equals(capabilities)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isMarkUp() {
        return markUp;
    }

    public synchronized void setMarkUp(boolean markUp) {
        this.markUp = markUp;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void writeConfigurationToFile() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(SAUCE_USER_NAME, getUserName());
            jsonObject.put(SAUCE_ACCESS_KEY, getAccessKey());
            jsonObject.put(SAUCE_HANDLE_UNSPECIFIED_CAPABILITIES, shouldHandleUnspecifiedCapabilities());
            //TODO handle selected browsers
            FileWriter file = new FileWriter(SAUCE_ONDEMAND_CONFIG_FILE);
            file.write(jsonObject.toString());
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean shouldHandleUnspecifiedCapabilities() {
        return shouldHandleUnspecifiedCapabilities;
    }

    public void setShouldHandleUnspecifiedCapabilities(boolean shouldHandleUnspecifiedCapabilities) {
        this.shouldHandleUnspecifiedCapabilities = shouldHandleUnspecifiedCapabilities;
    }

    public URL getRemoteHost() {
        if (shouldHandleUnspecifiedCapabilities()) {
            try {
//                return new URL("http://ondemand.saucelabs.com:80");
                return new URL("http://" + userName + ":" + accessKey + "@ondemand.saucelabs.com:80");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return remoteHost;
    }

    public void beforeCommand(TestSession session, HttpServletRequest request, HttpServletResponse response) {
        session.put("lastCommand", request.getMethod() + " - " + request.getPathInfo() + " executed.");

    }
}
