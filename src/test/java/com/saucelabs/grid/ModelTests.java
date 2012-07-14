package com.saucelabs.grid;

import com.saucelabs.grid.SauceOnDemandCapabilities;
import org.json.JSONException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ModelTests {


  private String stringCapabilities = "{\"selenium_name\" : \"iexplore\","
      + "  \"short_version\" : \"9\"," + "\"long_name\" : \"Internet Explorer\","
      + "\"long_version\" : \"9.0.8112.16421.\"," + " \"preferred_version\" : \"9\","
      + " \"os\" : \"Windows 2008\"}";

  
  // same content, different order.
  private String stringCapabilities2 =
      "{"
          + "  \"short_version\" : \"9\","
          + "\"long_name\" : \"Internet Explorer\","
          + "\"long_version\" : \"9.0.8112.16421.\", \"selenium_name\" : \"iexplore\", \"preferred_version\" : \"9\","
          + " \"os\" : \"Windows 2008\"}";

  private String stringCapabilitiesInvalid = "{\"selenium_name2\" : \"iexplore\","
      + "  \"short_version\" : \"9\"," + "\"long_name\" : \"Internet Explorer\","
      + "\"long_version\" : \"9.0.8112.16421.\"," + " \"preferred_version\" : \"9\","
      + " \"os\" : \"Windows 2008\"}";



  @Test
  public void sauceLabCapability() throws JSONException {
    SauceOnDemandCapabilities slc = new SauceOnDemandCapabilities(stringCapabilities);
    Assert.assertEquals(slc.getLongName(), "Internet Explorer");
    Assert.assertEquals(slc.getLongVersion(), "9.0.8112.16421.");
  }

  @Test
  public void sauceLabCapabilityConvert() throws JSONException {
    SauceOnDemandCapabilities slc = new SauceOnDemandCapabilities(stringCapabilities);
    DesiredCapabilities d = new DesiredCapabilities(slc.asMap());

    SauceOnDemandCapabilities slc2 = new SauceOnDemandCapabilities(d.asMap());
    Assert.assertEquals(slc.getMD5(), slc2.getMD5());
  }
  
  @Test
  public void orderDoesntImpactMD5() throws JSONException {
    SauceOnDemandCapabilities slc = new SauceOnDemandCapabilities(stringCapabilities);
    SauceOnDemandCapabilities slc2 = new SauceOnDemandCapabilities(stringCapabilities2);
  
    Assert.assertEquals(slc.getMD5(), slc2.getMD5());
  }



  @Test(expectedExceptions = {JSONException.class})
  public void sauceLabCapabilityInvalid() throws JSONException {
    new SauceOnDemandCapabilities(stringCapabilitiesInvalid);
  }

}
