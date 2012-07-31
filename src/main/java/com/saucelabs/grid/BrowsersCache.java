package com.saucelabs.grid;

import java.util.*;

/**
 * Retains a cache of the browser information retrieved from Sauce.
 *
 * TODO handle expiring of the cache
 *
 * @author François Reynaud - Initial version of plugin
 * @author Ross Rowe - Additional functionality
 */
public class BrowsersCache {

  private Map<String, SauceOnDemandCapabilities> map = new HashMap<String, SauceOnDemandCapabilities>();

  public BrowsersCache(List<SauceOnDemandCapabilities> caps) {
    for (SauceOnDemandCapabilities cap : caps) {
      map.put(cap.getMD5(), cap);
    }
  }


  public SauceOnDemandCapabilities get(String md5) {
    SauceOnDemandCapabilities res = map.get(md5);
    if (res == null) {
      return null;
    } else {
      return res;
    }
  }
  
  public Collection<SauceOnDemandCapabilities> getAllBrowsers(){
      ArrayList<SauceOnDemandCapabilities> list = new ArrayList<SauceOnDemandCapabilities>();
      list.addAll(map.values());
      Collections.sort(list);
      return list;
  }

}
