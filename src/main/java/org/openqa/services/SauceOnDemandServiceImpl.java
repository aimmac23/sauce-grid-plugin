package org.openqa.services;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.Helper;
import org.openqa.SauceOnDemandCapabilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author François Reynaud - Initial version of plugin
 * @author Ross Rowe - Additional functionality
 */
public class SauceOnDemandServiceImpl implements SauceOnDemandService {

  private final String host = "saucelabs.com";
  private final HttpHost h = new HttpHost(host);
  public final String STATUS = "http://" + host + "/rest/v1/info/status";
  public final String BROWSERS = "http://" + host + "/rest/v1/info/browsers";


  public SauceOnDemandServiceImpl() {

  }

  public boolean isSauceLabUp() throws SauceOnDemandRestAPIException {
    String s="none";
    try {
      s = executeCommand(STATUS);
      JSONObject result = new JSONObject(s);
      return result.getBoolean("up");
    } catch (Exception e) {
      throw new SauceOnDemandRestAPIException("raw response:"+s,e);
    }
  }

  public List<SauceOnDemandCapabilities> getBrowsers() throws SauceOnDemandRestAPIException {
    List<SauceOnDemandCapabilities> res = new ArrayList<SauceOnDemandCapabilities>();
    String s="none";
    try {
      s = executeCommand(BROWSERS);
      JSONArray results = new JSONArray(s);
      for (int i = 0; i < results.length(); i++) {
        JSONObject cap = results.getJSONObject(i);
        res.add(new SauceOnDemandCapabilities(cap.toString()));
      }
      return res;
    } catch (Exception e) {
      throw new SauceOnDemandRestAPIException("raw response:"+s,e);
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
      throw new RuntimeException("failed to execute " + url + " on " + host +" - "+response.getStatusLine());
    }
  }



}
