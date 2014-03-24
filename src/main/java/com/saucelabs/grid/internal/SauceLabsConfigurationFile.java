package com.saucelabs.grid.internal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.grid.common.JSONConfigurationUtils;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.web.servlet.handler.WebDriverRequest;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.saucelabs.grid.BrowsersCache;
import com.saucelabs.grid.SauceOnDemandCapabilities;
import com.saucelabs.grid.SauceOnDemandRemoteProxy;

public class SauceLabsConfigurationFile {
	
    private static final Logger log = Logger.getLogger(SauceLabsConfigurationFile.class.getName());

	
    private String userName = null;
    private String accessKey = null;
	private String sauceLabsHost = SauceOnDemandRemoteProxy.SAUCE_DEFAULT_HOST;
	private String sauceLabsPort = SauceOnDemandRemoteProxy.SAUCE_DEFAULT_PORT;
	private boolean handleUnspecifiedCapabilities = true;
	// Is this even a sensible property?
	private boolean enableSauce = true;
	List<String> webdriverBrowserHashes = new ArrayList<String>();
	List<String> seleniumRCBrowserHashes = new ArrayList<String>();
	
	Map<String, Object> sauceAdditionalCapabilities = new HashMap<String, Object>();
	
	public SauceLabsConfigurationFile(JSONObject file) {
		userName = getNullableProperty(file, SauceOnDemandRemoteProxy.SAUCE_USER_NAME, null);
		accessKey = getNullableProperty(file, SauceOnDemandRemoteProxy.SAUCE_ACCESS_KEY, null);
        sauceLabsHost = getNullableProperty(file, SauceOnDemandRemoteProxy.SELENIUM_HOST, SauceOnDemandRemoteProxy.SAUCE_DEFAULT_HOST);
        sauceLabsPort = getNullableProperty(file, SauceOnDemandRemoteProxy.SELENIUM_PORT, SauceOnDemandRemoteProxy.SAUCE_DEFAULT_PORT);
        
        handleUnspecifiedCapabilities = getNullableProperty(file, 
        		SauceOnDemandRemoteProxy.SAUCE_HANDLE_UNSPECIFIED_CAPABILITIES, Boolean.TRUE);
        
		enableSauce = getNullableProperty(file, SauceOnDemandRemoteProxy.SAUCE_ENABLE, Boolean.TRUE);

        webdriverBrowserHashes = parseBrowserArray(file, SauceOnDemandRemoteProxy.SAUCE_WEB_DRIVER_CAPABILITIES);
        seleniumRCBrowserHashes = parseBrowserArray(file, SauceOnDemandRemoteProxy.SAUCE_RC_CAPABILITIES);
        
        sauceAdditionalCapabilities = parseAdditionalCapabilities(file);
	}
	
	private Map<String, Object> parseAdditionalCapabilities(JSONObject file) {
		Map<String, Object> additionalCapabilities = new HashMap<String, Object>();
		
        JSONObject propertiesObject = getNullableProperty(file, SauceOnDemandRemoteProxy.SAUCE_ADDITIONAL_CAPABILITIES, new JSONObject());
        Iterator<?> it = propertiesObject.keys();
        while(it.hasNext()) {
        	Object propertyKey = it.next();
        	try {
				Object propertyValue = propertiesObject.get((String)propertyKey);
				additionalCapabilities.put((String) propertyKey, propertyValue);
			} catch (JSONException e) {
				log.warning("Couldn't read key/value pair for additional property: " + propertyKey);
			}
        }
        
        return additionalCapabilities;
	}
	
	private List<String> parseBrowserArray(JSONObject file, String key) {
        JSONArray browserArray = file.optJSONArray(key);
        List<String> browserHashes = new ArrayList<String>();
        
        if(browserArray != null) {
        	for(int i = 0; i < browserArray.length(); i++) {
        		try {
					browserHashes.add(browserArray.getString(i));
				} catch (JSONException e) {
					log.warning("Invalid JSON detected when examining key " + key);
				}
        	}
        }
        
        return browserHashes;
	}
	
