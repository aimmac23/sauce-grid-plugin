package com.saucelabs.grid.internal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.grid.common.JSONConfigurationUtils;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.saucelabs.grid.BrowsersCache;
import com.saucelabs.grid.SauceOnDemandCapabilities;
import com.saucelabs.grid.SauceOnDemandRemoteProxy;

public class SauceLabsConfigurationFile {
	
    private static final Logger log = Logger.getLogger(SauceLabsConfigurationFile.class.getName());

	
    private String userName = null;
    private String accessKey = null;
	private String sauceLabsHost = null;
	private String sauceLabsPort = null;
	private boolean handleUnspecifiedCapabilities = true;
	// Is this even a sensible property?
	private boolean enableSauce = true;
	List<String> webdriverBrowserHashes = new ArrayList<String>();
	List<String> seleniumRCBrowserHashes = new ArrayList<String>();;
	
	public SauceLabsConfigurationFile(JSONObject file) {
		userName = getNullableProperty(file, SauceOnDemandRemoteProxy.SAUCE_USER_NAME, null);
		accessKey = getNullableProperty(file, SauceOnDemandRemoteProxy.SAUCE_ACCESS_KEY, null);
        sauceLabsHost = getNullableProperty(file, SauceOnDemandRemoteProxy.SELENIUM_HOST, null);
        sauceLabsPort = getNullableProperty(file, SauceOnDemandRemoteProxy.SELENIUM_PORT, null);
        
        handleUnspecifiedCapabilities = getNullableProperty(file, 
        		SauceOnDemandRemoteProxy.SAUCE_HANDLE_UNSPECIFIED_CAPABILITIES, Boolean.TRUE);
        
		enableSauce = getNullableProperty(file, SauceOnDemandRemoteProxy.SAUCE_ENABLE, Boolean.TRUE);

        webdriverBrowserHashes = parseBrowserArray(file, SauceOnDemandRemoteProxy.SAUCE_WEB_DRIVER_CAPABILITIES);
        webdriverBrowserHashes = parseBrowserArray(file, SauceOnDemandRemoteProxy.SAUCE_RC_CAPABILITIES);
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
			toReturn.add(capabilities);
		}
		
		return toReturn;
	}
	
	public void updateRegistrationRequestBrowsers(RegistrationRequest request, 
			BrowsersCache webDriverBrowsers, BrowsersCache seleniumRCBrowsers, int maximumSessions) {
		if(webdriverBrowserHashes.isEmpty() && seleniumRCBrowserHashes.isEmpty()) {
			return;
		}
		
		request.getCapabilities().clear();
		
		request.getCapabilities().addAll(parseBrowsers(webDriverBrowsers, webdriverBrowserHashes, maximumSessions));
		request.getCapabilities().addAll(parseBrowsers(seleniumRCBrowsers, seleniumRCBrowserHashes, maximumSessions));
		
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

	

}
