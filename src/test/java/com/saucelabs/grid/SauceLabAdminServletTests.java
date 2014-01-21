package com.saucelabs.grid;

import com.saucelabs.grid.SauceOnDemandRemoteProxy;
import org.openqa.grid.common.GridRole;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.utils.SelfRegisteringRemote;
import org.openqa.grid.web.Hub;
import com.saucelabs.grid.utils.TestHelper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SauceLabAdminServletTests {

  private Hub hub;

  @BeforeClass(alwaysRun = false)
  public void prepare() throws Exception {
    hub = TestHelper.getHub(4444);
    registerSauceLabProxy();
  }

  @Test
  public void adminPage() {
    System.out.println("test");
  }

  private void registerSauceLabProxy() throws Exception {
    SelfRegisteringRemote remote =
        TestHelper.getRemoteWithoutCapabilities(hub.getUrl(), GridRole.NODE);
    

    remote.getConfiguration().put(RegistrationRequest.TIME_OUT, -1);
    remote.getConfiguration().put(RegistrationRequest.ID, "sauceProxy");
    remote.getConfiguration()
        .put(RegistrationRequest.PROXY_CLASS, "com.saucelabs.grid.SauceOnDemandRemoteProxy");
    remote.getConfiguration().put(SauceOnDemandRemoteProxy.SAUCE_ENABLE, true);
    System.out.println(remote.getConfiguration());
    remote.sendRegistrationRequest();

  }

}