	/**
	 * I'm not liking the API for this JSON library - consider looking for a different one?
	 *
	 * @param file
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T> T getNullableProperty(JSONObject file, String key, T defaultValue) {
		try {
			return (T) file.get(key);
		} catch (JSONException e) {
			return defaultValue;
		} catch(ClassCastException e) {
			// in case we made a bad assumption with our generics/json formatting
			log.info("Couldn't read property " + key + " due to incorrect JSON type");
			return defaultValue;
		}
	}
	
	/**
	 * Constructor when the config file is not found - use sensible defaults (what we initialise
	 * the fields to)
	 */
	public SauceLabsConfigurationFile() {
	}

	public static SauceLabsConfigurationFile readConfigFile() {
        File file = new File(SauceOnDemandRemoteProxy.SAUCE_ONDEMAND_CONFIG_FILE);
        if (file.exists()) {
            return new SauceLabsConfigurationFile(JSONConfigurationUtils.loadJSON(file.getName()));
        }
        
        return new SauceLabsConfigurationFile();
	}
	
	public void writeConfigurationToFile() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(SauceOnDemandRemoteProxy.SAUCE_USER_NAME, userName);
            jsonObject.put(SauceOnDemandRemoteProxy.SAUCE_ACCESS_KEY, accessKey);
            jsonObject.put(SauceOnDemandRemoteProxy.SAUCE_HANDLE_UNSPECIFIED_CAPABILITIES, handleUnspecifiedCapabilities);
            jsonObject.put(SauceOnDemandRemoteProxy.SAUCE_WEB_DRIVER_CAPABILITIES, webdriverBrowserHashes);
            jsonObject.put(SauceOnDemandRemoteProxy.SAUCE_RC_CAPABILITIES, seleniumRCBrowserHashes);
            jsonObject.put(SauceOnDemandRemoteProxy.SELENIUM_HOST, sauceLabsHost);
            jsonObject.put(SauceOnDemandRemoteProxy.SELENIUM_PORT, sauceLabsPort);
            jsonObject.put(SauceOnDemandRemoteProxy.SAUCE_ENABLE, enableSauce);
            jsonObject.put(SauceOnDemandRemoteProxy.SAUCE_ADDITIONAL_CAPABILITIES, sauceAdditionalCapabilities);
            
