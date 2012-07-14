package org.openqa.services;

/**
 * @author François Reynaud - Initial version of plugin
 * @author Ross Rowe - Additional functionality
 */
public class SauceOnDemandRestAPIException extends Exception {


  public SauceOnDemandRestAPIException(Throwable t) {
    super(t);
  }

  public SauceOnDemandRestAPIException(String msg, Exception e) {
   super(msg, e);
  }
}
