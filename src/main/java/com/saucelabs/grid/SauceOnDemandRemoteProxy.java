package com.saucelabs.grid;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.cglib.proxy.Enhancer;

import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.BaseRemoteProxy;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.TestSession;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.internal.listeners.CommandListener;
import org.openqa.grid.internal.listeners.TimeoutListener;
import org.openqa.grid.internal.utils.CapabilityMatcher;
import org.openqa.grid.internal.utils.HtmlRenderer;
import org.openqa.grid.web.servlet.handler.RequestType;
import org.openqa.grid.web.servlet.handler.WebDriverRequest;
import org.openqa.selenium.remote.internal.HttpClientFactory;

import com.saucelabs.grid.internal.SauceLabsConfigurationFile;
import com.saucelabs.grid.internal.TestSlotCapabilityInterceptor;
import com.saucelabs.grid.internal.TestSlotWithMinimalConstructor;
import com.saucelabs.grid.services.SauceOnDemandRestAPIException;
import com.saucelabs.grid.services.SauceOnDemandService;
import com.saucelabs.grid.services.SauceOnDemandServiceImpl;

/**
 * This proxy instance is designed to forward requests to Sauce OnDemand.  Requests will be forwarded to Sauce OnDemand
 * if the proxy is configured to 'failover' (ie. handle desired capabilities that are not handled by any other node in
 * the hub), or if the proxy is configured to respond to a specific desired capability.
 *
 * @author Franois Reynaud - Initial version of plugin
 * @author Ross Rowe - Additional functionality
 */
public class SauceOnDemandRemoteProxy extends BaseRemoteProxy implements CommandListener, TimeoutListener {

    private static final Logger logger = Logger.getLogger(SauceOnDemandRemoteProxy.class.getName());
    private static final SauceOnDemandService service = new SauceOnDemandServiceImpl();
    
    public static final String SAUCE_DEFAULT_HOST = "ondemand.saucelabs.com";
    public static final String SAUCE_DEFAULT_PORT = "80";

    public static final String SAUCE_ONDEMAND_CONFIG_FILE = "sauce-ondemand.json";
    public static final String SAUCE_USER_NAME = "sauceUserName";
    public static final String SAUCE_ACCESS_KEY = "sauceAccessKey";
    public static final String SAUCE_HANDLE_UNSPECIFIED_CAPABILITIES = "sauceHandleUnspecifiedCapabilities";
    public static final String SAUCE_CONNECT = "sauceConnect";
    public static final String SAUCE_ENABLE = "sauceEnable";
    public static final String SAUCE_WEB_DRIVER_CAPABILITIES = "sauceWebDriverCapabilities";
    public static final String SAUCE_RC_CAPABILITIES = "sauceSeleniumRCCapabilities";
    public static final String SAUCE_REQUEST_ALLOWED = "isSauceRequestAllowed";
    public static final String SAUCE_ADDITIONAL_CAPABILITIES = "sauceAdditionalCapabilities";
    private static final String URL_FORMAT = "http://{0}:{1}";
    public static final String SELENIUM_HOST = "seleniumHost";
    public static final String SELENIUM_PORT = "seleniumPort";

    private volatile boolean sauceAvailable = false;
    private CapabilityMatcher capabilityHelper;
    private int maxSauceSessions;
    private final SauceHttpClientFactory httpClientFactory;
    
    SauceLabsConfigurationFile configFile;
    
    // proxies for the real TestSlot objects, to make the TestSlots change capabilities when in use
    protected List<TestSlot> testSlotProxies = new ArrayList<TestSlot>();

