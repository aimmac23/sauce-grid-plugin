Retrieves the list of browsers from Sauce Labs.

To integrate the Sauce plugin to a grid instance, perform the following steps:

1. Run your hub instance by using the following arguments:

   -role hub -servlets com.saucelabs.grid.SauceOnDemandAdminServlet,com.saucelabs.grid.SauceOnDemandConsoleServlet

2. Run your Sauce OnDemand Node instance by using the following arguments:

   -role wd -proxy com.saucelabs.grid.SauceOnDemandRemoteProxy -hubHost localhost -sauce true