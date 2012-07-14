package org.openqa;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
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
      throw new RuntimeException("cap missing from cache");
    } else {
      return res;
    }
  }
  
  public Collection<SauceOnDemandCapabilities> getAllBrowsers(){
    return map.values();
  }

}
