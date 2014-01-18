package com.saucelabs.grid.services;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.saucelabs.grid.Helper;
import com.saucelabs.grid.SauceOnDemandCapabilities;
import com.saucelabs.saucerest.SauceREST;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 *
 * Handles invoking the Sauce REST API to return information about browsers/server status.
 *
 * TODO use the Sauce REST Java API
 *
 * @author Fran�ois Reynaud - Initial version of plugin
 * @author Ross Rowe - Additional functionality
 */
public class SauceOnDemandServiceImpl implements SauceOnDemandService {

    private static final String host = "saucelabs.com";
    private final HttpHost h = new HttpHost(host);
    public final static String STATUS = "http://" + host + "/rest/v1/info/status";
    public final static String BROWSERS = "http://" + host + "/rest/v1/info/browsers";
    public final static String PROVISIONING = "https://{0}:{1}@" + host + "/rest/v1/{0}/limits";
    private static final String SELENIUM_BROWSERS = BROWSERS + "/selenium-rc";
    private static final String WEB_DRIVER_BROWSERS = BROWSERS + "/webdriver";
	private LoadingCache<String, Boolean> sauceStatusCache;


    public SauceOnDemandServiceImpl() {
    	sauceStatusCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build(new SauceStatusLoader());
    }

    /**
     * @return
     * @throws SauceOnDemandRestAPIException
     */
    @Override
	public boolean isSauceLabUp() throws SauceOnDemandRestAPIException {
    	try {
			return sauceStatusCache.get("DUMMY").booleanValue();
		} catch (ExecutionException e) {
			Throwables.propagateIfPossible(e.getCause(), SauceOnDemandRestAPIException.class);
			throw new IllegalStateException("Caught unknown exception", e);
		}
    }

    @Override
	public List<SauceOnDemandCapabilities> getWebDriverBrowsers() throws SauceOnDemandRestAPIException {
        return getBrowsers(WEB_DRIVER_BROWSERS);
    }

    @Override
	public List<SauceOnDemandCapabilities> getSeleniumBrowsers() throws SauceOnDemandRestAPIException {
        return getBrowsers(SELENIUM_BROWSERS);
    }

    @Override
	public int getMaxiumumSessions(String userName, String accessKey) throws SauceOnDemandRestAPIException {
        String json = "none";
        try {
            SauceREST sauceRest = new SauceREST(userName, accessKey);
            json = sauceRest.retrieveResults(new URL(MessageFormat.format(PROVISIONING, userName, accessKey)));

            //json = executeCommand(MessageFormat.format(PROVISIONING, userName, accessKey));
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.getInt("concurrency");
        } catch (Exception e) {
            throw new SauceOnDemandRestAPIException("raw response:" + json, e);
        }
    }

    private List<SauceOnDemandCapabilities> getBrowsers(String url) throws SauceOnDemandRestAPIException {
        List<SauceOnDemandCapabilities> res = new ArrayList<SauceOnDemandCapabilities>();
        String s = "none";
        try {
            s = executeCommand(url);
            JSONArray results = new JSONArray(s);
            for (int i = 0; i < results.length(); i++) {
                JSONObject cap = results.getJSONObject(i);
                res.add(new SauceOnDemandCapabilities(cap.toString()));
            }
            return res;
        } catch (Exception e) {
            throw new SauceOnDemandRestAPIException("raw response:" + s, e);
        }
    }


    protected String executeCommand(String url) throws JSONException, IOException {
        HttpClient client = new DefaultHttpClient();
        BasicHttpRequest r = new BasicHttpRequest("GET", url);
        HttpResponse response = client.execute(h, r);
        if (response.getStatusLine().getStatusCode() == 200) {
            String result = Helper.extractResponse(response);
            return result;
        } else {
            throw new RuntimeException("failed to execute " + url + " on " + host + " - " + response.getStatusLine());
        }
    }
    
    private class SauceStatusLoader extends CacheLoader<String, Boolean> {

		@Override
		public Boolean load(String arg0) throws Exception {
	    	
	        String s = "none";
	        try {
	            s = executeCommand(STATUS);
	            JSONObject result = new JSONObject(s);
	            return result.getBoolean("service_operational");
	        } catch (Exception e) {
	            throw new SauceOnDemandRestAPIException("raw response:" + s, e);
	        }
		}    	
    }
}
