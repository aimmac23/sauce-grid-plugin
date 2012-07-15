package com.saucelabs.grid.services;

import com.saucelabs.grid.SauceOnDemandCapabilities;

import java.util.List;

/**
 * @author François Reynaud - Initial version of plugin
 * @author Ross Rowe - Additional functionality
 */
public interface SauceOnDemandService {

  boolean isSauceLabUp() throws SauceOnDemandRestAPIException;
  List<SauceOnDemandCapabilities> getWebDriverBrowsers() throws SauceOnDemandRestAPIException;
  List<SauceOnDemandCapabilities> getSeleniumBrowsers() throws SauceOnDemandRestAPIException;

}