    static {
        try {
            InputStream inputStream = SauceOnDemandRemoteProxy.class.getResourceAsStream("/logging.properties");
            if (inputStream != null) {
                LogManager.getLogManager().readConfiguration(inputStream);
            }
        } catch (MalformedURLException e) {
            //shouldn't happen
            logger.log(Level.SEVERE, "Error constructing remote host url", e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error constructing remote host url", e);
        }
    }

    public boolean shouldProxySauceOnDemand() {
        return configFile.isEnableSauce();
    }

    public SauceOnDemandRemoteProxy(RegistrationRequest req, Registry registry) {
        super(updateDesiredCapabilities(req), registry);
        configFile = SauceLabsConfigurationFile.readConfigFile();
        
        httpClientFactory = new SauceHttpClientFactory(this);
        try {
            if (configFile.isAuthenticationDetailsValid()) {
                this.maxSauceSessions = service.getMaxiumumSessions(configFile.getUserName(), configFile.getAccessKey());
                if (maxSauceSessions == -1) {
                    //this is actually infinity, but set it to 100
                    maxSauceSessions = 100;
                }
            }
            
        } catch (SauceOnDemandRestAPIException e) {
            logger.log(Level.SEVERE, "Error invoking Sauce REST API", e);
        }
        
        if(configFile.isHandleUnspecifiedCapabilities()) {
            for(TestSlot original : super.getTestSlots()) {
            	testSlotProxies.add(createCGLibTestSlotProxy(original));
            }
        }
    }

    private static RegistrationRequest updateDesiredCapabilities(RegistrationRequest request) {
    	
    	SauceLabsConfigurationFile configFile = SauceLabsConfigurationFile.readConfigFile();
    	
    	int concurrencyLevel = 5;
    	if(configFile.isAuthenticationDetailsValid()) {
    		try {
				concurrencyLevel = service.getMaxiumumSessions(configFile.getUserName(), configFile.getAccessKey());
				// apparently this represents infinite sessions?
				if(concurrencyLevel == -1) {
					concurrencyLevel = 100;
				}
			} catch (SauceOnDemandRestAPIException e) {
				logger.info("Couldn't determine SauceLabs concurrency level. Check that "
						+ "the authenication details are correct, and SauceLabs is up.");
			}
    	}
    	
    	configFile.updateRegistrationRequest(request, new Integer(concurrencyLevel));

    	try {
    		BrowsersCache webDriverBrowsers = new BrowsersCache(service.getWebDriverBrowsers());
        	BrowsersCache seleniumRCBrowsers = new BrowsersCache(service.getSeleniumBrowsers());
        	configFile.updateRegistrationRequestBrowsers(request, webDriverBrowsers, seleniumRCBrowsers, concurrencyLevel);
            	
    	} catch(SauceOnDemandRestAPIException e) {
    		logger.log(Level.SEVERE, "Could not retrieve browser list from SauceLabs", e);
    	}
    	
    	return request;
    }
    
    @Override
    public boolean hasCapability(Map<String, Object> requestedCapability) {
        logger.log(Level.INFO, "Checking capability: " + requestedCapability);
        
        // this call is also in SauceOnDemandCapabilityMatcher, but for some reason the matcher is not
        // used to sanity check that the request can be fulfilled.
        if(!isAllowedToProcessRequest(requestedCapability)) {
        	return false;
        }
        
        if (shouldHandleUnspecifiedCapabilities()/* && browser combination is supported by sauce labs*/) {
            logger.log(Level.INFO, "Handling capability: " + requestedCapability);
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
            this.sauceAvailable = service.isSauceLabUp();
            if (!sauceAvailable) {
                throw new RuntimeException("Sauce OnDemand is not available");
            }
        } catch (SauceOnDemandRestAPIException e) {
            logger.log(Level.SEVERE, "Error invoking Sauce REST API", e);
        }

        if ((shouldProxySauceOnDemand() && sauceAvailable)) {
            logger.log(Level.INFO, "Attempting to create new session for: " + requestedCapability);
            TestSession session = super.getNewSession(requestedCapability);
            if(session != null) {
            	logger.log(Level.INFO, "New session created for: " + requestedCapability);	
            }
            else {
            	logger.log(Level.INFO, "No session created for request: " + requestedCapability);
            }
            return session;
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

    /**
     * Need to ensure that Sauce proxy is handled last after all local nodes.
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(RemoteProxy o) {
        if (!(o instanceof SauceOnDemandRemoteProxy)) {
            //ensure that local nodes are listed first, so that if a local node can handle the request, it is given
            //precedence
            //TODO this should be configurable
            return 1;
        } else {
            // there should only be one sauce proxy in use, so this branch won't get executed
            return super.compareTo(o);
        }
    }

    public String getUserName() {
        return configFile.getUserName();
    }

    public void setUserName(String userName) {
        configFile.setUserName(userName);
    }

    public String getAccessKey() {
        return configFile.getAccessKey();
    }

    public void setAccessKey(String accessKey) {
    	configFile.setAccessKey(accessKey);
    }

    public void writeConfigurationToFile() {
    	configFile.writeConfigurationToFile();
    }

    public boolean shouldHandleUnspecifiedCapabilities() {
        return configFile.isHandleUnspecifiedCapabilities();
    }

    public void setShouldHandleUnspecifiedCapabilities(boolean shouldHandleUnspecifiedCapabilities) {
        this.configFile.setHandleUnspecifiedCapabilities(shouldHandleUnspecifiedCapabilities);
    }

    /**
     * There isn't an easy way to return a remote host based on the specific {@link TestSlot}, so we
     * always return ondemand.saucelabs.com
     *
     * @return
     */
    @Override
	public URL getRemoteHost() {
		try {
			return new URL(MessageFormat.format(URL_FORMAT,
					configFile.getSauceLabsHost(),
					configFile.getSauceLabsPort()));
		} catch (MalformedURLException e) {
			logger.log(
					Level.SEVERE,
					"Could not create URL for saucelabs! Host: "
							+ configFile.getSauceLabsHost() + " Port: "
							+ configFile.getSauceLabsPort(), e);
			throw new IllegalStateException(e);
		}
    }

    public URL getNodeHost() {
        return remoteHost;
    }

    @Override
    public HttpClientFactory getHttpClientFactory() {
        return httpClientFactory;
    }

    @Override
    public void afterCommand(TestSession session, HttpServletRequest request, HttpServletResponse response) {
        logger.log(Level.INFO, "Finished executing " + request.toString());
    }

    @Override
    public void beforeCommand(TestSession session, HttpServletRequest request, HttpServletResponse response) {

        logger.log(Level.INFO, "About to execute " + request.toString());
        if (request instanceof WebDriverRequest && request.getMethod().equals("POST")) {
            WebDriverRequest seleniumRequest = (WebDriverRequest) request;
            if (seleniumRequest.getRequestType().equals(RequestType.START_SESSION)) {
            	configFile.applySauceLabsCredentials(seleniumRequest);
            }
        }
    }

    @Override
    public int getMaxNumberOfConcurrentTestSessions() {
        int result;
        if (shouldProxySauceOnDemand()) {
            result = maxSauceSessions;
        } else {
            result = super.getMaxNumberOfConcurrentTestSessions();
        }
        logger.log(Level.INFO, "Maximum concurrent sessions: " + result);
        return result;
    }
    
    /**
     * Disallow Sauce Proxying if the requester specifically says no. This
     * allows us to still use the Selenium Grid as a standard grid, which can
     * be useful if we cannot route SauceLabs requests to the webapp being tested.
     *  
     * @param requestedCapability
     * @return
     */
	public boolean isAllowedToProcessRequest(
			Map<String, Object> requestedCapability) {
		if(requestedCapability.containsKey(SAUCE_REQUEST_ALLOWED)) {
			Object isSauceAllowed = requestedCapability.get(SAUCE_REQUEST_ALLOWED);
			if(isSauceAllowed != null && "false".equals(isSauceAllowed.toString().toLowerCase())) {
				return false;
			}
		}
		return true;
	}

    public void setWebDriverCapabilities(List<String> webDriverCapabilities) {
        configFile.setWebdriverBrowserHashes(webDriverCapabilities);
    }

    public List<String> getWebDriverCapabilities() {
        return configFile.getWebdriverBrowserHashes();
    }

    public void setSeleniumCapabilities(List<String> seleniumCapabilities) {
        configFile.setSeleniumRCBrowserHashes(seleniumCapabilities);
    }
    
     public List<String> getSeleniumRCCapabilities() {
         return configFile.getSeleniumRCBrowserHashes();
	}

    @Override
    public int getTotalUsed() {
        int totalUsed = super.getTotalUsed();
        logger.log(Level.INFO, "Total Slots Used: " + totalUsed);
        return totalUsed;
    }

    @Override
    public boolean isBusy() {
        boolean result = super.isBusy();
        logger.log(Level.INFO, "Proxy isBusy: " + result);
        return result;
    }

    public String getSeleniumHost() {
        return configFile.getSauceLabsHost();
    }

    public String getSeleniumPort() {
        return configFile.getSauceLabsPort();
    }

    public void setSeleniumHost(String seleniumHost) {
        this.configFile.setSauceLabsHost(seleniumHost);
    }

    public void setSeleniumPort(String seleniumPort) {
        this.configFile.setSauceLabsPort(seleniumPort);
    }
    
    @Override
    public List<TestSlot> getTestSlots() {
    	if(testSlotProxies.isEmpty()) {
    		super.getTestSlots();
    	}
    	// otherwise we want to re-map the capabilities when in use
    	return Collections.unmodifiableList(testSlotProxies);
    }
    
    private TestSlot createCGLibTestSlotProxy(TestSlot original) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(TestSlotWithMinimalConstructor.class);
        enhancer.setCallback(new TestSlotCapabilityInterceptor(original));
        
        return (TestSlot) enhancer.create(new Class[] {RemoteProxy.class}, new Object[] {this});
    }

	@Override
	public void beforeRelease(TestSession session) {
		// XXX: Implementation copy-pasted from DefaultRemoteProxy - is this correct for us?
	    // release the resources remotely.
	    if (session.getExternalKey() == null) {
	      throw new IllegalStateException(
	          "cannot release the resources, they haven't been reserved properly.");
	    }
	    boolean ok = session.sendDeleteSessionRequest();
	    if (!ok) {
	      logger.warning("Error releasing the resources on timeout for session " + session 
	    		  + " -  session already timed out on remote node?");
	    }
		
	}
}
