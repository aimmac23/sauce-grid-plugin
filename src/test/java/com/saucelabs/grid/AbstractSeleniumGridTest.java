package com.saucelabs.grid;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.Assert;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHttpRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.common.exception.GridException;
import org.openqa.grid.internal.utils.SelfRegisteringRemote;
import org.openqa.grid.web.Hub;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.internal.HttpClientFactory;

import com.saucelabs.grid.utils.TestHelper;

/**
 * An abstract test skeleton that helps start up a fresh selenium grid for each test.
 * 
 * Currently not thread-safe.
 * 
 * @author Alasdair Macmillan
 *
 */
public abstract class AbstractSeleniumGridTest {
	
	public static final String AMAZON_TITLE = 
			"Amazon.com: Online Shopping for Electronics, Apparel, Computers, Books, DVDs & more";

	
	public static final int HUB_PORT = 7777;
	protected Hub hub; 
    protected HttpClientFactory httpClientFactory;

	
	@Before
	public void setup() throws Exception {
		// we'll always need a hub
		hub = TestHelper.getHub(HUB_PORT);
	    this.httpClientFactory = new HttpClientFactory();

	}
	
	@After
	public void tearDown() throws Exception {
		hub.stop();
	}
	
	protected JSONObject getJSONFromURL(HttpHost host, String uri) {
		HttpClient client = httpClientFactory.getHttpClient();
		
		BasicHttpRequest request = new BasicHttpRequest("GET", uri);
		try {
			HttpResponse response = client.execute(host, request);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new GridException("Hub is down or not responding.");
			}
			
			return extractObject(response);
			
		} catch (Exception e) {
			throw new GridException("Hub is down or not responding: "
					+ e.getMessage());
		}
	}
	
	/**
	 * Copy-pasted from SelfRegisteringRemote, because it was defined as private.
	 * @param resp
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	  private static JSONObject extractObject(HttpResponse resp) throws IOException, JSONException {
		    BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
		    StringBuilder s = new StringBuilder();
		    String line;
		    while ((line = rd.readLine()) != null) {
		      s.append(line);
		    }
		    rd.close();
		    return new JSONObject(s.toString());
	  }
	  
	protected void assertNodeHandlingSession(RemoteWebDriver driver, int port) throws JSONException {
		String sessionKey = driver.getSessionId().toString();

		JSONObject json = getJSONFromURL(new HttpHost("127.0.0.1", port),
				"/wd/hub/sessions");

		JSONArray sessions = json.getJSONArray("value");
		assertTrue(sessions.length() > 0);
		
		for(int i = 0; i < sessions.length(); i++) {
			JSONObject sessionInfo = sessions.getJSONObject(i);
			
			// found it
			if(sessionKey.equals(sessionInfo.getString("id"))) {
				return;
			}
		}

		Assert.fail("Selenium node is not handling session " + sessionKey);
	}
	
	protected void assertNodeHandlingNoSessions(int port) throws JSONException {
		JSONObject json = getJSONFromURL(new HttpHost("127.0.0.1", port),
				"/wd/hub/sessions");
		JSONArray sessions = json.getJSONArray("value");
		assertEquals(0, sessions.length());
	}
	
    
    protected RegistrationRequest buildRegistrationRequest(int nodePort) {
        RegistrationRequest req = RegistrationRequest.build("-role", "node" , "-host", "localhost");

        req.getConfiguration().put(RegistrationRequest.PORT, nodePort);

        req.getConfiguration().put(RegistrationRequest.HUB_HOST, hub.getHost());
        req.getConfiguration().put(RegistrationRequest.HUB_PORT, hub.getPort());
        
        String url =
                "http://" + req.getConfiguration().get(RegistrationRequest.HOST) + ":"
                    + req.getConfiguration().get(RegistrationRequest.PORT);
            req.getConfiguration().put(RegistrationRequest.REMOTE_HOST, url);
                    
        // we don't want silly defaults
        req.getCapabilities().clear();
        
        return req;

    }
    
    protected SelfRegisteringRemote createSeleniumNode(int port, DesiredCapabilities capabilities) throws Exception {
    	RegistrationRequest request = buildRegistrationRequest(port);
    	request.addDesiredCapability(capabilities);
    	
    	SelfRegisteringRemote node = new SelfRegisteringRemote(request);

    	return node;
    }
    
    protected void startNode(SelfRegisteringRemote node) throws Exception {
    	node.startRemoteServer();
    	node.sendRegistrationRequest();
    }
    
    protected SelfRegisteringRemote createSauceNode(int port, DesiredCapabilities capabilities) throws Exception {
    	SelfRegisteringRemote node = createSeleniumNode(port, capabilities);
    	
    	node.getConfiguration().put(SauceOnDemandRemoteProxy.SAUCE_ENABLE, true);
    	node.getConfiguration()
        .put(RegistrationRequest.PROXY_CLASS, "com.saucelabs.grid.SauceOnDemandRemoteProxy");
        
    	return node;
    	
    }

}
