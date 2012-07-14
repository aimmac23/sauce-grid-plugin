package com.saucelabs.grid;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.grid.common.JSONConfigurationUtils;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.TestSession;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.internal.utils.HtmlRenderer;
import org.openqa.grid.selenium.proxy.DefaultRemoteProxy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * @author François Reynaud - Initial version of plugin
 * @author Ross Rowe - Additional functionality
 */
public class SauceOnDemandRemoteProxy extends DefaultRemoteProxy {

    public static String SAUCE_END_POINT = "http://ondemand.saucelabs.com:80/wd/hub";
    public static final String SAUCE_ONDEMAND_CONFIG_FILE = "sauce-ondemand.json";
    public static final String SAUCE_USER_NAME = "sauceUserName";
    public static final String SAUCE_ACCESS_KEY = "sauceAccessKey";
    private volatile boolean markUp = false;

    private String userName;
    private String accessKey;
    public static final String SAUCE_ONE = "sauce";
    private boolean shouldProxySauceOnDemand = false;

    public boolean shouldProxySauceOnDemand() {
        return shouldProxySauceOnDemand;
    }

    public SauceOnDemandRemoteProxy(RegistrationRequest req, Registry registry) {
        super(req, registry);
        //read configuration from sauce-ondemand.json
        JSONObject sauceConfiguration = readConfigurationFromFile();
        try {
            if (sauceConfiguration != null) {
                this.userName = sauceConfiguration.getString(SAUCE_USER_NAME);
                this.accessKey = sauceConfiguration.getString(SAUCE_ACCESS_KEY);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Object b = req.getConfiguration().get(SAUCE_ONE);
        if (b != null) {
            shouldProxySauceOnDemand = Boolean.valueOf(b.toString());
        }
    }

    private JSONObject readConfigurationFromFile() {

        File file = new File(SAUCE_ONDEMAND_CONFIG_FILE);
        if (file.exists()) {
            return JSONConfigurationUtils.loadJSON(file.getName());
        }
        return null;
    }

    @Override
    public TestSession getNewSession(Map<String, Object> requestedCapability) {
        if ((shouldProxySauceOnDemand && markUp) || !shouldProxySauceOnDemand) {
            return super.getNewSession(requestedCapability);
        } else {
            return null;
        }
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
}
