Retrieves the list of browsers from Sauce Labs.

To integrate the Sauce plugin to a grid instance, perform the following steps:

1. Run your hub instance by using the following arguments:

    -role hub -servlets org.openqa.SauceLabAdminServlet

2. Run your Sauce OnDemand Node instance by using the following arguments:

   -role wd -proxy org.openqa.SauceLabRemoteProxy -hubHost localhost -sauce true