            FileWriter file = new FileWriter(SauceOnDemandRemoteProxy.SAUCE_ONDEMAND_CONFIG_FILE);
            file.write(jsonObject.toString());
            file.flush();
            file.close();

        } catch (IOException e) {
            log.log(Level.SEVERE, "Error parsing JSON", e);
        } catch (JSONException e) {
            log.log(Level.SEVERE, "Error parsing JSON", e);
        }
    }
	
	private void addPropertyIfNotNull(Map<String, Object> target, String propertyName, Object ourValue) {
		if(ourValue != null) {
			target.put(propertyName, ourValue);
		}
	}
	
	public void updateRegistrationRequest(RegistrationRequest request, Integer maxSessions) {
		Map<String, Object> config = request.getConfiguration();
		
		addPropertyIfNotNull(config, SauceOnDemandRemoteProxy.SAUCE_USER_NAME, userName);
		addPropertyIfNotNull(config, SauceOnDemandRemoteProxy.SAUCE_ACCESS_KEY, accessKey);
		addPropertyIfNotNull(config, SauceOnDemandRemoteProxy.SELENIUM_HOST, sauceLabsHost);
		addPropertyIfNotNull(config, SauceOnDemandRemoteProxy.SELENIUM_PORT, sauceLabsPort);
		addPropertyIfNotNull(config, SauceOnDemandRemoteProxy.SAUCE_HANDLE_UNSPECIFIED_CAPABILITIES, 
				Boolean.toString(handleUnspecifiedCapabilities));
		addPropertyIfNotNull(config, SauceOnDemandRemoteProxy.SAUCE_ENABLE, 
				Boolean.toString(enableSauce));
		if(maxSessions != null) {
			config.put(RegistrationRequest.MAX_SESSION, maxSessions.toString());
		}
	}
	
	public List<DesiredCapabilities> parseBrowsers(BrowsersCache browserCache, 
			List<String> hashes, int maximumSessions) {
		ArrayList<DesiredCapabilities> toReturn = new ArrayList<DesiredCapabilities>();
		
		for(String driverHash : hashes) {
			SauceOnDemandCapabilities browser = browserCache.get(driverHash);
			DesiredCapabilities capabilities = new DesiredCapabilities(browser.asMap());
			capabilities.setCapability(RegistrationRequest.MAX_INSTANCES, maximumSessions);
			
			// in case we wish to tag the Saucelabs instance, for custom capability matchers
			applyExtraCapabilities(capabilities);
			
			toReturn.add(capabilities);
		}
		
		return toReturn;
	}
	
	private void applyExtraCapabilities(DesiredCapabilities capabilities) {
		for(String key : sauceAdditionalCapabilities.keySet()) {
			capabilities.setCapability(key, sauceAdditionalCapabilities.get(key));
		}
	}
	
	protected void updateRegistrationForHandleUnspecified(RegistrationRequest request, int maximumSessions) {
		request.getCapabilities().clear();
		
		DesiredCapabilities genericCapability = new DesiredCapabilities();
		genericCapability.setBrowserName("generic");
		genericCapability.setCapability(RegistrationRequest.MAX_INSTANCES, maximumSessions);
		
		// in case we wish to tag the Saucelabs instance, for custom capability matchers
		applyExtraCapabilities(genericCapability);
		
		request.addDesiredCapability(genericCapability);
	}
	
	public void updateRegistrationRequestBrowsers(RegistrationRequest request, 
			BrowsersCache webDriverBrowsers, BrowsersCache seleniumRCBrowsers, int maximumSessions) {
		if((webdriverBrowserHashes.isEmpty() && seleniumRCBrowserHashes.isEmpty()) || handleUnspecifiedCapabilities) {
			updateRegistrationForHandleUnspecified(request, maximumSessions);
			return;
		}
		
		request.getCapabilities().clear();
		
		request.getCapabilities().addAll(parseBrowsers(webDriverBrowsers, webdriverBrowserHashes, maximumSessions));
		request.getCapabilities().addAll(parseBrowsers(seleniumRCBrowsers, seleniumRCBrowserHashes, maximumSessions));
		
	}
	
	public void applySauceLabsCredentials(WebDriverRequest request) {
    	String body = request.getBody();
        //convert from String to JSON
        try {
            JSONObject json = new JSONObject(body);
            //add username/accessKey
            JSONObject desiredCapabilities = json.getJSONObject("desiredCapabilities");
            if(desiredCapabilities.opt("username") == null) {
                desiredCapabilities.put("username", this.userName);
            }
            if(desiredCapabilities.opt("accessKey") == null) {
            	desiredCapabilities.put("accessKey", this.accessKey);	
            }
            
            //convert from JSON to String
            request.setBody(json.toString());
            log.log(Level.INFO, "Updating desired capabilities : " + desiredCapabilities);
        } catch (JSONException e) {
            log.log(Level.SEVERE, "Error parsing JSON", e);
        }
	}
	
	public boolean isAuthenticationDetailsValid() {
		return userName != null && accessKey != null;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public String getAccessKey() {
		return accessKey;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSauceLabsHost() {
		return sauceLabsHost;
	}
	
	public String getSauceLabsPort() {
		return sauceLabsPort;
	}
	
	public void setSauceLabsHost(String sauceLabsHost) {
		this.sauceLabsHost = sauceLabsHost;
	}
	
	public void setSauceLabsPort(String sauceLabsPort) {
		this.sauceLabsPort = sauceLabsPort;
	}
	
	public void setSeleniumRCBrowserHashes(List<String> seleniumRCBrowserHashes) {
		this.seleniumRCBrowserHashes = seleniumRCBrowserHashes;
	}
	
	public void setWebdriverBrowserHashes(List<String> webdriverBrowserHashes) {
		this.webdriverBrowserHashes = webdriverBrowserHashes;
	}
	
	public List<String> getSeleniumRCBrowserHashes() {
		return seleniumRCBrowserHashes;
	}
	
	public List<String> getWebdriverBrowserHashes() {
		return webdriverBrowserHashes;
	}
	
	public void setHandleUnspecifiedCapabilities(
			boolean handleUnspecifiedCapabilities) {
		this.handleUnspecifiedCapabilities = handleUnspecifiedCapabilities;
	}
	public boolean isHandleUnspecifiedCapabilities() {
		return handleUnspecifiedCapabilities;
	}
	public void setEnableSauce(boolean enableSauce) {
		this.enableSauce = enableSauce;
	}
	
	public boolean isEnableSauce() {
		return enableSauce;
	}

}
