package com.saucelabs.grid;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.saucelabs.grid.services.SauceOnDemandRestAPIException;
import com.saucelabs.grid.services.SauceOnDemandService;
import com.saucelabs.grid.services.SauceOnDemandServiceImpl;
import com.saucelabs.grid.utils.SauceLabServiceHardcodedResponses;

public class SauceLabServiceTests {

  private SauceOnDemandService realOne = new SauceOnDemandServiceImpl();
  private SauceOnDemandService mockOne = new SauceLabServiceHardcodedResponses();

  // for now, as status doesn't seem implemented.
  @Test(expected = SauceOnDemandRestAPIException.class)
  public void testStatusAgainstSauceLab() throws SauceOnDemandRestAPIException {
    boolean up = realOne.isSauceLabUp();
    Assert.assertTrue(up);
  }

  @Test
  public void testBrowsersAgainstSauceLab() throws SauceOnDemandRestAPIException {
    List<SauceOnDemandCapabilities> caps = realOne.getWebDriverBrowsers();
    Assert.assertTrue(caps.size() > 50);
  }

  // for now, as status doesn't seem implemented.
  @Test(expected = SauceOnDemandRestAPIException.class)
  public void testStatus() throws SauceOnDemandRestAPIException {
    boolean up = mockOne.isSauceLabUp();
    Assert.assertTrue(up);
  }

  @Test
  public void testBrowsers() throws SauceOnDemandRestAPIException {
    List<SauceOnDemandCapabilities> caps = mockOne.getWebDriverBrowsers();
    Assert.assertEquals(caps.size(), 112);
    SauceOnDemandCapabilities first = caps.get(0);
    Assert.assertEquals(first.getName(), "iehta");
    Assert.assertEquals(first.getLongVersion(), "9.0.8112.16421.");
  }



}
