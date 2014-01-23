package com.saucelabs.grid;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.io.FileNotFoundException;
import java.net.URL;

import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.grid.internal.utils.SelfRegisteringRemote;
import org.openqa.selenium.Platform;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.saucerest.SauceREST;

/**
 * To get this test to pass you need to have a file in your home directory 
 * called ".sauce-ondemand", containing two properties: "username" and "key"
 *
 */
public class SauceOnDemandRemoteProxyTest extends AbstractSeleniumGridTest {

    private SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication();

    /**
     * I have no idea what this is meant to be testing
     * @throws Exception
     */
    @Test(expected = FileNotFoundException.class)
    @Ignore
    public void localCapabilities() throws Exception {
        SelfRegisteringRemote remote = createSauceNode(PortProber.findFreePort(), DesiredCapabilities.firefox());
        startNode(remote);
        RemoteWebDriver driver = null;
        try {
            DesiredCapabilities ff = DesiredCapabilities.firefox();
            driver = new RemoteWebDriver(new URL(hub.getUrl() + "/wd/hub"), ff);
            String sessionId = driver.getSessionId().toString();
            driver.get("http://www.amazon.com/");
            assertEquals(
                    "Amazon.com: Online Shopping for Electronics, Apparel, Computers, Books, DVDs & more",
                    driver.getTitle());
            SauceREST sauceREST = new SauceREST(authentication.getUsername(), authentication.getAccessKey());

            sauceREST.getJobInfo(sessionId);
            fail("FileNotFoundException was expected to be thrown");

        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    @Test()
    public void failoverToSauce() throws Exception {
        SelfRegisteringRemote remote = createSauceNode(PortProber.findFreePort(), DesiredCapabilities.firefox());
        remote.getConfiguration().put(SauceOnDemandRemoteProxy.SAUCE_USER_NAME, authentication.getUsername());
        remote.getConfiguration().put(SauceOnDemandRemoteProxy.SAUCE_ACCESS_KEY, authentication.getAccessKey());
        remote.getConfiguration().put(SauceOnDemandRemoteProxy.SAUCE_HANDLE_UNSPECIFIED_CAPABILITIES, true);
        startNode(remote);
        RemoteWebDriver driver = null;
        String sessionId = null;
        try {
            DesiredCapabilities ff = DesiredCapabilities.firefox();
            ff.setCapability("version", "12");
            ff.setCapability("platform", Platform.XP);
            driver = new RemoteWebDriver(new URL(hub.getUrl() + "/wd/hub"), ff);
            driver.get("http://www.amazon.com/");
            sessionId = driver.getSessionId().toString();
            assertEquals(
                    "Amazon.com: Online Shopping for Electronics, Apparel, Computers, Books, DVDs & more",
                    driver.getTitle());
        } finally {
            if (driver != null) {
                driver.quit();
                SauceREST sauceREST = new SauceREST(authentication.getUsername(), authentication.getAccessKey());
                assertNotNull("Session id is null", sessionId);
                String json = sauceREST.getJobInfo(sessionId);
                JSONObject jsonObject = new JSONObject(json);
                assertNotNull("Unable to parse JSON", jsonObject);
            }
        }
    }

    @Test()
    public void sauceSpecificBrowser() throws Exception {
        DesiredCapabilities capabillities = DesiredCapabilities.firefox();
        capabillities.setCapability("version", "12");
        capabillities.setCapability("platform", Platform.XP);

        SelfRegisteringRemote remote = createSauceNode(PortProber.findFreePort(), capabillities);
        remote.getConfiguration().put(SauceOnDemandRemoteProxy.SAUCE_USER_NAME, authentication.getUsername());
        remote.getConfiguration().put(SauceOnDemandRemoteProxy.SAUCE_ACCESS_KEY, authentication.getAccessKey());
        remote.getConfiguration().put(SauceOnDemandRemoteProxy.SAUCE_HANDLE_UNSPECIFIED_CAPABILITIES, false);
        startNode(remote);
        RemoteWebDriver driver = null;
        String sessionId = null;
        try {

            DesiredCapabilities ff = DesiredCapabilities.firefox();
                       ff.setCapability("version", "12");
                       ff.setCapability("platform", Platform.XP);
            driver = new RemoteWebDriver(new URL(hub.getUrl() + "/wd/hub"), ff);
            driver.get("http://www.amazon.com/");
            sessionId = driver.getSessionId().toString();
            assertEquals(
                    "Amazon.com: Online Shopping for Electronics, Apparel, Computers, Books, DVDs & more",
                    driver.getTitle());
        } finally {
            if (driver != null) {
                driver.quit();
                SauceREST sauceREST = new SauceREST(authentication.getUsername(), authentication.getAccessKey());
                assertNotNull("Session id is null", sessionId);
                String json = sauceREST.getJobInfo(sessionId);
                JSONObject jsonObject = new JSONObject(json);
                assertNotNull("Unable to parse JSON", jsonObject);
            }
        }
    }


    /*private SelfRegisteringRemote createSauceNode(DesiredCapabilities desiredCapabilities) throws Exception {
        SelfRegisteringRemote remote = TestHelper.getRemoteWithoutCapabilities(hub.getUrl(), GridRole.NODE);
        if (desiredCapabilities != null) {
            remote.addBrowser(desiredCapabilities, 2);
        }
        remote.getConfiguration().put(RegistrationRequest.TIME_OUT, -1);
        remote.getConfiguration().put(SauceOnDemandRemoteProxy.SAUCE_ENABLE, true);
        remote.getConfiguration()
                .put(RegistrationRequest.PROXY_CLASS, "com.saucelabs.grid.SauceOnDemandRemoteProxy");
        return remote;

    }*/

}
