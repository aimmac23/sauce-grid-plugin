package com.saucelabs.grid;

import org.json.JSONException;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.TestSession;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.internal.utils.HtmlRenderer;
import org.openqa.grid.selenium.proxy.DefaultRemoteProxy;

import java.util.Map;

/**
 * @author François Reynaud - Initial version of plugin
 * @author Ross Rowe - Additional functionality
 */
public class SauceOnDemandRemoteProxy extends DefaultRemoteProxy {

    public static String SAUCE_END_POINT = "http://ondemand.saucelabs.com:80/wd/hub";
    private volatile boolean markUp = false;

    private String userName;
    private String accessKey;
    public static final String SAUCE_ONE = "sauce";
    private boolean isSLOne = false;

    public boolean isTheSauceLabProxy() {
        return isSLOne;
    }

    public SauceOnDemandRemoteProxy(RegistrationRequest req, Registry registry) {
        super(req, registry);
        Object b = req.getConfiguration().get(SAUCE_ONE);
        if (b != null) {
            isSLOne = Boolean.valueOf(b.toString());
        }
    }

    @Override
    public TestSession getNewSession(Map<String, Object> requestedCapability) {
        if ((isSLOne && markUp) || !isSLOne) {
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

            if (this.isSLOne) {
                System.out.println("return -1, sslone");
                return 1;
            } else if (other.isSLOne) {
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
}
